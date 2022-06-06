package org.opengroup.osdu.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.exception.WorkflowRunCompletedException;
import org.opengroup.osdu.workflow.gsm.WorkflowStatusPublisher;
import org.opengroup.osdu.workflow.logging.AuditLogger;
import org.opengroup.osdu.workflow.model.TriggerWorkflowRequest;
import org.opengroup.osdu.workflow.model.TriggerWorkflowResponse;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.model.WorkflowRunResponse;
import org.opengroup.osdu.workflow.model.WorkflowRunsPage;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowSystemMetadataRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.opengroup.osdu.core.common.model.status.Status.FAILED;
import static org.opengroup.osdu.core.common.model.status.Status.IN_PROGRESS;
import static org.opengroup.osdu.core.common.model.status.Status.SUBMITTED;
import static org.opengroup.osdu.core.common.model.status.Status.SUCCESS;
import static org.opengroup.osdu.workflow.gsm.WorkflowStatusPublisher.USER_MADE_CHANGE;
import static org.opengroup.osdu.workflow.gsm.WorkflowStatusPublisher.WORKFLOW_FAILED;
import static org.opengroup.osdu.workflow.gsm.WorkflowStatusPublisher.WORKFLOW_FINISHED;
import static org.opengroup.osdu.workflow.gsm.WorkflowStatusPublisher.WORKFLOW_IN_PROGRESS;
import static org.opengroup.osdu.workflow.gsm.WorkflowStatusPublisher.WORKFLOW_SUBMITTED;
import static org.opengroup.osdu.workflow.gsm.WorkflowStatusPublisher.WORKFLOW_SUCCESS;
import static org.opengroup.osdu.workflow.logging.LoggerUtils.getTruncatedData;
import static org.opengroup.osdu.workflow.model.WorkflowStatusType.getActiveStatusTypes;
import static org.opengroup.osdu.workflow.model.WorkflowStatusType.getCompletedStatusTypes;

@Service
@RequiredArgsConstructor
public class WorkflowRunServiceImpl implements IWorkflowRunService {

  private static final String KEY_RUN_ID = "run_id";
  private static final String KEY_WORKFLOW_NAME = "workflow_name";
  private static final String KEY_CORRELATION_ID = "correlation_id";
  private static final String KEY_EXECUTION_CONTEXT = "execution_context";
  private static final String KEY_AUTH_TOKEN = "authToken";
  private static final String KEY_DAG_NAME = "dagName";
  private static final Integer WORKFLOW_RUN_LIMIT = 100;

  private final IWorkflowMetadataRepository workflowMetadataRepository;

  private final IWorkflowSystemMetadataRepository workflowSystemMetadataRepository;

  private final IWorkflowRunRepository workflowRunRepository;

  private final DpsHeaders dpsHeaders;

  private final IWorkflowEngineService workflowEngineService;

  private final AuditLogger auditLogger;

  private final WorkflowStatusPublisher statusPublisher;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public WorkflowRunResponse triggerWorkflow(final String workflowName, final TriggerWorkflowRequest request) {
    final WorkflowMetadata workflowMetadata = getWorkflowByName(workflowName);
    final String workflowId = workflowMetadata.getWorkflowId();
    final String runId = request.getRunId() != null ? request.getRunId() : UUID.randomUUID().toString();
    String dagName = null;
    Map<String, Object> instructions = workflowMetadata.getRegistrationInstructions();
    if (Objects.nonNull(instructions)) {
      dagName = (String) instructions.get(KEY_DAG_NAME);
    }
    if (Objects.isNull(dagName)) {
      dagName = workflowMetadata.getWorkflowName();
    }

    final WorkflowEngineRequest rq = WorkflowEngineRequest.builder()
        .runId(runId)
        .workflowId(workflowId)
        .workflowName(workflowName)
        .dagName(dagName)
        .isSystemWorkflow(workflowMetadata.isSystemWorkflow())
        .build();
    final Map<String, Object> context = createWorkflowPayload(workflowName, runId, dpsHeaders.getCorrelationId(), request);
    TriggerWorkflowResponse rs = workflowEngineService.triggerWorkflow(rq, context);
    final WorkflowRun workflowRun = buildWorkflowRun(rq, rs);
    auditLogger.workflowRunEvent(Collections.singletonList(getTruncatedData(request.toString())));
    final WorkflowRunResponse workflowRunResponse = buildWorkflowRunResponse(workflowRunRepository.saveWorkflowRun(workflowRun));
    statusPublisher.publishIngestorStatus(runId, dpsHeaders, WORKFLOW_SUBMITTED, SUBMITTED);

    return workflowRunResponse;
  }

  @Override
  public WorkflowRunResponse getWorkflowRunByName(final String workflowName, final String runId) {
    WorkflowRun workflowRun = workflowRunRepository.getWorkflowRun(workflowName, runId);
    return buildWorkflowRunResponse(fetchAndUpdateWorkflowRunStatus(workflowRun));
  }

