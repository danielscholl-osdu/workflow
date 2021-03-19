package org.opengroup.osdu.workflow.provider.azure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("azure.storage")
@Configuration
@Getter
@Setter
public class BlobConfig {
  private String tasksSharingStorageAccount;
}
