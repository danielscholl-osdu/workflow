package org.opengroup.osdu.workflow.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.VmCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static org.opengroup.osdu.workflow.provider.azure.consts.CacheConstants.ACTIVE_DAG_RUNS_LOCAL_CACHE_EXPIRATION_SECONDS;
import static org.opengroup.osdu.workflow.provider.azure.consts.CacheConstants.ACTIVE_DAG_RUNS_LOCAL_CACHE_MAXIMUM_SIZE;

@Component("ActiveDagRunsCache")
@ConditionalOnProperty(value = "runtime.env.local", havingValue = "true")
public class ActiveDagRunsVmCache extends VmCache<String, Integer> {
  public ActiveDagRunsVmCache() {
    super(ACTIVE_DAG_RUNS_LOCAL_CACHE_EXPIRATION_SECONDS, ACTIVE_DAG_RUNS_LOCAL_CACHE_MAXIMUM_SIZE);
  }
}
