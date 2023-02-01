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

package org.opengroup.osdu.gcp.workflow.workflow;

import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_DETAILS_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildUpdateWorkflowPayload;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.HttpMethod;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.opengroup.osdu.gcp.workflow.util.HTTPClientGCP;
import org.opengroup.osdu.workflow.workflow.v3.WorkflowRunV3IntegrationTests;

public class TestWorkflowRunV3Integration extends WorkflowRunV3IntegrationTests {

  private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
  private List<String> activeWorkflowStatusTypes = Arrays.asList("submitted", "running");

  @BeforeEach
  @Override
  public void setup() throws Exception {
    this.client = new HTTPClientGCP();
    this.headers = client.getCommonHeader();

    try {
      deleteTestWorkflows(CREATE_WORKFLOW_WORKFLOW_NAME);
    } catch (Exception e) {
      throw e;
    }
  }

  @AfterEach
  @Override
  public void tearDown() throws Exception {
    waitForWorkflowRunsToComplete();
    deleteAllTestWorkflowRecords();
    this.client = null;
    this.headers = null;
  }

  private void deleteAllTestWorkflowRecords() {
    createdWorkflows.stream().forEach(c -> {
      try {
        deleteTestWorkflows(c.get(WORKFLOW_NAME_FIELD));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  protected void deleteTestWorkflows(String workflowName) throws Exception {

    List<String> runIds = getWorkflowRuns(workflowName);

    for (String runId : runIds) {
      sendWorkflowRunFinishedUpdateRequest(workflowName, runId);
    }

    String url = CREATE_WORKFLOW_URL + "/" + workflowName;
    sendDeleteRequest(url);
  }

  /*
   * this test case will not work for airflow 2.0
   */
  @Override
  @Test
  @EnabledIfEnvironmentVariable(named = "OSDU_AIRFLOW_VERSION2", matches = "false")
  public void triggerWorkflowRun_should_returnBadRequest_when_givenDuplicateRunId() throws Exception {
    super.triggerWorkflowRun_should_returnBadRequest_when_givenDuplicateRunId();
  }

  /*
   * this test case will not work for airflow 1.0
   */
  @Override
  @Test
  @EnabledIfEnvironmentVariable(named = "OSDU_AIRFLOW_VERSION2", matches = "true")
  public void triggerWorkflowRun_should_returnConflict_when_givenDuplicateRunId_with_airflow2_stable_API()
      throws Exception {
    super.triggerWorkflowRun_should_returnConflict_when_givenDuplicateRunId_with_airflow2_stable_API();
  }

  /*
   *  This test case depends on Airflow. Status can be changed to 'success' early due to Airflow.
   */
  @Override
  @Ignore
  public void updateWorkflowRunStatus_should_returnSuccess_when_givenValidRequest_StatusRunning() throws Exception {
    super.updateWorkflowRunStatus_should_returnSuccess_when_givenValidRequest_StatusRunning();
  }

  protected ClientResponse sendWorkflowRunFinishedUpdateRequest(String workflowName, String runId)
      throws Exception {
    return client.send(
        HttpMethod.PUT,
        String.format(GET_DETAILS_WORKFLOW_RUN_URL, workflowName,
            runId),
        buildUpdateWorkflowPayload(),
        headers,
        client.getAccessToken()
    );
  }

  private List<String> getWorkflowRuns(String workflowName) throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(CREATE_WORKFLOW_RUN_URL, workflowName),
        null,
        headers,
        client.getAccessToken()
    );

    String respBody = response.getEntity(String.class);

    JsonElement responseDataArr = gson.fromJson(respBody, JsonElement.class);
    ArrayList<String> runIds = new ArrayList<>();

    if (responseDataArr instanceof JsonArray) {

      for (JsonElement responseData : (JsonArray) responseDataArr) {
        String workflowStatus = responseData.getAsJsonObject().get("status").getAsString();

        if (activeWorkflowStatusTypes.contains(workflowStatus)) {
          runIds.add(responseData.getAsJsonObject().get("runId").getAsString());
        }
      }
    }

    return runIds;

  }
}
