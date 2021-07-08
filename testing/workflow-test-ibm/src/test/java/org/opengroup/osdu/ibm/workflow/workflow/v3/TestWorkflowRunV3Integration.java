
package org.opengroup.osdu.ibm.workflow.workflow.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_DETAILS_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;

import java.util.UUID;

import javax.ws.rs.HttpMethod;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.ibm.workflow.util.HTTPClientIBM;
import org.opengroup.osdu.workflow.workflow.v3.WorkflowRunV3IntegrationTests;
import org.springframework.http.HttpStatus;

import com.sun.jersey.api.client.ClientResponse;

public class TestWorkflowRunV3Integration extends WorkflowRunV3IntegrationTests {

	@BeforeEach
	@Override
	public void setup() throws Exception {
		this.client = new HTTPClientIBM();
		this.headers = client.getCommonHeader();
		try {
		  deleteTestWorkflows(CREATE_WORKFLOW_WORKFLOW_NAME);
		} catch (Exception e) {
		  throw e;
		}
	}

	@AfterEach
	@Override
	public void tearDown() throws InterruptedException {
		Thread.sleep(30000);
		deleteAllTestWorkflowRecords();
		this.client = null;
		this.headers = null;
	}


	@Override
	@Test
	@Disabled
	public void shouldReturn400WhenGetDetailsForSpecificWorkflowRunInstance() throws Exception {
		String workflowId = UUID
		        .randomUUID().toString().replace("-", "");
		    String runId = UUID
		        .randomUUID().toString().replace("-", "");

		    ClientResponse getResponse = client.send(
		        HttpMethod.GET,
		        String.format(GET_DETAILS_WORKFLOW_RUN_URL, workflowId, runId),
		        null,
		        headers,
		        client.getAccessToken()
		    );
		    assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatus());
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
