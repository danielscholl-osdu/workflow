package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.azure.query.CosmosStorePageRequest;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.WorkflowRunNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.model.WorkflowRunsPage;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowRunDoc;
import org.opengroup.osdu.workflow.provider.azure.utils.CursorUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link WorkflowRunRepository}
 */
@ExtendWith(MockitoExtension.class)
public class WorkflowRunRepositoryTest {
  private static final String PARTITION_ID = "someId";
  private static final String WORKFLOW_NAME = "test-workflow-name";
  private static final String RUN_ID = "d13f7fd0-d27e-4176-8d60-6e9aad86e347";
  private static final String DATABASE_NAME = "someDbName";
  private static final String WORKFLOW_RUN_COLLECTION = "someCollection";
  private static final String TEST_CURSOR = "dGVzdC1jdXJzb3I=";
  private static final Integer TEST_LIMIT = 100;
  private static final Long WORKFLOW_RUN_END_TIMESTAMP = 1600258424158L;
  private static final String WORKFLOW_RUN = "{\n" +
      "  \"workflowName\": \"test-workflow-name\",\n" +
      "  \"workflowId\": \"test-workflow-name\",\n" +
      "  \"runId\": \"d13f7fd0-d27e-4176-8d60-6e9aad86e347\",\n" +
      "  \"startTimeStamp\": 1600145420675,\n" +
      "  \"workflowEngineExecutionDate\": \"2020-12-05T11:36:45\",\n" +
      "  \"status\": \"submitted\",\n" +
      "  \"submittedBy\": \"user@email.com\"\n" +
      "}";
  private static final String WORKFLOW_RUN_DOC = "{\n" +
      "  \"workflowName\": \"test-workflow-name\",\n" +
      "  \"id\": \"d13f7fd0-d27e-4176-8d60-6e9aad86e347\",\n" +
      "  \"startTimeStamp\": 1600145420675,\n" +
      "  \"workflowEngineExecutionDate\": \"2020-12-05T11:36:45\",\n" +
      "  \"status\": \"SUBMITTED\",\n" +
      "  \"submittedBy\": \"user@email.com\"\n" +
      "}";
  private static final String UPDATED_WORKFLOW_RUN_DOC = "{\n" +
      "  \"workflowName\": \"test-workflow-name\",\n" +
      "  \"id\": \"d13f7fd0-d27e-4176-8d60-6e9aad86e347\",\n" +
      "  \"startTimeStamp\": 1607430997362,\n" +
      "  \"endTimeStamp\": 1600258424158,\n" +
      "  \"status\": \"FINISHED\",\n" +
      "  \"submittedBy\": \"user@email.com\"\n" +
      "}";
  private static final String UPDATED_WORKFLOW_RUN = "{\n" +
      "  \"workflowName\" : \"test-workflow-name\",\n" +
      "  \"workflowId\": \"test-workflow-name\",\n" +
      "  \"runId\": \"d13f7fd0-d27e-4176-8d60-6e9aad86e347\",\n" +
      "  \"startTimeStamp\": 1607430997362,\n" +
      "  \"endTimeStamp\": 1600258424158,\n" +
      "  \"status\": \"finished\",\n" +
      "  \"submittedBy\": \"user@email.com\"\n" +
      "}";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mock
  private CosmosConfig cosmosConfig;

  @Mock
  private CosmosStore cosmosStore;

  @Mock
  private DpsHeaders dpsHeaders;

  @Mock
  private CursorUtils cursorUtils;

  @InjectMocks
  private WorkflowRunRepository workflowRunRepository;

  @Mock
  private JaxRsDpsLog jaxRsDpsLog;

