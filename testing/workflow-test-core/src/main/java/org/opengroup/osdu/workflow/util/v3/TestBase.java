/*
  Copyright 2020 Google LLC
  Copyright 2020 EPAM Systems, Inc

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

import java.util.Map;

import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.HttpMethod;
import org.opengroup.osdu.workflow.util.HTTPClient;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_URL;
import static org.opengroup.osdu.workflow.util.PayloadBuilder.buildCreateWorkflowValidPayload;

public abstract class TestBase {
  protected HTTPClient client;
  protected Map<String, String> headers;
  protected static final String WORKFLOW_NAME = "workflowName";

  public abstract void setup() throws Exception;
  public abstract void tearDown() throws Exception;

  protected String createWorkflow() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        CREATE_WORKFLOW_URL,
        buildCreateWorkflowValidPayload(),
        headers,
        client.getAccessToken()
    );
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    return response.getEntity(String.class);
  }

  protected ClientResponse sendDeleteRequest(String url) throws Exception {
    return client.send(
        HttpMethod.DELETE,
        url,
        null,
        headers,
        client.getAccessToken()
    );
  }

  protected void deleteTestWorkflows(String workflowName) throws Exception {
    String url = CREATE_WORKFLOW_URL + "/" + workflowName;
    sendDeleteRequest(url);
  }
}
