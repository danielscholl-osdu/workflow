package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder;
import org.opengroup.osdu.azure.workflow.framework.util.HTTPClient;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.WORKFLOW_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_DUMMY_DAG;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_ACTIVE_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_CONCURRENT_TASK_RUN_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_CONCURRENT_WORKFLOW_RUN_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_DESCRIPTION_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_NAME_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.TestDataUtil.getWorkflow;

public abstract class GetWorkflowByIdIntegrationTests extends TestBase {

  public static final String WORKFLOW_NOT_FOUND_MESSAGE = "Workflow: %s doesn't exist";
  public static final String INVALID_WORKFLOW_ID = "Invalid-Workflow-ID";
  public static final String INVALID_PARTITION = "invalid-partition";

  @Test
  public void should_returnSuccess_when_givenValidRequest() throws Exception {
    JsonObject workflow = getWorkflow(TEST_DUMMY_DAG);

    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(WORKFLOW_URL, workflow.get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString()),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());
    JsonObject workflowResponse = gson.fromJson(response.getEntity(String.class), JsonObject.class);

    assertEquals(workflowResponse.get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString(), workflow.get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString());
    assertEquals(workflowResponse.get(WORKFLOW_NAME_FIELD).getAsString(), workflow.get(WORKFLOW_NAME_FIELD).getAsString());
    assertEquals(workflowResponse.get(WORKFLOW_DESCRIPTION_FIELD).getAsString(), workflow.get(WORKFLOW_DESCRIPTION_FIELD).getAsString());
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_WORKFLOW_RUN_FIELD).getAsInt(), workflow.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_WORKFLOW_RUN_FIELD).getAsInt());
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_TASK_RUN_FIELD).getAsInt(), workflow.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_TASK_RUN_FIELD).getAsInt());
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_ACTIVE_FIELD).getAsBoolean(), workflow.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_ACTIVE_FIELD).getAsBoolean());
  }

  @Test
  public void should_returnNotFound_when_givenInvalidWorkflowId() throws Exception {

    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(WORKFLOW_URL, INVALID_WORKFLOW_ID),
        null,
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
        HttpMethod.GET,
        String.format(WORKFLOW_URL, getWorkflow(TEST_DUMMY_DAG)
            .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString()),
        null,
        headers,
        null
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenNoDataAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(WORKFLOW_URL, getWorkflow(TEST_DUMMY_DAG)
            .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString()),
        null,
        headers,
        client.getNoDataAccessToken()
    );

    assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenInvalidPartition() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(WORKFLOW_URL, getWorkflow(TEST_DUMMY_DAG)
            .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString()),
        null,
        HTTPClient.overrideHeader(headers, INVALID_PARTITION),
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }
}
