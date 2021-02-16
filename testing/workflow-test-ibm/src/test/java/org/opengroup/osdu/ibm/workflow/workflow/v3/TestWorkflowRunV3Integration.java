
package org.opengroup.osdu.ibm.workflow.workflow.v3;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.ibm.workflow.util.HTTPClientIBM;
import org.opengroup.osdu.workflow.workflow.v3.WorkflowRunV3IntegrationTests;

public class TestWorkflowRunV3Integration extends WorkflowRunV3IntegrationTests {

	@BeforeEach
	@Override
	public void setup() {
		this.client = new HTTPClientIBM();
		this.headers = client.getCommonHeader();
	}

	@AfterEach
	@Override
	public void tearDown() {
		deleteAllTestWorkflowRecords();
		this.client = null;
		this.headers = null;
	}

	@Override
	@Test
	@Disabled
	public void shouldReturn200WhenTriggerNewWorkflow() throws Exception {
		// TODO Auto-generated method stub
		super.shouldReturn200WhenTriggerNewWorkflow();
	}

	@Override
	@Test
	@Disabled
	public void shouldReturn200WhenGetAllRunInstances() throws Exception {
		// TODO Auto-generated method stub
		super.shouldReturn200WhenGetAllRunInstances();
	}

	@Override
	@Test
	@Disabled
	public void shouldReturn400WhenGetDetailsForSpecificWorkflowRunInstance() throws Exception {
		// TODO Auto-generated method stub
		super.shouldReturn400WhenGetDetailsForSpecificWorkflowRunInstance();
	}

	@Override
	@Test
	@Disabled
	public void shouldReturn200WhenUpdateWorkflowRunInstance() throws Exception {
		// TODO Auto-generated method stub
		super.shouldReturn200WhenUpdateWorkflowRunInstance();
	}

	private void deleteAllTestWorkflowRecords() {
		createdWorkflows.stream().forEach(c -> {
			try {
				deleteTestWorkflows(c.get(WORKFLOW_NAME));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
