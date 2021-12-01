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
import org.opengroup.osdu.azure.KeyVaultFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Named;

@Configuration
public class AzureBootstrapConfig {

  @Autowired
  private RedisConfig redisConfig;

  @Value("${azure.keyvault.url}")
  private String keyVaultURL;

  @Value("${osdu.azure.partitionId}")
  private String partitionId;

  @Bean
  @Named("KEY_VAULT_URL")
  public String keyVaultURL() {
    return keyVaultURL;
  }


  /**
   * NOTE:
   * Redis instance used here is used for caching purposes and is not impacted by single / multi partition
   * The redis instance used here is the one that resides in service resource
   * the hostname and password for which is obtained from the central key vault
   */

  @Bean
  @Qualifier("REDIS_HOST")
  public String redisHost(SecretClient kv) {
    return KeyVaultFacade.getSecretWithValidation(kv, "redis-hostname");
  }

  @Bean
  @Qualifier("REDIS_PASSWORD")
  public String redisPassword(SecretClient kv) {
    return KeyVaultFacade.getSecretWithValidation(kv, "redis-password");
  }

  /**
   * Use redis config in order to obtain port / ttl used for various use cases
   */
  @Bean
  @Qualifier("REDIS_PORT")
  public int redisPort(SecretClient kv) {
    return redisConfig.getRedisPort();
  }

  @Bean
  @Qualifier("WORKFLOW_METADATA_REDIS_TTL")
  public int workflowMetadataTtl(SecretClient kv) {
    return redisConfig.getWorkflowMetadataTtl();
  }

  /*This is done to support Single partition support for slb. Once implementation is complete for multi-partition we can remove this method */
  @Bean
  public CosmosClient buildCosmosClient(SecretClient kv) {
    final String partitionId = getPartitionId();
    final String cosmosEndpoint = KeyVaultFacade.getSecretWithValidation(kv, String.format("%s-cosmos-endpoint", partitionId));
    final String cosmosPrimaryKey = KeyVaultFacade.getSecretWithValidation(kv, String.format("%s-cosmos-primary-key", partitionId));
    return new CosmosClientBuilder().endpoint(cosmosEndpoint).key(cosmosPrimaryKey).buildClient();
  }

  public String getPartitionId() {
    return this.partitionId;
  }
}
