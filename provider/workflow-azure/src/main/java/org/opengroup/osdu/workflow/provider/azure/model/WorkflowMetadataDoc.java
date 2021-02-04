package org.opengroup.osdu.workflow.provider.azure.model;

import lombok.*;

import java.util.Map;

@Builder
@Getter
@NonNull
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkflowMetadataDoc {
  private String id;
  private String partitionKey;
  private String workflowName;
  private String description;
  private String createdBy;
  private Long creationTimestamp;
  private Long version;
  private Map<String, Object> registrationInstructions;
}
