package org.opengroup.osdu.workflow.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.RedisCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("ActiveDagRunsCache")
@ConditionalOnProperty(value = "runtime.env.local", havingValue = "false", matchIfMissing = true)
public class ActiveDagRunsRedisCache extends RedisCache<String, Integer> {

  public ActiveDagRunsRedisCache(
      final @Qualifier("REDIS_HOST") String host,
      final @Qualifier("REDIS_PORT") int port,
      final @Qualifier("REDIS_PASSWORD") String password,
      final @Qualifier("ACTIVE_DAG_RUNS_REDIS_TTL") int activeDagRunsTtl) {
    super(host, port, password, activeDagRunsTtl, String.class, Integer.class);
  }
}
