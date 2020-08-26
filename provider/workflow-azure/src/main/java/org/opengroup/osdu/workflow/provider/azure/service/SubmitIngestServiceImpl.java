//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.workflow.provider.azure.service;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.provider.interfaces.ISubmitIngestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.inject.Named;
import javax.ws.rs.core.MediaType;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitIngestServiceImpl implements ISubmitIngestService {

  private static Logger logger = Logger.getLogger(SubmitIngestServiceImpl.class.getName());

  private final static String AIRFLOW_PAYLOAD_PARAMETER_NAME = "conf";
  private final static String RUN_ID_PARAMETER_NAME = "run_id";

  @Autowired
  @Named("AIRFLOW_URL")
  private String airflowURL;

  @Autowired
  @Named("AIRFLOW_APP_KEY")
  private String airflowAppKey;

  @Override
  public boolean submitIngest(String dagName, Map<String, Object> data) {
    logger.log(Level.INFO, String.format("Submitting ingestion with Airflow with dagName: {%s}",
      dagName));

    String airflowApiUrl = String.format("%s/api/experimental/dags/%s/dag_runs", airflowURL, dagName);

/*    String workflowId = data.get(RUN_ID_PARAMETER_NAME).toString();
    data.remove(RUN_ID_PARAMETER_NAME);*/
    String workflowId = data.get(WorkflowStatus.Fields.WORKFLOW_ID).toString();
    data.remove(WorkflowStatus.Fields.WORKFLOW_ID);

    JSONObject requestBody = new JSONObject();
    requestBody.put(RUN_ID_PARAMETER_NAME, workflowId);
    requestBody.put(AIRFLOW_PAYLOAD_PARAMETER_NAME, data);

    logger.log(Level.INFO, String.format("Airflow endpoint: {%s}", airflowApiUrl));

    Client restClient = Client.create();
    WebResource webResource = restClient.resource(airflowApiUrl);

    ClientResponse response = webResource
      .type(MediaType.APPLICATION_JSON)
      .header("Authorization", "Basic " + airflowAppKey)
      .post(ClientResponse.class, requestBody.toString());

    logger.log(Level.INFO, String.format("Airflow response: {%s}.", response.getStatus()));

    return  response.getStatus() == 200 ? true : false;
  }
}
