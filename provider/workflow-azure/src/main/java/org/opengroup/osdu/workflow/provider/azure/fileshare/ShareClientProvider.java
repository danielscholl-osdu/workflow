package org.opengroup.osdu.workflow.provider.azure.fileshare;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShareClientProvider {
  private static final String ENDPOINT_TEMPLATE = "https://%s.file.core.windows.net";

  @Autowired
  private SecretProvider secretProvider;

  @Autowired
  private FileShareConfig fileShareConfig;

  @Bean
  public ShareClient buildClient() {
//    final String accountName = secretProvider.getKeyVaultSecret(fileShareConfig.getAccountKeyName());
//    final String accountKey = secretProvider.getKeyVaultSecret(fileShareConfig.getAccountKeyKeyName());
    // This is an intrim solution until airflow is available in gitlab environment
    final String accountName = System.getenv("AIRFLOW_STORAGE_ACCOUNT_NAME");
    final String accountKey = System.getenv("AIRFLOW_STORAGE_ACCOUNT_KEY");
    final StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
    return new ShareClientBuilder()
        .credential(credential)
        .shareName(fileShareConfig.getShareName())
        .endpoint(String.format(ENDPOINT_TEMPLATE, accountName))
        .buildClient();
  }
}
