package org.opengroup.osdu.workflow.provider.azure.utils;

import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowTasksSharingDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WorkflowTasksSharingUtils {
    private static final String LOGGER_NAME = WorkflowTasksSharingUtils.class.getName();

    @Autowired
    private CosmosConfig cosmosConfig;

    @Autowired
    private CosmosStore cosmosStore;

    @Autowired
    private BlobStore blobStore;

    @Autowired
    private JaxRsDpsLog logger;

    public void deleteTasksSharingInfoContainer(String dataPartitionId, String workflowName, String runId) throws WorkflowNotFoundException {
        final Optional<WorkflowTasksSharingDoc> optionalWorkflowTasksSharingDoc =
            cosmosStore.findItem(dataPartitionId, cosmosConfig.getDatabase(), cosmosConfig.getWorkflowTasksSharingCollection(), runId, workflowName, WorkflowTasksSharingDoc.class);

        if (optionalWorkflowTasksSharingDoc.isPresent()) {
          String containerId = optionalWorkflowTasksSharingDoc.get().getContainerId();
            blobStore.deleteBlobContainer(dataPartitionId, containerId);
            cosmosStore.deleteItem(
                dataPartitionId,
                cosmosConfig.getDatabase(),
                cosmosConfig.getWorkflowTasksSharingCollection(),
                runId,
                workflowName);
        } else {
          final String errorMessage = String.format("Workflow: %s doesn't exist", workflowName);
          logger.error(LOGGER_NAME, errorMessage);
          throw new WorkflowNotFoundException(errorMessage);
        }
    }
}
