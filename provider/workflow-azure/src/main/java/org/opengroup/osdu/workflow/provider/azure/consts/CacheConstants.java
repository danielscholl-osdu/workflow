package org.opengroup.osdu.workflow.provider.azure.consts;

public class CacheConstants {
  // Number of seconds the workflow metadata will be retained in the local cache
  public static final int WORKFLOW_METADATA_LOCAL_CACHE_EXPIRATION_SECONDS = 600;
  // Maximum number of entries the workflow metadata local cache will contain
  public static final int WORKFLOW_METADATA_LOCAL_CACHE_MAXIMUM_SIZE = 1000;
}
