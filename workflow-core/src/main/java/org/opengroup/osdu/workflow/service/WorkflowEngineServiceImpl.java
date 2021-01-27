package org.opengroup.osdu.workflow.service;

import java.util.Map;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusService;

import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowEngineServiceImpl implements IWorkflowEngineService {
  private static final String AIRFLOW_PAYLOAD_PARAMETER_NAME = "conf";
  private static final String TRIGGER_AIRFLOW_ENDPOINT = "api/experimental/dags/%s/dag_runs";

  private final Client restClient;
  private final AirflowConfig airflowConfig;
  private final IWorkflowStatusService workflowStatusRepository;

  @Override
  public void createWorkflow(final Map<String, Object> registrationInstruction, String workflowName) {}

  @Override
  public void deleteWorkflow(String workflowName) {}

  @Override
  public void saveCustomOperator(String customOperatorDefinition, String fileName) {}

  @Override
  public void triggerWorkflow(String runId, String workflowId, String workflowName,
      Map<String, Object> inputData, long executionTimeStamp) {
    log.info("Submitting ingestion with Airflow with dagName: {}", workflowName);
    String triggerDAGEndpoint = String.format(TRIGGER_AIRFLOW_ENDPOINT, workflowName);

    JSONObject requestBody = new JSONObject();
    requestBody.put(AIRFLOW_PAYLOAD_PARAMETER_NAME, inputData);
    ClientResponse response =
        callAirflowApi(triggerDAGEndpoint, HttpMethod.POST, requestBody.toString(),
            String.format("Failed to trigger workflow with id %s and name %s", workflowId, workflowName));
    workflowStatusRepository.saveWorkflowStatus(response,
        workflowId, workflowName, runId);
  }

  @Override
  public WorkflowStatusType getWorkflowRunStatus(String workflowName, long executionTimeStamp) {
    log.info("getting status of WorkflowRun of Workflow {} executed on {}", workflowName,
        executionTimeStamp);
    return null;
  }

  private ClientResponse callAirflowApi(String apiEndpoint, String method, Object body,
      String errorMessage) {
    String url = String.format("%s/%s", airflowConfig.getUrl(), apiEndpoint);
    log.info("Calling airflow endpoint {} with method {}", url, method);

    WebResource webResource = restClient.resource(url);
    ClientResponse response = webResource
        .type(MediaType.APPLICATION_JSON)
        .header("Authorization", "Basic " + airflowConfig.getAppKey())
        .method(method, ClientResponse.class, body);

    final int status = response.getStatus();
    log.info("Received response status: {}.", status);

    if (status != 200) {
      String responseBody = response.getEntity(String.class);
      throw new AppException(status, responseBody, errorMessage);
    }
    return response;
  }
}

