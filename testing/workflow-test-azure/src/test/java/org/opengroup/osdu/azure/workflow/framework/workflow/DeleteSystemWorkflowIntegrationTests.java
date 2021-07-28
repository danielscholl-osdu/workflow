package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.util.HTTPClient;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.*;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_DUMMY_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_DUMMY_DAG_SYSTEM;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.getValidCreateWorkflowRequest;
import static org.opengroup.osdu.azure.workflow.framework.util.TestDataUtil.getWorkflow;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.buildTriggerWorkflowPayload;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.getWorkflowRunIdFromPayload;
import static org.opengroup.osdu.azure.workflow.framework.workflow.PostCreateWorkflowIntegrationTests.TEST_WORKFLOW_FILE_NAME;

public abstract class DeleteSystemWorkflowIntegrationTests extends TestBase {
  private static final String INVALID_WORKFLOW_ID = "Invalid-Workflow-ID";

  @Test
  public void should_delete_when_given_valid_workflow_id() throws Exception {
    String createWorkflowRequestBody = getValidCreateWorkflowRequest(TEST_DUMMY_DAG_SYSTEM,
        TEST_WORKFLOW_FILE_NAME);

    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_SYSTEM_WORKFLOW_URL,
        createWorkflowRequestBody,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());
    JsonObject responseBody = gson.fromJson(response.getEntity(String.class), JsonObject.class);

    ClientResponse deleteResponse = client.send(
        HttpMethod.DELETE,
        String.format(GET_SYSTEMWORKFLOW_BY_ID_URL, responseBody.get(WORKFLOW_ID_FIELD).getAsString()),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NO_CONTENT, deleteResponse.getStatus());
  }

  @Test
  public void should_throw_error_when_given_invalid_workflow_id() throws Exception {
    ClientResponse deleteResponse = client.send(
        HttpMethod.DELETE,
        String.format(GET_SYSTEMWORKFLOW_BY_ID_URL, INVALID_WORKFLOW_ID),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NOT_FOUND, deleteResponse.getStatus());
  }

}
