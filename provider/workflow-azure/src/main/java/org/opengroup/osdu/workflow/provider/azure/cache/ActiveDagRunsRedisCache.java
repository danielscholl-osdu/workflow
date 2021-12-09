package org.opengroup.osdu.workflow.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.RedisCache;
import org.opengroup.osdu.workflow.provider.azure.config.RedisConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("ActiveDagRunsCache")
@ConditionalOnProperty(value = "runtime.env.local", havingValue = "false", matchIfMissing = true)
public class ActiveDagRunsRedisCache extends RedisCache<String, Integer> {

  public ActiveDagRunsRedisCache(RedisConfig redisConfig) {
    super(redisConfig.getRedisHost(), redisConfig.getRedisPort(), redisConfig.getRedisPassword(), redisConfig.getActiveDagRunsTtl(), String.class, Integer.class);
  }
}
