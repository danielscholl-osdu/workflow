package org.opengroup.osdu.workflow.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.RedisCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.inject.Named;

@Component("ActiveDagRunsCache")
@ConditionalOnProperty(value = "runtime.env.local", havingValue = "false", matchIfMissing = true)
public class ActiveDagRunsRedisCache extends RedisCache<String, Integer> {

  public ActiveDagRunsRedisCache(
      final @Named("REDIS_HOST") String host,
      final @Named("REDIS_PORT") int port,
      final @Named("REDIS_PASSWORD") String password,
      final @Named("CURSOR_REDIS_TTL") int timeout,
      @Value("${redis.database}") final int database) {
    super(host, port, password, timeout, database, String.class, Integer.class);
  }
}
