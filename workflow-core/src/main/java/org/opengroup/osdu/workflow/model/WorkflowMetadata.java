package org.opengroup.osdu.workflow.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Builder
@NonNull
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowMetadata {
  private String workflowId;
  private String workflowName;
  private String description;
  private String createdBy;
  private Long creationTimestamp;
  private Long version;
  private Map<String, Object> registrationInstructions;
}
