package org.opengroup.osdu.azure.workflow.workflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opengroup.osdu.azure.workflow.framework.workflow.GetAllRunInstancesIntegrationTests;
import org.opengroup.osdu.azure.workflow.utils.HTTPClientAzure;

public class TestGetAllRunInstancesIntegration extends GetAllRunInstancesIntegrationTests {

  @BeforeEach
  @Override
  public void setup() throws Exception {
    super.setup();
    this.client = new HTTPClientAzure();
    this.headers = client.getCommonHeader();
    this.initializeTriggeredWorkflow();
  }

  @AfterEach
  @Override
  public void tearDown() throws Exception {
    this.client = null;
    this.headers = null;
  }
}
