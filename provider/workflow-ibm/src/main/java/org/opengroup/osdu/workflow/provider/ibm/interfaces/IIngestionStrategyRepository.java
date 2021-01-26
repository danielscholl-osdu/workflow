package org.opengroup.osdu.workflow.provider.ibm.interfaces;

import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.workflow.model.IngestionStrategy;

public interface IIngestionStrategyRepository {
  IngestionStrategy findByWorkflowTypeAndDataTypeAndUserId(WorkflowType workflowType,
                                                           String dataType, String userId);
}
