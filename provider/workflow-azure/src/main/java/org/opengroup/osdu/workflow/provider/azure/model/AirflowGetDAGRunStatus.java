package org.opengroup.osdu.workflow.provider.azure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;

@Data
public class AirflowGetDAGRunStatus {

  @JsonProperty("state")
  WorkflowStatusType statusType;

}
