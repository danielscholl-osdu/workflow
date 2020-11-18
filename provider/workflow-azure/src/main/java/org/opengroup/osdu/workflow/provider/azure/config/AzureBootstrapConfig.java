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
import org.opengroup.osdu.common.Validators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.misc.BASE64Encoder;

import javax.inject.Named;

@Configuration
public class AzureBootstrapConfig {

  @Value("${azure.keyvault.url}")
  private String keyVaultURL;

  @Value("${azure.airflow.url}")
  private String airflowURL;

  @Value("${azure.airflow.username}")
  private String airflowUsername;

  @Value("${azure.airflow.password}")
  private String airflowPassword;

  @Bean
  @Named("AIRFLOW_URL")
  public String airflowURL() {
    return airflowURL;
  }

  @Bean
  @Named("AIRFLOW_APP_KEY")
  public String airflowAppKey() {
    Validators.checkNotNull(airflowUsername, "Airflow username cannot be null");
    Validators.checkNotNull(airflowPassword, "Airflow password cannot be null");
    String airflowAuthString = airflowUsername + ":" + airflowPassword;
    return new BASE64Encoder().encode(airflowAuthString.getBytes());
  }

  @Bean
  public CosmosClient buildCosmosClient(SecretClient kv) {
    final String cosmosEndpoint = KeyVaultFacade.getSecretWithValidation(kv, "opendes-cosmos-endpoint");
    final String cosmosPrimaryKey = KeyVaultFacade.getSecretWithValidation(kv, "opendes-cosmos-primary-key");
    return new CosmosClientBuilder().endpoint(cosmosEndpoint).key(cosmosPrimaryKey).buildClient();
  }

  @Bean
  @Named("KEY_VAULT_URL")
  public String keyVaultURL() {
    return keyVaultURL;
  }
}
