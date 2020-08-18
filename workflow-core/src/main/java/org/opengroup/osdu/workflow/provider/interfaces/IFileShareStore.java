package org.opengroup.osdu.workflow.provider.interfaces;

public interface IFileShareStore {
  void createFile(final String contents, final String name);

  void deleteFile(final String fileName);
}
