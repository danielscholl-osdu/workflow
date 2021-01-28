package org.opengroup.osdu.workflow.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.exception.WorkflowRunNotFoundException;
import org.opengroup.osdu.workflow.model.*;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunRepository;
import org.opengroup.osdu.workflow.service.WorkflowRunServiceImpl;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link WorkflowRunServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
@Disabled
public class WorkflowRunServiceTest {
  private static final String KEY_RUN_ID = "runId";
  private static final String KEY_AUTH_TOKEN = "authToken";
  private static final String KEY_RUN_CONFIG = "runConfiguration";
  private static final String KEY_WORKFLOW_ID = "workflowId";
  private static final String KEY_CORRELATION_ID = "correlationId";
  private static final String AUTH_TOKEN = "Bearer Dummy";
  private static final String WORKFLOW_ID = "some-workflow-id";
  private static final String WORKFLOW_NAME = "some-dag-name";
  private static final String CORRELATION_ID = "some-correlation-id";
  private static final long WORKFLOW_RUN_START_TIMESTAMP = 1236331L;
  private static final String RUN_ID = "d13f7fd0-d27e-4176-8d60-6e9aad86e347";
  private static final String USER_EMAIL = "user@email.com";
  private static final String TEST_CURSOR = "test-cursor";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String WORKFLOW_METADATA = "{\n" +
      "  \"workflowId\": \"SGVsbG9Xb3JsZA==\",\n" +
      "  \"workflowName\": \"HelloWorld\",\n" +
      "  \"description\": \"This is a test workflow\",\n" +
      "  \"concurrentWorkflowRun\": 5,\n" +
      "  \"concurrentTaskRun\": 5,\n" +
      "  \"active\": true,\n" +
      "  \"creationDate\": 1600144876028,\n" +
      "  \"createdBy\": \"user@email.com\",\n" +
      "  \"version\": 1\n" +
      "}";
  private static final String WORKFLOW_TRIGGER_REQUEST_DATA = "{\n" +
      "  \"runId\": \"d13f7fd0-d27e-4176-8d60-6e9aad86e347\",\n" +
      "  \"workflowTriggerConfig\": {\n" +
      "    \"id\": \"someid\",\n" +
      "    \"kind\": \"somekind\",\n" +
      "    \"dataPartitionId\": \"someId\"\n" +
      "  },\n" +
      "  \"additionalProperties\": {}\n" +
      "}";
  private static final String SUBMITTED_WORKFLOW_RUN = "{\n" +
      "  \"workflowId\": \"SGVsbG9Xb3JsZA==\",\n" +
      "  \"runId\": \"d13f7fd0-d27e-4176-8d60-6e9aad86e347\",\n" +
      "  \"startTimeStamp\": 1236331,\n" +
      "  \"status\": \"submitted\",\n" +
      "  \"submittedBy\": \"0b16033e-6e20-481f-9951-ad59efbf88fc\"\n" +
      "}";
  private static final String RUNNING_WORKFLOW_RUN = "{\n" +
      "  \"workflowId\": \"SGVsbG9Xb3JsZA==\",\n" +
      "  \"runId\": \"d13f7fd0-d27e-4176-8d60-6e9aad86e34523\",\n" +
      "  \"startTimeStamp\": 1236331,\n" +
      "  \"status\": \"running\",\n" +
      "  \"submittedBy\": \"0b16033e-6e20-481f-9951-ad59efbf88fc\"\n" +
      "}";
  private static final String FINISHED_WORKFLOW_RUN = "{\n" +
      "  \"workflowId\": \"SGVsbG9Xb3JsZA==\",\n" +
      "  \"runId\": \"d13f7fd0-d27e-4176-8d60-6e9aad86eq781641\",\n" +
      "  \"startTimeStamp\": 1236331,\n" +
      "  \"endTimeStamp\": 1231238912,\n" +
      "  \"status\": \"finished\",\n" +
      "  \"submittedBy\": \"0b16033e-6e20-481f-9951-ad59efbf88fc\"\n" +
      "}";

  @Mock
  private IWorkflowMetadataRepository workflowMetadataRepository;

  @Mock
  private IWorkflowRunRepository workflowRunRepository;

  @Mock
  private DpsHeaders dpsHeaders;

  @Mock
  private IWorkflowEngineService workflowEngineService;

  @InjectMocks
  private WorkflowRunServiceImpl workflowRunService;

