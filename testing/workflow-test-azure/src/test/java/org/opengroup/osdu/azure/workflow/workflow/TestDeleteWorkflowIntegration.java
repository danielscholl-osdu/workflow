package org.opengroup.osdu.azure.workflow.workflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opengroup.osdu.azure.workflow.framework.workflow.DeleteWorkflowIntegrationTests;
import org.opengroup.osdu.azure.workflow.utils.HTTPClientAzure;

public class TestDeleteWorkflowIntegration extends DeleteWorkflowIntegrationTests {
  @BeforeEach
  @Override
  public void setup() throws Exception {
    super.setup();
    this.client = new HTTPClientAzure();
    this.headers = client.getCommonHeader();
  }

  @AfterEach
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    this.client = null;
    this.headers = null;
  }
}
