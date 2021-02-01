package org.opengroup.osdu.workflow.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.WorkflowRunCompletedException;
import org.opengroup.osdu.workflow.model.*;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.opengroup.osdu.workflow.model.WorkflowStatusType.getActiveStatusTypes;
import static org.opengroup.osdu.workflow.model.WorkflowStatusType.getCompletedStatusTypes;

@Component
public class WorkflowRunServiceImpl implements IWorkflowRunService {
  private static final String KEY_RUN_ID = "run_id";
  private static final String KEY_WORKFLOW_ID = "workflow_id";
  private static final String KEY_CORRELATION_ID = "correlation_id";
  private static final String KEY_EXECUTION_CONTEXT = "execution_context";
  private static final String KEY_AUTH_TOKEN = "authToken";
  private static final Integer WORKFLOW_RUN_LIMIT = 100;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired
  private IWorkflowMetadataRepository workflowMetadataRepository;

  @Autowired
  private IWorkflowRunRepository workflowRunRepository;

  @Autowired
  private DpsHeaders dpsHeaders;

  @Autowired
  private IWorkflowEngineService workflowEngineService;

  @Override
  public WorkflowRunResponse triggerWorkflow(final String workflowName, final TriggerWorkflowRequest request) {
    final WorkflowMetadata workflowMetadata = workflowMetadataRepository.getWorkflow(workflowName);
    final String workflowId = workflowMetadata.getWorkflowId();
    final String runId = request.getRunId() != null ? request.getRunId() : UUID.randomUUID().toString();
    final WorkflowRun workflowRun = buildWorkflowRun(workflowName, workflowId, runId);
    final WorkflowEngineRequest rq = new WorkflowEngineRequest(runId, workflowId, workflowName);
    final Map<String, Object> context = createWorkflowPayload(workflowId, runId, dpsHeaders.getCorrelationId(), request);
    workflowEngineService.triggerWorkflow(rq, context);
    return buildWorkflowRunResponse(workflowRunRepository.saveWorkflowRun(workflowRun));
  }

  @Override
  public WorkflowRunResponse getWorkflowRunByName(final String workflowName, final String runId) {
    WorkflowRun workflowRun = workflowRunRepository.getWorkflowRun(workflowName, runId);
    if (getActiveStatusTypes().contains(workflowRun.getStatus())) {
      return buildWorkflowRunResponse(fetchAndUpdateWorkflowRunStatus(workflowRun));
    }
    return buildWorkflowRunResponse(workflowRun);
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
                                                        Map<String, Object> params) {
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
    for(WorkflowRun workflowRun: workflowRuns) {
      if(activeStatusTypes.contains(workflowRun.getStatus())) {
        return true;
      }
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

  private Map<String, Object> createWorkflowPayload(final String runId,
                                                    final String workflowId,
                                                    final String correlationId,
                                                    final TriggerWorkflowRequest request) {
    final Map<String, Object> payload = new HashMap<>();
    payload.put(KEY_RUN_ID, runId);
    payload.put(KEY_WORKFLOW_ID, workflowId);
    payload.put(KEY_AUTH_TOKEN, dpsHeaders.getAuthorization());
    payload.put(KEY_CORRELATION_ID, correlationId);
    payload.put(KEY_EXECUTION_CONTEXT, OBJECT_MAPPER.convertValue(request.getExecutionContext(), Map.class));
    return payload;
  }

  private WorkflowRun fetchAndUpdateWorkflowRunStatus(final WorkflowRun workflowRun) {
    final WorkflowMetadata workflowMetadata = workflowMetadataRepository.getWorkflow(workflowRun.getWorkflowName());
    final String workflowName = workflowMetadata.getWorkflowName();
    final WorkflowEngineRequest rq = new WorkflowEngineRequest(workflowName, workflowRun.getStartTimeStamp());
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
    return workflowRun;
  }

  private WorkflowRun buildWorkflowRun(final String workflowName,
                                       final String workflowId, final String runId) {
    return WorkflowRun.builder()
        .runId(runId)
        .startTimeStamp(System.currentTimeMillis())
        .submittedBy(dpsHeaders.getUserEmail())
        .status(WorkflowStatusType.SUBMITTED)
        .workflowId(workflowId)
        .workflowName(workflowName)
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
