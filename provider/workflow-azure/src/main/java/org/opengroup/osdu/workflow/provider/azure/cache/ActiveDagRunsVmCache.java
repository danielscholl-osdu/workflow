package org.opengroup.osdu.workflow.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.VmCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("ActiveDagRunsCache")
@ConditionalOnProperty(value = "runtime.env.local", havingValue = "true")
public class ActiveDagRunsVmCache extends VmCache<String, Integer> {
  public ActiveDagRunsVmCache() {
    super(20, 1000);
  }
}
