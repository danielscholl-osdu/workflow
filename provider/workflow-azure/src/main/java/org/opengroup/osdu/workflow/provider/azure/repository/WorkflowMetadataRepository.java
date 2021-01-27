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

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Component
public class WorkflowMetadataRepository implements IWorkflowMetadataRepository {
  private static final String LOGGER_NAME = WorkflowMetadataRepository.class.getName();

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
          cosmosConfig.getWorkflowMetadataCollection(), workflowMetadataDoc.getWorkflowId(),
          workflowMetadataDoc);
      return buildWorkflowMetadata(workflowMetadataDoc);
    } catch (AppException e) {
      if(e.getError().getCode() == 409) {
        final String errorMessage = String.format("Workflow with name %s and id %s already exists",
            workflowMetadataDoc.getWorkflowName(), workflowMetadataDoc.getWorkflowId());
        logger.error(errorMessage, e);
        throw new ResourceConflictException(workflowMetadataDoc.getWorkflowId(), errorMessage);
      } else {
        throw e;
      }
    }
  }

  @Override
  public WorkflowMetadata getWorkflow(final String workflowId) {
    final Optional<WorkflowMetadataDoc> workflowMetadataDoc =
        cosmosStore.findItem(dpsHeaders.getPartitionId(),
            cosmosConfig.getDatabase(),
            cosmosConfig.getWorkflowMetadataCollection(),
            workflowId,
            workflowId,
            WorkflowMetadataDoc.class);
    if (!workflowMetadataDoc.isPresent()) {
      final String errorMessage = String.format("Workflow: %s doesn't exist", workflowId);
      logger.error(LOGGER_NAME, errorMessage);
      throw new WorkflowNotFoundException(errorMessage);
    } else {
      return buildWorkflowMetadata(workflowMetadataDoc.get());
    }
  }

  @Override
  public void deleteWorkflow(String workflowId) {
    cosmosStore.deleteItem(dpsHeaders.getPartitionId(), cosmosConfig.getDatabase(),
        cosmosConfig.getWorkflowMetadataCollection(), workflowId, workflowId);
  }

  @Override
  public List<WorkflowMetadata> getAllWorkflowForTenant(String prefix) {
    throw new UnsupportedOperationException("getAllWorkflowForTenant is not implemented for Azure");
  }

  private WorkflowMetadataDoc buildWorkflowMetadataDoc(final WorkflowMetadata workflowMetadata) {
    // If we need to save multiple versions of workflow, then choose id as guid and get becomes a query.
    // This is to avoid conflicts. Only one combination of Id and partition key should exist.
    final String workflowId = convertToBase64(workflowMetadata.getWorkflowName());
    return WorkflowMetadataDoc.builder()
            .id(workflowId)
            .workflowId(workflowId)
            .workflowName(workflowMetadata.getWorkflowName())
            .description(workflowMetadata.getDescription())
            .createdBy(workflowMetadata.getCreatedBy())
            .creationDate(workflowMetadata.getCreationTimestamp())
            .version(workflowMetadata.getVersion()).build();
  }

  private WorkflowMetadata buildWorkflowMetadata(final WorkflowMetadataDoc workflowMetadataDoc) {
    return WorkflowMetadata.builder()
        .workflowId(workflowMetadataDoc.getWorkflowId())
        .workflowName(workflowMetadataDoc.getWorkflowName())
        .description(workflowMetadataDoc.getDescription())
        .createdBy(workflowMetadataDoc.getCreatedBy())
        .creationTimestamp(workflowMetadataDoc.getCreationDate())
        .version(workflowMetadataDoc.getVersion()).build();
  }

  private String convertToBase64(final String input) {
    return Base64.getUrlEncoder().encodeToString(input.getBytes());
  }
}
