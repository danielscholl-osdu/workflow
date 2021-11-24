package org.opengroup.osdu.workflow.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.RedisCache;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("WorkflowMetadataCache")
@ConditionalOnProperty(value = "runtime.env.local", havingValue = "false", matchIfMissing = true)
public class WorkflowMetadataRedisCache extends RedisCache<String, WorkflowMetadata> {
  public WorkflowMetadataRedisCache(
      final @Qualifier("REDIS_HOST") String host,
      final @Qualifier("REDIS_PORT") int port,
      final @Qualifier("REDIS_PASSWORD") String password,
      final @Qualifier("WORKFLOW_METADATA_REDIS_TTL") int workflowMetadataRedisTtl) {
    super(host, port, password, workflowMetadataRedisTtl, String.class, WorkflowMetadata.class);
  }
}
