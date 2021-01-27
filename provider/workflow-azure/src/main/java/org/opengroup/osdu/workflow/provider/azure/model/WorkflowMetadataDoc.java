package org.opengroup.osdu.workflow.provider.azure.model;

import lombok.*;

@Builder
@Getter
@NonNull
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkflowMetadataDoc {
  private String id;
  private String workflowId;
  private String workflowName;
  private String description;
  private int concurrentWorkflowRun;
  private int concurrentTaskRun;
  private boolean active;
  private String createdBy;
  private Long creationDate;
  private Long version;
}