  @Test
  public void testSaveWorkflowRun() throws Exception {
    final WorkflowRun workflowRun = OBJECT_MAPPER.readValue(WORKFLOW_RUN, WorkflowRun.class);
    final WorkflowRunDoc workflowRunDoc = OBJECT_MAPPER.readValue(WORKFLOW_RUN_DOC, WorkflowRunDoc.class);
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowRunCollection()).thenReturn(WORKFLOW_RUN_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    doNothing().when(cosmosStore)
        .createItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_RUN_COLLECTION), eq(WORKFLOW_NAME), eq(workflowRunDoc));
    final WorkflowRun response = workflowRunRepository.saveWorkflowRun(workflowRun);
    verify(cosmosStore, times(1))
        .createItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_RUN_COLLECTION), eq(WORKFLOW_NAME), eq(workflowRunDoc));
    verify(cosmosConfig, times(1)).getDatabase();
    verify(cosmosConfig, times(1)).getWorkflowRunCollection();
    verify(dpsHeaders, times(1)).getPartitionId();
    assertThat(response, equalTo(workflowRun));
  }

  @Test
  public void testGetWorkflowRunWithExistingWorkflowRun() throws Exception {
    final WorkflowRunDoc workflowRunDoc = OBJECT_MAPPER.readValue(WORKFLOW_RUN_DOC,WorkflowRunDoc.class);
    final WorkflowRun workflowRun = OBJECT_MAPPER.readValue(WORKFLOW_RUN,WorkflowRun.class);
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowRunCollection()).thenReturn(WORKFLOW_RUN_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    when(cosmosStore.findItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_RUN_COLLECTION),
        eq(RUN_ID), eq(WORKFLOW_NAME), eq(WorkflowRunDoc.class)))
        .thenReturn(Optional.of(workflowRunDoc));
    final WorkflowRun response = workflowRunRepository.getWorkflowRun(WORKFLOW_NAME,RUN_ID);
    verify(cosmosStore).findItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_RUN_COLLECTION),
        eq(RUN_ID), eq(WORKFLOW_NAME), eq(WorkflowRunDoc.class));
    verify(cosmosConfig).getDatabase();
    verify(cosmosConfig).getWorkflowRunCollection();
    verify(dpsHeaders).getPartitionId();
    assertThat(response, equalTo(workflowRun));
  }

  @Test
  public void testGetWorkflowRunWithNonExistingWorkflowRun() throws Exception {
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowRunCollection()).thenReturn(WORKFLOW_RUN_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    when(cosmosStore.findItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_RUN_COLLECTION),
        eq(RUN_ID), eq(WORKFLOW_NAME), eq(WorkflowRunDoc.class)))
        .thenReturn(Optional.empty());
    Assertions.assertThrows(WorkflowRunNotFoundException.class, () -> {
      workflowRunRepository.getWorkflowRun(WORKFLOW_NAME,RUN_ID);
    });
    verify(cosmosStore, times(1)).findItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_RUN_COLLECTION),
        eq(RUN_ID), eq(WORKFLOW_NAME), eq(WorkflowRunDoc.class));
    verify(cosmosConfig, times(1)).getDatabase();
    verify(cosmosConfig, times(1)).getWorkflowRunCollection();
    verify(dpsHeaders, times(1)).getPartitionId();
  }

  @Test
  public void testUpdateWorkflowRunStatusWithExistingWorkflowRun() throws Exception {
    final WorkflowRun updatedWorkflowRun = OBJECT_MAPPER.readValue(UPDATED_WORKFLOW_RUN, WorkflowRun.class);
    final WorkflowRunDoc updatedWorkflowRunDoc = OBJECT_MAPPER.readValue(UPDATED_WORKFLOW_RUN_DOC, WorkflowRunDoc.class);
    final ArgumentCaptor<WorkflowRunDoc> workflowRunDocArgumentCaptor = ArgumentCaptor.forClass(WorkflowRunDoc.class);
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowRunCollection()).thenReturn(WORKFLOW_RUN_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    doNothing().when(cosmosStore).replaceItem(eq(PARTITION_ID), eq(DATABASE_NAME),
        eq(WORKFLOW_RUN_COLLECTION), eq(RUN_ID), eq(WORKFLOW_NAME), workflowRunDocArgumentCaptor.capture());
    when(cosmosStore.findItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_RUN_COLLECTION),
        eq(RUN_ID), eq(WORKFLOW_NAME), eq(WorkflowRunDoc.class))).thenReturn(Optional.of(updatedWorkflowRunDoc));
    final WorkflowRun response = workflowRunRepository.updateWorkflowRun(updatedWorkflowRun);
    verify(cosmosStore).findItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_RUN_COLLECTION),
        eq(RUN_ID), eq(WORKFLOW_NAME), eq(WorkflowRunDoc.class));
    verify(cosmosStore).replaceItem(eq(PARTITION_ID), eq(DATABASE_NAME),
        eq(WORKFLOW_RUN_COLLECTION), eq(RUN_ID), eq(WORKFLOW_NAME), any(WorkflowRunDoc.class));
    verify(cosmosConfig,times(2)).getDatabase();
    verify(cosmosConfig, times(2)).getWorkflowRunCollection();
    verify(dpsHeaders, times(2)).getPartitionId();
    assertThat(workflowRunDocArgumentCaptor.getValue().getStatus(), equalTo(response.getStatus().toString()));
    assertThat(workflowRunDocArgumentCaptor.getValue().getId(), equalTo(response.getRunId()));
    assertThat(workflowRunDocArgumentCaptor.getValue().getWorkflowName(), equalTo(response.getWorkflowId()));
    assertThat(workflowRunDocArgumentCaptor.getValue().getSubmittedBy(), equalTo(response.getSubmittedBy()));
    assertThat(workflowRunDocArgumentCaptor.getValue().getEndTimeStamp(), equalTo(WORKFLOW_RUN_END_TIMESTAMP));
  }

  @Test
  public void testGetWorkflowRunsByWorkflowIdWithValidWorkflowId() throws Exception {
    final WorkflowRunDoc workflowRunDoc = OBJECT_MAPPER.readValue(WORKFLOW_RUN_DOC,
        WorkflowRunDoc.class);
    List<WorkflowRun> workflowRunList = verifyAndGetWorkflowRunsByWorkflowName(WORKFLOW_NAME, null,
        Arrays.asList(workflowRunDoc));
    Assertions.assertEquals(1, workflowRunList.size());
    WorkflowRun returnedWorkflowRun = workflowRunList.get(0);
    final WorkflowRun expectedWorkflowRun =
        OBJECT_MAPPER.readValue(WORKFLOW_RUN, WorkflowRun.class);
    Assertions.assertEquals(expectedWorkflowRun, returnedWorkflowRun);
  }

  @Test
  public void testGetWorkflowRunsByWorkflowIdWithValidWorkflowIdWithCursor() throws Exception {
    final WorkflowRunDoc workflowRunDoc = OBJECT_MAPPER.readValue(WORKFLOW_RUN_DOC,
        WorkflowRunDoc.class);
    List<WorkflowRun> workflowRunList = verifyAndGetWorkflowRunsByWorkflowName(WORKFLOW_NAME,
        TEST_CURSOR, Arrays.asList(workflowRunDoc));
    Assertions.assertEquals(1, workflowRunList.size());
    WorkflowRun returnedWorkflowRun = workflowRunList.get(0);
    final WorkflowRun expectedWorkflowRun =
        OBJECT_MAPPER.readValue(WORKFLOW_RUN, WorkflowRun.class);
    Assertions.assertEquals(expectedWorkflowRun, returnedWorkflowRun);
  }

  @Test
  public void testGetWorkflowRunsByWorkflowIdWithInValidWorkflowId() throws Exception {
    List<WorkflowRun> workflowRunList = verifyAndGetWorkflowRunsByWorkflowName("invalid-workflow-id",
        null, new ArrayList<>());
    Assertions.assertEquals(0, workflowRunList.size());
  }

  @Test
  public void testDeleteWorkflowRuns() throws Exception {
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowRunCollection()).thenReturn(WORKFLOW_RUN_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    ArgumentCaptor<String> runIdCaptor = ArgumentCaptor.forClass(String.class);
    doNothing().when(cosmosStore).deleteItem(eq(PARTITION_ID), eq(DATABASE_NAME),
        eq(WORKFLOW_RUN_COLLECTION), runIdCaptor.capture(), eq(WORKFLOW_NAME));

    List<String> runIds = Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    workflowRunRepository.deleteWorkflowRuns(WORKFLOW_NAME, runIds);
    verify(cosmosStore, times(runIds.size())).deleteItem(eq(PARTITION_ID), eq(DATABASE_NAME),
        eq(WORKFLOW_RUN_COLLECTION), anyString(), eq(WORKFLOW_NAME));
    List<String> capturedRunIds = runIdCaptor.getAllValues();
    for(int i = 0; i < runIds.size(); i++) {
      Assertions.assertEquals(runIds.get(i), capturedRunIds.get(i));
    }
    verify(cosmosConfig, times(runIds.size())).getDatabase();
    verify(cosmosConfig, times(runIds.size())).getWorkflowRunCollection();
    verify(dpsHeaders, times(runIds.size())).getPartitionId();
  }

  private List<WorkflowRun> verifyAndGetWorkflowRunsByWorkflowName(String workflowId, String cursor,
                                                                   List<WorkflowRunDoc> toBeReturnedWorkflowRunDocs) {
    if(cursor != null) {
      when(cursorUtils.decodeCosmosCursor(eq(cursor))).thenReturn(cursor);
    }
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowRunCollection()).thenReturn(WORKFLOW_RUN_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    ArgumentCaptor<SqlQuerySpec> sqlQuerySpecArgumentCaptor =
        ArgumentCaptor.forClass(SqlQuerySpec.class);
    Page<WorkflowRunDoc> workflowRunDocPage = new PageImpl<>(toBeReturnedWorkflowRunDocs,
        CosmosStorePageRequest.of(1,1, null, Sort.unsorted()), 1);
    when(cosmosStore.queryItemsPage(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_RUN_COLLECTION),
        sqlQuerySpecArgumentCaptor.capture(), eq(WorkflowRunDoc.class), eq(TEST_LIMIT),
        eq(cursor))).thenReturn(workflowRunDocPage);

    WorkflowRunsPage workflowRunsPage =
        workflowRunRepository.getWorkflowRunsByWorkflowName(workflowId, TEST_LIMIT, cursor);

    verify(cosmosStore).queryItemsPage(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_RUN_COLLECTION),
        any(SqlQuerySpec.class), eq(WorkflowRunDoc.class), eq(TEST_LIMIT), eq(cursor));
    SqlQuerySpec capturedSqlQuerySpec = sqlQuerySpecArgumentCaptor.getValue();
    Assertions.assertEquals("SELECT * from c where c.workflowName = @workflowName ORDER BY c._ts DESC",
        capturedSqlQuerySpec.getQueryText());
    Assertions.assertEquals(1, capturedSqlQuerySpec.getParameters().size());
    SqlParameter capturedSqlParameter = capturedSqlQuerySpec.getParameters().get(0);
    Assertions.assertEquals("@workflowName", capturedSqlParameter.getName());
    Assertions.assertEquals(workflowId, capturedSqlParameter.getValue(String.class));
    verify(cosmosConfig).getDatabase();
    verify(cosmosConfig).getWorkflowRunCollection();
    verify(dpsHeaders).getPartitionId();
    if(cursor != null) {
      verify(cursorUtils).decodeCosmosCursor(eq(cursor));
    }

    return workflowRunsPage.getItems();
  }

}
