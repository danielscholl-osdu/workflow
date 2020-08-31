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

package org.opengroup.osdu.workflow.aws.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.workflow.exception.OsduRuntimeException;
import org.opengroup.osdu.workflow.provider.interfaces.ISubmitIngestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitIngestServiceImpl implements ISubmitIngestService {

  @Value("${airflow.baseUrl}")
  String airflowBaseUrl;

  AirflowClient airflowClient;

  private final static String PARSE_ERROR_MSG = "Unable to parse data for dag kickoff";

  /**
   * Spring boot constructor newing up an airflow client
   */
  @PostConstruct
  public void init(){
    airflowClient = new AirflowClient();
  }

  /**
   * This gets called by the core code, it's meant to kick off a dag on an
   * airflow instance.
   * @param dagName
   * @param data
   * @return
   */
  @Override
  public boolean submitIngest(String dagName, Map<String, Object> data) {
    String serializedData = serializeData(data);
    try {
      String airflowDagUrlStr = format("%s/api/experimental/dags/%s/dag_runs", airflowBaseUrl, dagName);
      airflowClient.makeRequestToAirflow(airflowDagUrlStr, serializedData, dagName);
      return true;
    } catch (IOException e) {
      throw new OsduRuntimeException("Request execution exception", e);
    }
  }

  /**
   * Helper function that serializes a dictionary into a string to be sent to airflow
   * @param data
   * @return
   */
  private String serializeData(Map<String, Object> data){
    String serializedData;
    try {
      ObjectMapper mapper = new ObjectMapper();
      serializedData = mapper.writeValueAsString(data);
    } catch (JsonProcessingException e){
      throw new OsduRuntimeException(PARSE_ERROR_MSG, e);
    }
    return serializedData;
  }


}
