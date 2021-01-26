/**
 * Copyright 2020 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.provider.ibm.service;

import static java.lang.String.format;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.workflow.exception.RuntimeException;
import org.opengroup.osdu.workflow.provider.ibm.interfaces.ISubmitIngestService;
import org.opengroup.osdu.workflow.provider.ibm.property.AirflowProperties;
import org.springframework.stereotype.Service;
import java.util.Collections;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitIngestServiceImpl implements ISubmitIngestService {

  final AirflowProperties airflowProperties;

  private static final String AIRFLOW_PAYLOAD_PARAMETER_NAME = "conf";

  @Override
  public boolean submitIngest(String dagName, Map<String, Object> data) {

    try {

    	String airflowUrl = airflowProperties.getUrl();
        String webServerUrl = format("%s/api/experimental/dags/%s/dag_runs", airflowUrl, dagName);

        Map<String, Object> confdata = Collections.singletonMap(AIRFLOW_PAYLOAD_PARAMETER_NAME, data);

        JSONObject requestBody = new JSONObject(confdata);
        Client restClient = Client.create();
        WebResource webResource = restClient.resource(webServerUrl);

        ClientResponse response = webResource
		        .type(MediaType.APPLICATION_JSON)
		        .post(ClientResponse.class, requestBody.toString());


        return  response.getStatus() == 200 ? true : false;
 //   } catch (HttpResponseException e) {
  //    throw new IntegrationException("Airflow request fail", e);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
