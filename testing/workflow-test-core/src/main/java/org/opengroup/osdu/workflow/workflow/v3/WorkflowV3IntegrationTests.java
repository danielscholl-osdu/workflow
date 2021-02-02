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
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_ALL_WORKFLOW_URL;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildCreateWorkflowPayloadWithIncorrectDag;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildCreateWorkflowPayloadWithIncorrectWorkflowName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.workflow.util.v3.TestBase;
import org.springframework.http.HttpStatus;

public abstract class WorkflowV3IntegrationTests extends TestBase {

  protected static List<Map<String, String>> createdWorkflows = new ArrayList<>();

  @Test
  public void shouldReturnSuccessWhenGivenValidRequestWorkflowCreate() throws Exception {
    String responseBody = createWorkflow();
    createdWorkflows.add(new ObjectMapper().readValue(responseBody, HashMap.class));
  }

  @Test
  public void shouldReturnBadRequestWhenInvalidDagNameWorkflowCreate() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        buildCreateWorkflowPayloadWithIncorrectDag(),
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  @Test
  public void shouldReturnInternalServerErrorWhenIncorrectWorkflowNameWorkflowCreate()
      throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        buildCreateWorkflowPayloadWithIncorrectWorkflowName(),
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
  }

  @Test
  public void shouldReturn200WhenGetListWorkflowForTenant() throws Exception {
    String responseBody = createWorkflow();
    Map<String, String> workflowInfo =
        new ObjectMapper().readValue(responseBody, HashMap.class);
    createdWorkflows.add(workflowInfo);

    ClientResponse response = client.send(
        HttpMethod.GET,
        GET_ALL_WORKFLOW_URL,
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
  public void shouldReturn200WhenGetCompleteDetailsForWorkflow() throws Exception {
    String responseBody = createWorkflow();
    Map<String, String> workflowInfo = new ObjectMapper().readValue(responseBody, HashMap.class);
    createdWorkflows.add(workflowInfo);

    String url = CREATE_WORKFLOW_URL + "/" + CREATE_WORKFLOW_WORKFLOW_NAME;
    ClientResponse response = client.send(
        HttpMethod.GET,
        url,
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    responseBody = response.getEntity(String.class);
    Map<String, Object> result = new ObjectMapper().readValue(responseBody, HashMap.class);
    assertTrue(!result.isEmpty());
  }

  @Test
  public void shouldReturnBadRequestWhenGetCompleteDetailsForWorkflow() throws Exception {
    String responseBody = createWorkflow();
    Map<String, String> workflowInfo = new ObjectMapper().readValue(responseBody, HashMap.class);
    createdWorkflows.add(workflowInfo);

    String url = CREATE_WORKFLOW_URL + "/_" + CREATE_WORKFLOW_WORKFLOW_NAME;
    ClientResponse response = client.send(
        HttpMethod.GET,
        url,
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
  }

  @Test
  @Disabled
  public void shouldDeleteWorkflowDefinition() throws Exception {
    String responseBody = createWorkflow();
    Map<String, String> workflowInfo = new ObjectMapper().readValue(responseBody, HashMap.class);
    createdWorkflows.add(workflowInfo);

    String url = CREATE_WORKFLOW_URL + "/" + workflowInfo.get(WORKFLOW_NAME);
    ClientResponse response = sendDeleteRequest(url);
    assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
  }
}