  @Test
  @Disabled
  public void testTriggerWorkflowWithExistingWorkflowId() throws Exception {
    final WorkflowMetadata workflowMetadata = OBJECT_MAPPER.readValue(WORKFLOW_METADATA, WorkflowMetadata.class);
    final ArgumentCaptor<Long> startTimeStampArgumentCaptor = ArgumentCaptor.forClass(Long.class);
    final TriggerWorkflowRequest request =
        OBJECT_MAPPER.readValue(WORKFLOW_TRIGGER_REQUEST_DATA, TriggerWorkflowRequest.class);
    when(workflowMetadataRepository.getWorkflow(eq(WORKFLOW_NAME))).thenReturn(workflowMetadata);
    doNothing().when(workflowEngineService).triggerWorkflow(eq(workflowEngineRequest(RUN_ID, WORKFLOW_ID, WORKFLOW_NAME)), eq(workflowPayload(RUN_ID, request)));
    when(dpsHeaders.getAuthorization()).thenReturn(AUTH_TOKEN);
    when(dpsHeaders.getUserEmail()).thenReturn(USER_EMAIL);
    when(dpsHeaders.getCorrelationId()).thenReturn(CORRELATION_ID);
    final ArgumentCaptor<WorkflowRun> workflowRunArgumentCaptor = ArgumentCaptor.forClass(WorkflowRun.class);
    final WorkflowRun responseWorkflowRun = mock(WorkflowRun.class);
    when(workflowRunRepository.saveWorkflowRun(workflowRunArgumentCaptor.capture())).thenReturn(responseWorkflowRun);
    final WorkflowRun returnedWorkflowRun = workflowRunService.triggerWorkflow(WORKFLOW_NAME, request);
    verify(workflowMetadataRepository).getWorkflow(eq(WORKFLOW_NAME));
    verify(workflowEngineService)
        .triggerWorkflow(eq(workflowEngineRequest(RUN_ID, WORKFLOW_ID, WORKFLOW_NAME)), eq(workflowPayload(RUN_ID, request)));
    verify(workflowRunRepository).saveWorkflowRun(any(WorkflowRun.class));
    verify(dpsHeaders).getAuthorization();
    verify(dpsHeaders).getUserEmail();
    verify(dpsHeaders).getCorrelationId();
    assertThat(returnedWorkflowRun, equalTo(responseWorkflowRun));
    assertThat(workflowRunArgumentCaptor.getValue().getRunId(), equalTo(RUN_ID));
    assertThat(workflowRunArgumentCaptor.getValue().getWorkflowId(), equalTo(WORKFLOW_NAME));
    assertThat(workflowRunArgumentCaptor.getValue().getSubmittedBy(), equalTo(USER_EMAIL));
    assertThat(workflowRunArgumentCaptor.getValue().getStartTimeStamp(), equalTo(startTimeStampArgumentCaptor.getValue()));
    assertThat(workflowRunArgumentCaptor.getValue().getStatus(), equalTo(WorkflowStatusType.SUBMITTED));
  }

  @Test
  public void testTriggerWorkflowWithNonExistingWorkflowId() throws Exception {
    when(workflowMetadataRepository.getWorkflow(eq(WORKFLOW_NAME))).thenThrow(WorkflowNotFoundException.class);
    final TriggerWorkflowRequest request =
        OBJECT_MAPPER.readValue(WORKFLOW_TRIGGER_REQUEST_DATA, TriggerWorkflowRequest.class);
    Assertions.assertThrows(WorkflowNotFoundException.class, () -> {
      workflowRunService.triggerWorkflow(WORKFLOW_NAME, request);
    });
    verify(workflowMetadataRepository).getWorkflow(eq(WORKFLOW_NAME));
  }

