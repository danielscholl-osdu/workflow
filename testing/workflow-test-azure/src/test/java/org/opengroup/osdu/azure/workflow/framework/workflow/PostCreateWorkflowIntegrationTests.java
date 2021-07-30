package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.CREATE_WORKFLOW_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.GET_WORKFLOW_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.CREATE_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_DUMMY_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_CUSTOM_OPERATOR_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_HTTP_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_KUBERNETES_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_PYTHON_DAG;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.*;
import static org.opengroup.osdu.azure.workflow.framework.util.TestDataUtil.getWorkflow;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.buildTriggerWorkflowPayload;

public abstract class PostCreateWorkflowIntegrationTests extends TestBase {
  public static final String CREATE_WORKFLOW_INVALID_REQUEST_MESSAGE = "Unrecognized field";
  public static final String CREATE_WORKFLOW_IGNORE_CONTENT_MESSAGE = "not found in DagModel";
  public static final String WORKFLOW_NAME_CONFLICT_MESSAGE = "ResourceConflictException: Workflow with name %s already exists";
  public static final String TEST_WORKFLOW_FILE_NAME = "test_dummy_dag.py";
  public static final String TEST_SIMPLE_WORKFLOW_FILE_NAME = "test_simple_python_dag.py";

  @Test
  public void should_returnSuccess_when_givenValidRequest() {
    JsonObject workflowResponse = getWorkflow(TEST_SIMPLE_PYTHON_DAG);

    assertTrue(isNotBlank(workflowResponse.get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString()));
    assertTrue(isNotBlank(workflowResponse.get(WORKFLOW_NAME_FIELD).getAsString()));
    assertEquals(workflowResponse.get(WORKFLOW_DESCRIPTION_FIELD).getAsString(), WORKFLOW_DESCRIPTION);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_WORKFLOW_RUN_FIELD).getAsInt(), WORKFLOW_CONCURRENT_WORKFLOW_RUN);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_TASK_RUN_FIELD).getAsInt(),WORKFLOW_CONCURRENT_TASK_RUN);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_ACTIVE_FIELD).getAsBoolean(), WORKFLOW_ACTIVE);
  }

  @Test
  public void should_returnSuccess_when_givenValidRequest_httpOperator() {
    JsonObject workflowResponse = getWorkflow(TEST_SIMPLE_HTTP_DAG);

    assertTrue(isNotBlank(workflowResponse.get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString()));
    assertTrue(isNotBlank(workflowResponse.get(WORKFLOW_NAME_FIELD).getAsString()));
    assertEquals(workflowResponse.get(WORKFLOW_DESCRIPTION_FIELD).getAsString(), WORKFLOW_DESCRIPTION);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_WORKFLOW_RUN_FIELD).getAsInt(), WORKFLOW_CONCURRENT_WORKFLOW_RUN);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_TASK_RUN_FIELD).getAsInt(),WORKFLOW_CONCURRENT_TASK_RUN);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_ACTIVE_FIELD).getAsBoolean(), WORKFLOW_ACTIVE);
  }

  @Test
  public void should_returnSuccess_when_givenValidRequest_kubernetesOperator() {
    JsonObject workflowResponse = getWorkflow(TEST_SIMPLE_KUBERNETES_DAG);

    assertTrue(isNotBlank(workflowResponse.get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString()));
    assertTrue(isNotBlank(workflowResponse.get(WORKFLOW_NAME_FIELD).getAsString()));
    assertEquals(workflowResponse.get(WORKFLOW_DESCRIPTION_FIELD).getAsString(), WORKFLOW_DESCRIPTION);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_WORKFLOW_RUN_FIELD).getAsInt(), WORKFLOW_CONCURRENT_WORKFLOW_RUN);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_TASK_RUN_FIELD).getAsInt(),WORKFLOW_CONCURRENT_TASK_RUN);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_ACTIVE_FIELD).getAsBoolean(), WORKFLOW_ACTIVE);
  }

  @Test
  public void should_returnSuccess_when_givenValidRequest_customOperator_dag() {
    JsonObject workflowResponse = getWorkflow(TEST_SIMPLE_CUSTOM_OPERATOR_DAG);

    assertTrue(isNotBlank(workflowResponse.get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString()));
    assertTrue(isNotBlank(workflowResponse.get(WORKFLOW_NAME_FIELD).getAsString()));
    assertEquals(workflowResponse.get(WORKFLOW_DESCRIPTION_FIELD).getAsString(), WORKFLOW_DESCRIPTION);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_WORKFLOW_RUN_FIELD).getAsInt(), WORKFLOW_CONCURRENT_WORKFLOW_RUN);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_TASK_RUN_FIELD).getAsInt(),WORKFLOW_CONCURRENT_TASK_RUN);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_ACTIVE_FIELD).getAsBoolean(), WORKFLOW_ACTIVE);
  }

  @Test
  public void should_returnSuccess_when_givenValidRequest_dummy_dag() {
    JsonObject workflowResponse = getWorkflow(TEST_DUMMY_DAG);

    assertTrue(isNotBlank(workflowResponse.get(CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD).getAsString()));
    assertTrue(isNotBlank(workflowResponse.get(WORKFLOW_NAME_FIELD).getAsString()));
    assertEquals(workflowResponse.get(WORKFLOW_DESCRIPTION_FIELD).getAsString(), WORKFLOW_DESCRIPTION);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_WORKFLOW_RUN_FIELD).getAsInt(), WORKFLOW_CONCURRENT_WORKFLOW_RUN);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_CONCURRENT_TASK_RUN_FIELD).getAsInt(),WORKFLOW_CONCURRENT_TASK_RUN);
    assertEquals(workflowResponse.get("registrationInstructions").getAsJsonObject().get(WORKFLOW_ACTIVE_FIELD).getAsBoolean(), WORKFLOW_ACTIVE);
  }

  @Test
  @Ignore("Enable this test to test the ignore DAG content functionality")
  public void should_ignoreDagcontent_when_givenValidRequest() throws Exception {
    String createWorkflowRequestBody = getValidCreateWorkflowRequest(TEST_SIMPLE_PYTHON_DAG,
            TEST_SIMPLE_WORKFLOW_FILE_NAME);

    ClientResponse response = client.send(
            HttpMethod.POST,
            CREATE_WORKFLOW_URL,
            createWorkflowRequestBody,
            headers,
            client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());

    String workflowId = new Gson().fromJson(createWorkflowRequestBody, JsonObject.class).get(WORKFLOW_NAME_FIELD)
            .getAsString();
    Map<String, Object> triggerWorkflowRequestPayload = buildTriggerWorkflowPayload();

    ClientResponse triggerResponse = client.send(
            HttpMethod.POST,
            String.format(CREATE_WORKFLOW_RUN_URL, workflowId),
            gson.toJson(triggerWorkflowRequestPayload),
            headers,
            client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NOT_FOUND, triggerResponse.getStatus());

    String error = triggerResponse.getEntity(String.class);
    assertTrue(error.contains(CREATE_WORKFLOW_IGNORE_CONTENT_MESSAGE));

    ClientResponse deleteResponse = client.send(
            HttpMethod.DELETE,
            String.format(GET_WORKFLOW_URL, workflowId),
            null,
            headers,
            client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NO_CONTENT, deleteResponse.getStatus());
  }
}
