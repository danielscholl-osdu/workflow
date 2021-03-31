package org.opengroup.osdu.azure.workflow.framework.workflow;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.models.WorkflowRun;
import org.opengroup.osdu.azure.workflow.framework.util.HTTPClient;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.GET_ALL_WORKFLOW_RUNS_URL;

public abstract class GetAllRunInstancesIntegrationTests extends TestBase {

  public static final String INVALID_WORKFLOW_ID = "Invalid-Workflow-ID";
  public static final String INVALID_PARTITION = "invalid-partition";
  public static WorkflowRun triggeredWorkflow = null;

  public void initializeTriggeredWorkflow() throws Exception {
    if (triggeredWorkflow == null) {
      triggeredWorkflow = triggerDummyWorkflow(client, headers);
    }
  }

  @Test
  public void should_returnSuccess_when_givenValidRequest() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_ALL_WORKFLOW_RUNS_URL,
            triggeredWorkflow.getWorkflowId()),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_OK, response.getStatus());
    Type WorkflowRunsListType = new TypeToken<ArrayList<WorkflowRun>>(){}.getType();
    List<WorkflowRun> workflowRunsList =
        gson.fromJson(response.getEntity(String.class), WorkflowRunsListType);

    assertTrue(workflowRunsList.size() > 0);
    for (int i = 0; i < workflowRunsList.size(); ++i) {
      assertEquals(workflowRunsList.get(i).getWorkflowId(), triggeredWorkflow.getWorkflowId());
    }
  }


  @Test
  public void should_returnNotFound_when_givenInvalidWorkflowId() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_ALL_WORKFLOW_RUNS_URL, INVALID_WORKFLOW_ID),
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
        String.format(GET_ALL_WORKFLOW_RUNS_URL, triggeredWorkflow.getWorkflowId()),
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
        String.format(GET_ALL_WORKFLOW_RUNS_URL, triggeredWorkflow.getWorkflowId()),
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
        String.format(GET_ALL_WORKFLOW_RUNS_URL, triggeredWorkflow.getWorkflowId()),
        null,
        HTTPClient.overrideHeader(headersWithInvalidPartition, INVALID_PARTITION),
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void should_returnForbidden_when_notGivenWorkflowId() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_ALL_WORKFLOW_RUNS_URL, ""),
        null,
        HTTPClient.overrideHeader(headers, INVALID_PARTITION),
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }
}