  @Test
  @Disabled
  public void testTriggerWorkflowFailedWhenSubmitIngestThrowsException() throws Exception {
    final WorkflowMetadata workflowMetadata = OBJECT_MAPPER.readValue(WORKFLOW_METADATA, WorkflowMetadata.class);
    final TriggerWorkflowRequest request =
        OBJECT_MAPPER.readValue(WORKFLOW_TRIGGER_REQUEST_DATA, TriggerWorkflowRequest.class);
    when(workflowMetadataRepository.getWorkflow(eq(WORKFLOW_NAME))).thenReturn(workflowMetadata);
    doThrow(new CoreException("Failed to trigger workflow"))
        .when(workflowEngineService).triggerWorkflow(workflowEngineRequest(RUN_ID, WORKFLOW_ID, WORKFLOW_NAME), eq(workflowPayload(RUN_ID, request)));
    when(dpsHeaders.getAuthorization()).thenReturn(AUTH_TOKEN);
    when(dpsHeaders.getCorrelationId()).thenReturn(CORRELATION_ID);
    Assertions.assertThrows(CoreException.class, () -> {
      workflowRunService.triggerWorkflow(WORKFLOW_NAME, request);
    });
    verify(workflowMetadataRepository).getWorkflow(eq(WORKFLOW_NAME));
    verify(workflowEngineService)
        .triggerWorkflow(eq(workflowEngineRequest(RUN_ID, WORKFLOW_ID, WORKFLOW_NAME)), eq(workflowPayload(RUN_ID, request)));
    verify(dpsHeaders).getAuthorization();
    verify(dpsHeaders).getCorrelationId();
    verify(workflowRunRepository, never()).saveWorkflowRun(any());
  }

  @Test
  @Disabled
  public void testGetWorkflowRunByIdWhenExistingWorkflowRunFinished() throws Exception {
    final WorkflowMetadata workflowMetadata = OBJECT_MAPPER.readValue(WORKFLOW_METADATA, WorkflowMetadata.class);
    final WorkflowRun submittedWorkflowRun = OBJECT_MAPPER.readValue(SUBMITTED_WORKFLOW_RUN, WorkflowRun.class);
    final WorkflowRun finishedWorkflowRun = OBJECT_MAPPER.readValue(FINISHED_WORKFLOW_RUN, WorkflowRun.class);
    final ArgumentCaptor<WorkflowRun> workflowRunArgumentCaptor = ArgumentCaptor.forClass(WorkflowRun.class);

    when(workflowRunRepository.getWorkflowRun(eq(WORKFLOW_NAME), eq(RUN_ID))).thenReturn(submittedWorkflowRun);
    when(workflowMetadataRepository.getWorkflow(eq(WORKFLOW_NAME))).thenReturn(workflowMetadata);
    when(workflowEngineService.
        getWorkflowRunStatus(eq(workflowEngineRequest(WORKFLOW_NAME)))).
        thenReturn(WorkflowStatusType.FINISHED);
    when(workflowRunRepository.updateWorkflowRun(workflowRunArgumentCaptor.capture())).
        thenReturn(finishedWorkflowRun);

    final WorkflowRun returnedWorkflowRun = workflowRunService.
        getWorkflowRunByName(WORKFLOW_NAME, RUN_ID);

    verify(workflowMetadataRepository).getWorkflow(eq(WORKFLOW_NAME));
    verify(workflowRunRepository).getWorkflowRun(eq(WORKFLOW_NAME), eq(RUN_ID));
    verify(workflowEngineService).getWorkflowRunStatus(eq(workflowEngineRequest(WORKFLOW_NAME)));
    verify(workflowRunRepository).updateWorkflowRun(any(WorkflowRun.class));

    assertThat(returnedWorkflowRun, equalTo(finishedWorkflowRun));
    assertThat(workflowRunArgumentCaptor.getValue().getWorkflowId(), equalTo(submittedWorkflowRun.getWorkflowId()));
    assertThat(workflowRunArgumentCaptor.getValue().getSubmittedBy(), equalTo(submittedWorkflowRun.getSubmittedBy()));
    assertThat(workflowRunArgumentCaptor.getValue().getStartTimeStamp(), equalTo(submittedWorkflowRun.getStartTimeStamp()));
    assertThat(workflowRunArgumentCaptor.getValue().getRunId(), equalTo(submittedWorkflowRun.getRunId()));
    assertThat(workflowRunArgumentCaptor.getValue().getStatus(), equalTo(WorkflowStatusType.FINISHED));
  }

