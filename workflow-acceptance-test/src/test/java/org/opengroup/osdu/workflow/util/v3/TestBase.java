/*
  Copyright 2020-2025 Google LLC
  Copyright 2020-2025 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.opengroup.osdu.workflow.util.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_SYSTEM_WORKFLOW_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_WORKFLOW_RUN_URL;
import static org.opengroup.osdu.workflow.consts.TestConstants.WORKFLOW_NAME_EXTERNAL_AIRFLOW;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildCreateWorkflowRunValidPayload;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildCreateWorkflowValidPayload;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildCreateWorkflowValidPayloadExternalAirflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.workflow.util.HTTPClient;
import org.springframework.http.HttpStatus;

@Slf4j
public abstract class TestBase {
	protected HTTPClient client;
	protected Map<String, String> headers;
	protected List<String> createdWorkflowsWorkflowNames = new ArrayList<>();
  protected List<CreatedWorkflowRun> createdWorkflowRuns = new ArrayList<>();
	protected static final String WORKFLOW_NAME_FIELD = "workflowName";
	protected static final String WORKFLOW_ID_FIELD = "workflowId";
	protected static final String WORKFLOW_RUN_ID_FIELD = "runId";
	protected static final String WORKFLOW_RUN_STATUS_FIELD = "status";
	protected static final String INVALID_WORKFLOW_NAME = "this-is-an-invalid-workflow-name";
	protected static final String INVALID_WORKFLOW_RUN_ID = "invalid-workflow-run-id";
	protected static final String INVALID_PARTITION = "invalid-partition";

	public abstract void setup() throws Exception;

	public abstract void tearDown() throws Exception;

	protected String createWorkflow() throws Exception {
		ClientResponse response = client.send(HttpMethod.POST, CREATE_WORKFLOW_URL, buildCreateWorkflowValidPayload(),
				headers, client.getAccessToken());
		assertEquals(HttpStatus.OK.value(), response.getStatus(), response.toString());
		return response.getEntity(String.class);
	}

  protected String createWorkflowExternalAirflow() throws Exception {
    ClientResponse response = client.send(HttpMethod.POST, CREATE_WORKFLOW_URL,
        buildCreateWorkflowValidPayloadExternalAirflow(), headers, client.getAccessToken());
    assertEquals(HttpStatus.OK.value(), response.getStatus(), response.toString());
    return response.getEntity(String.class);
  }

	protected String createSystemWorkflow() throws Exception {
		ClientResponse response = client.send(HttpMethod.POST, CREATE_SYSTEM_WORKFLOW_URL,
				buildCreateWorkflowValidPayload(), headers, client.getAccessToken());
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		return response.getEntity(String.class);
	}

	protected String createWorkflowRun() throws Exception {
		ClientResponse response = client.send(HttpMethod.POST,
				String.format(CREATE_WORKFLOW_RUN_URL, CREATE_WORKFLOW_WORKFLOW_NAME),
				buildCreateWorkflowRunValidPayload(), headers, client.getAccessToken());
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		return response.getEntity(String.class);
	}

  protected String createWorkflowRunExternalAirflow() throws Exception {
    ClientResponse response = client.send(HttpMethod.POST,
        String.format(CREATE_WORKFLOW_RUN_URL, WORKFLOW_NAME_EXTERNAL_AIRFLOW),
        buildCreateWorkflowRunValidPayload(), headers, client.getAccessToken());
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    return response.getEntity(String.class);
  }

	protected ClientResponse sendDeleteRequest(String url) throws Exception {
		return client.send(HttpMethod.DELETE, url, null, headers, client.getAccessToken());
	}

	protected void deleteTestSystemWorkflows(String workflowName) throws Exception {
		String url = CREATE_SYSTEM_WORKFLOW_URL + "/" + workflowName;
		sendDeleteRequest(url);
	}

	public String getWorkflowRunStatus(String workflowName, String workflowRunId) throws Exception {
		ClientResponse response = client.send(HttpMethod.GET,
				String.format(GET_WORKFLOW_RUN_URL, workflowName, workflowRunId), null, headers,
				client.getAccessToken());

		if (response.getStatus() == org.apache.http.HttpStatus.SC_OK) {
			Map<String, String> workflowRunInfo = new ObjectMapper().readValue(response.getEntity(String.class),
					HashMap.class);
			return workflowRunInfo.get(WORKFLOW_RUN_STATUS_FIELD);
		} else {
			throw new Exception(
					String.format("Error getting status for workflow run id %s. Status code: %s. Response: %s",
							workflowRunId, response.getStatus(), response));
		}
	}

	protected Map<String, String> getWorkflowInfoFromCreateWorkflowResponseBody(String responseBody)
			throws JsonProcessingException {
		return new ObjectMapper().readValue(responseBody, HashMap.class);
	}

  protected void createAndTrackWorkflow() throws Exception {
    String workflowResponseBody = createWorkflow();
    trackWorkflow(workflowResponseBody);
  }

  protected void createAndTrackWorkflowExternalAirflow() throws Exception {
    String workflowResponseBody = createWorkflowExternalAirflow();
    trackWorkflow(workflowResponseBody);
  }

  protected void trackWorkflow(String workflowResponseBody) throws Exception {
    Map<String, String> workflowInfo = new ObjectMapper().readValue(workflowResponseBody, HashMap.class);
    createdWorkflowsWorkflowNames.add(workflowInfo.get(WORKFLOW_NAME_FIELD));
  }

  protected Map<String, String> createAndTrackWorkflowRun() throws Exception {
    String workflowRunResponseBody = createWorkflowRun();
    return trackWorkflowRun(CREATE_WORKFLOW_WORKFLOW_NAME, workflowRunResponseBody);
  }

  protected Map<String, String> createAndTrackWorkflowRunExternalAirflow() throws Exception {
    String workflowRunResponseBody = createWorkflowRunExternalAirflow();
    return trackWorkflowRun(WORKFLOW_NAME_EXTERNAL_AIRFLOW, workflowRunResponseBody);
  }

  private Map<String, String> trackWorkflowRun(String workflowName, String workflowRunResponseBody) throws JsonProcessingException {
    Map<String, String> workflowRunInfo = new ObjectMapper().readValue(workflowRunResponseBody, HashMap.class);
    createdWorkflowRuns.add(
        CreatedWorkflowRun.builder()
            .workflowName(workflowName)
            .workflowRunId(workflowRunInfo.get(WORKFLOW_RUN_ID_FIELD))
            .build());
    return workflowRunInfo;
  }
}
