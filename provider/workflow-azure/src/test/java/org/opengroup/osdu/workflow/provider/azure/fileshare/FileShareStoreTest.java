package org.opengroup.osdu.workflow.provider.azure.fileshare;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link FileShareStore}
 */
@ExtendWith(MockitoExtension.class)
public class FileShareStoreTest {
  private static final String CONTENTS = "Hello World";
  private static final String NAME = "someName.py";
  private static final long CONTENT_LENGTH = CONTENTS.getBytes().length;

  @Mock
  private ShareDirectoryClient shareDirectoryClient;

  @Mock
  private ShareFileClient shareFileClient;

  @InjectMocks
  private FileShareStore fileShareStore;

  @Test
  public void testCreateFile() {
    when(shareDirectoryClient.createFile(eq(NAME), eq(CONTENT_LENGTH))).thenReturn(shareFileClient);
    final ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);
    when(shareFileClient.upload(inputStreamArgumentCaptor.capture(), eq(CONTENT_LENGTH))).thenReturn(null);
    fileShareStore.createFile(CONTENTS, NAME);
    verify(shareDirectoryClient, times(1)).createFile(eq(NAME), eq(CONTENT_LENGTH));
    verify(shareFileClient, times(1)).upload(any(InputStream.class), eq(CONTENT_LENGTH));
  }

  @Test
  public void testDeleteFile() {
    doNothing().when(shareDirectoryClient).deleteFile(eq(NAME));
    fileShareStore.deleteFile(NAME);
    verify(shareDirectoryClient).deleteFile(eq(NAME));
  }
}
