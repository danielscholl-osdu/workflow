package org.opengroup.osdu.azure.workflow.framework.workflow;

import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.framework.models.WorkflowMetadata;
import org.opengroup.osdu.azure.workflow.framework.util.TestBase;
import org.opengroup.osdu.workflow.util.HTTPClient;

import javax.ws.rs.HttpMethod;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opengroup.osdu.azure.workflow.framework.consts.TestConstants.GET_WORKFLOWS_FOR_TENANT_URL;
import static org.opengroup.osdu.azure.workflow.framework.util.TestDataUtil.getAllWorkflows;

public abstract class GetAllWorkflowsForTenantIntegrationTests extends TestBase {

  public static final String INVALID_PARTITION = "invalid-partition";

  public static final String PREFIX = "test";
  public static Map<String, WorkflowMetadata> testDataWorkflowNameToInfo;

  @BeforeAll
  public static void initialize() {
    testDataWorkflowNameToInfo = getAllWorkflows();
  }

  @Test
  public void should_return_workflows_when_valid_request() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOWS_FOR_TENANT_URL, PREFIX),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_OK, response.getStatus());

    Type WorkflowMetadataListType = new TypeToken<ArrayList<WorkflowMetadata>>(){}.getType();
    List<WorkflowMetadata> workflowMetadataList =
        gson.fromJson(response.getEntity(String.class), WorkflowMetadataListType);
    assertNotNull(workflowMetadataList);
    verifyResponseWorkflowMetadataWithTestData(workflowMetadataList,
        testDataWorkflowNameToInfo.size());
  }

  @Test
  public void should_return_workflows_when_valid_request_empty_prefix() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOWS_FOR_TENANT_URL,""),
        null,
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.SC_OK, response.getStatus());

    Type WorkflowMetadataListType = new TypeToken<ArrayList<WorkflowMetadata>>(){}.getType();
    List<WorkflowMetadata> workflowMetadataList =
        gson.fromJson(response.getEntity(String.class), WorkflowMetadataListType);
    assertNotNull(workflowMetadataList);
    verifyResponseWorkflowMetadataWithTestData(workflowMetadataList,
        testDataWorkflowNameToInfo.size());
  }

  @Test
  public void should_returnUnauthorized_when_notGivenAccessToken() {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOWS_FOR_TENANT_URL, PREFIX),
        null,
        headers,
        null
    );

    assertTrue(response.getStatus()== HttpStatus.SC_FORBIDDEN || response.getStatus()== HttpStatus.SC_UNAUTHORIZED) ;

  }

  @Test
  public void should_returnUnauthorized_when_givenNoDataAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOWS_FOR_TENANT_URL, PREFIX),
        null,
        headers,
        client.getNoDataAccessToken()
    );

    assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void should_returnUnauthorized_when_givenInvalidPartition() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.GET,
        String.format(GET_WORKFLOWS_FOR_TENANT_URL, PREFIX),
        null,
        HTTPClient.overrideHeader(headers, INVALID_PARTITION),
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

  private void verifyResponseWorkflowMetadataWithTestData(List<WorkflowMetadata> responseData,
                                                          int expectedWorkflowCountToMatch) {
    int matchedWorkflowCount = 0;
    for (WorkflowMetadata responseWorkflow : responseData) {
      String responseWorkflowName = responseWorkflow.getWorkflowName();
      if (testDataWorkflowNameToInfo.containsKey(responseWorkflowName)) {
        WorkflowMetadata workflowMetadata = testDataWorkflowNameToInfo.get(responseWorkflowName);
        assertEquals(workflowMetadata, responseWorkflow);
        matchedWorkflowCount++;
      }
    }
    assertEquals(expectedWorkflowCountToMatch, matchedWorkflowCount);
    checkForDuplicateItems(responseData);
  }

  private void checkForDuplicateItems(List<WorkflowMetadata> items) {
    Set<String> workflowNames = new HashSet<>();
    for (WorkflowMetadata workflowMetadata : items) {
      String workflowName = workflowMetadata.getWorkflowName();
      assertFalse(workflowNames.contains(workflowName));
      workflowNames.add(workflowName);
    }
  }
}
