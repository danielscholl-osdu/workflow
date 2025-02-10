package org.opengroup.osdu.workflow.workflow.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_SYSTEM_WORKFLOW_BY_ID_URL;

import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.workflow.util.v3.TestBase;

import com.sun.jersey.api.client.ClientResponse;

public abstract class DeleteSystemWorkflowV3IntegrationTests extends TestBase {

	@Test
	public void should_delete_when_givenValidWorkflowName() throws Exception {
		String responseBody = createSystemWorkflow();
		Map<String, String> workflowInfo = getWorkflowInfoFromCreateWorkflowResponseBody(responseBody);
		createdWorkflows.add(workflowInfo);

		ClientResponse deleteResponse = client.send(HttpMethod.DELETE,
				String.format(GET_SYSTEM_WORKFLOW_BY_ID_URL, CREATE_WORKFLOW_WORKFLOW_NAME), null, headers,
				client.getAccessToken());
		assertEquals(HttpStatus.SC_NO_CONTENT, deleteResponse.getStatus());
	}

	@Test
	public void should_ReturnNotFound_when_givenInvalidWorkflowName() throws Exception {
		String responseBody = createSystemWorkflow();
		Map<String, String> workflowInfo = getWorkflowInfoFromCreateWorkflowResponseBody(responseBody);
		createdWorkflows.add(workflowInfo);

		ClientResponse deleteResponse = client.send(HttpMethod.DELETE,
				String.format(GET_SYSTEM_WORKFLOW_BY_ID_URL, INVALID_WORKFLOW_NAME), null, headers,
				client.getAccessToken());

		assertEquals(HttpStatus.SC_NOT_FOUND, deleteResponse.getStatus());
	}

	@Test
	public void should_returnForbidden_when_notGivenAccessToken() throws Exception {

		String responseBody = createSystemWorkflow();
		Map<String, String> workflowInfo = getWorkflowInfoFromCreateWorkflowResponseBody(responseBody);
		createdWorkflows.add(workflowInfo);

		ClientResponse response = client.send(HttpMethod.DELETE,
				String.format(GET_SYSTEM_WORKFLOW_BY_ID_URL, CREATE_WORKFLOW_WORKFLOW_NAME), null, headers, null);
		assertTrue(
				HttpStatus.SC_FORBIDDEN == response.getStatus() || HttpStatus.SC_UNAUTHORIZED == response.getStatus());
	}
}
