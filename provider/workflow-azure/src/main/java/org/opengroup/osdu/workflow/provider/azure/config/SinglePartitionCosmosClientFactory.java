package org.opengroup.osdu.workflow.provider.azure.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.internal.AsyncDocumentClient;
import org.opengroup.osdu.azure.cosmosdb.ICosmosClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SinglePartitionCosmosClientFactory implements ICosmosClientFactory {

  @Autowired
  CosmosClient cosmosClient;

  @Override
  public CosmosClient getClient(final String s) {
    return cosmosClient;
  }

  @Override
  public AsyncDocumentClient getAsyncClient(final String s) {
    return null;
  }
}
