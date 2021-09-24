package org.opengroup.osdu.workflow.provider.ibm.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowSystemMetadataRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WorkflowSystemMetadataRepository implements IWorkflowSystemMetadataRepository {
  /**
   * Returns workflow metadata based on workflowName
   *
   * @param workflowName Name of the workflow for which metadata should be retrieved.
   * @return Workflow metadata
   */
  @Override
  public WorkflowMetadata getSystemWorkflow(String workflowName) {
    throw new AppException(HttpStatus.SC_NOT_FOUND, "workflow not found", String.format("Workflow: %s doesn't exist", workflowName));
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