  @Test
  @Disabled
  public void testGetWorkflowRunByIdWhenExistingWorkflowRunRunning() throws Exception {
    final WorkflowMetadata workflowMetadata = OBJECT_MAPPER.readValue(WORKFLOW_METADATA, WorkflowMetadata.class);
    final WorkflowRun submittedWorkflowRun = OBJECT_MAPPER.readValue(SUBMITTED_WORKFLOW_RUN, WorkflowRun.class);
    final WorkflowRun runningWorkflowRun = OBJECT_MAPPER.readValue(RUNNING_WORKFLOW_RUN, WorkflowRun.class);
    final ArgumentCaptor<WorkflowRun> workflowRunArgumentCaptor = ArgumentCaptor.forClass(WorkflowRun.class);

    when(workflowMetadataRepository.getWorkflow(eq(WORKFLOW_NAME))).thenReturn(workflowMetadata);
    when(workflowRunRepository.getWorkflowRun(eq(WORKFLOW_NAME), eq(RUN_ID))).
        thenReturn(submittedWorkflowRun);
    when(workflowEngineService.
        getWorkflowRunStatus(eq(workflowEngineRequest(WORKFLOW_NAME)))).
        thenReturn(WorkflowStatusType.RUNNING);
    when(workflowRunRepository.updateWorkflowRun(workflowRunArgumentCaptor.capture())).
        thenReturn(runningWorkflowRun);

    final WorkflowRun returnedWorkflowRun = workflowRunService.getWorkflowRunByName(WORKFLOW_NAME, RUN_ID);

    verify(workflowMetadataRepository).getWorkflow(eq(WORKFLOW_NAME));
    verify(workflowRunRepository).getWorkflowRun(eq(WORKFLOW_NAME), eq(RUN_ID));
    verify(workflowEngineService).getWorkflowRunStatus(eq(workflowEngineRequest(WORKFLOW_NAME)));
    verify(workflowRunRepository).updateWorkflowRun(any(WorkflowRun.class));

    assertThat(returnedWorkflowRun, equalTo(runningWorkflowRun));
    assertThat(workflowRunArgumentCaptor.getValue().getWorkflowId(), equalTo(submittedWorkflowRun.getWorkflowId()));
    assertThat(workflowRunArgumentCaptor.getValue().getSubmittedBy(), equalTo(submittedWorkflowRun.getSubmittedBy()));
    assertThat(workflowRunArgumentCaptor.getValue().getStartTimeStamp(), equalTo(submittedWorkflowRun.getStartTimeStamp()));
    assertThat(workflowRunArgumentCaptor.getValue().getEndTimeStamp(), equalTo(submittedWorkflowRun.getEndTimeStamp()));
    assertThat(workflowRunArgumentCaptor.getValue().getRunId(), equalTo(submittedWorkflowRun.getRunId()));
    assertThat(workflowRunArgumentCaptor.getValue().getStatus(), equalTo(WorkflowStatusType.RUNNING));
  }

  @Test
  public void testGetWorkflowRunByIdWhenExistingFinishedWorkflowRun() throws Exception {
    final WorkflowRun finishedWorkflowRun = OBJECT_MAPPER.readValue(FINISHED_WORKFLOW_RUN, WorkflowRun.class);
    when(workflowRunRepository.getWorkflowRun(eq(WORKFLOW_NAME), eq(RUN_ID))).thenReturn(finishedWorkflowRun);
    final WorkflowRun returnedWorkflowRun = workflowRunService.getWorkflowRunByName(WORKFLOW_NAME, RUN_ID);
    verify(workflowRunRepository).getWorkflowRun(eq(WORKFLOW_NAME), eq(RUN_ID));
    assertThat(returnedWorkflowRun, equalTo(finishedWorkflowRun));
  }

  @Test
  public void testGetWorkflowRunByIdWhenNonExistingWorkflowRun() throws Exception {
    when(workflowRunRepository.getWorkflowRun(eq(WORKFLOW_NAME), eq(RUN_ID)))
        .thenThrow(WorkflowRunNotFoundException.class);
    Assertions.assertThrows(WorkflowRunNotFoundException.class, () -> {
      workflowRunService.getWorkflowRunByName(WORKFLOW_NAME,RUN_ID );
    });
    verify(workflowRunRepository).getWorkflowRun(eq(WORKFLOW_NAME), eq(RUN_ID));
  }

  @Test
  public void testDeleteWorkflowRunsByWorkflowIdWithInActiveWorkflowRuns() throws Exception {
    final WorkflowRun finishedWorkflowRun = OBJECT_MAPPER.readValue(FINISHED_WORKFLOW_RUN,
        WorkflowRun.class);
    when(workflowRunRepository.getWorkflowRunsByWorkflowName(eq(WORKFLOW_NAME), anyInt(), eq(null)))
        .thenReturn(new WorkflowRunsPage(Arrays.asList(finishedWorkflowRun, finishedWorkflowRun),
            null));
    ArgumentCaptor<List<String>> runIdListCaptor = ArgumentCaptor.forClass(List.class);
    doNothing().when(workflowRunRepository).deleteWorkflowRuns(eq(WORKFLOW_NAME),
        runIdListCaptor.capture());

    workflowRunService.deleteWorkflowRunsByWorkflowName(WORKFLOW_NAME);

    verify(workflowRunRepository).getWorkflowRunsByWorkflowName(eq(WORKFLOW_NAME), anyInt(), eq(null));
    verify(workflowRunRepository).deleteWorkflowRuns(eq(WORKFLOW_NAME), any(List.class));
    List<String> capturedRunIds = runIdListCaptor.getValue();
    for(String capturedRunId: capturedRunIds) {
      Assertions.assertEquals(finishedWorkflowRun.getRunId(), capturedRunId);
    }
  }

