package org.opengroup.osdu.workflow.provider.azure.fileshare;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileShareStoreProvider {
  @Bean
  @Qualifier("dags")
  public FileShareStore buildDagsStore(ShareClient shareClient, FileShareConfig config) {
    ShareDirectoryClient client = shareClient.getDirectoryClient(config.getDagsFolder());
    return new FileShareStore(client);
  }

  @Bean
  @Qualifier("customOperators")
  public FileShareStore buildCustomOperatorsStore(ShareClient shareClient, FileShareConfig config) {
    ShareDirectoryClient client = shareClient.getDirectoryClient(config.getCustomOperatorsFolder());
    return new FileShareStore(client);
  }
}
