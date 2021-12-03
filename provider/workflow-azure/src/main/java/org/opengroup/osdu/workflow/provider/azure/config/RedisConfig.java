package org.opengroup.osdu.workflow.provider.azure.config;

import com.azure.security.keyvault.secrets.SecretClient;
import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.azure.KeyVaultFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ConfigurationProperties("osdu.azure.redis")
@Getter
@Setter
public class RedisConfig {
  /**
   * NOTE:
   * Redis instance used here is used for caching purposes and is not impacted by single / multi partition
   * The redis instance used here is the one that resides in service resource
   * the hostname and password for which is obtained from the central key vault
   */
  private boolean defaultRedisConfig = true;
  private String redisHost;
  private String redisPassword;
  private int redisPort;
  private int workflowMetadataTtl;

  @Autowired
  private SecretClient kv;

  @PostConstruct
  private void setupRedisConfig() {
    if (this.isDefaultRedisConfig()) {
      this.redisPort = 6380;
      this.workflowMetadataTtl = 600;
      this.redisHost = KeyVaultFacade.getSecretWithValidation(kv, "redis-hostname");;
      this.redisPassword = KeyVaultFacade.getSecretWithValidation(kv, "redis-password");;
    }
  }

  /** Beans declared for injecting into the super method of WorkflowMetadataRedisCache **/
  @Bean
  @Qualifier("REDIS_HOST")
  public String redisHost() {
    return redisHost;
  }

  @Bean
  @Qualifier("REDIS_PORT")
  public int redisPort() {
    return redisPort;
  }

  @Bean
  @Qualifier("REDIS_PASSWORD")
  public String redisPassword() {
    return redisPassword;
  }

  @Bean
  @Qualifier("WORKFLOW_METADATA_REDIS_TTL")
  public int workflowMetadataTtl() {
    return workflowMetadataTtl;
  }
}
