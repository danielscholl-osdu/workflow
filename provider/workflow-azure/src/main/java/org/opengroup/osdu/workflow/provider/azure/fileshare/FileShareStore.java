package org.opengroup.osdu.workflow.provider.azure.fileshare;

import com.azure.storage.file.share.ShareDirectoryClient;

import java.io.ByteArrayInputStream;

public class FileShareStore {
  private ShareDirectoryClient directoryClient;
  public FileShareStore(final ShareDirectoryClient directoryClient) {
    this.directoryClient = directoryClient;
  }

  public void createFile(final String contents, final String name) {
    directoryClient.createFile(name, contents.getBytes().length)
        .upload(new ByteArrayInputStream(contents.getBytes()), contents.getBytes().length);
  }

  public void deleteFile(final String fileName) {
    directoryClient.deleteFile(fileName);
  }
}
