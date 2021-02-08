package org.opengroup.osdu.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkflowRun {
  private String workflowId;
  private String workflowName;
  private String runId;
  private Long startTimeStamp;
  private Long endTimeStamp;
  private WorkflowStatusType status;
  private String submittedBy;
  private String workflowEngineExecutionDate;
}
