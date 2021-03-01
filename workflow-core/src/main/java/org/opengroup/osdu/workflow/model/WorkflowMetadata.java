package org.opengroup.osdu.workflow.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private boolean isDeployedThroughWorkflowService;
  private Map<String, Object> registrationInstructions;

  @JsonIgnore
  public boolean isDeployedThroughWorkflowService() {
    return isDeployedThroughWorkflowService;
  }

  public void setIsDeployedThroughWorkflowService(boolean isDeployedThroughWorkflowService) {
    this.isDeployedThroughWorkflowService = isDeployedThroughWorkflowService;
  }
}
