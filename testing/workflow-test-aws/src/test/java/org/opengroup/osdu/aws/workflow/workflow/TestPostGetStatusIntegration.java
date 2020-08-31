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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opengroup.osdu.aws.workflow.util.HTTPClientAWS;
import org.opengroup.osdu.aws.workflow.util.WorkflowStatusDoc;
import org.opengroup.osdu.aws.workflow.util.WorkflowStatusUtil;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.workflow.workflow.PostGetStatusIntegrationTests;

import javax.ws.rs.HttpMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.FINISHED_WORKFLOW_ID;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.getEnvironmentVariableOrDefaultKey;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_STATUS_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.WORKFLOW_STATUS_TYPE_FINISHED;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildWorkflowIdPayload;

public class TestPostGetStatusIntegration extends PostGetStatusIntegrationTests {

  private WorkflowStatusUtil workflowStatusUtil;
  String finishedWorkflowId;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		this.client = new HTTPClientAWS();
		this.headers = client.getCommonHeader();

    // need to insert a row in dynamo directly to test updating a finished
    // workflow. see integration test: should_returnFinished_when_givenFinishedWorkflowId
    // normally, it would use just endpoints but no delete endpoint exists so it all
    // needs to go directly against dynamo
    workflowStatusUtil = new WorkflowStatusUtil();
    finishedWorkflowId = workflowStatusUtil.insertWorkflowStatus();
	}

	@Test
	@Override
  public void should_returnFinished_when_givenFinishedWorkflowId() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        GET_STATUS_URL,
        buildWorkflowIdPayload(finishedWorkflowId),
        headers,
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_OK, response.getStatus());

    String responseBody = response.getEntity(String.class);

    assertTrue(StringUtils.contains(responseBody, WORKFLOW_STATUS_TYPE_FINISHED));
  }


  @Test
  @Override
  public void should_returnUnauthorized_when_notGivenAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        GET_STATUS_URL,
        buildWorkflowIdPayload(getEnvironmentVariableOrDefaultKey(FINISHED_WORKFLOW_ID)),
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
