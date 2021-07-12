package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.azure.config.AzureWorkflowEngineConfig;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowMetadataDoc;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.workflow.provider.azure.utils.ThreadLocalUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@Component
public class WorkflowMetadataRepository implements IWorkflowMetadataRepository {
  private static final String LOGGER_NAME = WorkflowMetadataRepository.class.getName();
  private static final String KEY_DAG_CONTENT = "dagContent";
  private static final String KEY_DAG_TYPE = "dagType";

  @Autowired
  private CosmosConfig cosmosConfig;

  @Autowired
  private CosmosStore cosmosStore;

  @Autowired
  private DpsHeaders dpsHeaders;

  @Autowired
  private JaxRsDpsLog logger;

  @Autowired
  private AzureWorkflowEngineConfig workflowEngineConfig;

  @Override
  public WorkflowMetadata createWorkflow(final WorkflowMetadata workflowMetadata) {
    final WorkflowMetadataDoc workflowMetadataDoc = buildWorkflowMetadataDoc(workflowMetadata);
    try {
      if(StringUtils.isEmpty(dpsHeaders.getPartitionId())) {
        cosmosStore.createItem( cosmosConfig.getSystemdatabase(),
            cosmosConfig.getWorkflowMetadataCollection(), workflowMetadataDoc.getPartitionKey(),
            workflowMetadataDoc);
      }
      else {
      cosmosStore.createItem(dpsHeaders.getPartitionId(), cosmosConfig.getDatabase(),
          cosmosConfig.getWorkflowMetadataCollection(), workflowMetadataDoc.getPartitionKey(),
          workflowMetadataDoc);
      }
      return buildWorkflowMetadata(workflowMetadataDoc);
    } catch (AppException e) {
      if(e.getError().getCode() == 409) {
        final String errorMessage = String.format("Workflow with name %s already exists",
            workflowMetadataDoc.getWorkflowName());
        logger.error(errorMessage, e);
        throw new ResourceConflictException(workflowMetadataDoc.getWorkflowName(), errorMessage);
      } else {
        throw e;
      }
    }
  }

  @Override
  public WorkflowMetadata getWorkflow(final String workflowName) {
    Optional<WorkflowMetadataDoc> workflowMetadataDoc=null;
    //this means this call is from deleteWorkflow system endpoint
     if(StringUtils.isEmpty(dpsHeaders.getPartitionId())) {
       workflowMetadataDoc= getSystemWorkflow(workflowName);
     }
    else {
       workflowMetadataDoc= getPrivateWorkflow(workflowName);
       if(null == workflowMetadataDoc || !workflowMetadataDoc.isPresent()) {
         workflowMetadataDoc= getSystemWorkflow(workflowName);
       }
     }
    if (null == workflowMetadataDoc || !workflowMetadataDoc.isPresent()) {
      final String errorMessage = String.format("Workflow: %s doesn't exist", workflowName);
      logger.error(LOGGER_NAME, errorMessage);
      throw new WorkflowNotFoundException(errorMessage);
    }
    WorkflowMetadata workflowMetadata= buildWorkflowMetadata(workflowMetadataDoc.get());
    return workflowMetadata;
  }

  private  Optional<WorkflowMetadataDoc> getSystemWorkflow(final String workflowName) {
    final Optional<WorkflowMetadataDoc> workflowSystemMetadataDoc =
        cosmosStore.findItem(
            cosmosConfig.getSystemdatabase(),
            cosmosConfig.getWorkflowMetadataCollection(),
            workflowName,
            workflowName,
            WorkflowMetadataDoc.class);
    if(null != workflowSystemMetadataDoc && workflowSystemMetadataDoc.isPresent()) {
      ThreadLocalUtils.setSystemDagFlag(true);
      Map<String, Object> registrationInstructions =  workflowSystemMetadataDoc.get().getRegistrationInstructions();
      registrationInstructions.put(KEY_DAG_TYPE, "system");
    }
    return workflowSystemMetadataDoc;
  }

