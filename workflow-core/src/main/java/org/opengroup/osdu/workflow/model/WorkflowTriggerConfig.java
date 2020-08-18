package org.opengroup.osdu.workflow.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class WorkflowTriggerConfig {
  private String id;
  private String kind;
  private String dataPartitionId;
}
