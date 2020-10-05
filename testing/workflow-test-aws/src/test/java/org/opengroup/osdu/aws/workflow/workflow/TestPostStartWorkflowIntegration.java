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
import org.opengroup.osdu.aws.workflow.util.DynamoSetupUtil;
import org.opengroup.osdu.aws.workflow.util.HTTPClientAWS;
import org.opengroup.osdu.workflow.workflow.PostStartWorkflowIntegrationTests;

import javax.ws.rs.HttpMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.workflow.consts.TestConstants.START_WORKFLOW_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.getValidWorkflowPayload;

public class TestPostStartWorkflowIntegration extends PostStartWorkflowIntegrationTests {

  private DynamoSetupUtil dynamoSetupUtil;
  String strategyId;

  @BeforeEach
	@Override
	public void setup() throws Exception {
		this.client = new HTTPClientAWS();
		this.headers = client.getCommonHeader();
		dynamoSetupUtil = new DynamoSetupUtil();

		// needs to insert ingestion strategy row
    strategyId = dynamoSetupUtil.insertIngestionStrategy();
	}

  @Test
  @Override
  public void should_returnUnauthorized_when_notGivenAccessToken() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        START_WORKFLOW_URL,
        getValidWorkflowPayload(),
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
		dynamoSetupUtil.deleteStrategy(strategyId);
	}
}
