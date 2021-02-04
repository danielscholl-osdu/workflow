package org.opengroup.osdu.workflow.provider.azure.config;

import com.azure.storage.blob.BlobServiceClient;
import org.opengroup.osdu.azure.blobstorage.IBlobServiceClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
class SinglePartitionBlobServiceClientFactory implements IBlobServiceClientFactory {

  @Autowired
  private BlobServiceClient blobServiceClient;

  @Override
  public BlobServiceClient getBlobServiceClient(String dataPartitionId) {
    return blobServiceClient;
  }
}
