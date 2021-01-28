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

package org.opengroup.osdu.workflow.workflow.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_DETAILS_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildCreateWorkflowRunValidPayload;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildUpdateWorkflowPayload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.HttpMethod;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.workflow.util.v3.TestBase;
import org.springframework.http.HttpStatus;

public abstract class WorkflowRunV3IntegrationTests extends TestBase {

  protected static List<Map<String, String>> createdWorkflows = new ArrayList<>();

  @Test
  public void shouldReturn200WhenTriggerNewWorkflow() throws Exception {
    String responseBody = createWorkflow();
    Map<String, String> workflowInfo = new ObjectMapper().readValue(responseBody, HashMap.class);
    createdWorkflows.add(workflowInfo);

    createWorkflowRun(String.format(CREATE_WORKFLOW_RUN_URL, CREATE_WORKFLOW_WORKFLOW_NAME));
  }

  @Test
  public void shouldReturn200WhenGetAllRunInstances() throws Exception {
    String responseBody = createWorkflow();
    Map<String, String> workflowInfo = new ObjectMapper().readValue(responseBody, HashMap.class);
    createdWorkflows.add(workflowInfo);

    String url = String.format(CREATE_WORKFLOW_RUN_URL, CREATE_WORKFLOW_WORKFLOW_NAME);
    createWorkflowRun(url);

    ClientResponse response = client.send(
        HttpMethod.GET,
        url,
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.OK.value(), response.getStatus());

    responseBody = response.getEntity(String.class);
    List<Object> list =
        new ObjectMapper().readValue(responseBody, ArrayList.class);
    assertTrue(!list.isEmpty());
  }

  @Test
  public void shouldReturn400WhenGetDetailsForSpecificWorkflowRunInstance() throws Exception {
    String workflowId = UUID
        .randomUUID().toString().replace("-", "");
    String runId = UUID
        .randomUUID().toString().replace("-", "");

    ClientResponse getResponse = client.send(
        HttpMethod.GET,
        String.format(GET_DETAILS_WORKFLOW_RUN_URL, workflowId, runId),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.BAD_REQUEST.value(), getResponse.getStatus());
  }

  @Test
  public void shouldReturn200WhenUpdateWorkflowRunInstance() throws Exception {
    String responseBody = createWorkflow();
    Map<String, String> workflowInfo = new ObjectMapper().readValue(responseBody, HashMap.class);
    createdWorkflows.add(workflowInfo);

    String url = String.format(CREATE_WORKFLOW_RUN_URL, CREATE_WORKFLOW_WORKFLOW_NAME);
    String response = createWorkflowRun(url);

    Map<String, Object> responseDetails = new ObjectMapper().readValue(response, HashMap.class);

    ClientResponse getResponse = client.send(
        HttpMethod.PUT,
        String.format(GET_DETAILS_WORKFLOW_RUN_URL, CREATE_WORKFLOW_WORKFLOW_NAME,
            responseDetails.get("runId")),
        buildUpdateWorkflowPayload(),
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.OK.value(), getResponse.getStatus());
  }

  private String createWorkflowRun(String url) throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        url,
        buildCreateWorkflowRunValidPayload(),
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    return response.getEntity(String.class);
  }
}
