package org.opengroup.osdu.workflow.aws.interfaces;

import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;

public interface IWorkflowStatusRepository {
  WorkflowStatus findWorkflowStatus(String workflowId);

  WorkflowStatus saveWorkflowStatus(WorkflowStatus workflowStatus);

  WorkflowStatus updateWorkflowStatus(String workflowId, WorkflowStatusType workflowStatusType);
}
