package org.opengroup.osdu.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

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
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private boolean isSystemWorkflow;

  @JsonIgnore
  public boolean isDeployedThroughWorkflowService() {
    return isDeployedThroughWorkflowService;
  }

  public void setIsDeployedThroughWorkflowService(boolean isDeployedThroughWorkflowService) {
    this.isDeployedThroughWorkflowService = isDeployedThroughWorkflowService;
  }

  @JsonIgnore
  public boolean isSystemWorkflow() {
    return isSystemWorkflow;
  }

  public void setIsSystemWorkflow(boolean isSystemWorkflow) {
    this.isSystemWorkflow = isSystemWorkflow;
  }
}
