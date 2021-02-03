package org.opengroup.osdu.workflow.provider.interfaces;

import java.util.List;

import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;

public interface IWorkflowManagerService {
  /**
   * Creates workflow with given request.
   * @param request Request object which has information to create workflow.
   * @return Workflow metadata.
   */
  WorkflowMetadata createWorkflow(final CreateWorkflowRequest request);

  /**
   * Returns workflow metadata based on workflowName
   * @param workflowName Id of the workflow for which metadata should be retrieved.
   * @return Workflow metadata
   */
  WorkflowMetadata getWorkflowByName(final String workflowName);

  /**
   * Deletes workflow based on workflowName
   * @param workflowName Id of the workflow which needs to be deleted.
   */
  void deleteWorkflow(final String workflowName);

  /**
   * Get List all the workflows for the tenant.
   * @param prefix Filter workflow names which start with the full prefix specified.
   */
  List<WorkflowMetadata> getAllWorkflowForTenant(String prefix);
}
