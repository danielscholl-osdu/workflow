package org.opengroup.osdu.workflow.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class WorkflowEngineRequest {
  private final String runId;
  private final String workflowId;
  private final String workflowName;
  private final String dagName;
  private final String workflowEngineExecutionDate;
  private final long executionTimeStamp;
  private final boolean isDeployedThroughWorkflowService;
  private final boolean isSystemWorkflow;

  public WorkflowEngineRequest(String runId, String workflowId, String workflowName,
      long executionTimeStamp, String dagName, String workflowEngineExecutionDate,
      boolean isDeployedThroughWorkflowService, boolean isSystemWorkflow) {
    this.runId = runId;
    this.workflowId = workflowId;
    this.workflowName = workflowName;
    this.executionTimeStamp = executionTimeStamp;
    this.dagName = dagName;
    this.workflowEngineExecutionDate = workflowEngineExecutionDate;
    this.isDeployedThroughWorkflowService = isDeployedThroughWorkflowService;
    this.isSystemWorkflow = isSystemWorkflow;
  }

  // NOTE [aaljain]: Too many constructors, better to use builder pattern

  public WorkflowEngineRequest(String runId, String workflowId, String workflowName) {
    this(runId, workflowId, workflowName, System.currentTimeMillis(), null, null, false, false);
  }

  public WorkflowEngineRequest(String workflowName, Long startTimeStamp) {
    this(null, null, workflowName, startTimeStamp, null, null, false, false);
  }

  public WorkflowEngineRequest(String workflowName, Long startTimeStamp, String workflowEngineExecutionDate) {
    this(null, null, workflowName, startTimeStamp, null, workflowEngineExecutionDate, false, false);
  }

  public WorkflowEngineRequest(String workflowName, Long startTimeStamp, String workflowEngineExecutionDate, boolean isSystemWorkflow) {
    this(null, null, workflowName, startTimeStamp, null, workflowEngineExecutionDate, false, isSystemWorkflow);
  }

  public WorkflowEngineRequest(String workflowName) {
    this(null, null, workflowName, System.currentTimeMillis(), null, null, false, false);
  }

  public WorkflowEngineRequest(String workflowName, boolean isDeployedThroughWorkflowService) {
    this(null, null, workflowName, System.currentTimeMillis(), null, null, isDeployedThroughWorkflowService, false);
  }

  public WorkflowEngineRequest(String workflowName, boolean isDeployedThroughWorkflowService, boolean isSystemWorkflow) {
    this(null, null, workflowName, System.currentTimeMillis(), null, null, isDeployedThroughWorkflowService, isSystemWorkflow);
  }

  public WorkflowEngineRequest(String runId, String workflowId, String workflowName, String dagName) {
    this(runId, workflowId, workflowName, System.currentTimeMillis(), dagName, null, false, false);
  }

  public WorkflowEngineRequest(String runId, String workflowId, String workflowName, String dagName, boolean isSystemWorkflow) {
    this(runId, workflowId, workflowName, System.currentTimeMillis(), dagName, null, false, isSystemWorkflow);
  }

  public WorkflowEngineRequest(String runId, String workflowId, String workflowName, long executionTimeStamp, String workflowEngineExecutionDate, boolean isDeployedThroughWorkflowService) {
    this(runId, workflowId, workflowName, executionTimeStamp, null, workflowEngineExecutionDate, isDeployedThroughWorkflowService, false);
  }
}
