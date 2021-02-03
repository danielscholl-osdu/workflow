package org.opengroup.osdu.workflow.model;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class TriggerWorkflowRequest {
  private String runId;
  private Map<String, Object> executionContext;
}
