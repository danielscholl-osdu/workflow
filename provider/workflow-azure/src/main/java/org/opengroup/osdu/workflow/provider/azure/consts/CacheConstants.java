package org.opengroup.osdu.workflow.provider.azure.consts;

public class CacheConstants {
  // Number of seconds the information for active number of dag runs will be retained in the local cache
  public static final int ACTIVE_DAG_RUNS_LOCAL_CACHE_EXPIRATION_SECONDS = 20;
  // Maximum number of entries the active dag runs local cache will contain
  public static final int ACTIVE_DAG_RUNS_LOCAL_CACHE_MAXIMUM_SIZE = 1000;
}
