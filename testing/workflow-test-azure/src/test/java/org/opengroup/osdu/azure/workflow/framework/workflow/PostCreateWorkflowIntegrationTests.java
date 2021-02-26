package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder;
import org.opengroup.osdu.azure.workflow.framework.util.HTTPClient;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;

import javax.ws.rs.HttpMethod;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.CREATE_WORKFLOW_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.INVALID_PARTITION;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.GET_WORKFLOW_BY_ID_URL;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_DUMMY_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_CUSTOM_OPERATOR_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_HTTP_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_KUBERNETES_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_PYTHON_DAG;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_ACTIVE;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_ACTIVE_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_CONCURRENT_TASK_RUN;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_CONCURRENT_TASK_RUN_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_CONCURRENT_WORKFLOW_RUN;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_CONCURRENT_WORKFLOW_RUN_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_DESCRIPTION;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_DESCRIPTION_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_ID_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.WORKFLOW_NAME_FIELD;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.getInvalidCreateWorkflowRequest;
import static org.opengroup.osdu.azure.workflow.framework.util.CreateWorkflowTestsBuilder.getValidCreateWorkflowRequest;
import static org.opengroup.osdu.azure.workflow.framework.util.TestDataUtil.getWorkflow;

public abstract class PostCreateWorkflowIntegrationTests extends TestBase {
  public static final String CREATE_WORKFLOW_INVALID_REQUEST_MESSAGE = "Invalid JSON input: Unrecognized field";
  public static final String WORKFLOW_NAME_CONFLICT_MESSAGE = "ResourceConflictException: Workflow with name %s already exists";
  public static final String TEST_WORKFLOW_FILE_NAME = "test_dummy_dag.py";

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
  public void should_returnBadRequest_when_givenInvalidRequest() throws Exception{
    String createWorkflowRequestBody = getInvalidCreateWorkflowRequest(TEST_DUMMY_DAG,
        TEST_WORKFLOW_FILE_NAME);

    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        createWorkflowRequestBody,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());

    String error = response.getEntity(String.class);

    assertTrue(error.contains(CREATE_WORKFLOW_INVALID_REQUEST_MESSAGE));
  }

  @Test
  public void should_returnWorkflowExists_when_givenDuplicateRequest() throws Exception{
    String createWorkflowRequestBody = getValidCreateWorkflowRequest(TEST_DUMMY_DAG,
        TEST_WORKFLOW_FILE_NAME);

    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        createWorkflowRequestBody,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());
    JsonObject responseBody = gson.fromJson(response.getEntity(String.class), JsonObject.class);

    ClientResponse duplicateResponse = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        createWorkflowRequestBody,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_CONFLICT, duplicateResponse.getStatus());

    JsonObject duplicateResponseBody = gson.fromJson(duplicateResponse.getEntity(String.class),
        JsonObject.class);
    assertEquals(responseBody.get("workflowId").getAsString(),
        duplicateResponseBody.get("conflictId").getAsString());
    assertEquals(String.format(WORKFLOW_NAME_CONFLICT_MESSAGE,
        responseBody.get("workflowName").getAsString()),
        duplicateResponseBody.get("message").getAsString());

    ClientResponse deleteResponse = client.send(
        HttpMethod.DELETE,
        String.format(GET_WORKFLOW_BY_ID_URL, responseBody.get(WORKFLOW_ID_FIELD).getAsString()),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_NO_CONTENT, deleteResponse.getStatus());
  }

  @Test
  public void should_returnForbidden_when_notGivenAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        getValidCreateWorkflowRequest(TEST_DUMMY_DAG, TEST_WORKFLOW_FILE_NAME),
        headers,
        null
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenNoDataAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        getValidCreateWorkflowRequest(TEST_DUMMY_DAG, TEST_WORKFLOW_FILE_NAME),
        headers,
        client.getNoDataAccessToken()
    );

    assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void should_returnForbidden_when_givenInvalidPartition() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        getValidCreateWorkflowRequest(TEST_DUMMY_DAG, TEST_WORKFLOW_FILE_NAME),
        HTTPClient.overrideHeader(headers, INVALID_PARTITION),
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }
}
