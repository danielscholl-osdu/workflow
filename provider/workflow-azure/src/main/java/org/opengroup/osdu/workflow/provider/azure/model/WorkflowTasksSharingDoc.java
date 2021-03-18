package org.opengroup.osdu.workflow.provider.azure.model;

import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import lombok.*;
import org.springframework.data.annotation.Id;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkflowTasksSharingDoc {
  private String id;
  private String partitionKey;
  private String runId;
  private String workflowName;
  private String containerId;
  private Long createdAt;
  private String createdBy;
}
