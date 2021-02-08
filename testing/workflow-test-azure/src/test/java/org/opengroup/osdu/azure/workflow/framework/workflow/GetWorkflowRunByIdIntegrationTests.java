package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.models.WorkflowRun;
import org.opengroup.osdu.azure.workflow.framework.util.HTTPClient;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.GET_WORKFLOW_RUN_URL;

public abstract class GetWorkflowRunByIdIntegrationTests extends TestBase {

  public static final String WORKFLOW_RUN_NOT_FOUND_MESSAGE = "WorkflowRun: %s for Workflow: %s doesn't exist";
  public static final String INVALID_WORKFLOW_ID = "Invalid-Workflow-ID";
  public static final String INVALID_WORKFLOW_RUN_ID = "Invalid-WorkflowRun-ID";
  public static final String INVALID_PARTITION = "invalid-partition";
  public static WorkflowRun triggeredWorkflow = null;

  public void initializeTriggeredWorkflow() throws Exception {
    if(triggeredWorkflow == null) {
      triggeredWorkflow = triggerDummyWorkflow(client, headers);
    }
  }

  @Test
  public void should_returnSuccess_when_givenValidRequest() throws Exception {
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

    WorkflowRun workflowRun = gson.fromJson(workflowRunResponse.getEntity(String.class),
        WorkflowRun.class);

    assertEquals(workflowRun.getRunId(), triggeredWorkflow.getRunId());
    assertEquals(workflowRun.getWorkflowId(), triggeredWorkflow.getWorkflowId());
  }

  @Test
  public void should_returnNotFound_when_givenInvalidWorkflowId() throws Exception {

    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOW_RUN_URL, INVALID_WORKFLOW_ID, INVALID_WORKFLOW_RUN_ID),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());
    String error = response.getEntity(String.class);
    assertTrue(error.contains(String.format(WORKFLOW_RUN_NOT_FOUND_MESSAGE,
        INVALID_WORKFLOW_RUN_ID, INVALID_WORKFLOW_ID)));
  }

  @Test
  public void should_returnNotFound_when_givenInvalidWorkflowRunId() throws Exception {

    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOW_RUN_URL, triggeredWorkflow.getWorkflowId(),
            INVALID_WORKFLOW_RUN_ID),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());

    String error = response.getEntity(String.class);
    assertTrue(error.contains(String.format(WORKFLOW_RUN_NOT_FOUND_MESSAGE, INVALID_WORKFLOW_RUN_ID,
        triggeredWorkflow.getWorkflowId())));
  }

  @Test
  public void should_returnUnauthorized_when_notGivenAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOW_RUN_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
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
        String.format(GET_WORKFLOW_RUN_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
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
        String.format(GET_WORKFLOW_RUN_URL,
            triggeredWorkflow.getWorkflowId(),
            triggeredWorkflow.getRunId()),
        null,
        HTTPClient.overrideHeader(headers, INVALID_PARTITION),
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }
}