  private  Optional<WorkflowMetadataDoc> getPrivateWorkflow(final String workflowName) {
    final Optional<WorkflowMetadataDoc> workflowMetadataDoc =
        cosmosStore.findItem(dpsHeaders.getPartitionId(),
            cosmosConfig.getDatabase(),
            cosmosConfig.getWorkflowMetadataCollection(),
            workflowName,
            workflowName,
            WorkflowMetadataDoc.class);
    if(null != workflowMetadataDoc && workflowMetadataDoc.isPresent()) {
      ThreadLocalUtils.setSystemDagFlag(false);
      Map<String, Object> registrationInstructions =  workflowMetadataDoc.get().getRegistrationInstructions();
      registrationInstructions.put(KEY_DAG_TYPE, "private");
    }
    return workflowMetadataDoc;
  }
  @Override
  public void deleteWorkflow(String workflowName) {
    if(StringUtils.isEmpty(dpsHeaders.getPartitionId())) {
      cosmosStore.deleteItem( cosmosConfig.getSystemdatabase(),
          cosmosConfig.getWorkflowMetadataCollection(), workflowName, workflowName);
    }
    else {
        cosmosStore.deleteItem(dpsHeaders.getPartitionId(), cosmosConfig.getDatabase(),
            cosmosConfig.getWorkflowMetadataCollection(), workflowName, workflowName);
      }
  }

  @Override
  public List<WorkflowMetadata> getAllWorkflowForTenant(String prefix) {
    try {
      SqlQuerySpec sqlQuerySpec;
      if(prefix != null && !(prefix.isEmpty())) {
        SqlParameter prefixParameter = new SqlParameter("@prefix", prefix);
        sqlQuerySpec = new SqlQuerySpec("SELECT * FROM c " +
                "where STARTSWITH(c.workflowName, @prefix, true) " +
                "ORDER BY c._ts DESC", prefixParameter);
      }
      else {
        sqlQuerySpec = new SqlQuerySpec("SELECT * FROM c " +
            "ORDER BY c._ts DESC");
      }
      final List<WorkflowMetadataDoc> workflowMetadataDocs = cosmosStore.queryItems(
              dpsHeaders.getPartitionId(),
              cosmosConfig.getDatabase(),
              cosmosConfig.getWorkflowMetadataCollection(),
              sqlQuerySpec,
              new CosmosQueryRequestOptions(),
              WorkflowMetadataDoc.class);
      //looking for shared workflows

      final List<WorkflowMetadataDoc> workflowSystemMetadataDocs = cosmosStore.queryItems(
          cosmosConfig.getSystemdatabase(),
          cosmosConfig.getWorkflowMetadataCollection(),
          sqlQuerySpec,
          new CosmosQueryRequestOptions(),
          WorkflowMetadataDoc.class);
      workflowMetadataDocs.addAll(workflowSystemMetadataDocs);
      List<WorkflowMetadata> workflowMetadataList = new ArrayList<>();
      for(WorkflowMetadataDoc workflowMetadataDoc: workflowMetadataDocs)
      {
        workflowMetadataList.add(buildWorkflowMetadata(workflowMetadataDoc));
      }
      return workflowMetadataList;
    } catch (CosmosException e) {
      throw new AppException(e.getStatusCode(), e.getMessage(), e.getMessage(), e);
    }
  }

  private WorkflowMetadataDoc buildWorkflowMetadataDoc(final WorkflowMetadata workflowMetadata) {
    // If we need to save multiple versions of workflow, then choose id as guid and get becomes a query.
    // This is to avoid conflicts. Only one combination of Id and partition key should exist.
    Map<String, Object> registrationInstructionForMetadata =
        new HashMap<>(workflowMetadata.getRegistrationInstructions());
    String dagContent =
        (String) registrationInstructionForMetadata.remove(KEY_DAG_CONTENT);
    if (workflowEngineConfig.getIgnoreDagContent()) {
      dagContent = "";
    }

    return WorkflowMetadataDoc.builder()
        .id(workflowMetadata.getWorkflowName())
        .partitionKey(workflowMetadata.getWorkflowName())
        .workflowName(workflowMetadata.getWorkflowName())
        .description(workflowMetadata.getDescription())
        .createdBy(workflowMetadata.getCreatedBy())
        .creationTimestamp(workflowMetadata.getCreationTimestamp())
        .version(workflowMetadata.getVersion())
        .isRegisteredByWorkflowService(
            dagContent != null && !dagContent.isEmpty())
        .registrationInstructions(registrationInstructionForMetadata).build();
  }

  private WorkflowMetadata buildWorkflowMetadata(final WorkflowMetadataDoc workflowMetadataDoc) {
    return WorkflowMetadata.builder()
        .workflowId(workflowMetadataDoc.getId())
        .workflowName(workflowMetadataDoc.getWorkflowName())
        .description(workflowMetadataDoc.getDescription())
        .createdBy(workflowMetadataDoc.getCreatedBy())
        .creationTimestamp(workflowMetadataDoc.getCreationTimestamp())
        .version(workflowMetadataDoc.getVersion())
        .isDeployedThroughWorkflowService(workflowMetadataDoc.getIsRegisteredByWorkflowService())
        .registrationInstructions(workflowMetadataDoc.getRegistrationInstructions()).build();
  }
}
