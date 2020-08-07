package org.opengroup.osdu.gcp.workflow.workflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opengroup.osdu.gcp.workflow.util.HTTPClientGCP;
import org.opengroup.osdu.workflow.workflow.PostStartWorkflowIntegrationTests;

public class TestPostStartWorkflowIntegration extends PostStartWorkflowIntegrationTests {

	@BeforeEach
	@Override
	public void setup() throws Exception {
		this.client = new HTTPClientGCP();
		this.headers = client.getCommonHeader();
	}

	@AfterEach
	@Override
	public void tearDown() throws Exception {
		this.client = null;
		this.headers = null;
	}
}
