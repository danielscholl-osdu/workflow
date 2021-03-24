package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.storage.blob.sas.BlobContainerSasPermission;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowTasksSharingDoc;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowTasksSharingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class WorkflowTasksSharingRepository implements IWorkflowTasksSharingRepository {

  private static final String LOGGER_NAME = WorkflowTasksSharingRepository.class.getName();

  @Autowired
  BlobStore blobStore;

  @Autowired
  DpsHeaders dpsHeaders;

  @Autowired
  CosmosStore cosmosStore;

  @Autowired
  CosmosConfig cosmosConfig;

  @Autowired
  private JaxRsDpsLog logger;

  @Override
  public String getSignedUrl(String workflowName, String runId) {
    final String dataPartitionId = dpsHeaders.getPartitionId();
    // TODO : Add support for using user provided expiry time and permissions (?)
    final int expiryDays = 7;
    final OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(expiryDays);
    // TODO : Add support for custom permission (?)
    final BlobContainerSasPermission permissions = new BlobContainerSasPermission()
        .setCreatePermission(true)
        .setReadPermission(true)
        .setWritePermission(true)
        .setListPermission(true);

    final Optional<WorkflowTasksSharingDoc> optionalWorkflowTasksSharingDoc =
        cosmosStore.findItem(dataPartitionId, cosmosConfig.getDatabase(), cosmosConfig.getWorkflowTasksSharingCollection(), runId, workflowName, WorkflowTasksSharingDoc.class);

    String containerId;

    if (optionalWorkflowTasksSharingDoc.isPresent()) {
      containerId = optionalWorkflowTasksSharingDoc.get().getContainerId();
    } else {
      containerId = UUID.randomUUID().toString();
      blobStore.createBlobContainer(dataPartitionId, containerId);
      WorkflowTasksSharingDoc workflowTasksSharingDocNewContainer = workflowTasksSharingDocBuilder(workflowName, runId, containerId);
      cosmosStore.createItem(dataPartitionId, cosmosConfig.getDatabase(),
          cosmosConfig.getWorkflowTasksSharingCollection(), workflowTasksSharingDocNewContainer.getPartitionKey(), workflowTasksSharingDocNewContainer);
    }
    return blobStore.generatePreSignedURL(dataPartitionId, containerId, expiryTime, permissions);
  }

  WorkflowTasksSharingDoc workflowTasksSharingDocBuilder(String workflowName, String runId, String containerId) {
    return WorkflowTasksSharingDoc.builder()
        .id(runId)
        .partitionKey(workflowName)
        .containerId(containerId)
        .workflowName(workflowName)
        .runId(runId)
        .createdAt(System.currentTimeMillis())
        .createdBy(dpsHeaders.getUserEmail())
        .build();
  }

  public void deleteTasksSharingInfoContainer(String dataPartitionId, String workflowName, String runId) throws WorkflowNotFoundException {
    final Optional<WorkflowTasksSharingDoc> optionalWorkflowTasksSharingDoc =
        cosmosStore.findItem(dataPartitionId, cosmosConfig.getDatabase(), cosmosConfig.getWorkflowTasksSharingCollection(), runId, workflowName, WorkflowTasksSharingDoc.class);

    if (optionalWorkflowTasksSharingDoc.isPresent()) {
      String containerId = optionalWorkflowTasksSharingDoc.get().getContainerId();
      blobStore.deleteBlobContainer(dataPartitionId, containerId);
      cosmosStore.deleteItem(
          dataPartitionId,
          cosmosConfig.getDatabase(),
          cosmosConfig.getWorkflowTasksSharingCollection(),
          runId,
          workflowName);
    } else {
      final String errorMessage = String.format("Workflow: %s doesn't exist", workflowName);
      logger.error(LOGGER_NAME, errorMessage);
      throw new WorkflowNotFoundException(errorMessage);
    }
  }
}
