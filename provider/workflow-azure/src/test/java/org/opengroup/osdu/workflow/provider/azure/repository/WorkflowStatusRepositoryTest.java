package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.cosmos.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.azure.WorkflowApplication;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowStatusDoc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import javax.inject.Named;

import java.io.IOException;


@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
public class WorkflowStatusRepositoryTest {
  private static final String TEST_WORKFLOW_ID = "test-workflow-id";
  private static final String TEST_AIRFLOW_RUN_ID = "test-airflow-run-id";
  @Mock
  private CosmosItem cosmosItem;
  @Mock
  private CosmosItemResponse cosmosResponse;
  @Mock
  private CosmosItemProperties cosmosItemProperties;
  @Mock
  @Named("WORKFLOW_STATUS_CONTAINER")
  private CosmosContainer workflowStatusContainer;
  @InjectMocks
  private WorkflowStatusRepository workflowStatusRepository;

  @Before
  public void initMocks() throws Exception {
    doReturn(cosmosItem).when(workflowStatusContainer).getItem(any(), any());
    doReturn(cosmosResponse).when(cosmosItem).read(any());
    doReturn(cosmosItemProperties).when(cosmosResponse).getProperties();
  }

  @Test
  public void shouldFindWorkflowStatusByWorkflowId() throws IOException {
    WorkflowStatusDoc workflowStatusDoc = createWorkflowStatusDocWithStatusFinished();

    doReturn(workflowStatusDoc)
        .when(cosmosItemProperties)
        .getObject(any());
    WorkflowStatus workflowStatus = workflowStatusRepository.findWorkflowStatus("TestWorkflowId");
    Assert.assertNotNull(workflowStatus);
    Assert.assertEquals(getWorkflowStatus().getAirflowRunId(), workflowStatusDoc.getAirflowRunId());
    Assert.assertEquals(getWorkflowStatus().getWorkflowId(), workflowStatusDoc.getWorkflowId());

  }


  @Test(expected = WorkflowNotFoundException.class)
  public void shouldThrowExceptionWhenWorkflowNotFound() throws CosmosClientException {
    doThrow(NotFoundException.class)
        .when(cosmosItem)
        .read(any());
    workflowStatusRepository.findWorkflowStatus("InvalidWorkflowId");
  }

  @Test(expected = AppException.class)
  public void shouldThrowExceptionWhenCosmosException() throws CosmosClientException {
    doThrow(CosmosClientException.class)
        .when(cosmosItem)
        .read(any());
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
    doReturn(createWorkflowStatusDocWithStatusFinished())
        .when(cosmosItemProperties)
        .getObject(any());
    WorkflowStatus status = workflowStatusRepository.saveWorkflowStatus(workflowstatus);
    Assert.assertNotNull(status);
    Assert.assertEquals(status.getWorkflowId(), workflowstatus.getWorkflowId());
    Assert.assertEquals(status.getAirflowRunId(), workflowstatus.getAirflowRunId());

  }

  @Test
  public void saveWorkflowStatus_return_exising_status_from_collection() throws CosmosClientException {
    WorkflowStatus workflowstatus = WorkflowStatus.builder()
        .workflowId(TEST_WORKFLOW_ID)
        .airflowRunId(TEST_AIRFLOW_RUN_ID)
        .workflowStatusType(WorkflowStatusType.SUBMITTED)
        .build();
    doThrow(NotFoundException.class)
        .when(cosmosItem)
        .read(any());
    WorkflowStatus status = workflowStatusRepository.saveWorkflowStatus(workflowstatus);
    Assert.assertNotNull(status);
    Assert.assertEquals(status.getWorkflowId(), workflowstatus.getWorkflowId());
    Assert.assertEquals(status.getAirflowRunId(), workflowstatus.getAirflowRunId());

  }

  @Test
  public void updateWorkflowStatus() throws IOException {

    doReturn(createWorkflowStatusDocWithStatusSubmitted())
        .when(cosmosItemProperties)
        .getObject(any());
    WorkflowStatus workflowStatus = workflowStatusRepository.updateWorkflowStatus(TEST_WORKFLOW_ID, WorkflowStatusType.FINISHED);
    Assert.assertNotNull(workflowStatus);
    Assert.assertEquals(workflowStatus.getWorkflowId(), TEST_WORKFLOW_ID);
    Assert.assertEquals(workflowStatus.getAirflowRunId(), TEST_AIRFLOW_RUN_ID);
    Assert.assertNotEquals(workflowStatus.getWorkflowStatusType(), createWorkflowStatusDocWithStatusSubmitted().getWorkflowStatusType());

  }

  @Test(expected = WorkflowNotFoundException.class)
  public void updateWorkflowStatus_Throw_WorkflowID_NotFound() throws CosmosClientException {
    doThrow(NotFoundException.class)
        .when(cosmosItem)
        .read(any());
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
