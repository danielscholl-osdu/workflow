package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.util.AzureTestBase;

import javax.ws.rs.HttpMethod;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_CUSTOM_OPERATOR_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_HTTP_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_KUBERNETES_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_SIMPLE_PYTHON_DAG;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestDAGNames.TEST_VALIDATE_RUN_CONFIG_DAG;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.buildInvalidTriggerWorkflowRunPayload;
import static org.opengroup.osdu.azure.workflow.framework.util.TriggerWorkflowTestsBuilder.buildTriggerWorkflowPayloadWithMaxRequestSize;
import static org.opengroup.osdu.azure.workflow.utils.AzurePayLoadBuilder.buildCreateWorkflowValidPayloadWithGivenWorkflowName;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.WORKFLOW_STATUS_TYPE_SUCCESS;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildCreateWorkflowRunValidPayload;

public abstract class PostTriggerWorkflowIntegrationTests extends AzureTestBase {

  /** Test functionality for dags other then TEST_DUMMY_DAG **/

  private static final String INVALID_REQUEST_SIZE_MESSAGE = "Request content exceeded limit of 2000 kB";

  @Test
  @Disabled
  public void getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testSimpleCustomOperatorDagName() throws Exception {
    getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testDagName(TEST_SIMPLE_CUSTOM_OPERATOR_DAG);
  }

  @Test
  public void getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testSimpleHttpDagName() throws Exception {
    getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testDagName(TEST_SIMPLE_HTTP_DAG);
  }

  @Test
  public void getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testSimpleKubernetesDagName() throws Exception {
    getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testDagName(TEST_SIMPLE_KUBERNETES_DAG);
  }

  @Test
  public void getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testSimplePythonDagName() throws Exception {
    getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testDagName(TEST_SIMPLE_PYTHON_DAG);
  }

  @Test
  public void getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testValidateRunConfigDagName() throws Exception {
    getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testDagName(TEST_VALIDATE_RUN_CONFIG_DAG);
  }

  @Test
  public void should_returnBadRequest_when_givenInvalidRequest() throws Exception {
    String workflowResponseBody = createWorkflow();
    Map<String, String> workflowInfo = new ObjectMapper().readValue(workflowResponseBody, HashMap.class);
    createdWorkflows.add(workflowInfo);

    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(CREATE_WORKFLOW_RUN_URL, CREATE_WORKFLOW_WORKFLOW_NAME),
        new Gson().toJson(buildInvalidTriggerWorkflowRunPayload()),
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
  }

  private void getWorkflowRunById_should_returnSuccess_when_givenValidRequest_testDagName(String workflowName) throws Exception {
    try {
      deleteTestWorkflows(workflowName);
    } catch (Exception e) {
      e.printStackTrace();
    }
    String workflowResponseBody = createWorkflowWithGivenName(workflowName);
    Map<String, String> workflowInfo = new ObjectMapper().readValue(workflowResponseBody, HashMap.class);
    createdWorkflows.add(workflowInfo);

    String workflowRunResponseBody = createWorkflowRunWithGivenWorkflowName(workflowName);
    Map<String, String> workflowRunInfo = new ObjectMapper().readValue(workflowRunResponseBody, HashMap.class);
    createdWorkflowRuns.add(workflowRunInfo);

    waitForWorkflowRunsToComplete();

    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOW_RUN_URL, workflowName, workflowRunInfo.get(WORKFLOW_RUN_ID_FIELD)),
        null,
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus(), response.toString());

    workflowRunResponseBody = response.getEntity(String.class);
    workflowRunInfo = new ObjectMapper().readValue(workflowRunResponseBody, HashMap.class);

    assertTrue(isNotBlank(workflowRunInfo.get(WORKFLOW_ID_FIELD)));
    assertTrue(isNotBlank(workflowRunInfo.get(WORKFLOW_RUN_ID_FIELD)));
    assertEquals(workflowRunInfo.get(WORKFLOW_RUN_STATUS_FIELD), WORKFLOW_STATUS_TYPE_SUCCESS);
  }

  private String createWorkflowWithGivenName(String workflowName) throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        buildCreateWorkflowValidPayloadWithGivenWorkflowName(workflowName),
        headers,
        client.getAccessToken()
    );
    assertEquals(org.springframework.http.HttpStatus.OK.value(), response.getStatus(), response.toString());
    return response.getEntity(String.class);
  }

  private String createWorkflowRunWithGivenWorkflowName(String workflowName) throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(CREATE_WORKFLOW_RUN_URL, workflowName),
        buildCreateWorkflowRunValidPayload(),
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_OK, response.getStatus());
    return response.getEntity(String.class);
  }

  @Test
  public void should_returnException_when_givenRequestSizeExceedMaxRequestSize() throws Exception {
    String workflowResponseBody = createWorkflow();
    Map<String, String> workflowInfo = new ObjectMapper().readValue(workflowResponseBody, HashMap.class);
    createdWorkflows.add(workflowInfo);

    ClientResponse response = client.send(
        HttpMethod.POST,
        String.format(CREATE_WORKFLOW_RUN_URL, CREATE_WORKFLOW_WORKFLOW_NAME),
        new Gson().toJson(buildTriggerWorkflowPayloadWithMaxRequestSize()),
        headers,
        client.getAccessToken()
    );
    String messageBody = response.getEntity(String.class);
    Map<String, String> messageInfo = new ObjectMapper().readValue(messageBody, HashMap.class);
    assertEquals(INVALID_REQUEST_SIZE_MESSAGE,messageInfo.get("message"));
    assertEquals(HttpStatus.SC_REQUEST_TOO_LONG,response.getStatus());
  }
}
