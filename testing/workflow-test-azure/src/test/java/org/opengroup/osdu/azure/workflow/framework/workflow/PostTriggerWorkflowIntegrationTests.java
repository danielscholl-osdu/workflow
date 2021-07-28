package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.CREATE_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_DUMMY_DAG;
import static org.opengroup.osdu.azure.workflow.framework.util.TestDataUtil.getWorkflow;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.buildInvalidTriggerWorkflowRunPayload;

public abstract class PostTriggerWorkflowIntegrationTests extends TestBase {
  private static final String EXPECTED_ERROR_MESSAGE =
      "Failed to trigger workflow with id %s and name %s";

  public static final String INVALID_WORKFLOW_ID = "Invalid-Workflow-ID";
  private static final List<String> SUCCESSFUL_WORKFLOW_RUN_STATUSES =
      Arrays.asList("finished", "success");

  @Test
  public void should_returnBadRequest_when_givenInvalidRequest() throws Exception{
    String workflowId = getWorkflow(TEST_DUMMY_DAG)
        .get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString();
    Map<String, Object> triggerWorkflowRequestPayload = buildInvalidTriggerWorkflowRunPayload();

    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(CREATE_WORKFLOW_RUN_URL, workflowId),
        gson.toJson(triggerWorkflowRequestPayload),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
  }
}
