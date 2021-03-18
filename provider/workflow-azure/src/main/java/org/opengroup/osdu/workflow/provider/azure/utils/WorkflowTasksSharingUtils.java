package org.opengroup.osdu.workflow.provider.azure.utils;

import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.workflow.exception.WorkflowRunNotFoundException;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowTasksSharingDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public List<WorkflowTasksSharingDoc> getWorkflowTasksSharingDocsForWorkflowNameAndRunId(String dataPartitionId, String workflowName, String runId) {
        SqlParameter workflowNameParameter = new SqlParameter("@workflowName", workflowName);
        SqlParameter runIdParameter = new SqlParameter("@runId", runId);
        SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(
            "SELECT * from c where c.workflowName = @workflowName and c.runId = @runId ORDER BY c._ts DESC",
            workflowNameParameter, runIdParameter);
        return cosmosStore.queryItems(dataPartitionId, cosmosConfig.getDatabase(),
            cosmosConfig.getWorkflowTasksSharingCollection(), sqlQuerySpec, null, WorkflowTasksSharingDoc.class);
    }

    public void deleteTasksSharingInfoContainer(String dataPartitionId, String workflowName, String runId) {
        final List<WorkflowTasksSharingDoc> workflowTasksSharingDocs =
            getWorkflowTasksSharingDocsForWorkflowNameAndRunId(dataPartitionId, workflowName, runId);

        if (!workflowTasksSharingDocs.isEmpty()) {
            String id = workflowTasksSharingDocs.get(0).getId();
            String containerId = workflowTasksSharingDocs.get(0).getContainerId();
            blobStore.deleteBlobContainer(dataPartitionId, containerId);
            cosmosStore.deleteItem(
                dataPartitionId,
                cosmosConfig.getDatabase(),
                cosmosConfig.getWorkflowTasksSharingCollection(),
                id,
                workflowName);
        } else {
            // throw error (do we require a separate exception class for this?)
        }
    }
}
