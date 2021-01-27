package org.opengroup.osdu.workflow.provider.azure.model;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkflowTasksSharingDoc {
  private String id;
  private String workflowId;
  private String runId;
  private String filePath;
  private Long createdAt;
  private String createdBy;
}
