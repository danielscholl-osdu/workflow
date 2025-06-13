/*
 *  Copyright 2020-2023 Google LLC
 *  Copyright 2020-2023 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.workflow.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.client.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.model.ClientResponse;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.service.AirflowV2WorkflowEngineExtension;

import javax.ws.rs.HttpMethod;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.opengroup.osdu.workflow.service.AirflowV2WorkflowEngineExtension.*;

@RunWith(MockitoJUnitRunner.class)
public class IWorkflowEngineExtensionTest {

  public static final String WORKFLOW_NAME = "test_workflow";
  public static final String DAG_RUN_ID = "test_id";
  public static final String LAST_TASK_ID = "update_status_finished";
  public static final String SAVED_RECORDS_XCOM_KEY = "saved_record_ids";
  public static final String SKIPPED_RECORDS_XCOM_KEY = "skipped_ids";
  public static final String AIRFLOW_RESP_DIR = "airflow_responses/";
  public static final String AIRFLOW_RESPONSES_TASK_INSTANCES_JSON =
      AIRFLOW_RESP_DIR + "task-instances.json";
  public static final String AIRFLOW_RESPONSES_XCOM_KEYS_JSON = AIRFLOW_RESP_DIR + "xcom_keys.json";
  public static final String AIRFLOW_RESPONSES_XCOM_SAVED_RECORD_IDS_JSON =
      AIRFLOW_RESP_DIR + "xcom_saved_record_ids.json";
  public static final String AIRFLOW_RESPONSES_XCOM_SKIPPED_IDS_JSON =
      AIRFLOW_RESP_DIR + "xcom_skipped_ids.json";
  public static final String AIRFLOW_RESPONSES_EXPECTED_RESP_JSON =
      AIRFLOW_RESP_DIR + "expected_resp.json";

  @Mock private Client client;

  @Mock private AirflowConfig airflowConfig;

  @Mock private DpsHeaders dpsHeaders;

  private MockAirflowEngine engineExtension;

  @Before
  public void setUp() {
    MockAirflowEngine mockEngine = new MockAirflowEngine(client, airflowConfig, dpsHeaders);
    engineExtension = Mockito.spy(mockEngine);
  }

  @Test
  public void testGetLastDetails() throws JsonProcessingException {
    ClientResponse tasksResponse =
        ClientResponse.builder()
            .responseBody(readRespBodyFromFile(AIRFLOW_RESPONSES_TASK_INSTANCES_JSON))
            .build();

    String endpoint = format(TASK_INSTANCES, WORKFLOW_NAME, DAG_RUN_ID);
    String tasksErrMsg = format(GET_RUN_TASKS_ERROR_MESSAGE, DAG_RUN_ID, WORKFLOW_NAME);

    doReturn(tasksResponse)
        .when(engineExtension)
        .callAirflow(HttpMethod.GET, endpoint, null, null, tasksErrMsg);

    String taskXcomEntriesEndpoint = format(XCOM_ENTRIES, WORKFLOW_NAME, DAG_RUN_ID, LAST_TASK_ID);
    String xcomEntriesErrMsg = format(GET_TASKS_XCOM_ERROR_MESSAGE, LAST_TASK_ID);

    ClientResponse xcomKeysResponse =
        ClientResponse.builder()
            .responseBody(readRespBodyFromFile(AIRFLOW_RESPONSES_XCOM_KEYS_JSON))
            .build();

    doReturn(xcomKeysResponse)
        .when(engineExtension)
        .callAirflow(HttpMethod.GET, taskXcomEntriesEndpoint, null, null, xcomEntriesErrMsg);

    String savedRecordIds =
        format(XCOM_VALUES, WORKFLOW_NAME, DAG_RUN_ID, LAST_TASK_ID, SAVED_RECORDS_XCOM_KEY);
    String xcomValErrMsg = format(GET_XCOM_VALUES_ERROR_MESSAGE, LAST_TASK_ID);

    ClientResponse xcomValSavedRecordEndpoint =
        ClientResponse.builder()
            .responseBody(readRespBodyFromFile(AIRFLOW_RESPONSES_XCOM_SAVED_RECORD_IDS_JSON))
            .build();

    doReturn(xcomValSavedRecordEndpoint)
        .when(engineExtension)
        .callAirflow(HttpMethod.GET, savedRecordIds, null, null, xcomValErrMsg);

    String skippedRecordIds =
        format(XCOM_VALUES, WORKFLOW_NAME, DAG_RUN_ID, LAST_TASK_ID, SKIPPED_RECORDS_XCOM_KEY);

    ClientResponse xcomValSkippedRecordEndpoint =
        ClientResponse.builder()
            .responseBody(readRespBodyFromFile(AIRFLOW_RESPONSES_XCOM_SKIPPED_IDS_JSON))
            .build();

    doReturn(xcomValSkippedRecordEndpoint)
        .when(engineExtension)
        .callAirflow(HttpMethod.GET, skippedRecordIds, null, null, xcomValErrMsg);

    Object lastDetails = engineExtension.getLatestTaskDetails(WORKFLOW_NAME, DAG_RUN_ID);

    ObjectMapper objectMapper = new ObjectMapper();
    Object expectedResponse = readRespBodyFromFile(AIRFLOW_RESPONSES_EXPECTED_RESP_JSON);
    JsonNode expectedValue = objectMapper.readValue(expectedResponse.toString(), ObjectNode.class);

    assertEquals(expectedValue, lastDetails);
  }

  private Object readRespBodyFromFile(String fileName) {
    InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
    StringBuilder stringBuilder = new StringBuilder();
    try {
      assert resourceAsStream != null;
      try (BufferedReader bufferedReader =
          new BufferedReader(new InputStreamReader(resourceAsStream))) {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          stringBuilder.append(line);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return stringBuilder.toString();
  }

  class MockAirflowEngine extends AirflowV2WorkflowEngineExtension {

    protected MockAirflowEngine(
        Client restClient, AirflowConfig airflowConfig, DpsHeaders dpsHeaders) {
      super(restClient, airflowConfig, dpsHeaders);
    }

    @Override
    public ClientResponse callAirflow(
        String httpMethod,
        String apiEndpoint,
        String body,
        WorkflowEngineRequest rq,
        String errorMessage) {
      return super.callAirflow(httpMethod, apiEndpoint, body, rq, errorMessage);
    }
  }
}
