package org.opengroup.osdu.azure.workflow.workflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opengroup.osdu.azure.workflow.utils.HTTPClientAzureV3;
import org.opengroup.osdu.workflow.workflow.v3.WorkflowV3IntegrationTests;

import java.util.ArrayList;

import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;

public class TestWorkflowV3Integration extends WorkflowV3IntegrationTests {

  @BeforeEach
  @Override
  public void setup() throws Exception {
    this.client = new HTTPClientAzureV3();
    this.headers = client.getCommonHeader();
    // Delete workflow in case it's still existing and not deleted from the failed previous tests
    deleteTestWorkflows(CREATE_WORKFLOW_WORKFLOW_NAME);
  }

  @AfterEach
  @Override
  public void tearDown() {
    deleteAllTestWorkflowRecords();
    this.client = null;
    this.headers = null;
    this.createdWorkflows = new ArrayList<>();
  }

//  @Override
//  @Test
//  @Disabled
//  public void shouldReturnBadRequestWhenInvalidDagNameWorkflowCreate() throws Exception { }

  private void deleteAllTestWorkflowRecords() {
    createdWorkflows.stream().forEach(c -> {
      try {
        deleteTestWorkflows(c.get(WORKFLOW_ID_FIELD));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
}
