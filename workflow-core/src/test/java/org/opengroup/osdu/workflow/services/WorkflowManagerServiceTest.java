package org.opengroup.osdu.workflow.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunService;
import org.opengroup.osdu.workflow.service.WorkflowManagerServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link WorkflowManagerServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
public class WorkflowManagerServiceTest {
  private static final String CREATE_WORKFLOW_REQUEST = "{\n" +
      "  \"workflowName\": \"HelloWorld\",\n" +
      "  \"workflowDetailContent\": \"from airflow import DAG\\r\\nfrom airflow.operators import BashOperator\",\n" +
      "  \"description\": \"This is a test workflow\",\n" +
      "  \"concurrentWorkflowRun\": 5,\n" +
      "  \"concurrentTaskRun\": 5,\n" +
      "  \"active\": true\n" +
      "}";

  private static final String GET_WORKFLOW_RESPONSE = "{\n" +
      "  \"workflowId\": \"2afccfb8-1351-41c6-9127-61f2d7f22ff8\",\n" +
      "  \"workflowName\": \"HelloWorld\",\n" +
      "  \"description\": \"This is a test workflow\",\n" +
      "  \"concurrentWorkflowRun\": 5,\n" +
      "  \"concurrentTaskRun\": 5,\n" +
      "  \"active\": true,\n" +
      "  \"creationDate\": 1600144876028,\n" +
      "  \"createdBy\": \"user@email.com\",\n" +
      "  \"version\": 1\n" +
      "}";

  private static final String WORKFLOW_NAME = "test_dag_name";
  private static final String INVALID_WORKFLOW_ID = "invalid-workflow-id";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String USER_EMAIL = "user@email.com";
  private static final long SEED_VERSION = 1;

  @Mock
  private IWorkflowMetadataRepository workflowMetadataRepository;

  @Mock
  private DpsHeaders dpsHeaders;

  @Mock
  private IWorkflowEngineService workflowEngineService;

  @Mock
  private IWorkflowRunService workflowRunService;

  @InjectMocks
  private WorkflowManagerServiceImpl workflowManagerService;

  @BeforeEach
  public void setup() {
  }

  @Test
  @Disabled
  public void testCreateWorkflowWithValidData() throws Exception {
    when(dpsHeaders.getUserEmail()).thenReturn(USER_EMAIL);
    final CreateWorkflowRequest request =
        OBJECT_MAPPER.readValue(CREATE_WORKFLOW_REQUEST, CreateWorkflowRequest.class);
    final ArgumentCaptor<WorkflowMetadata> workflowMetadataCaptor = ArgumentCaptor.forClass(WorkflowMetadata.class);
    final WorkflowMetadata responseMetadata = mock(WorkflowMetadata.class);
    when(workflowMetadataRepository.createWorkflow(workflowMetadataCaptor.capture())).thenReturn(responseMetadata);
    final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    doNothing().when(workflowEngineService)
        .createWorkflow(eq(request.getRegistrationInstructions()), fileNameCaptor.capture());
    final WorkflowMetadata returnedMetadata = workflowManagerService.createWorkflow(request);
    verify(workflowMetadataRepository).createWorkflow(any(WorkflowMetadata.class));
    verify(workflowEngineService).createWorkflow(eq(request.getRegistrationInstructions()), anyString());
    verify(dpsHeaders, times(1)).getUserEmail();
    assertThat(returnedMetadata, equalTo(responseMetadata));
    assertThat(fileNameCaptor.getValue(), equalTo(workflowMetadataCaptor.getValue().getWorkflowName()));
    assertThat(workflowMetadataCaptor.getValue().getWorkflowName(), equalTo(request.getWorkflowName()));
    assertThat(workflowMetadataCaptor.getValue().getDescription(), equalTo(request.getDescription()));
    assertThat(workflowMetadataCaptor.getValue().getCreatedBy(), equalTo(USER_EMAIL));
    assertThat(workflowMetadataCaptor.getValue().getVersion(), equalTo(SEED_VERSION));
  }

  @Test
  @Disabled
  public void testCreateWorkflowWithConflict() throws Exception {
    final CreateWorkflowRequest request =
        OBJECT_MAPPER.readValue(CREATE_WORKFLOW_REQUEST, CreateWorkflowRequest.class);
    final ArgumentCaptor<WorkflowMetadata> workflowMetadataCaptor = ArgumentCaptor.forClass(WorkflowMetadata.class);
    final WorkflowMetadata responseMetadata = mock(WorkflowMetadata.class);
    when(workflowMetadataRepository.createWorkflow(workflowMetadataCaptor.capture()))
        .thenThrow(new ResourceConflictException("conflictId", "conflicted"));
    when(dpsHeaders.getUserEmail()).thenReturn(USER_EMAIL);
    boolean isExceptionThrown = false;

    Assertions.assertThrows(CoreException.class, () -> {
      workflowManagerService.createWorkflow(request);
    });
    verify(workflowMetadataRepository, times(1)).createWorkflow(any(WorkflowMetadata.class));
    verify(workflowEngineService, times(0))
        .createWorkflow(eq(request.getRegistrationInstructions()), anyString());
    verify(dpsHeaders, times(1)).getUserEmail();
    assertThat(workflowMetadataCaptor.getValue().getWorkflowName(), equalTo(request.getWorkflowName()));
    assertThat(workflowMetadataCaptor.getValue().getDescription(), equalTo(request.getDescription()));
    assertThat(workflowMetadataCaptor.getValue().getCreatedBy(), equalTo(USER_EMAIL));
    assertThat(workflowMetadataCaptor.getValue().getVersion(), equalTo(SEED_VERSION));
  }

