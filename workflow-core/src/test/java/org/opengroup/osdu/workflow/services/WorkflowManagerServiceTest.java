package org.opengroup.osdu.workflow.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunService;
import org.opengroup.osdu.workflow.service.WorkflowManagerServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WorkflowManagerServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class WorkflowManagerServiceTest {
  private static final String WORKFLOW_NAME = "test_dag_name";
  private static final String USER_EMAIL = "user@email.com";
  private static final long SEED_VERSION = 1;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String CREATE_WORKFLOW_REQUEST = "{\n" +
      "  \"workflowName\": \"HelloWorld\",\n" +
      "  \"description\": \"This is a test workflow\"\n" +
      "}";

  private static final String GET_WORKFLOW_RESPONSE = "{\n" +
      "  \"workflowId\": \"2afccfb8-1351-41c6-9127-61f2d7f22ff8\",\n" +
      "  \"workflowName\": \"HelloWorld\",\n" +
      "  \"description\": \"This is a test workflow\",\n" +
      "  \"creationTimestamp\": 1600144876028,\n" +
      "  \"createdBy\": \"user@email.com\",\n" +
      "  \"version\": 1\n" +
      "}";

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
  void testCreateWorkflowWithValidData() throws Exception {
    when(dpsHeaders.getUserEmail()).thenReturn(USER_EMAIL);
    final CreateWorkflowRequest request =
        OBJECT_MAPPER.readValue(CREATE_WORKFLOW_REQUEST, CreateWorkflowRequest.class);
    final ArgumentCaptor<WorkflowMetadata> workflowMetadataCaptor = ArgumentCaptor
        .forClass(WorkflowMetadata.class);
    final WorkflowMetadata responseMetadata = mock(WorkflowMetadata.class);
    when(workflowMetadataRepository.createWorkflow(workflowMetadataCaptor.capture()))
        .thenReturn(responseMetadata);
    final WorkflowMetadata returnedMetadata = workflowManagerService.createWorkflow(request);

    verify(workflowMetadataRepository).createWorkflow(any(WorkflowMetadata.class));
    verify(workflowEngineService).createWorkflow(any(), any());
    verify(dpsHeaders, times(1)).getUserEmail();

    assertThat(returnedMetadata, equalTo(responseMetadata));
    assertThat(workflowMetadataCaptor.getValue().getWorkflowName(),
        equalTo(request.getWorkflowName()));
    assertThat(workflowMetadataCaptor.getValue().getDescription(),
        equalTo(request.getDescription()));
    assertThat(workflowMetadataCaptor.getValue().getCreatedBy(), equalTo(USER_EMAIL));
    assertThat(workflowMetadataCaptor.getValue().getVersion(), equalTo(SEED_VERSION));
  }

  @Test
  void testCreateWorkflowWithConflict() throws Exception {
    final CreateWorkflowRequest request =
        OBJECT_MAPPER.readValue(CREATE_WORKFLOW_REQUEST, CreateWorkflowRequest.class);
    final ArgumentCaptor<WorkflowMetadata> workflowMetadataCaptor = ArgumentCaptor
        .forClass(WorkflowMetadata.class);
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
        .createWorkflow(workflowEngineRequest(anyString()),
            eq(request.getRegistrationInstructions()));
    verify(dpsHeaders, times(1)).getUserEmail();
    assertThat(workflowMetadataCaptor.getValue().getWorkflowName(),
        equalTo(request.getWorkflowName()));
    assertThat(workflowMetadataCaptor.getValue().getDescription(),
        equalTo(request.getDescription()));
    assertThat(workflowMetadataCaptor.getValue().getCreatedBy(), equalTo(USER_EMAIL));
    assertThat(workflowMetadataCaptor.getValue().getVersion(), equalTo(SEED_VERSION));
  }

  @Test
  void testGetWorkflowByIdWithExistingWorkflow() throws Exception {
    final WorkflowMetadata responseMetadata = mock(WorkflowMetadata.class);
    final ArgumentCaptor<String> workflowIdCaptor = ArgumentCaptor.forClass(String.class);
    when(workflowMetadataRepository.getWorkflow(workflowIdCaptor.capture()))
        .thenReturn(responseMetadata);
    final WorkflowMetadata returnedMetadata = workflowManagerService
        .getWorkflowByName(WORKFLOW_NAME);
    verify(workflowMetadataRepository).getWorkflow(anyString());
    assertThat(returnedMetadata, equalTo(responseMetadata));
    assertThat(workflowIdCaptor.getValue(), equalTo(WORKFLOW_NAME));
  }

  @Test
  void testGetWorkflowByIdWithNonExistingWorkflow() throws Exception {
    final ArgumentCaptor<String> workflowIdCaptor = ArgumentCaptor.forClass(String.class);
    when(workflowMetadataRepository.getWorkflow(workflowIdCaptor.capture()))
        .thenThrow(WorkflowNotFoundException.class);
    Assertions.assertThrows(WorkflowNotFoundException.class, () -> {
      workflowManagerService.getWorkflowByName(WORKFLOW_NAME);
    });
    verify(workflowMetadataRepository).getWorkflow(anyString());
    assertThat(workflowIdCaptor.getValue(), equalTo(WORKFLOW_NAME));
  }

  @Test
  void testDeleteWorkflowWithValidId() throws Exception {
    doNothing().when(workflowRunService).deleteWorkflowRunsByWorkflowName(WORKFLOW_NAME);
    doNothing().when(workflowMetadataRepository).deleteWorkflow(WORKFLOW_NAME);
    doNothing().when(workflowEngineService).deleteWorkflow(any());

    workflowManagerService.deleteWorkflow(WORKFLOW_NAME);

    verify(workflowMetadataRepository).deleteWorkflow(WORKFLOW_NAME);
    verify(workflowRunService).deleteWorkflowRunsByWorkflowName(WORKFLOW_NAME);
    verify(workflowEngineService).deleteWorkflow(any());
  }

  private WorkflowEngineRequest workflowEngineRequest(final String workflowName) {
    return new WorkflowEngineRequest(workflowName);
  }
}
