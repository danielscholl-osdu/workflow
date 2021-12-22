package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.util.AzureTestBase;

import javax.ws.rs.HttpMethod;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_WORKFLOW_URL;

public abstract class DeleteWorkflowIntegrationTests extends AzureTestBase {

  @Test
  public void should_throw_error_when_given_valid_workflow_id_with_active_runs() throws Exception {
    String responseBody = createWorkflow();
    Map<String, String> workflowInfo = getWorkflowInfoFromCreateWorkflowResponseBody(responseBody);
    createdWorkflows.add(workflowInfo);

    String workflowRunResponseBody = createWorkflowRun();
    Map<String, String> workflowRunInfo = new ObjectMapper().readValue(workflowRunResponseBody, HashMap.class);
    createdWorkflowRuns.add(workflowRunInfo);

    ClientResponse deleteResponse = client.send(
        HttpMethod.DELETE,
        String.format(GET_WORKFLOW_URL, CREATE_WORKFLOW_WORKFLOW_NAME),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_PRECONDITION_FAILED, deleteResponse.getStatus());
  }
}
