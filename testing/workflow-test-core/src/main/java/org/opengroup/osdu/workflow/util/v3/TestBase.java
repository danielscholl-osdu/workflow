/*
  Copyright 2020 Google LLC
  Copyright 2020 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.opengroup.osdu.workflow.util.v3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.workflow.util.HTTPClient;
import org.springframework.http.HttpStatus;

import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;
import static org.opengroup.osdu.workflow.consts.TestConstants.FINISHED_WORKFLOW_RUN_STATUSES;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildCreateWorkflowRunValidPayload;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildCreateWorkflowValidPayload;

@Slf4j
public abstract class TestBase {
  protected HTTPClient client;
  protected Map<String, String> headers;
  protected List<Map<String, String>> createdWorkflows = new ArrayList<>();
  protected List<Map<String, String>> createdWorkflowRuns = new ArrayList<>();
  protected static final String WORKFLOW_NAME_FIELD = "workflowName";
  protected static final String WORKFLOW_ID_FIELD = "workflowId";
  protected static final String WORKFLOW_RUN_ID_FIELD = "runId";
  protected static final String WORKFLOW_RUN_STATUS_FIELD = "status";

  public abstract void setup() throws Exception;
  public abstract void tearDown() throws Exception;

  protected String createWorkflow() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        buildCreateWorkflowValidPayload(),
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    return response.getEntity(String.class);
  }

  protected String createWorkflowRun() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(CREATE_WORKFLOW_RUN_URL, CREATE_WORKFLOW_WORKFLOW_NAME),
        buildCreateWorkflowRunValidPayload(),
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    return response.getEntity(String.class);
  }

  protected ClientResponse sendDeleteRequest(String url) throws Exception {
    return client.send(
        HttpMethod.DELETE,
        url,
        null,
        headers,
        client.getAccessToken()
    );
  }

  protected void deleteTestWorkflows(String workflowName) throws Exception {
    String url = CREATE_WORKFLOW_URL + "/" + workflowName;
    sendDeleteRequest(url);
  }

  protected void waitForWorkflowRunsToComplete(List<Map<String, String>> createdWorkflowRuns,
                                               Set<String> completedWorkflowRunIds) throws Exception {
    for(Map<String, String> createdWorkflow: createdWorkflowRuns) {
      String workflowName = createdWorkflow.get(WORKFLOW_NAME_FIELD);
      String workflowRunId = createdWorkflow.get(WORKFLOW_RUN_ID_FIELD);
      if(!completedWorkflowRunIds.contains(workflowRunId)) {
        String workflowRunStatus;
        try {
          workflowRunStatus = getWorkflowRunStatus(workflowName, workflowRunId);
        } catch (Exception e) {
          throw new RetryException(e.getMessage());
        }
        if(FINISHED_WORKFLOW_RUN_STATUSES.contains(workflowRunStatus)) {
          completedWorkflowRunIds.add(workflowRunId);
        } else {
          throw new RetryException(String.format(
              "Unexpected status %s received for workflow run id %s", workflowRunStatus,
              workflowRunId));
        }
      }
    }
  }

  public <T> T executeWithWaitAndRetry(Callable<T> callable, int noOfRetries,
                                       long timeToWait, TimeUnit timeUnit) throws Exception {
    long totalTimeToWaitInMillis = timeUnit.toMillis(timeToWait * noOfRetries);
    if(totalTimeToWaitInMillis < 1000) {
      throw new RuntimeException("Minimum wait time should be at least 1 second");
    }
    long waitTimeToAdd = totalTimeToWaitInMillis / noOfRetries;

    long elapsedTime = 0;
    long nextRetryTime = elapsedTime + waitTimeToAdd;
    while(elapsedTime < totalTimeToWaitInMillis) {
      long startTime = System.currentTimeMillis();
      try {
        return callable.call();
      } catch (RetryException e) {
        log.info("Received RetryException: {}. Will wait and retry", e.getMessage());
      } catch (Exception e) {
        log.error("Error calling callable", e);
        throw e;
      }
      long completedTime = System.currentTimeMillis();

      elapsedTime += (completedTime - startTime);
      if(elapsedTime > nextRetryTime) {
        nextRetryTime = (elapsedTime + waitTimeToAdd);
      } else {
        log.info("Waiting for {} seconds", (nextRetryTime - elapsedTime) / 1000);
        Thread.sleep(nextRetryTime - elapsedTime);
        elapsedTime = nextRetryTime;
        nextRetryTime += waitTimeToAdd;
      }
    }
    throw new Exception("Execution failed even after retries");
  }

  public String getWorkflowRunStatus(String workflowName, String workflowRunId) throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOW_RUN_URL, workflowName, workflowRunId),
        null,
        headers,
        client.getAccessToken()
    );

    if(response.getStatus() == org.apache.http.HttpStatus.SC_OK) {
      Map<String, String> workflowRunInfo = new ObjectMapper().readValue(response.getEntity(String.class), HashMap.class);
      return workflowRunInfo.get("status");
    } else {
      throw new Exception(String.format("Error getting status for workflow run id %s",
          workflowRunId));
    }
  }

  protected void waitForWorkflowRunsToComplete() throws Exception {
    try {
      Set<String> completedWorkflowRunIds = new HashSet<>();
      if(createdWorkflowRuns.size() != completedWorkflowRunIds.size()) {
        executeWithWaitAndRetry(() -> {
          waitForWorkflowRunsToComplete(createdWorkflowRuns, completedWorkflowRunIds);
          return null;
        }, 20, 15, TimeUnit.SECONDS);
      }
    } finally {
      Long integrationTestEndTime = System.currentTimeMillis();
      log.info("Completed integration test at {}", integrationTestEndTime);
    }
  }
}
