package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.CREATE_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.GET_WORKFLOW_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_DUMMY_DAG;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.TestDataUtil.getWorkflow;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.buildTriggerWorkflowPayload;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.getWorkflowRunIdFromPayload;

public abstract class DeleteWorkflowIntegrationTests extends TestBase {
  @Test
  public void should_throw_error_when_given_valid_workflow_id_with_active_runs() throws Exception {
    String existingWorkflowId =  getWorkflow(TEST_DUMMY_DAG).get(WORKFLOW_ID_FIELD).getAsString();
    Map<String, Object> triggerWorkflowPayload = buildTriggerWorkflowPayload();

    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(CREATE_WORKFLOW_RUN_URL, existingWorkflowId),
        gson.toJson(triggerWorkflowPayload),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus(), response.toString());

    trackTriggeredWorkflowRun(existingWorkflowId,
        getWorkflowRunIdFromPayload(triggerWorkflowPayload));

    ClientResponse deleteResponse = client.send(
        HttpMethod.DELETE,
        String.format(GET_WORKFLOW_URL, existingWorkflowId),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_PRECONDITION_FAILED, deleteResponse.getStatus());
  }
}
