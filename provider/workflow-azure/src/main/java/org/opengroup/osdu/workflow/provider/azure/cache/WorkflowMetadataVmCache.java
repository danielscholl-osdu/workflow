package org.opengroup.osdu.workflow.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.VmCache;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("WorkflowMetadataCache")
@ConditionalOnProperty(value = "runtime.env.local", havingValue = "true")
public class WorkflowMetadataVmCache extends VmCache<String, WorkflowMetadata> {
  public WorkflowMetadataVmCache() {
    super(600, 1000);
  }

}