  @Override
  public void deleteWorkflowRunsByWorkflowName(String workflowName) {
    List<WorkflowRun> workflowRuns = getAllWorkflowRuns(workflowName);
    if(!isActiveRunsPresent(workflowRuns)) {
      List<String> runIdsToDelete = new ArrayList<>();
      for (WorkflowRun workflowRun : workflowRuns) {
        runIdsToDelete.add(workflowRun.getRunId());
      }
      if (!runIdsToDelete.isEmpty()) {
        workflowRunRepository.deleteWorkflowRuns(workflowName, runIdsToDelete);
      }
    } else {
      String errorMessage = String.format("Active workflow runs found for %s", workflowName);
      throw new AppException(412, "Failed to delete workflow runs", errorMessage);
    }
  }

  @Override
  public List<WorkflowRun> getAllRunInstancesOfWorkflow(String workflowName,
                                                        Map<String, Object> params)
      throws WorkflowNotFoundException {
    // Calling getWorkflowByName will throw WorkflowNotFoundException
    getWorkflowByName(workflowName);
    return workflowRunRepository.getAllRunInstancesOfWorkflow(workflowName, params);
  }

  @Override
  public WorkflowRunResponse updateWorkflowRunStatus(String workflowName, String runId,
                                             WorkflowStatusType status) {
    WorkflowRun workflowRun = workflowRunRepository.getWorkflowRun(workflowName, runId);
    WorkflowStatusType oldStatus = workflowRun.getStatus();
    if (getCompletedStatusTypes().contains(oldStatus)) {
      throw new WorkflowRunCompletedException(workflowName, runId);
    } else {
      WorkflowRunResponse result = null;
      if (getActiveStatusTypes().contains(status)) {
        result = buildWorkflowRunResponse(workflowRunRepository.updateWorkflowRun(
            buildUpdatedWorkflowRun(workflowRun, status, null)));
      } else {
        result = buildWorkflowRunResponse(workflowRunRepository.updateWorkflowRun(
            buildUpdatedWorkflowRun(workflowRun, status, System.currentTimeMillis())));
      }
        logUpdatedStatus(status, oldStatus, runId);

      return result;
    }
  }

  private void logUpdatedStatus(WorkflowStatusType newStatus, WorkflowStatusType oldStatus, String runId) {
    if (newStatus.equals(oldStatus)) {
      return;
    }

    switch (newStatus) {
      case SUBMITTED:
        statusPublisher
            .publishStatusWithNoErrors(runId, dpsHeaders, WORKFLOW_SUBMITTED + USER_MADE_CHANGE, SUBMITTED);
        break;
      case RUNNING:
        statusPublisher
            .publishStatusWithNoErrors(runId, dpsHeaders, WORKFLOW_IN_PROGRESS + USER_MADE_CHANGE, IN_PROGRESS);
        break;
      case FINISHED:
        statusPublisher
            .publishStatusWithNoErrors(runId, dpsHeaders, WORKFLOW_FINISHED + USER_MADE_CHANGE, SUCCESS);
        break;
      case FAILED:
        statusPublisher
            .publishStatusWithUnexpectedErrors(runId, dpsHeaders, WORKFLOW_FAILED + USER_MADE_CHANGE, FAILED);
        break;
      case SUCCESS:
        statusPublisher
            .publishStatusWithNoErrors(runId, dpsHeaders, WORKFLOW_SUCCESS + USER_MADE_CHANGE, SUCCESS);
        break;
      default:
        break;
    }
  }

  private boolean isActiveRunsPresent(List<WorkflowRun> workflowRuns) {
    List<WorkflowStatusType> activeStatusTypes = WorkflowStatusType.getActiveStatusTypes();
    for (WorkflowRun workflowRun : workflowRuns) {
      WorkflowRun updatedWorkflowRun = fetchAndUpdateWorkflowRunStatus(workflowRun);
      if (activeStatusTypes.contains(updatedWorkflowRun.getStatus()))
        return true;
    }
    return false;
  }

  // The below code is borrowed from WorkflowManagerServiceImpl
  // Can't directly consume WorkflowManagerServiceImpl here as it will lead to cyclic dependency
  private WorkflowMetadata getWorkflowByName(String workflowName) {
    try {
      return workflowMetadataRepository.getWorkflow(workflowName);
    } catch (WorkflowNotFoundException e) {
      return workflowSystemMetadataRepository.getSystemWorkflow(workflowName);
    }
  }

  private List<WorkflowRun> getAllWorkflowRuns(String workflowName) {
    String cursor = null;
    List<WorkflowRun> workflowRuns = new ArrayList<>();

    do {
      WorkflowRunsPage workflowRunsPage = workflowRunRepository
          .getWorkflowRunsByWorkflowName(workflowName, WORKFLOW_RUN_LIMIT, cursor);
      workflowRuns.addAll(workflowRunsPage.getItems());
      cursor = workflowRunsPage.getCursor();
    } while (cursor != null);

    return workflowRuns;
  }

