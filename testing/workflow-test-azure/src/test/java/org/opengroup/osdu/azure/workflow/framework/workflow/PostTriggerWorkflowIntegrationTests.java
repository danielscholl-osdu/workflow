package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.exception.RetryException;
import org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder;
import org.opengroup.osdu.azure.workflow.framework.util.HTTPClient;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.FINISHED_WORKFLOW_RUN_STATUSES;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.INVALID_PARTITION;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.TRIGGER_WORKFLOW_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.WORKFLOW_NOT_FOUND_MESSAGE;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_DUMMY_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_CUSTOM_OPERATOR_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_HTTP_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_KUBERNETES_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_PYTHON_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_VALIDATE_RUN_CONFIG_DAG;
import static org.opengroup.osdu.azure.workflow.framework.util.TestDataUtil.getWorkflow;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.NEW_WORKFLOW_RUN_ID_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.NEW_WORKFLOW_RUN_STATUS;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.NEW_WORKFLOW_RUN_STATUS_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.buildInvalidTriggerWorkflowRunPayload;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.buildTriggerWorkflowPayload;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.getWorkflowRunIdFromPayload;

public abstract class PostTriggerWorkflowIntegrationTests extends TestBase {
  private static final String EXPECTED_ERROR_MESSAGE =
      "Failed to trigger workflow with id %s and name %s";

  public static final String INVALID_WORKFLOW_ID = "Invalid-Workflow-ID";
  private static final List<String> SUCCESSFUL_WORKFLOW_RUN_STATUSES =
      Arrays.asList("finished", "success");

  @Test
  public void should_complete_execution_when_givenValidRequest() throws Exception {
    Map<String, String> workflowIdToWorkflowRunId = new HashMap<>();
    /*workflowIdToWorkflowRunId.put(getWorkflow(TEST_SIMPLE_PYTHON_DAG)
            .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString(),
        verifyAndTriggerWorkflowRun(TEST_SIMPLE_PYTHON_DAG));

    workflowIdToWorkflowRunId.put(getWorkflow(TEST_SIMPLE_HTTP_DAG)
            .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString(),
        verifyAndTriggerWorkflowRun(TEST_SIMPLE_HTTP_DAG));

    workflowIdToWorkflowRunId.put(getWorkflow(TEST_SIMPLE_KUBERNETES_DAG)
            .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString(),
        verifyAndTriggerWorkflowRun(TEST_SIMPLE_KUBERNETES_DAG));*/

    workflowIdToWorkflowRunId.put(getWorkflow(TEST_SIMPLE_CUSTOM_OPERATOR_DAG)
            .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString(),
        verifyAndTriggerWorkflowRun(TEST_SIMPLE_CUSTOM_OPERATOR_DAG));

    /*workflowIdToWorkflowRunId.put(getWorkflow(TEST_VALIDATE_RUN_CONFIG_DAG)
            .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString(),
        verifyAndTriggerWorkflowRun(TEST_VALIDATE_RUN_CONFIG_DAG));

    workflowIdToWorkflowRunId.put(getWorkflow(TEST_DUMMY_DAG)
            .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString(),
        verifyAndTriggerWorkflowRun(TEST_DUMMY_DAG));*/

    Map<String, String> runIdExecutionStatus = new HashMap<>();

    executeWithWaitAndRetry(() -> {
      checkWorkflowRunStatus(workflowIdToWorkflowRunId, runIdExecutionStatus);
      return null;
    }, 20, 30, TimeUnit.SECONDS);
  }