  @Test
  public void testGetWorkflowByIdWithExistingWorkflow() throws Exception {
    final WorkflowMetadata responseMetadata = mock(WorkflowMetadata.class);
    final ArgumentCaptor<String> workflowIdCaptor = ArgumentCaptor.forClass(String.class);
    when(workflowMetadataRepository.getWorkflow(workflowIdCaptor.capture())).thenReturn(responseMetadata);
    final WorkflowMetadata returnedMetadata = workflowManagerService.getWorkflowByName(WORKFLOW_NAME);
    verify(workflowMetadataRepository).getWorkflow(anyString());
    assertThat(returnedMetadata, equalTo(responseMetadata));
    assertThat(workflowIdCaptor.getValue(), equalTo(WORKFLOW_NAME));
  }

  @Test
  public void testGetWorkflowByIdWithNonExistingWorkflow() throws Exception {
    final ArgumentCaptor<String> workflowIdCaptor = ArgumentCaptor.forClass(String.class);
    when(workflowMetadataRepository.getWorkflow(workflowIdCaptor.capture())).thenThrow(WorkflowNotFoundException.class);
    Assertions.assertThrows(WorkflowNotFoundException.class, () -> {
      workflowManagerService.getWorkflowByName(WORKFLOW_NAME);
    });
    verify(workflowMetadataRepository).getWorkflow(anyString());
    assertThat(workflowIdCaptor.getValue(), equalTo(WORKFLOW_NAME));
  }

  @Test
  @Disabled
  public void testDeleteWorkflowWithValidId() throws Exception {
    final WorkflowMetadata workflowMetadata = OBJECT_MAPPER.readValue(GET_WORKFLOW_RESPONSE,
        WorkflowMetadata.class);
    when(workflowMetadataRepository.getWorkflow(WORKFLOW_NAME)).thenReturn(workflowMetadata);
    doNothing().when(workflowRunService).deleteWorkflowRunsByWorkflowName(WORKFLOW_NAME);
    doNothing().when(workflowMetadataRepository).deleteWorkflow(WORKFLOW_NAME);
    doNothing().when(workflowEngineService).deleteWorkflow(eq(workflowMetadata.getWorkflowName()));

    workflowManagerService.deleteWorkflow(WORKFLOW_NAME);

    verify(workflowMetadataRepository).getWorkflow(WORKFLOW_NAME);
    verify(workflowMetadataRepository).deleteWorkflow(WORKFLOW_NAME);
    verify(workflowRunService).deleteWorkflowRunsByWorkflowName(WORKFLOW_NAME);
    verify(workflowEngineService).deleteWorkflow(eq(workflowMetadata.getWorkflowName()));
  }

  @Test
  @Disabled
  public void testDeleteWorkflowWithInvalidId() throws Exception {
    when(workflowMetadataRepository.getWorkflow(INVALID_WORKFLOW_ID))
        .thenThrow(new WorkflowNotFoundException("not found"));

    Assertions.assertThrows(WorkflowNotFoundException.class, () -> {
      workflowManagerService.deleteWorkflow(INVALID_WORKFLOW_ID);
    });

    verify(workflowMetadataRepository).getWorkflow(INVALID_WORKFLOW_ID);
    verify(workflowMetadataRepository, times(0)).deleteWorkflow(WORKFLOW_NAME);
    verify(workflowRunService, times(0)).deleteWorkflowRunsByWorkflowName(WORKFLOW_NAME);
    verify(workflowEngineService, times(0)).deleteWorkflow(anyString());
  }

  @Test
  @Disabled
  public void testDeleteWorkflowIfException() throws Exception {
    final WorkflowMetadata workflowMetadata = OBJECT_MAPPER.readValue(GET_WORKFLOW_RESPONSE,
        WorkflowMetadata.class);
    when(workflowMetadataRepository.getWorkflow(WORKFLOW_NAME)).thenReturn(workflowMetadata);
    doThrow(new AppException(419, "error", "error"))
        .when(workflowRunService).deleteWorkflowRunsByWorkflowName(WORKFLOW_NAME);

    Assertions.assertThrows(AppException.class, () -> {
      workflowManagerService.deleteWorkflow(WORKFLOW_NAME);
    });

    verify(workflowMetadataRepository).getWorkflow(WORKFLOW_NAME);
    verify(workflowMetadataRepository, times(0)).deleteWorkflow(WORKFLOW_NAME);
    verify(workflowRunService).deleteWorkflowRunsByWorkflowName(WORKFLOW_NAME);
    verify(workflowEngineService, times(0)).deleteWorkflow(eq(workflowMetadata.getWorkflowName()));
  }
}
