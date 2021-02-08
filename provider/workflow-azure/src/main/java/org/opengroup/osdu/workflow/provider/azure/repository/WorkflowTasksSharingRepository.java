package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.storage.blob.sas.BlobContainerSasPermission;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.provider.azure.config.BlobConfig;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.interfaces.IWorkflowTasksSharingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class WorkflowTasksSharingRepository implements IWorkflowTasksSharingRepository {

  @Autowired
  BlobStore blobStore;

  @Autowired
  DpsHeaders headers;

  @Autowired
  CosmosStore cosmosStore;

  @Autowired
  CosmosConfig cosmosConfig;

  @Autowired
  BlobConfig blobConfig;

  @Override
  public String getSignedUrl(String workflowId, String runId) {
    final String dataPartitionId = headers.getPartitionId();
    final String uuid = UUID.randomUUID().toString();
    final String filePath = String.format("%s/%s/%s", workflowId, runId, uuid);
    // TODO : Add support for using user provided expiry time and permissions (?)
    int expiryDays = 7;
    final OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(expiryDays);
    final BlobContainerSasPermission permissions =
        new BlobContainerSasPermission().setCreatePermission(true).setReadPermission(true)
            .setWritePermission(true).setListPermission(true);
    final String signedUrl =
        blobStore.generatePreSignedURL(dataPartitionId, blobConfig.getTasksSharingContainer(), expiryTime, permissions);
//
//    WorkflowTasksSharingDoc workflowTasksSharingDoc =
//        WorkflowTasksSharingDoc.builder()
//            .id(uuid)
//            .workflowId(workflowId)
//            .runId(runId)
//            .filePath(filePath)
//            .createdAt(System.currentTimeMillis())
//            .createdBy(headers.getUserEmail())
//            .build();
//
//    cosmosStore.createItem(headers.getPartitionId(), cosmosConfig.getDatabase(),
//        cosmosConfig.getWorkflowTasksSharingCollection(), workflowTasksSharingDoc.getWorkflowId(), workflowTasksSharingDoc);

    return signedUrl;
  }
}
