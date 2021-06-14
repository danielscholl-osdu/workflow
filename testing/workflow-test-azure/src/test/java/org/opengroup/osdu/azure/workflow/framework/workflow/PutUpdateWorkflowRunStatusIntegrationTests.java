package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.models.WorkflowRun;
import org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder;
import org.opengroup.osdu.azure.workflow.framework.util.HTTPClient;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.GET_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.UPDATE_WORKFLOW_RUN_STATUS_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.WORKFLOW_STATUS_TYPE_RUNNING;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_DUMMY_DAG;
import static org.opengroup.osdu.azure.workflow.framework.util.TestDataUtil.getWorkflow;
import static org.opengroup.osdu.azure.workflow.framework.util.UpdateWorkflowRunStatusTestsBuilder.buildInvalidUpdateWorkflowRunStatusRequestBody;
import static org.opengroup.osdu.azure.workflow.framework.util.UpdateWorkflowRunStatusTestsBuilder.buildInvalidUpdateWorkflowRunStatusRequestBodyIncorrectValue;
import static org.opengroup.osdu.azure.workflow.framework.util.UpdateWorkflowRunStatusTestsBuilder.buildUpdateWorkflowRunFinishedStatusRequest;
import static org.opengroup.osdu.azure.workflow.framework.util.UpdateWorkflowRunStatusTestsBuilder.buildUpdateWorkflowRunRunningStatusRequest;
import static org.opengroup.osdu.workflow.consts.TestConstants.WORKFLOW_STATUS_TYPE_FINISHED;

public abstract class PutUpdateWorkflowRunStatusIntegrationTests extends TestBase {

  public static final String WORKFLOW_RUN_NOT_FOUND_MESSAGE = "WorkflowRun: %s for Workflow: %s doesn't exist";
  public static final String WORKFLOW_COMPLETED_EXCEPTION_MESSAGE = "WorkflowRunCompletedException: " +
      "WorkflowRun with name %s and for Run Id %s already completed";
  public static final String INVALID_REQUEST_FIELD_MESSAGE = "Unrecognized field ";
  public static final String INVALID_STATUS_MESSAGE = "Cannot deserialize value";
  public static final String INVALID_WORKFLOW_ID = "Invalid-Workflow-ID";
  public static final String INVALID_WORKFLOW_RUN_ID = "Invalid-WorkflowRun-ID";
  public static final String INVALID_PARTITION = "invalid-partition";
  private WorkflowRun triggeredWorkflow = null;
  private final Gson gson = new Gson();

  public void updateWorkflowSetup() throws Exception {
    triggeredWorkflow = triggerDummyWorkflow(client, headers);
  }

