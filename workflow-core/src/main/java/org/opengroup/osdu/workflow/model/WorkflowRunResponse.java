package org.opengroup.osdu.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class WorkflowRunResponse {
  private String workflowId;
  private String runId;
  private Long startTimeStamp;
  private Long endTimeStamp;
  private WorkflowStatusType status;
  private String submittedBy;
}

