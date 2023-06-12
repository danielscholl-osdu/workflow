package org.opengroup.osdu.workflow.model;

import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class TriggerWorkflowRequest {
  private String runId;
  private Map<String, Object> executionContext = new HashMap<>();
}