  private String verifyAndTriggerWorkflowRun(String dagName) throws Exception{
    String workflowId = getWorkflow(dagName).get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD)
        .getAsString();
    Map<String, Object> triggerWorkflowRequestPayload = buildTriggerWorkflowPayload();

    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(TRIGGER_WORKFLOW_URL, workflowId),
        gson.toJson(triggerWorkflowRequestPayload),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());

    trackTriggeredWorkflowRun(workflowId, getWorkflowRunIdFromPayload(
        triggerWorkflowRequestPayload));

    JsonObject workflowRunResponse = gson.fromJson(response.getEntity(String.class), JsonObject.class);

    assertEquals(workflowRunResponse.get(NEW_WORKFLOW_RUN_ID_FIELD).getAsString(),
        triggerWorkflowRequestPayload.get(NEW_WORKFLOW_RUN_ID_FIELD));
    assertEquals(workflowRunResponse.get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString(),
        getWorkflow(dagName).get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString());
    assertEquals(workflowRunResponse.get(NEW_WORKFLOW_RUN_STATUS_FIELD).getAsString(), NEW_WORKFLOW_RUN_STATUS);

    return workflowRunResponse.get(NEW_WORKFLOW_RUN_ID_FIELD).getAsString();
  }

  private void checkWorkflowRunStatus(Map<String, String> workflowIdToWorkflowRunId,
                                      Map<String, String> runIdExecutionStatus) throws Exception {
    for(Map.Entry<String, String> entry: workflowIdToWorkflowRunId.entrySet()) {
      String workflowId = entry.getKey();
      String workflowRunId = entry.getValue();
      if(!runIdExecutionStatus.containsKey(workflowRunId)) {
        String workflowRunStatus;
        try {
          workflowRunStatus = getWorkflowRunStatus(workflowId, workflowRunId);
        } catch (Exception e) {
          throw new RetryException(e.getMessage());
        }

        if(FINISHED_WORKFLOW_RUN_STATUSES.contains(workflowRunStatus)) {
          runIdExecutionStatus.put(workflowRunId, workflowRunStatus);
        } else {
          throw new RetryException(String.format("Unexpected status %s received for workflow run id %s",
              workflowRunStatus, workflowRunId));
        }
      }
    }
    for(Map.Entry<String, String> executionStatusEntry: runIdExecutionStatus.entrySet()) {
      if(!SUCCESSFUL_WORKFLOW_RUN_STATUSES.contains(executionStatusEntry.getValue())) {
        fail(String.format("Workflow run id %s is not successful", executionStatusEntry.getKey()));
      }
    }
  }

  @Test
  public void should_returnBadRequest_when_givenInvalidRequest() throws Exception{
    String workflowId = getWorkflow(TEST_DUMMY_DAG)
        .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString();
    Map<String, Object> triggerWorkflowRequestPayload = buildInvalidTriggerWorkflowRunPayload();

    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(TRIGGER_WORKFLOW_URL, workflowId),
        gson.toJson(triggerWorkflowRequestPayload),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void should_returnBadRequest_when_givenExistingRunId() throws  Exception {
    Map<String, Object> triggerWorkflowRequestPayload = buildTriggerWorkflowPayload();

    String workflowId = getWorkflow(TEST_DUMMY_DAG)
        .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString();
    String workflowName = getWorkflow(TEST_DUMMY_DAG)
        .get(CreateWorkflowTestsBuilder.WORKFLOW_NAME_FIELD).getAsString();

    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(TRIGGER_WORKFLOW_URL, workflowId),
        gson.toJson(triggerWorkflowRequestPayload),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());

    trackTriggeredWorkflowRun(workflowId,
        getWorkflowRunIdFromPayload(triggerWorkflowRequestPayload));

    response = client.send(
        HttpMethod.POST,
        String.format(TRIGGER_WORKFLOW_URL, workflowId),
        gson.toJson(triggerWorkflowRequestPayload),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    JsonObject responseBody = gson.fromJson(response.getEntity(String.class), JsonObject.class);
    assertEquals(400, responseBody.get("code").getAsInt());
    assertEquals(String.format(EXPECTED_ERROR_MESSAGE, workflowId, workflowName),
        responseBody.get("message").getAsString());
    assertFalse(responseBody.get("reason").getAsString().isEmpty());
  }

  @Test
  public void should_return_WorkflowNotFound_when_givenWrongWorkflowId() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(TRIGGER_WORKFLOW_URL, INVALID_WORKFLOW_ID),
        gson.toJson(buildTriggerWorkflowPayload()),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());

    String error = response.getEntity(String.class);
    assertTrue(error.contains(String.format(WORKFLOW_NOT_FOUND_MESSAGE, INVALID_WORKFLOW_ID)));
  }

  @Test
  public void should_returnUnauthorized_when_notGivenAccessToken() {
    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(TRIGGER_WORKFLOW_URL,
            getWorkflow(TEST_DUMMY_DAG).get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD)
                .getAsString()),
        gson.toJson(buildTriggerWorkflowPayload()),
        headers,
        null
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenNoDataAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(TRIGGER_WORKFLOW_URL,
            getWorkflow(TEST_DUMMY_DAG).get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD)
                .getAsString()),
        gson.toJson(buildTriggerWorkflowPayload()),
        headers,
        client.getNoDataAccessToken()
    );

    assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenInvalidPartition() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(TRIGGER_WORKFLOW_URL,
            getWorkflow(TEST_DUMMY_DAG).get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD)
                .getAsString()),
        gson.toJson(buildTriggerWorkflowPayload()),
        HTTPClient.overrideHeader(headers, INVALID_PARTITION),
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }
}
