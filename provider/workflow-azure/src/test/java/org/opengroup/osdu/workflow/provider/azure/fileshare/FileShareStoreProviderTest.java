package org.opengroup.osdu.workflow.provider.azure.fileshare;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FileShareStoreProvider}
 */
@ExtendWith(MockitoExtension.class)
public class FileShareStoreProviderTest {
  private static final String DAGS_FOLDER = "dagsTest";
  @Mock
  private ShareClient shareClient;

  @Mock
  private FileShareConfig config;

  @Mock
  private ShareDirectoryClient directoryClient;

  @InjectMocks
  private FileShareStoreProvider fileShareStoreProvider;

  @Test
  public void testBuildDagsStore() {
    when(config.getDagsFolder()).thenReturn(DAGS_FOLDER);
    when(shareClient.getDirectoryClient(eq(DAGS_FOLDER))).thenReturn(directoryClient);
    fileShareStoreProvider.buildDagsStore(shareClient, config);
    verify(config).getDagsFolder();
    verify(shareClient).getDirectoryClient(eq(DAGS_FOLDER));

  }
}
