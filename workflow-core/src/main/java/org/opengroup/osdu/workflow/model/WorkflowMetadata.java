package org.opengroup.osdu.workflow.model;

import java.util.Map;

import lombok.*;

@Getter
@Setter
@Builder
@NonNull
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class WorkflowMetadata {
  private String workflowId;
  private String workflowName;
  private String description;
  private String createdBy;
  private Long creationTimestamp;
  private Long version;
  private Map<String, Object> registrationInstructions;
}
