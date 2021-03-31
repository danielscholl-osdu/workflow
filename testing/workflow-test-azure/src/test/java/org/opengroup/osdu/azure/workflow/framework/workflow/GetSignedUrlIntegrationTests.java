package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.models.WorkflowRun;
import org.opengroup.osdu.azure.workflow.framework.util.HTTPClient;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.GET_SIGNED_URL_URL;

public abstract class GetSignedUrlIntegrationTests extends TestBase {

  private static final String INVALID_WORKFLOW_ID = "Invalid-Workflow-ID";
  private static final String INVALID_WORKFLOW_RUN_ID = "Invalid-WorkflowRun-ID";
  private static final String INVALID_PARTITION = "invalid-partition";
  private static final String SIGNED_URL_FIELD = "url";
  private static WorkflowRun triggeredWorkflow = null;

  public void initializeTriggeredWorkflow() throws Exception {
    if (triggeredWorkflow == null) {
      triggeredWorkflow = triggerDummyWorkflow(client, headers);
    }
  }

  @Test
  public void should_returnSuccess_when_givenValidRequest() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_SIGNED_URL_URL, triggeredWorkflow.getWorkflowId(), triggeredWorkflow.getRunId()),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());
    JsonObject workflowResponse = new Gson().fromJson(response.getEntity(String.class), JsonObject.class);
    assertTrue(workflowResponse.has(SIGNED_URL_FIELD));
    String signedUrl = workflowResponse.get(SIGNED_URL_FIELD).getAsString();
    String[] parts = signedUrl.split("[?]");
    assertEquals(parts.length, 2);
  }

  @Test
  public void should_returnNotFound_when_givenInvalidWorkflowId() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_SIGNED_URL_URL, INVALID_WORKFLOW_ID, triggeredWorkflow.getRunId()),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void should_returnNotFound_when_givenInvalidWorkflowRunId() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_SIGNED_URL_URL, triggeredWorkflow.getWorkflowId(), INVALID_WORKFLOW_RUN_ID),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenNoDataAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_SIGNED_URL_URL, triggeredWorkflow.getWorkflowId(), triggeredWorkflow.getRunId()),
        null,
        headers,
        client.getNoDataAccessToken()
    );
    assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void should_returnForbidden_when_notGivenAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_SIGNED_URL_URL, triggeredWorkflow.getWorkflowId(), triggeredWorkflow.getRunId()),
        null,
        headers,
        null
    );
    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void should_returnForbidden_when_givenInvalidPartition() throws Exception {
    Map<String, String> headersWithInvalidPartition = new HashMap<>(headers);

    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_SIGNED_URL_URL, triggeredWorkflow.getWorkflowId(), triggeredWorkflow.getRunId()),
        null,
        HTTPClient.overrideHeader(headersWithInvalidPartition, INVALID_PARTITION),
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }
}
