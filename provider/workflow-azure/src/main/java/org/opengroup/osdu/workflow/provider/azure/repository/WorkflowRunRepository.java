package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.azure.query.CosmosStorePageRequest;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.WorkflowRunNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.model.WorkflowRunsPage;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowRunDoc;
import org.opengroup.osdu.workflow.provider.azure.utils.CursorUtils;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import static org.opengroup.osdu.workflow.model.WorkflowStatusType.getCompletedStatusTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class WorkflowRunRepository implements IWorkflowRunRepository {

  private static final String LOGGER_NAME = WorkflowRunRepository.class.getName();
  private static final int WORKFLOW_RUNS_LIMIT = 50;
  private static final String INVALID_WORKFLOW_RUN_PREFIX = "backfill";

  @Autowired
  private CosmosConfig cosmosConfig;

  @Autowired
  private CosmosStore cosmosStore;

  @Autowired
  private DpsHeaders dpsHeaders;

  @Autowired
  private JaxRsDpsLog logger;

  @Autowired
  private CursorUtils cursorUtils;

  @Autowired
  private WorkflowTasksSharingRepository workflowTasksSharingRepository;

  @Autowired
  private BlobStore blobStore;

  @Override
  public WorkflowRun saveWorkflowRun(final WorkflowRun workflowRun) {
    final WorkflowRunDoc workflowRunDoc = buildWorkflowRunDoc(workflowRun);
    cosmosStore.createItem(dpsHeaders.getPartitionId(), cosmosConfig.getDatabase(),
        cosmosConfig.getWorkflowRunCollection(), workflowRunDoc.getPartitionKey(), workflowRunDoc);
    return buildWorkflowRun(workflowRunDoc);
  }

  @Override
  public WorkflowRun getWorkflowRun(String workflowName, String runId) {
    final Optional<WorkflowRunDoc> workflowRunDoc =
        cosmosStore.findItem(dpsHeaders.getPartitionId(),
            cosmosConfig.getDatabase(),
            cosmosConfig.getWorkflowRunCollection(),
            runId,
            workflowName,
            WorkflowRunDoc.class);
    if (!workflowRunDoc.isPresent()) {
      final String errorMessage = String.format("WorkflowRun: %s for Workflow: %s doesn't exist",
          runId, workflowName);
      logger.error(LOGGER_NAME, errorMessage);
      throw new WorkflowRunNotFoundException(errorMessage);
    } else {
      return buildWorkflowRun(workflowRunDoc.get());
    }
  }

  @Override
  public WorkflowRunsPage getWorkflowRunsByWorkflowName(String workflowName, Integer limit,
                                                        String cursor) {
    if (cursor != null) {
      cursor = cursorUtils.decodeCosmosCursor(cursor);
    }

    try {
      SqlParameter workflowNameParameter = new SqlParameter("@workflowName", workflowName);
      SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(
          "SELECT * from c where c.partitionKey = @workflowName ORDER BY c._ts DESC",
          workflowNameParameter);
      final Page<WorkflowRunDoc> pagedWorkflowRunDoc =
          cosmosStore.queryItemsPage(dpsHeaders.getPartitionId(), cosmosConfig.getDatabase(),
              cosmosConfig.getWorkflowRunCollection(), sqlQuerySpec, WorkflowRunDoc.class,
              limit, cursor);
      return buildWorkflowRunsPage(pagedWorkflowRunDoc);
    } catch (CosmosException e) {
      throw new AppException(e.getStatusCode(), e.getMessage(), e.getMessage(), e);
    }
  }

  @Override
  public List<WorkflowRun> getAllRunInstancesOfWorkflow(String workflowName,
                                                        Map<String, Object> params) {
    String queryText = buildQueryTextForGetAllRunInstances(params);
    int limit = WORKFLOW_RUNS_LIMIT;
    if (params.get("limit") != null) {
      limit = Integer.parseInt((String) params.get("limit"));
    }
    String cursor = (String) params.get("cursor");
    if (cursor != null) {
      cursor = cursorUtils.decodeCosmosCursor(cursor);
    }
    try {
      SqlParameter workflowNameParameter = new SqlParameter("@workflowName", workflowName);
      SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(queryText, workflowNameParameter);
      final Page<WorkflowRunDoc> pagedWorkflowRunDoc =
          cosmosStore.queryItemsPage(dpsHeaders.getPartitionId(), cosmosConfig.getDatabase(),
              cosmosConfig.getWorkflowRunCollection(), sqlQuerySpec, WorkflowRunDoc.class,
              limit, cursor);
      return buildWorkflowRunsPage(pagedWorkflowRunDoc).getItems();
    } catch (CosmosException e) {
      throw new AppException(e.getStatusCode(), e.getMessage(), e.getMessage(), e);
    }
  }

  private String buildQueryTextForGetAllRunInstances(Map<String, Object> params) {
    String queryText = "SELECT * from c where c.partitionKey = @workflowName";
    String prefix = (String) params.get("prefix");
    if (prefix != null) {
      if (prefix.equals(INVALID_WORKFLOW_RUN_PREFIX)) {
        throw new AppException(HttpStatus.SC_BAD_REQUEST, "Invalid prefix", "Prefix must not contain the word 'backfill'");
      }
      queryText = String.format("%s and startswith(c.id, '%s')", queryText, prefix);
    }
    String startTimeStamp = (String) params.get("startDate");
    if (startTimeStamp != null) {
      queryText = String.format("%s and c.startTimeStamp >= %s", queryText, startTimeStamp);
    }
    String endTimeStamp = (String) params.get("endDate");
    if (endTimeStamp != null) {
      queryText = String.format("%s and c.endTimeStamp <= %s", queryText, endTimeStamp);
    }
    queryText = String.format("%s ORDER BY c._ts DESC", queryText);
    return queryText;
  }

  @Override
  public void deleteWorkflowRuns(final String workflowName, final List<String> runIds) {
    for(String runId: runIds) {
      cosmosStore.deleteItem(dpsHeaders.getPartitionId(), cosmosConfig.getDatabase(),
          cosmosConfig.getWorkflowRunCollection(), runId, workflowName);
    }
  }

  @Override
  public WorkflowRun updateWorkflowRun(final WorkflowRun workflowRun) {
    logger.info(LOGGER_NAME, String.format("Update called for workflow id: %s,  run id: %s",
        workflowRun.getWorkflowId(), workflowRun.getRunId()));
    final WorkflowRunDoc workflowRunDoc = buildWorkflowRunDoc(workflowRun);
    cosmosStore.replaceItem(dpsHeaders.getPartitionId(),
        cosmosConfig.getDatabase(),
        cosmosConfig.getWorkflowRunCollection(),
        workflowRunDoc.getId(),
        workflowRunDoc.getPartitionKey(),
        workflowRunDoc);
    logger.info(LOGGER_NAME, String.format("Updated workflowRun with id : %s of workflowId: %s",
        workflowRunDoc.getId(), workflowRunDoc.getWorkflowName()));

    // TODO [aaljain]: The feature for deleting container needs to be moved to service folder later
    final WorkflowStatusType currentStatusType = workflowRun.getStatus();
    if (getCompletedStatusTypes().contains(currentStatusType)) {
        workflowTasksSharingRepository.deleteTasksSharingInfoContainer(dpsHeaders.getPartitionId(), workflowRun.getWorkflowName(), workflowRun.getRunId());
    }
    return getWorkflowRun(workflowRun.getWorkflowId(), workflowRun.getRunId());
  }

  private WorkflowRunDoc buildWorkflowRunDoc(final WorkflowRun workflowRun) {
    return WorkflowRunDoc.builder()
        .id(workflowRun.getRunId())
        .runId(workflowRun.getRunId())
        .partitionKey(workflowRun.getWorkflowName())
        .workflowName(workflowRun.getWorkflowName())
        .workflowEngineExecutionDate(workflowRun.getWorkflowEngineExecutionDate())
        .startTimeStamp(workflowRun.getStartTimeStamp())
        .endTimeStamp(workflowRun.getEndTimeStamp())
        .status(workflowRun.getStatus().name())
        .submittedBy(workflowRun.getSubmittedBy()).build();
  }

  private WorkflowRun buildWorkflowRun(final WorkflowRunDoc workflowRunDoc) {
    return WorkflowRun.builder()
        .runId(workflowRunDoc.getRunId())
        .workflowId(workflowRunDoc.getWorkflowName())
        .workflowName(workflowRunDoc.getWorkflowName())
        .status(WorkflowStatusType.valueOf(workflowRunDoc.getStatus()))
        .workflowEngineExecutionDate(workflowRunDoc.getWorkflowEngineExecutionDate())
        .startTimeStamp(workflowRunDoc.getStartTimeStamp())
        .endTimeStamp(workflowRunDoc.getEndTimeStamp())
        .submittedBy(workflowRunDoc.getSubmittedBy())
        .build();
  }

  private WorkflowRunsPage buildWorkflowRunsPage(
      final Page<WorkflowRunDoc> pagedWorkflowRunDoc) {
    CosmosStorePageRequest cosmosPageRequest =
        (CosmosStorePageRequest) pagedWorkflowRunDoc.getPageable();
    List<WorkflowRun> workflowRuns = new ArrayList<>();
    for(WorkflowRunDoc workflowRunDoc: pagedWorkflowRunDoc.getContent()) {
      workflowRuns.add(buildWorkflowRun(workflowRunDoc));
    }

    WorkflowRunsPage.WorkflowRunsPageBuilder workflowRunsPageBuilder =
        WorkflowRunsPage.builder().items(workflowRuns);
    if(cosmosPageRequest.getRequestContinuation() != null) {
      workflowRunsPageBuilder.cursor(cursorUtils
          .encodeCosmosCursor(cosmosPageRequest.getRequestContinuation()));
    }
    return workflowRunsPageBuilder.build();
  }
}
