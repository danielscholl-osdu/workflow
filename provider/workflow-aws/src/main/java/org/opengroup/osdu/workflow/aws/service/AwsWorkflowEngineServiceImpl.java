/*
  Copyright © 2021 Amazon Web Services

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

package org.opengroup.osdu.workflow.aws.service;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.json.JSONObject;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.aws.config.AwsAirflowApiMode;
import org.opengroup.osdu.workflow.aws.config.AwsServiceConfig;
import org.opengroup.osdu.workflow.aws.service.airflow.sqs.WorkflowRequestBodyFactory;
import org.opengroup.osdu.workflow.aws.service.airflow.sqs.WorkflowSqsClient;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.model.TriggerWorkflowResponse;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Primary
public class AwsWorkflowEngineServiceImpl implements IWorkflowEngineService {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(AwsWorkflowEngineServiceImpl.class);  

  @Inject 
  AwsServiceConfig awsConfig;

  @Inject
  private AirflowConfig airflowConfig;

  @Inject
  private Client restClient;

  @Inject
  DpsHeaders headers;

  @Inject
  WorkflowSqsClient sqsClient;

  @Inject
  private WorkflowRequestBodyFactory workflowRequestBodyFactory;

  final ObjectMapper objectMapper = new ObjectMapper();

  // @Autowired
  // @Qualifier("dags")
  // private FileShareStore dagsFileShareStore;
  // @Autowired
  // @Qualifier("customOperators")
  // private FileShareStore customOperatorsFileShareStore;

  @Override
  public void createWorkflow(
      final WorkflowEngineRequest rq, final Map<String, Object> registrationInstruction) {
    
  }

  @Override
  public void deleteWorkflow(WorkflowEngineRequest rq) {
    // throw new AppException(HttpStatus.NOT_IMPLEMENTED.value(), 
    //                       HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(),
    //                       "Deleting a running workflow is not supported");
  }

  @Override
  public void saveCustomOperator(final String customOperatorDefinition, final String fileName) {
    
  }

  @Override
  public TriggerWorkflowResponse triggerWorkflow(WorkflowEngineRequest rq,
      Map<String, Object> inputData) {

        String workflowName = rq.getWorkflowName();
        String runId = rq.getRunId();
        String workflowId = rq.getWorkflowId();

        TriggerWorkflowResponse resp = null;

        switch (awsConfig.airflowApiMode) {
          case AwsAirflowApiMode.HTTP:
            ClientResponse clientResp = triggerWorkflowUsingApi(runId, workflowId, workflowName, inputData);
            try {
              resp = objectMapper
              .readValue(clientResp.getEntity(String.class), TriggerWorkflowResponse.class);            
            }
            catch (Exception e) {
              throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Failure parsing Airflow response");
            }
            break;
          case AwsAirflowApiMode.SQS:
            triggerWorkflowUsingSqs(runId, workflowId, workflowName, inputData);
            resp = TriggerWorkflowResponse.builder()
                                  .runId(runId)
                                  .executionDate("UNKNOWN")
                                  .message("Triggered via SQS")
                                  .build();
            break;
          default:
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                                   HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), 
                                   "Unsupported Airflow API mode set. cannot execute workflow");
        }        
        

        return resp;
   
  }

  private void triggerWorkflowUsingSqs(final String runId, final String workflowId,
  String workflowName, final Map<String, Object> inputData) {

    String serializedData = workflowRequestBodyFactory.getSerializedWorkflowRequest(inputData,
        workflowName, runId, headers, true);
    
    sqsClient.sendMessageToWorkflowQueue(serializedData);

  }

  private ClientResponse triggerWorkflowUsingApi(final String runId, final String workflowId,
      String workflowName, final Map<String, Object> inputData) {
    String triggerDAGEndpoint = String.format("api/experimental/dags/%s/dag_runs", workflowName);

    JSONObject requestBody = new JSONObject();
    requestBody.put(AirflowConstants.RUN_ID_PARAMETER_NAME, runId);
    requestBody.put(AirflowConstants.AIRFLOW_PAYLOAD_PARAMETER_NAME, inputData);
    requestBody.put(AirflowConstants.AIRFLOW_MICROSECONDS_FLAG, "false");

    return callAirflowApi(triggerDAGEndpoint, HttpMethod.POST, requestBody.toString(),
        String.format(AirflowConstants.AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE, workflowId, workflowName));
  }

  private ClientResponse callAirflowApi(String apiEndpoint, String method, Object body,
      String errorMessage) {
    String url = String.format("%s/%s", airflowConfig.getUrl(), apiEndpoint);
    LOGGER.info("Calling airflow endpoint {} with method {}", url, method);

    WebResource webResource = restClient.resource(url);
    ClientResponse response = webResource
        .type(MediaType.APPLICATION_JSON)
        .header("Authorization", "Basic " + airflowConfig.getAppKey())
        .method(method, ClientResponse.class, body);

    final int status = response.getStatus();
    LOGGER.info("Received response status: {}.", status);

    if (status != 200) {
      String responseBody = response.getEntity(String.class);
      throw new AppException(status, responseBody, errorMessage);
    }
    return response;
  }

  @Override
  public WorkflowStatusType getWorkflowRunStatus(WorkflowEngineRequest rq) {
    
    //we don't have this visibility on SQS yet.  Need to work on it. 
    return null;
  }
}