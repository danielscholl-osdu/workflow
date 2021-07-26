package org.opengroup.osdu.azure.workflow.framework.workflow;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.models.WorkflowRun;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.GET_ALL_WORKFLOW_RUNS_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.GET_WORKFLOW_RUN_URL;

public abstract class GetAllRunInstancesIntegrationTests extends TestBase {

  private static final String INVALID_WORKFLOW_ID = "invalid-workflow-id";
  private static final String INVALID_PARTITION = "invalid-partition";
  private static final String INVALID_PREFIX = "backfill";
  private static final Integer INVALID_LIMIT = 1000;

  private static WorkflowRun triggeredWorkflow = null;

  public void initializeTriggeredWorkflow() throws Exception {
    if (triggeredWorkflow == null) {
      triggeredWorkflow = triggerDummyWorkflow(client, headers);
    }
  }

  @Test
  public void should_returnBadRequest_when_givenInvalidPrefix() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_ALL_WORKFLOW_RUNS_URL + "?prefix=%s", triggeredWorkflow.getWorkflowId(), INVALID_PREFIX),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void should_returnBadRequest_when_givenInvalidLimit() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_ALL_WORKFLOW_RUNS_URL + "?limit=%s", triggeredWorkflow.getWorkflowId(), INVALID_LIMIT),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void should_returnSuccess_when_givenValidLimitAndValidPrefix() throws Exception {
    int limit = 1;
    String prefix = triggeredWorkflow.getRunId().substring(0, 2);
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_ALL_WORKFLOW_RUNS_URL + "?limit=%s&prefix=%s", triggeredWorkflow.getWorkflowId(), limit, prefix),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_OK, response.getStatus());
    Type WorkflowRunsListType = new TypeToken<ArrayList<WorkflowRun>>(){}.getType();
    List<WorkflowRun> workflowRunsList =
        gson.fromJson(response.getEntity(String.class), WorkflowRunsListType);

    assertEquals(limit, workflowRunsList.size());
    for (WorkflowRun workflowRun : workflowRunsList) {
      assertTrue(workflowRun.getRunId().startsWith(prefix));
    }
  }

  @Test
  public void should_returnSuccess_when_givenValidStartDateParam() throws Exception {
    Long startTimeStamp = triggeredWorkflow.getStartTimeStamp();
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_ALL_WORKFLOW_RUNS_URL + "?startDate=%s", triggeredWorkflow.getWorkflowId(), startTimeStamp),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_OK, response.getStatus());
    Type WorkflowRunsListType = new TypeToken<ArrayList<WorkflowRun>>(){}.getType();
    List<WorkflowRun> workflowRunsList =
        gson.fromJson(response.getEntity(String.class), WorkflowRunsListType);

    assertTrue(workflowRunsList.size() > 0);
    for (WorkflowRun workflowRun : workflowRunsList) {
      assertTrue(workflowRun.getStartTimeStamp() >= startTimeStamp);
    }
  }

  @Test
  public void should_returnSuccess_when_givenValidEndDateParam() throws Exception {
    Long endTimestamp = getLatestUpdatedWorkflowRun(triggeredWorkflow.getWorkflowId(), triggeredWorkflow.getRunId()).getEndTimeStamp();
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_ALL_WORKFLOW_RUNS_URL + "?endDate=%s", triggeredWorkflow.getWorkflowId(), endTimestamp),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_OK, response.getStatus());

    Type WorkflowRunsListType = new TypeToken<ArrayList<WorkflowRun>>(){}.getType();
    List<WorkflowRun> workflowRunsList =
        gson.fromJson(response.getEntity(String.class), WorkflowRunsListType);

    assertTrue(workflowRunsList.size() > 0);
    for (WorkflowRun workflowRun : workflowRunsList) {
      assertTrue(workflowRun.getEndTimeStamp() <= endTimestamp);
    }
  }

  WorkflowRun getLatestUpdatedWorkflowRun(String workflowId, String runId) throws Exception {
    executeWithWaitAndRetry(() -> {
      waitForWorkflowRunsToComplete(trackedWorkflowRuns, completedWorkflowRunIds);
      return null;
    }, 10, 30, TimeUnit.SECONDS);
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOW_RUN_URL, workflowId, runId),
        null,
        headers,
        client.getAccessToken()
    );
    if (response.getStatus() == HttpStatus.SC_OK) {
      return gson.fromJson(response.getEntity(String.class), WorkflowRun.class);
    } else {
      throw new Exception(String.format("Error getting status for workflow run id %s", runId));
    }
  }
}
