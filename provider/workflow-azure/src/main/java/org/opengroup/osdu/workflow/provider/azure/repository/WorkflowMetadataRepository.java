package org.opengroup.osdu.workflow.provider.azure.repository;

import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowMetadataDoc;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class WorkflowMetadataRepository implements IWorkflowMetadataRepository {
  private static final String LOGGER_NAME = WorkflowMetadataRepository.class.getName();
  private static final String KEY_WORKFLOW_DETAIL_CONTENT = "workflowDetailContent";

  @Autowired
  private CosmosConfig cosmosConfig;

  @Autowired
  private CosmosStore cosmosStore;

  @Autowired
  private DpsHeaders dpsHeaders;

  @Autowired
  private JaxRsDpsLog logger;

  @Override
  public WorkflowMetadata createWorkflow(final WorkflowMetadata workflowMetadata) {
    final WorkflowMetadataDoc workflowMetadataDoc = buildWorkflowMetadataDoc(workflowMetadata);
    try {
      cosmosStore.createItem(dpsHeaders.getPartitionId(), cosmosConfig.getDatabase(),
          cosmosConfig.getWorkflowMetadataCollection(), workflowMetadataDoc.getPartitionKey(),
          workflowMetadataDoc);
      return buildWorkflowMetadata(workflowMetadataDoc);
    } catch (AppException e) {
      if(e.getError().getCode() == 409) {
        final String errorMessage = String.format("Workflow with name %s already exists",
            workflowMetadataDoc.getWorkflowName());
        logger.error(errorMessage, e);
        throw new ResourceConflictException(workflowMetadataDoc.getWorkflowName(), errorMessage);
      } else {
        throw e;
      }
    }
  }

  @Override
  public WorkflowMetadata getWorkflow(final String workflowName) {
    final Optional<WorkflowMetadataDoc> workflowMetadataDoc =
        cosmosStore.findItem(dpsHeaders.getPartitionId(),
            cosmosConfig.getDatabase(),
            cosmosConfig.getWorkflowMetadataCollection(),
            workflowName,
            workflowName,
            WorkflowMetadataDoc.class);
    if (!workflowMetadataDoc.isPresent()) {
      final String errorMessage = String.format("Workflow: %s doesn't exist", workflowName);
      logger.error(LOGGER_NAME, errorMessage);
      throw new WorkflowNotFoundException(errorMessage);
    } else {
      return buildWorkflowMetadata(workflowMetadataDoc.get());
    }
  }

  @Override
  public void deleteWorkflow(String workflowName) {
    cosmosStore.deleteItem(dpsHeaders.getPartitionId(), cosmosConfig.getDatabase(),
        cosmosConfig.getWorkflowMetadataCollection(), workflowName, workflowName);
  }

  @Override
  public List<WorkflowMetadata> getAllWorkflowForTenant(String prefix) {
    throw new UnsupportedOperationException("getAllWorkflowForTenant is not implemented for Azure");
  }

  private WorkflowMetadataDoc buildWorkflowMetadataDoc(final WorkflowMetadata workflowMetadata) {
    // If we need to save multiple versions of workflow, then choose id as guid and get becomes a query.
    // This is to avoid conflicts. Only one combination of Id and partition key should exist.
    Map<String, Object> registrationInstructionForMetadata =
        new HashMap<>(workflowMetadata.getRegistrationInstructions());
    String workflowDetailContent =
        (String) registrationInstructionForMetadata.remove(KEY_WORKFLOW_DETAIL_CONTENT);

    return WorkflowMetadataDoc.builder()
        .id(workflowMetadata.getWorkflowName())
        .partitionKey(workflowMetadata.getWorkflowName())
        .workflowName(workflowMetadata.getWorkflowName())
        .description(workflowMetadata.getDescription())
        .createdBy(workflowMetadata.getCreatedBy())
        .creationTimestamp(workflowMetadata.getCreationTimestamp())
        .version(workflowMetadata.getVersion())
        .isRegisteredByWorkflowService(
            workflowDetailContent == null || workflowDetailContent.isEmpty())
        .registrationInstructions(registrationInstructionForMetadata).build();
  }

  private WorkflowMetadata buildWorkflowMetadata(final WorkflowMetadataDoc workflowMetadataDoc) {
    return WorkflowMetadata.builder()
        .workflowId(workflowMetadataDoc.getId())
        .workflowName(workflowMetadataDoc.getWorkflowName())
        .description(workflowMetadataDoc.getDescription())
        .createdBy(workflowMetadataDoc.getCreatedBy())
        .creationTimestamp(workflowMetadataDoc.getCreationTimestamp())
        .version(workflowMetadataDoc.getVersion())
        .registrationInstructions(workflowMetadataDoc.getRegistrationInstructions()).build();
  }
}