  @Test
  public void testDeleteWorkflowRunsByWorkflowIdWithActiveWorkflowRuns() throws Exception {
    final WorkflowRun finishedWorkflowRun = OBJECT_MAPPER.readValue(FINISHED_WORKFLOW_RUN,
        WorkflowRun.class);
    final WorkflowRun submittedWorkflowRun = OBJECT_MAPPER.readValue(SUBMITTED_WORKFLOW_RUN,
        WorkflowRun.class);
    final WorkflowRun runningWorkflowRun = OBJECT_MAPPER.readValue(RUNNING_WORKFLOW_RUN,
        WorkflowRun.class);
    when(workflowRunRepository.getWorkflowRunsByWorkflowName(eq(WORKFLOW_NAME), anyInt(), eq(null)))
        .thenReturn(new WorkflowRunsPage(Arrays.asList(finishedWorkflowRun, submittedWorkflowRun),
            TEST_CURSOR));
    when(workflowRunRepository.getWorkflowRunsByWorkflowName(eq(WORKFLOW_NAME), anyInt(),
        eq(TEST_CURSOR))).thenReturn(new WorkflowRunsPage(Arrays.asList(runningWorkflowRun),
        null));

    boolean isExceptionThrown = false;
    try {
      workflowRunService.deleteWorkflowRunsByWorkflowName(WORKFLOW_NAME);
    } catch (AppException e) {
      isExceptionThrown = true;
      Assertions.assertEquals(412, e.getError().getCode());
    }

    Assertions.assertTrue(isExceptionThrown);
    verify(workflowRunRepository).getWorkflowRunsByWorkflowName(eq(WORKFLOW_NAME), anyInt(), eq(null));
    verify(workflowRunRepository).getWorkflowRunsByWorkflowName(eq(WORKFLOW_NAME), anyInt(),
        eq(TEST_CURSOR));
    verify(workflowRunRepository, times(0)).deleteWorkflowRuns(eq(WORKFLOW_NAME), any(List.class));
  }

  @Test
  public void testDeleteWorkflowRunsByWorkflowIdWithZeroWorkflowRuns() {
    when(workflowRunRepository.getWorkflowRunsByWorkflowName(eq(WORKFLOW_NAME), anyInt(), eq(null)))
        .thenReturn(new WorkflowRunsPage(new ArrayList<>(), null));

    workflowRunService.deleteWorkflowRunsByWorkflowName(WORKFLOW_NAME);

    verify(workflowRunRepository).getWorkflowRunsByWorkflowName(eq(WORKFLOW_NAME), anyInt(), eq(null));
    verify(workflowRunRepository, times(0)).deleteWorkflowRuns(eq(WORKFLOW_NAME), any(List.class));
  }


  private WorkflowEngineRequest workflowEngineRequest(final String workflowName) {
    return workflowEngineRequest(RUN_ID, WORKFLOW_ID, WORKFLOW_NAME);
  }

  private WorkflowEngineRequest workflowEngineRequest(final String runId, final String workflowId, final String workflowName) {
    return new WorkflowEngineRequest(runId,workflowId, workflowName, WORKFLOW_RUN_START_TIMESTAMP);
  }

  private Map<String, Object> workflowPayload(final String runId, final TriggerWorkflowRequest request) {
    final Map<String, Object> payload = new HashMap<>();
    payload.put(KEY_RUN_ID, runId);
    payload.put(KEY_AUTH_TOKEN, AUTH_TOKEN);
    payload.put(KEY_RUN_CONFIG,
        OBJECT_MAPPER.convertValue(request.getExecutionContext(), Map.class));
    payload.put(KEY_WORKFLOW_ID, WORKFLOW_NAME);
    payload.put(KEY_CORRELATION_ID, CORRELATION_ID);
    return payload;
  }
}
