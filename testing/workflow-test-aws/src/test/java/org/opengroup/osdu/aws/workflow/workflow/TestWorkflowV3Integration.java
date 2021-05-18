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

package org.opengroup.osdu.aws.workflow.workflow;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.opengroup.osdu.aws.workflow.util.HTTPClientAWS;
import org.opengroup.osdu.workflow.workflow.v3.WorkflowV3IntegrationTests;

import javax.ws.rs.HttpMethod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_DETAILS_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildUpdateWorkflowPayload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestWorkflowV3Integration extends WorkflowV3IntegrationTests {

  private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

  @BeforeEach
  @Override
  public void setup() {
    this.client = new HTTPClientAWS();
    this.headers = client.getCommonHeader();

    // cleanup any leftover workflows from previous int test runs
    try {
      deleteTestWorkflows(CREATE_WORKFLOW_WORKFLOW_NAME);
    } catch (Exception e) {      
      e.printStackTrace();
    }
  }

  @AfterEach
  @Override
  public void tearDown() {
    deleteAllTestWorkflowRecords();
    this.client = null;
    this.headers = null;
  }

  @Override
	@Test
	@Disabled
	public void shouldReturnBadRequestWhenInvalidDagNameWorkflowCreate() throws Exception {
		// Validation logic is missing in core. issue raised to opengroup
		//super.shouldReturnBadRequestWhenInvalidDagNameWorkflowCreate();
	}

	@Override
	@Test
	@Disabled
	public void shouldReturnInternalServerErrorWhenIncorrectWorkflowNameWorkflowCreate() throws Exception {
		// Validation logic is missing in core. issue raised to opengroup
		//super.shouldReturnInternalServerErrorWhenIncorrectWorkflowNameWorkflowCreate();
	}

  private void deleteAllTestWorkflowRecords() {
    createdWorkflows.stream().forEach(c -> {
      try {        
        deleteTestWorkflows(c.get(WORKFLOW_NAME));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  protected void deleteTestWorkflows(String workflowName) throws Exception {

    List<String> runIds = getWorkflowRuns(workflowName);

    for (String runId : runIds) {
      sendWorkflowRunFinishedUpdateRequest(workflowName, runId);
    }

    String url = CREATE_WORKFLOW_URL + "/" + workflowName;
    sendDeleteRequest(url);
  }

  protected ClientResponse sendWorkflowRunFinishedUpdateRequest(String workflowName, String runId) throws Exception {
    
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

    JsonArray responseDataArr = gson.fromJson(respBody, JsonArray.class);


    ArrayList<String> runIds = new ArrayList<String>();

    for (JsonElement responseData: responseDataArr) {
        runIds.add(responseData.getAsJsonObject().get("runId").getAsString());
    }
    

    return runIds;

  }


}
