package org.opengroup.osdu.workflow.provider.azure.config;

import com.azure.security.keyvault.secrets.SecretClient;
import org.opengroup.osdu.azure.KeyVaultFacade;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

  @Value("${redis.port:6380}")
  public int redisPort;

  @Value("${redis.workflowMetadata.ttl:600}")
  public int workflowMetadataTtl;

  @Bean
  @Qualifier("REDIS_PORT")
  public int getRedisPort() {
    return redisPort;
  }

  @Bean
  @Qualifier("WORKFLOW_METADATA_REDIS_TTL")
  public int getWorkflowMetadataTtl() {
    return workflowMetadataTtl;
  }

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
}
