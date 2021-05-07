package org.opengroup.osdu.workflow.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.exception.WorkflowRunCompletedException;
import org.opengroup.osdu.workflow.logging.AuditLogger;
import org.opengroup.osdu.workflow.model.*;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.opengroup.osdu.workflow.logging.LoggerUtils.getTruncatedData;
import static org.opengroup.osdu.workflow.model.WorkflowStatusType.getActiveStatusTypes;
import static org.opengroup.osdu.workflow.model.WorkflowStatusType.getCompletedStatusTypes;

@Component
public class WorkflowRunServiceImpl implements IWorkflowRunService {
  private static final String KEY_RUN_ID = "run_id";
  private static final String KEY_WORKFLOW_NAME = "workflow_name";
  private static final String KEY_CORRELATION_ID = "correlation_id";
  private static final String KEY_EXECUTION_CONTEXT = "execution_context";
  private static final String KEY_AUTH_TOKEN = "authToken";
  private static final Integer WORKFLOW_RUN_LIMIT = 100;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String KEY_DAG_NAME = "dagName";

  @Autowired
  private IWorkflowMetadataRepository workflowMetadataRepository;

  @Autowired
  private IWorkflowRunRepository workflowRunRepository;

  @Autowired
  private DpsHeaders dpsHeaders;

  @Autowired
  private IWorkflowEngineService workflowEngineService;

  @Autowired
  private AuditLogger auditLogger;

  @Override
  public WorkflowRunResponse triggerWorkflow(final String workflowName, final TriggerWorkflowRequest request) {
    final WorkflowMetadata workflowMetadata = workflowMetadataRepository.getWorkflow(workflowName);
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

    final WorkflowEngineRequest rq = new WorkflowEngineRequest(runId, workflowId, workflowName, dagName);
    final Map<String, Object> context = createWorkflowPayload(workflowName, runId, dpsHeaders.getCorrelationId(), request);
    TriggerWorkflowResponse rs = workflowEngineService.triggerWorkflow(rq, context);
    final WorkflowRun workflowRun = buildWorkflowRun(rq, rs);
    auditLogger.workflowRunEvent(Collections.singletonList(getTruncatedData(request.toString())));
    return buildWorkflowRunResponse(workflowRunRepository.saveWorkflowRun(workflowRun));
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
    // Calling getWorkflow will throw WorkflowNotFoundException
    workflowMetadataRepository.getWorkflow(workflowName);
    return workflowRunRepository.getAllRunInstancesOfWorkflow(workflowName, params);
  }

  @Override
  public WorkflowRunResponse updateWorkflowRunStatus(String workflowName, String runId,
                                             WorkflowStatusType status) {
    WorkflowRun workflowRun = workflowRunRepository.getWorkflowRun(workflowName, runId);
    if (getCompletedStatusTypes().contains(workflowRun.getStatus())) {
      throw new WorkflowRunCompletedException(workflowName, runId);
    } else {
      if (getActiveStatusTypes().contains(status)) {
        return buildWorkflowRunResponse(workflowRunRepository.updateWorkflowRun(
            buildUpdatedWorkflowRun(workflowRun, status, null)));
      } else {
        return buildWorkflowRunResponse(workflowRunRepository.updateWorkflowRun(
            buildUpdatedWorkflowRun(workflowRun, status, System.currentTimeMillis())));
      }
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
      final WorkflowMetadata workflowMetadata = workflowMetadataRepository.getWorkflow(workflowRun.getWorkflowName());
      final String workflowName = workflowMetadata.getWorkflowName();
      final WorkflowEngineRequest rq = new WorkflowEngineRequest(workflowName,
          workflowRun.getStartTimeStamp(), workflowRun.getWorkflowEngineExecutionDate());
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
