package org.opengroup.osdu.workflow.provider.azure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.logging.AuditLogger;
import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowManagerService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunService;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WorkflowSystemManagerServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class WorkflowSystemManagerServiceImplTest {
  private static final String WORKFLOW_NAME = "test_dag_name";
  private static final String INVALID_WORKFLOW_NAME = "invalid-workflow-name";
  private static final String USER_EMAIL = "user@email.com";
  private static final long SEED_VERSION = 1;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String CREATE_WORKFLOW_REQUEST = "{\n" +
      "  \"workflowName\": \"HelloWorld\",\n" +
      "  \"description\": \"This is a test workflow\",\n" +
      "  \"registrationInstructions\": {\n" +
      "    \"dagContent\": \"sample-dag-content\"\n" +
      "  }\n" +
      "}";

  private static final String CREATE_WORKFLOW_REQUEST_WITH_INVALID_WORKFLOW_NAME = "{\n" +
      "  \"workflowName\": \"\",\n" +
      "  \"description\": \"This is a test workflow\",\n" +
      "  \"registrationInstructions\": {\n" +
      "    \"dagContent\": \"sample-dag-content\"\n" +
      "  }\n" +
      "}";

  private static final String GET_WORKFLOW_RESPONSE = "{\n" +
      "  \"workflowId\": \"2afccfb8-1351-41c6-9127-61f2d7f22ff8\",\n" +
      "  \"workflowName\": \"HelloWorld\",\n" +
      "  \"description\": \"This is a test workflow\",\n" +
      "  \"creationTimestamp\": 1600144876028,\n" +
      "  \"createdBy\": \"user@email.com\",\n" +
      "  \"version\": 1\n" +
      "}";

  private static final String PREFIX_INPUT = "hwoello";

  @Mock
  private IWorkflowMetadataRepository workflowMetadataRepository;

  @Mock
  private DpsHeaders dpsHeaders;

  @Mock
  private IWorkflowEngineService workflowEngineService;
  @Mock
  private IWorkflowManagerService workflowManagerService;

  @Mock
  private IWorkflowRunService workflowRunService;

  @Mock
  private AuditLogger auditLogger;

  @InjectMocks
  private WorkflowSystemManagerServiceImpl workflowSystemManagerService;


  @BeforeEach
  public void setup() {
  }

  @Test
  void testCreateWorkflowWithValidData() throws Exception {
    final CreateWorkflowRequest request =
        OBJECT_MAPPER.readValue(CREATE_WORKFLOW_REQUEST, CreateWorkflowRequest.class);
    final WorkflowMetadata responseMetadata = mock(WorkflowMetadata.class);
    when(workflowManagerService.createWorkflow(request))
        .thenReturn(responseMetadata);

    workflowSystemManagerService.createSystemWorkflow(request);

    verify(workflowManagerService).createWorkflow(request);
  }


  @Test
  public void testCreateWorkflowWithInvalidWorkflowName() throws Exception {
    final CreateWorkflowRequest request =
        OBJECT_MAPPER.readValue(CREATE_WORKFLOW_REQUEST_WITH_INVALID_WORKFLOW_NAME, CreateWorkflowRequest.class);
    Assertions.assertThrows(BadRequestException.class, () -> {
      workflowSystemManagerService.createSystemWorkflow(request);
    });
  }


  @Test
  void testDeleteWorkflowWithValidId() throws Exception {
    final WorkflowMetadata workflowMetadata = OBJECT_MAPPER.readValue(GET_WORKFLOW_RESPONSE,
        WorkflowMetadata.class);
    when(workflowMetadataRepository.getWorkflow(WORKFLOW_NAME)).thenReturn(workflowMetadata);
    doNothing().when(workflowMetadataRepository).deleteWorkflow(WORKFLOW_NAME);
    final ArgumentCaptor<WorkflowEngineRequest> workflowEngineRequestCaptor =
        ArgumentCaptor.forClass(WorkflowEngineRequest.class);
    doNothing().when(workflowEngineService).deleteWorkflow(workflowEngineRequestCaptor.capture());

    workflowSystemManagerService.deleteSystemWorkflow(WORKFLOW_NAME);

    verify(workflowMetadataRepository).deleteWorkflow(WORKFLOW_NAME);
    verify(workflowEngineService).deleteWorkflow(any());
    assertThat(workflowEngineRequestCaptor.getValue().getWorkflowName(), equalTo(WORKFLOW_NAME));
  }

  @Test
  public void testDeleteWorkflowWithInvalidId() throws Exception {
    when(workflowMetadataRepository.getWorkflow(INVALID_WORKFLOW_NAME))
        .thenThrow(new WorkflowNotFoundException("not found"));

    Assertions.assertThrows(WorkflowNotFoundException.class, () -> {
      workflowSystemManagerService.deleteSystemWorkflow(INVALID_WORKFLOW_NAME);
    });

    verify(workflowMetadataRepository).getWorkflow(INVALID_WORKFLOW_NAME);
    verify(workflowMetadataRepository, times(0)).deleteWorkflow(INVALID_WORKFLOW_NAME);
    verify(workflowEngineService, times(0)).deleteWorkflow(any(WorkflowEngineRequest.class));
  }

}
