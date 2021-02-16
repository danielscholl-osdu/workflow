
package org.opengroup.osdu.ibm.workflow.workflow.v3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.ibm.workflow.util.HTTPClientIBM;
import org.opengroup.osdu.workflow.workflow.v3.WorkflowV3IntegrationTests;

public class TestWorkflowV3Integration extends WorkflowV3IntegrationTests {

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
	public void shouldReturnSuccessWhenGivenValidRequestWorkflowCreate() throws Exception {
		// TODO Auto-generated method stub
		super.shouldReturnSuccessWhenGivenValidRequestWorkflowCreate();
	}

	@Override
	@Test
	@Disabled
	public void shouldReturnBadRequestWhenInvalidDagNameWorkflowCreate() throws Exception {
		// TODO Auto-generated method stub
		super.shouldReturnBadRequestWhenInvalidDagNameWorkflowCreate();
	}

	@Override
	@Test
	@Disabled
	public void shouldReturnInternalServerErrorWhenIncorrectWorkflowNameWorkflowCreate() throws Exception {
		// TODO Auto-generated method stub
		super.shouldReturnInternalServerErrorWhenIncorrectWorkflowNameWorkflowCreate();
	}

	@Override
	@Test
	@Disabled
	public void shouldReturn200WhenGetListWorkflowForTenant() throws Exception {
		// TODO Auto-generated method stub
		super.shouldReturn200WhenGetListWorkflowForTenant();
	}

	@Override
	@Test
	@Disabled
	public void shouldReturn200WhenGetCompleteDetailsForWorkflow() throws Exception {
		// TODO Auto-generated method stub
		super.shouldReturn200WhenGetCompleteDetailsForWorkflow();
	}

	@Override
	@Test
	@Disabled
	public void shouldReturnBadRequestWhenGetCompleteDetailsForWorkflow() throws Exception {
		// TODO Auto-generated method stub
		super.shouldReturnBadRequestWhenGetCompleteDetailsForWorkflow();
	}

	@Override
	@Test
	@Disabled
	public void shouldDeleteWorkflowDefinition() throws Exception {
		// TODO Auto-generated method stub
		super.shouldDeleteWorkflowDefinition();
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
