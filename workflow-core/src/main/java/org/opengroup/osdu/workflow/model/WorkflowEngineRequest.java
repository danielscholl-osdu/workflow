package org.opengroup.osdu.workflow.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@EqualsAndHashCode
public class WorkflowEngineRequest {
  private final String runId;
  private final String workflowId;
  private final String workflowName;
  private final long executionTimeStamp;


  public WorkflowEngineRequest(String runId, String workflowId, String workflowName, long executionTimeStamp) {
    this.runId = runId;
    this.workflowId = workflowId;
    this.workflowName = workflowName;
    this.executionTimeStamp = executionTimeStamp;
  }

  public WorkflowEngineRequest(String runId, String workflowId, String workflowName) {
    this(runId, workflowId, workflowName, System.currentTimeMillis());
  }

  public WorkflowEngineRequest(String workflowName, Long startTimeStamp) {
    this(null, null, workflowName, System.currentTimeMillis());
  }

  public WorkflowEngineRequest(String workflowName) {
    this(null, null, workflowName, System.currentTimeMillis());
  }
}
