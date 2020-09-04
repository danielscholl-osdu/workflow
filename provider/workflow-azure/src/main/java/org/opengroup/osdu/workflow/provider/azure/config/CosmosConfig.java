package org.opengroup.osdu.workflow.provider.azure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("azure.cosmosdb")
@Configuration
@Getter
@Setter
public class CosmosConfig {
  private String database;
  private String ingestionStrategyCollection;
  private String workflowStatusCollection;
}
