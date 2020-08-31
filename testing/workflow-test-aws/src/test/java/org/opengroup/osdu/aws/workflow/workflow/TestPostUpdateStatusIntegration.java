// Copyright © 2020 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.aws.workflow.workflow;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.aws.workflow.util.HTTPClientAWS;
import org.opengroup.osdu.aws.workflow.util.WorkflowStatusUtil;
import org.opengroup.osdu.workflow.workflow.PostUpdateStatusIntegrationTests;

import javax.ws.rs.HttpMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.FINISHED_WORKFLOW_ID;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.getEnvironmentVariableOrDefaultKey;
import static org.opengroup.osdu.workflow.consts.TestConstants.*;
import static org.opengroup.osdu.workflow.consts.TestConstants.WORKFLOW_STATUS_TYPE_FINISHED;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildUpdateStatus;

public class TestPostUpdateStatusIntegration extends PostUpdateStatusIntegrationTests {

  private WorkflowStatusUtil workflowStatusUtil;
  String finishedWorkflowId;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		this.client = new HTTPClientAWS();
		this.headers = client.getCommonHeader();

		// need to insert a row in dynamo directly to test updating a finished
    // workflow. see integration test: should_returnBadRequest_when_givenFinishedWorkflowId
    // normally, it would use just endpoints but no delete endpoint exists so it all
    // needs to go directly against dynamo
    workflowStatusUtil = new WorkflowStatusUtil();
    finishedWorkflowId = workflowStatusUtil.insertWorkflowStatus();
	}

	@Test
  @Override
  public void should_returnBadRequest_when_givenFinishedWorkflowId() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        UPDATE_STATUS_URL,
        buildUpdateStatus(finishedWorkflowId, WORKFLOW_STATUS_TYPE_FINISHED),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());

    String error = response.getEntity(String.class);

    assertTrue(error.contains(
        String.format(WORKFLOW_ALREADY_HAS_STATUS_MESSAGE,
            finishedWorkflowId,
            WORKFLOW_STATUS_TYPE_FINISHED.toUpperCase())));
  }

  @Test
  @Override
  public void should_returnUnauthorized_when_notGivenAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        UPDATE_STATUS_URL,
        buildUpdateStatus(getEnvironmentVariableOrDefaultKey(FINISHED_WORKFLOW_ID), WORKFLOW_STATUS_TYPE_FINISHED),
        headers,
        null
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

	@AfterEach
	@Override
	public void tearDown() throws Exception {
		this.client = null;
		this.headers = null;
		workflowStatusUtil.deleteWorkflow(finishedWorkflowId);
	}

}
