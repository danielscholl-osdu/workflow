package org.opengroup.osdu.workflow.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class WorkflowEngineRequest {
  private final String runId;
  private final String workflowId;
  private final String workflowName;
  private final String workflowEngineExecutionDate;
  private final long executionTimeStamp;


  public WorkflowEngineRequest(String runId, String workflowId, String workflowName,
      long executionTimeStamp, String workflowEngineExecutionDate) {
    this.runId = runId;
    this.workflowId = workflowId;
    this.workflowName = workflowName;
    this.executionTimeStamp = executionTimeStamp;
    this.workflowEngineExecutionDate = workflowEngineExecutionDate;
  }

  public WorkflowEngineRequest(String runId, String workflowId, String workflowName) {
    this(runId, workflowId, workflowName, System.currentTimeMillis(), null);
  }

  public WorkflowEngineRequest(String workflowName, Long startTimeStamp) {
    this(null, null, workflowName, startTimeStamp, null);
  }

  public WorkflowEngineRequest(String workflowName, Long startTimeStamp, String workflowEngineExecutionDate) {
    this(null, null, workflowName, startTimeStamp, workflowEngineExecutionDate);
  }

  public WorkflowEngineRequest(String workflowName) {
    this(null, null, workflowName, System.currentTimeMillis(), null);
  }
}
