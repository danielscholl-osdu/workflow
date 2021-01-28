package org.opengroup.osdu.workflow.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.HttpMethod;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.model.AirflowGetDAGRunStatus;
import org.opengroup.osdu.workflow.model.ClientResponse;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.IAuthenticationService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;

import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class AirflowWorkflowEngineServiceImpl implements IWorkflowEngineService {
  private static final String RUN_ID_PARAMETER_NAME = "run_id";
  private static final String AIRFLOW_EXECUTION_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
  private static final String AIRFLOW_PAYLOAD_PARAMETER_NAME = "conf";
  private static final String EXECUTION_DATE_PARAMETER_NAME = "execution_date";
  private static final String TRIGGER_AIRFLOW_ENDPOINT = "api/experimental/dags/%s/dag_runs";
  private static final String AIRFLOW_RUN_ENDPOINT = "api/experimental/dags/%s/dag_runs/%s";

  private static final String AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE =
      "Failed to trigger workflow with id %s and name %s";
  private static final String AIRFLOW_WORKFLOW_RUN_NOT_FOUND =
      "No WorkflowRun executed for Workflow: %s on %s ";

  private final AirflowConfig airflowConfig;
  private final IAuthenticationService restClient;

  @Override
  public void createWorkflow(final WorkflowEngineRequest rq, final Map<String, Object> registrationInstruction) {
    // This is not relevant for a default implementation
  }

  @Override
  public void deleteWorkflow(final WorkflowEngineRequest rq) {
    // This is not relevant for a default implementation
  }

  @Override
  public void saveCustomOperator(String customOperatorDefinition, String fileName) {
    //
  }

  @Override
  public void triggerWorkflow(WorkflowEngineRequest rq, Map<String, Object> context) {
    log.info("Submitting ingestion with Airflow with dagName: {}", rq.getWorkflowName());
    final String url = format(TRIGGER_AIRFLOW_ENDPOINT, rq.getWorkflowName());
    final JSONObject requestBody = new JSONObject();
    requestBody.put(RUN_ID_PARAMETER_NAME, rq.getRunId());
    requestBody.put(AIRFLOW_PAYLOAD_PARAMETER_NAME, context);
    requestBody.put(EXECUTION_DATE_PARAMETER_NAME, executionDate(rq.getExecutionTimeStamp()));
    final String errMsg = format(AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE, rq.getWorkflowId(), rq.getWorkflowName());
    callAirflow(
        HttpMethod.POST,
        url,
        requestBody.toString(),
        rq,
        errMsg
    );
  }

  @Override
  public WorkflowStatusType getWorkflowRunStatus(WorkflowEngineRequest rq) {
    log.info("getting status of WorkflowRun of Workflow {} executed on {}", rq.getWorkflowName(),
        rq.getExecutionTimeStamp());
    final String executionDate = executionDate(rq.getExecutionTimeStamp());
    final String url = format(AIRFLOW_RUN_ENDPOINT, rq.getWorkflowName(), executionDate);
    final String errMsg = String.format(AIRFLOW_WORKFLOW_RUN_NOT_FOUND, rq.getWorkflowName(), executionDate);
    final ClientResponse response = callAirflow(
        HttpMethod.GET,
        url,
        null,
        rq,
        errMsg);
    try {
      final ObjectMapper objectMapper = new ObjectMapper();
      final AirflowGetDAGRunStatus airflowResponse =
          objectMapper.readValue(response.getResponseBody().toString(),
              AirflowGetDAGRunStatus.class);
      return airflowResponse.getStatusType();
    } catch (JsonProcessingException e) {
      final String errorMessage = format("Unable to Process Json Received. %s", e.getMessage());
      log.error(errorMessage, e);
      throw new AppException(500, "Failed to Get Status from Airflow", errorMessage);
    }
  }

  private ClientResponse callAirflow(String httpMethod, String apiEndpoint, String body,
      WorkflowEngineRequest rq, String errorMessage) {
    String url = format("%s/%s", airflowConfig.getUrl(), apiEndpoint);
    log.info("Calling airflow endpoint {} with method {}", url, httpMethod);

    ClientResponse response = restClient.sendAirflowRequest(httpMethod, url, body, rq);
    int status = response.getStatusCode();
    log.info("Received response status: {}.", status);
    if (status != 200) {
      throw new AppException(status, (String) response.getResponseBody(), errorMessage);
    }
    return response;
  }

  private String executionDate(final Long executionTimeStamp){
    Instant instant = Instant.ofEpochMilli(executionTimeStamp);
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    return zonedDateTime.format(DateTimeFormatter.ofPattern(AIRFLOW_EXECUTION_DATE_FORMAT));
  }
}