  public void updateWorkflowTeardown() throws Exception {
    String updateWorkflowRunStatusRequestBody = gson.toJson(buildUpdateWorkflowRunFinishedStatusRequest());

    if (!triggeredWorkflow.getStatus().equals(WORKFLOW_STATUS_TYPE_FINISHED)) {
      client.send(
          HttpMethod.PUT,
          String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
              triggeredWorkflow.getWorkflowId(),
              triggeredWorkflow.getRunId()),
          updateWorkflowRunStatusRequestBody,
          headers,
          client.getAccessToken()
      );
    }
  }

  @Test
  public void should_returnSuccess_when_givenValidRequest_StatusRunning() throws Exception {

    final String updateWorkflowRunStatusRequestBody =
        gson.toJson(buildUpdateWorkflowRunRunningStatusRequest());

    ClientResponse response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        updateWorkflowRunStatusRequestBody,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());
    final WorkflowRun updatedWorkflowRun =
        gson.fromJson(response.getEntity(String.class), WorkflowRun.class);

    assertEquals(updatedWorkflowRun.getRunId(), triggeredWorkflow.getRunId());
    assertEquals(updatedWorkflowRun.getWorkflowId(), triggeredWorkflow.getWorkflowId());
    assertEquals(updatedWorkflowRun.getStatus(), WORKFLOW_STATUS_TYPE_RUNNING);

    ClientResponse workflowRunResponse = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOW_RUN_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_OK, workflowRunResponse.getStatus());

    final WorkflowRun workflowRun =
        gson.fromJson(workflowRunResponse.getEntity(String.class), WorkflowRun.class);

    assertEquals(workflowRun.getStatus(), WORKFLOW_STATUS_TYPE_RUNNING);

  }

  @Test
  public void should_returnSuccess_when_givenValidRequest_StatusFinished() throws Exception {

    final String updateWorkflowRunStatusRequestBody =
        gson.toJson(buildUpdateWorkflowRunFinishedStatusRequest());

    ClientResponse response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        updateWorkflowRunStatusRequestBody,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());
    final WorkflowRun updatedWorkflowRun =
        gson.fromJson(response.getEntity(String.class), WorkflowRun.class);

    assertEquals(updatedWorkflowRun.getRunId(), triggeredWorkflow.getRunId());
    assertEquals(updatedWorkflowRun.getWorkflowId(), triggeredWorkflow.getWorkflowId());
    assertEquals(updatedWorkflowRun.getStatus(), WORKFLOW_STATUS_TYPE_FINISHED);

    ClientResponse workflowRunResponse = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOW_RUN_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_OK, workflowRunResponse.getStatus());

    final WorkflowRun workflowRun =
        gson.fromJson(workflowRunResponse.getEntity(String.class), WorkflowRun.class);

    assertEquals(workflowRun.getStatus(), WORKFLOW_STATUS_TYPE_FINISHED);
  }

  @Test
  public void should_returnBadRequest_when_GivenInvalidRequest() throws Exception {

    final String updateWorkflowRunStatusRequestBody =
        gson.toJson(buildInvalidUpdateWorkflowRunStatusRequestBody());

    ClientResponse response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        updateWorkflowRunStatusRequestBody,
        headers,
        client.getAccessToken()
    );

    final String error = response.getEntity(String.class);
    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    assertTrue(error.contains(INVALID_REQUEST_FIELD_MESSAGE));

  }

  @Test
  public void should_returnBadRequest_when_GivenInvalidStatus() throws Exception {

    final String updateWorkflowRunStatusRequestBody =
        gson.toJson(buildInvalidUpdateWorkflowRunStatusRequestBodyIncorrectValue());

    ClientResponse response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        updateWorkflowRunStatusRequestBody,
        headers,
        client.getAccessToken()
    );

    final String error = response.getEntity(String.class);
    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    assertTrue(error.contains(INVALID_STATUS_MESSAGE));
  }

  @Test
  public void should_returnBadRequest_when_GivenCompletedWorkflowRun() throws Exception {

    String updateWorkflowRunStatusRequestBody =
        gson.toJson(buildUpdateWorkflowRunFinishedStatusRequest());

    ClientResponse response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        updateWorkflowRunStatusRequestBody,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());

    final WorkflowRun updatedWorkflowRun =
        gson.fromJson(response.getEntity(String.class), WorkflowRun.class);

    assertEquals(updatedWorkflowRun.getStatus(), WORKFLOW_STATUS_TYPE_FINISHED);

    ClientResponse workflowRunResponse = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOW_RUN_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_OK, workflowRunResponse.getStatus());

    final WorkflowRun workflowRun =
        gson.fromJson(workflowRunResponse.getEntity(String.class), WorkflowRun.class);

    assertEquals(workflowRun.getStatus(), WORKFLOW_STATUS_TYPE_FINISHED);

    updateWorkflowRunStatusRequestBody =
        gson.toJson(buildUpdateWorkflowRunRunningStatusRequest());

    response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        updateWorkflowRunStatusRequestBody,
        headers,
        client.getAccessToken()
    );
    final String error = response.getEntity(String.class);
    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    assertTrue(error.contains(String.format(WORKFLOW_COMPLETED_EXCEPTION_MESSAGE,
        triggeredWorkflow.getWorkflowId(), triggeredWorkflow.getRunId())));

  }

  @Test
  public void should_returnUnauthorized_when_notGivenAccessToken() {

    final String updateWorkflowRunStatusRequestBody =
        gson.toJson(buildUpdateWorkflowRunRunningStatusRequest());

    ClientResponse response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        updateWorkflowRunStatusRequestBody,
        headers,
        null
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenNoDataAccessToken() throws Exception {

    final String updateWorkflowRunStatusRequestBody =
        gson.toJson(buildUpdateWorkflowRunRunningStatusRequest());

    ClientResponse response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        updateWorkflowRunStatusRequestBody,
        headers,
        client.getNoDataAccessToken()
    );

    assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenInvalidPartition() throws Exception {

    final String updateWorkflowRunStatusRequestBody =
        gson.toJson(buildUpdateWorkflowRunRunningStatusRequest());

    Map<String, String> headersWithInvalidPartition = new HashMap<>(headers);

    ClientResponse response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        updateWorkflowRunStatusRequestBody,
        HTTPClient.overrideHeader(headersWithInvalidPartition, INVALID_PARTITION),
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void should_returnNotFound_when_givenInvalidWorkflowRunId() throws Exception {

    ClientResponse response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL,
            triggeredWorkflow.getWorkflowId(),
            INVALID_WORKFLOW_RUN_ID),
        gson.toJson(buildUpdateWorkflowRunRunningStatusRequest()),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());

    String error = response.getEntity(String.class);
    assertTrue(error.contains(String.format(WORKFLOW_RUN_NOT_FOUND_MESSAGE,
        INVALID_WORKFLOW_RUN_ID,
        getWorkflow(TEST_DUMMY_DAG).get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString())));
  }

  @Test
  public void should_returnNotFound_when_givenInvalidWorkflowId() throws Exception {

    ClientResponse response = client.send(
        HttpMethod.PUT,
        String.format(UPDATE_WORKFLOW_RUN_STATUS_URL, INVALID_WORKFLOW_ID, INVALID_WORKFLOW_RUN_ID),
        gson.toJson(buildUpdateWorkflowRunRunningStatusRequest()),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());
    final String error = response.getEntity(String.class);
    assertTrue(error.contains(String.format(WORKFLOW_RUN_NOT_FOUND_MESSAGE,
        INVALID_WORKFLOW_RUN_ID, INVALID_WORKFLOW_ID)));
  }

}
