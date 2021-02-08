package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.util.HTTPClient;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.CREATE_WORKFLOW_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.TRIGGER_WORKFLOW_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.WORKFLOW_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_DUMMY_DAG;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.getValidCreateWorkflowRequest;
import static org.opengroup.osdu.azure.workflow.framework.util.TestDataUtil.getWorkflow;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.buildTriggerWorkflowPayload;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.getWorkflowRunIdFromPayload;
import static org.opengroup.osdu.azure.workflow.framework.workflow.PostCreateWorkflowIntegrationTests.TEST_WORKFLOW_FILE_NAME;

public abstract class DeleteWorkflowIntegrationTests extends TestBase {
  private static final String INVALID_PARTITION = "invalid-partition";
  private static final String INVALID_WORKFLOW_ID = "Invalid-Workflow-ID";

  @Test
  public void should_delete_when_given_valid_workflow_id() throws Exception {
    String createWorkflowRequestBody = getValidCreateWorkflowRequest(TEST_DUMMY_DAG,
        TEST_WORKFLOW_FILE_NAME);

    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        createWorkflowRequestBody,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());
    JsonObject responseBody = gson.fromJson(response.getEntity(String.class), JsonObject.class);

    ClientResponse deleteResponse = client.send(
        HttpMethod.DELETE,
        String.format(WORKFLOW_URL, responseBody.get(WORKFLOW_ID_FIELD).getAsString()),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NO_CONTENT, deleteResponse.getStatus());
  }

  @Test
  public void should_throw_error_when_given_invalid_workflow_id() throws Exception {
    ClientResponse deleteResponse = client.send(
        HttpMethod.DELETE,
        String.format(WORKFLOW_URL, INVALID_WORKFLOW_ID),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NOT_FOUND, deleteResponse.getStatus());
  }

  @Test
  public void should_throw_error_when_given_valid_workflow_id_with_active_runs() throws Exception {
    String existingWorkflowId =  getWorkflow(TEST_DUMMY_DAG).get(WORKFLOW_ID_FIELD).getAsString();
    Map<String, Object> triggerWorkflowPayload = buildTriggerWorkflowPayload();

    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(TRIGGER_WORKFLOW_URL, existingWorkflowId),
        gson.toJson(triggerWorkflowPayload),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());

    trackTriggeredWorkflowRun(existingWorkflowId,
        getWorkflowRunIdFromPayload(triggerWorkflowPayload));

    ClientResponse deleteResponse = client.send(
        HttpMethod.DELETE,
        String.format(WORKFLOW_URL, existingWorkflowId),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_PRECONDITION_FAILED, deleteResponse.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_notGivenAccessToken() {
    ClientResponse response = client.send(
        HttpMethod.DELETE,
        String.format(WORKFLOW_URL, getWorkflow(TEST_DUMMY_DAG)
            .get(WORKFLOW_ID_FIELD).getAsString()),
        null,
        headers,
        null
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenNoDataAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.DELETE,
        String.format(WORKFLOW_URL, getWorkflow(TEST_DUMMY_DAG)
            .get(WORKFLOW_ID_FIELD).getAsString()),
        null,
        headers,
        client.getNoDataAccessToken()
    );

    assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenInvalidPartition() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.DELETE,
        String.format(WORKFLOW_URL, getWorkflow(TEST_DUMMY_DAG)
            .get(WORKFLOW_ID_FIELD).getAsString()),
        null,
        HTTPClient.overrideHeader(headers, INVALID_PARTITION),
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }
}
