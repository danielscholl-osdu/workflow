package org.opengroup.osdu.workflow.model;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class CreateWorkflowRequest {
  private String workflowName;
  private String description;
  private Map<String, Object> registrationInstructions;
}
