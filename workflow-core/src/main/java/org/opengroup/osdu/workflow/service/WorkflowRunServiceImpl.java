package org.opengroup.osdu.workflow.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
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
  public WorkflowRun triggerWorkflow(final String workflowName, final TriggerWorkflowRequest request) {
    final WorkflowMetadata workflowMetadata = workflowMetadataRepository.getWorkflow(workflowName);
    final String workflowId = workflowMetadata.getWorkflowId();
    final String runId = request.getRunId() != null ? request.getRunId() : UUID.randomUUID().toString();
    final WorkflowRun workflowRun = buildWorkflowRun(workflowName, workflowId, runId);
    final WorkflowEngineRequest rq = new WorkflowEngineRequest(runId, workflowId, workflowName);
    final Map<String, Object> context = createWorkflowPayload(workflowId, runId, dpsHeaders.getCorrelationId(), request);
    workflowEngineService.triggerWorkflow(rq, context);
    return workflowRunRepository.saveWorkflowRun(workflowRun);
  }

  @Override
  public WorkflowRun getWorkflowRunByName(final String workflowName, final String runId) {
    WorkflowRun workflowRun = workflowRunRepository.getWorkflowRun(workflowName, runId);
    if (getActiveStatusTypes().contains(workflowRun.getStatus())) {
      return fetchAndUpdateWorkflowRunStatus(workflowRun);
    }
    return workflowRun;
  }

  @Override
  public void deleteWorkflowRunsByWorkflowName(String workflowName) {
    List<WorkflowRun> workflowRuns = getAllWorkflowRuns(workflowName);
    List<String> runIdsToDelete = new ArrayList<>();
    for (WorkflowRun workflowRun : workflowRuns) {
      runIdsToDelete.add(workflowRun.getRunId());
    }
    if (!runIdsToDelete.isEmpty()) {
      workflowRunRepository.deleteWorkflowRuns(workflowName, runIdsToDelete);
    }
  }

  @Override
  public List<WorkflowRun> getAllRunInstancesOfWorkflow(String workflowName,
                                                        Map<String, Object> params) {
    return workflowRunRepository.getAllRunInstancesOfWorkflow(workflowName, params);
  }

  @Override
  public WorkflowRun updateWorkflowRunStatus(String workflowName, String runId,
                                             WorkflowStatusType status) {
    WorkflowRun workflowRun = workflowRunRepository.getWorkflowRun(workflowName, runId);
    workflowRun.setStatus(status);
    return workflowRunRepository.saveWorkflowRun(workflowRun);
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
        return workflowRunRepository.updateWorkflowRun(buildWorkflowRunStatus(workflowRun,
            currentStatusType, System.currentTimeMillis()));
      } else {
        return workflowRunRepository.updateWorkflowRun(buildWorkflowRunStatus(workflowRun,
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

  private WorkflowRun buildWorkflowRunStatus(final WorkflowRun workflowRun,
                                             final WorkflowStatusType workflowStatusType,
                                             final Long workflowRunEndTimeStamp) {
    return WorkflowRun.builder()
        .runId(workflowRun.getRunId())
        .startTimeStamp(workflowRun.getStartTimeStamp())
        .endTimeStamp(workflowRunEndTimeStamp)
        .submittedBy(workflowRun.getSubmittedBy())
        .status(workflowStatusType)
        .workflowId(workflowRun.getWorkflowId())
        .build();
  }
}
