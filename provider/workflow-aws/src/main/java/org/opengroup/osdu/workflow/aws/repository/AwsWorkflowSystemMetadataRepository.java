package org.opengroup.osdu.workflow.aws.repository;

import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowSystemMetadataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequestScope
public class AwsWorkflowSystemMetadataRepository implements IWorkflowSystemMetadataRepository {
  /**
   * Returns workflow metadata based on workflowName
   *
   * @param workflowName Name of the workflow for which metadata should be retrieved.
   * @return Workflow metadata
   */
  @Override
  public WorkflowMetadata getSystemWorkflow(String workflowName) {
    throw new WorkflowNotFoundException(String.format("Workflow: '%s' not found", workflowName));
  }

  /**
   * Creates workflow metadata record in persistence store.
   *
   * @param workflowMetadata Workflow metadata object to save in persistence store.
   * @return Workflow metadata
   */
  @Override
  public WorkflowMetadata createSystemWorkflow(WorkflowMetadata workflowMetadata) {
    return null;
  }

  /**
   * Deletes workflow metadata based on workflowName
   *
   * @param workflowName Name of the workflow for which metadata should be deleted.
   */
  @Override
  public void deleteSystemWorkflow(String workflowName) { }

  /**
   * Get all system workflows metadata based on prefix
   *
   * @param prefix Name of the system workflow for which metadata should be deleted.
   */
  @Override
  public List<WorkflowMetadata> getAllSystemWorkflow(String prefix) {
    return new ArrayList<>();
  }
}
