// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.workflow.provider.azure.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.opengroup.osdu.azure.KeyVaultFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Named;

@Configuration
public class AzureBootstrapConfig {

  @Value("${azure.keyvault.url}")
  private String keyVaultURL;

  @Value("${osdu.azure.partitionId}")
  private String partitionId;

  @Bean
  public CosmosClient buildCosmosClient(SecretClient kv) {
    final String partitionId = getPartitionId();
    final String cosmosEndpoint = KeyVaultFacade.getSecretWithValidation(kv, String.format("%s-cosmos-endpoint", partitionId));
    final String cosmosPrimaryKey = KeyVaultFacade.getSecretWithValidation(kv, String.format("%s-cosmos-primary-key", partitionId));
    return new CosmosClientBuilder().endpoint(cosmosEndpoint).key(cosmosPrimaryKey).buildClient();
  }

  @Bean
  public BlobServiceClient blobServiceClient(SecretClient kv) {
    final String partitionId = getPartitionId();
    final String accountName = KeyVaultFacade.getSecretWithValidation(kv, String.format("%s-storage", partitionId));
    final String accountKey = KeyVaultFacade.getSecretWithValidation(kv, String.format("%s-storage-key", partitionId));
    StorageSharedKeyCredential storageSharedKeyCredential = new StorageSharedKeyCredential(accountName, accountKey);
    String endpoint = String.format("https://%s.blob.core.windows.net", accountName);

    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
        .endpoint(endpoint)
        .credential(storageSharedKeyCredential)
        .buildClient();

    return blobServiceClient;
  }

  @Bean
  @Named("KEY_VAULT_URL")
  public String keyVaultURL() {
    return keyVaultURL;
  }

  public String getPartitionId() {
    return this.partitionId;
  }
}
