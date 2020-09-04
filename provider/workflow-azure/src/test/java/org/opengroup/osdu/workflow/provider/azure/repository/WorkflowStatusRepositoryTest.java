package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.cosmos.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.CosmosStore;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.azure.WorkflowApplication;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowStatusDoc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.inject.Named;

import java.io.IOException;
import java.util.Optional;


@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
public class WorkflowStatusRepositoryTest {
  private static final String TEST_WORKFLOW_ID = "test-workflow-id";
  private static final String TEST_AIRFLOW_RUN_ID = "test-airflow-run-id";
  private static final String DATABASE_NAME = "someDatabase";
  private static final String WORKFLOW_STATUS_COLLECTION_NAME = "someWorkflowStatusName";
  private static final String PARTITION_ID = "somePartition";

  @Mock
  private CosmosStore cosmosStore;

  @Mock
  private CosmosConfig cosmosConfig;

  @Mock
  private DpsHeaders dpsHeaders;

  @InjectMocks
  private WorkflowStatusRepository workflowStatusRepository;

  @Before
  public void initMocks() throws Exception {
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowStatusCollection()).thenReturn(WORKFLOW_STATUS_COLLECTION_NAME);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
  }

  @Test
  public void shouldFindWorkflowStatusByWorkflowId() throws IOException {
    WorkflowStatusDoc workflowStatusDoc = createWorkflowStatusDocWithStatusFinished();
    when(cosmosStore.findItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_STATUS_COLLECTION_NAME),
        eq(TEST_WORKFLOW_ID),
        eq(TEST_WORKFLOW_ID),
        eq(WorkflowStatusDoc.class))).thenReturn(Optional.of(workflowStatusDoc));
    WorkflowStatus workflowStatus = workflowStatusRepository.findWorkflowStatus(TEST_WORKFLOW_ID);
    Assert.assertNotNull(workflowStatus);
    Assert.assertEquals(getWorkflowStatus().getAirflowRunId(), workflowStatusDoc.getAirflowRunId());
    Assert.assertEquals(getWorkflowStatus().getWorkflowId(), workflowStatusDoc.getWorkflowId());

  }


  @Test(expected = WorkflowNotFoundException.class)
  public void shouldThrowExceptionWhenWorkflowNotFound() throws CosmosClientException {
    when(cosmosStore.findItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_STATUS_COLLECTION_NAME),
        eq(TEST_WORKFLOW_ID),
        eq(TEST_WORKFLOW_ID),
        eq(WorkflowStatusDoc.class))).thenReturn(Optional.empty());
    workflowStatusRepository.findWorkflowStatus(TEST_WORKFLOW_ID);
  }

  @Test(expected = AppException.class)
  public void shouldThrowExceptionWhenCosmosException() throws CosmosClientException {
    when(cosmosStore.findItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_STATUS_COLLECTION_NAME),
        eq(TEST_WORKFLOW_ID),
        eq(TEST_WORKFLOW_ID),
        eq(WorkflowStatusDoc.class))).thenThrow(new AppException(500, "", ""));
    workflowStatusRepository.findWorkflowStatus(TEST_WORKFLOW_ID);
  }

  private WorkflowStatus getWorkflowStatus() {
    return WorkflowStatus.builder()
        .airflowRunId(TEST_AIRFLOW_RUN_ID)
        .workflowId(TEST_WORKFLOW_ID)
        .workflowStatusType(WorkflowStatusType.FINISHED)
        .build();
  }

  @Test
  public void saveWorkflowStatusAndReturnExistingCollection() throws IOException {
    WorkflowStatus workflowstatus = WorkflowStatus.builder()
        .workflowId(TEST_WORKFLOW_ID)
        .airflowRunId(TEST_AIRFLOW_RUN_ID)
        .workflowStatusType(WorkflowStatusType.FINISHED)
        .build();
    when(cosmosStore.findItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_STATUS_COLLECTION_NAME),
        eq(TEST_WORKFLOW_ID),
        eq(TEST_WORKFLOW_ID),
        eq(WorkflowStatusDoc.class))).thenReturn(Optional.empty());
    doNothing().when(cosmosStore).upsertItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_STATUS_COLLECTION_NAME),
        any());

    WorkflowStatus status = workflowStatusRepository.saveWorkflowStatus(workflowstatus);
    Assert.assertNotNull(status);
    Assert.assertEquals(status.getWorkflowId(), workflowstatus.getWorkflowId());
    Assert.assertEquals(status.getAirflowRunId(), workflowstatus.getAirflowRunId());
  }

  @Test
  public void updateWorkflowStatus() throws IOException {
    when(cosmosStore.findItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_STATUS_COLLECTION_NAME),
        eq(TEST_WORKFLOW_ID),
        eq(TEST_WORKFLOW_ID),
        eq(WorkflowStatusDoc.class)))
        .thenReturn(Optional.of(createWorkflowStatusDocWithStatusSubmitted()));
    WorkflowStatus workflowStatus = workflowStatusRepository.updateWorkflowStatus(TEST_WORKFLOW_ID,
        WorkflowStatusType.FINISHED);
    Assert.assertNotNull(workflowStatus);
    Assert.assertEquals(workflowStatus.getWorkflowId(), TEST_WORKFLOW_ID);
    Assert.assertEquals(workflowStatus.getAirflowRunId(), TEST_AIRFLOW_RUN_ID);
    Assert.assertNotEquals(workflowStatus.getWorkflowStatusType(), createWorkflowStatusDocWithStatusSubmitted().getWorkflowStatusType());

  }

  @Test(expected = WorkflowNotFoundException.class)
  public void updateWorkflowStatusThrowWorkflowIDNotFound() throws CosmosClientException {
    when(cosmosStore.findItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_STATUS_COLLECTION_NAME),
        eq(TEST_WORKFLOW_ID),
        eq(TEST_WORKFLOW_ID),
        eq(WorkflowStatusDoc.class))).thenReturn(Optional.empty());
    workflowStatusRepository.updateWorkflowStatus(TEST_WORKFLOW_ID, WorkflowStatusType.FINISHED);
  }

  private WorkflowStatusDoc createWorkflowStatusDocWithStatusFinished() {
    WorkflowStatusDoc workflowStatusDoc = new WorkflowStatusDoc();
    workflowStatusDoc.setAirflowRunId(TEST_AIRFLOW_RUN_ID);
    workflowStatusDoc.setId("TestID");
    workflowStatusDoc.setWorkflowId(TEST_WORKFLOW_ID);
    workflowStatusDoc.setWorkflowStatusType(WorkflowStatusType.FINISHED);
    return workflowStatusDoc;
  }

  private WorkflowStatusDoc createWorkflowStatusDocWithStatusSubmitted() {
    WorkflowStatusDoc workflowStatusDoc = new WorkflowStatusDoc();
    workflowStatusDoc.setAirflowRunId(TEST_AIRFLOW_RUN_ID);
    workflowStatusDoc.setId("TestID");
    workflowStatusDoc.setWorkflowId(TEST_WORKFLOW_ID);
    workflowStatusDoc.setWorkflowStatusType(WorkflowStatusType.SUBMITTED);
    return workflowStatusDoc;
  }
}