  private Map<String, Object> createWorkflowPayload(final String workflowName,
                                                    final String runId,
                                                    final String correlationId,
                                                    final TriggerWorkflowRequest request) {
    final Map<String, Object> payload = new HashMap<>();
    payload.put(KEY_RUN_ID, runId);
    payload.put(KEY_WORKFLOW_NAME, workflowName);
    payload.put(KEY_AUTH_TOKEN, dpsHeaders.getAuthorization());
    payload.put(KEY_CORRELATION_ID, correlationId);
    payload.put(KEY_EXECUTION_CONTEXT, OBJECT_MAPPER.convertValue(request.getExecutionContext(), Map.class));
    return payload;
  }

  private WorkflowRun fetchAndUpdateWorkflowRunStatus(final WorkflowRun workflowRun) {
    List<WorkflowStatusType> activeStatusTypes = WorkflowStatusType.getActiveStatusTypes();
    if (activeStatusTypes.contains(workflowRun.getStatus())) {
      final WorkflowMetadata workflowMetadata = getWorkflowByName(workflowRun.getWorkflowName());
      final String workflowName = workflowMetadata.getWorkflowName();

      final WorkflowEngineRequest rq = WorkflowEngineRequest.builder()
    	  .runId(workflowRun.getRunId())
          .workflowName(workflowName)
          .executionTimeStamp(workflowRun.getStartTimeStamp())
          .workflowEngineExecutionDate(workflowRun.getWorkflowEngineExecutionDate())
          .dagName(getDagName(workflowMetadata))
          .isSystemWorkflow(workflowMetadata.isSystemWorkflow())
          .build();

      final WorkflowStatusType currentStatusType = workflowEngineService.getWorkflowRunStatus(rq);
      if (currentStatusType != workflowRun.getStatus() && currentStatusType != null) {
        if (getCompletedStatusTypes().contains(currentStatusType)) {
          // Setting EndTimeStamp with the timestamp of Instant when this API is called.
          // Currently no EndTimeStamp is returned in the response from Workflow engine.
          // Going forward with the endTimeStamp response from airflow the value can be changed.
          return workflowRunRepository.updateWorkflowRun(buildUpdatedWorkflowRun(workflowRun,
              currentStatusType, System.currentTimeMillis()));
        } else {
          return workflowRunRepository.updateWorkflowRun(buildUpdatedWorkflowRun(workflowRun,
              currentStatusType, null));
        }
      }
    }
    return workflowRun;
  }

  private String getDagName(WorkflowMetadata workflowMetadata) {
    Map<String, Object> instructions = workflowMetadata.getRegistrationInstructions();
    return instructions != null && instructions.get(KEY_DAG_NAME) != null
        ? instructions.get(KEY_DAG_NAME).toString()
        : workflowMetadata.getWorkflowName();
  }

  private WorkflowRun buildWorkflowRun(final WorkflowEngineRequest rq,
                                       final TriggerWorkflowResponse rs) {
    return WorkflowRun.builder()
        .runId(rq.getRunId())
        .startTimeStamp(rq.getExecutionTimeStamp())
        .workflowEngineExecutionDate(rs != null ? rs.getExecutionDate() : null)
        .submittedBy(dpsHeaders.getUserEmail())
        .status(WorkflowStatusType.SUBMITTED)
        .workflowId(rq.getWorkflowId())
        .workflowName(rq.getWorkflowName())
        .build();
  }

  private WorkflowRunResponse buildWorkflowRunResponse(final WorkflowRun workflowRun) {
    if(workflowRun == null) {
      return null;
    }
    return WorkflowRunResponse.builder()
        .workflowId(workflowRun.getWorkflowId())
        .runId(workflowRun.getRunId())
        .startTimeStamp(workflowRun.getStartTimeStamp())
        .endTimeStamp(workflowRun.getEndTimeStamp())
        .submittedBy(workflowRun.getSubmittedBy())
        .status(workflowRun.getStatus())
        .build();
  }

  private WorkflowRun buildUpdatedWorkflowRun(final WorkflowRun workflowRun,
      final WorkflowStatusType workflowStatusType,
      final Long workflowRunEndTimeStamp) {
    return WorkflowRun.builder()
        .workflowId(workflowRun.getWorkflowId())
        .runId(workflowRun.getRunId())
        .startTimeStamp(workflowRun.getStartTimeStamp())
        .endTimeStamp(workflowRunEndTimeStamp)
        .submittedBy(workflowRun.getSubmittedBy())
        .workflowEngineExecutionDate(workflowRun.getWorkflowEngineExecutionDate())
        .status(workflowStatusType)
        .workflowName(workflowRun.getWorkflowName())
        .build();
  }
}
