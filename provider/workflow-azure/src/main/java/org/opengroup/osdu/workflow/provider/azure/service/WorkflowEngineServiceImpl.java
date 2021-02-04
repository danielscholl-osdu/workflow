package org.opengroup.osdu.workflow.provider.azure.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONObject;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.model.AirflowGetDAGRunStatus;
import org.opengroup.osdu.workflow.model.TriggerWorkflowResponse;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.azure.fileshare.FileShareStore;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.service.AirflowWorkflowEngineServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
@Primary
public class WorkflowEngineServiceImpl implements IWorkflowEngineService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SubmitIngestServiceImpl.class);
  private static final String AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE =
      "Failed to trigger workflow with id %s and name %s";
  private static final String AIRFLOW_DELETE_DAG_ERROR_MESSAGE =
      "Failed to delete workflow with name %s";
  private static final String AIRFLOW_WORKFLOW_RUN_NOT_FOUND =
      "No WorkflowRun executed for Workflow: %s on %s ";
  private final static String AIRFLOW_PAYLOAD_PARAMETER_NAME = "conf";
  private final static String RUN_ID_PARAMETER_NAME = "run_id";
  private final static String EXECUTION_DATE_PARAMETER_NAME = "execution_date";
  private final static String EXECUTION_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
  private static final String FILE_NAME_PREFIX = ".py";
  private final static String AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_TRIGGER_CONFIGURATION = "_trigger_config";
  private final static String AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_WORKFLOW_ID = "trigger_dag_id";
  private final static String AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_WORKFLOW_RUN_ID = "trigger_dag_run_id";
  private final static String AIRFLOW_MICROSECONDS_FLAG = "replace_microseconds";
  private static final String KEY_WORKFLOW_DETAIL_CONTENT = "workflowDetailContent";

  @Autowired
  private AirflowConfig airflowConfig;
  @Autowired
  private Client restClient;
  @Autowired
  @Qualifier("dags")
  private FileShareStore dagsFileShareStore;
  @Autowired
  @Qualifier("customOperators")
  private FileShareStore customOperatorsFileShareStore;

  @Override
  public void createWorkflow(
      final WorkflowEngineRequest rq, final Map<String, Object> registrationInstruction) {
    String workflowDetailContent = (String) registrationInstruction.get(KEY_WORKFLOW_DETAIL_CONTENT);
    dagsFileShareStore.createFile(workflowDetailContent, getFileNameFromWorkflow(rq.getWorkflowName()));
  }

  @Override
  public void deleteWorkflow(WorkflowEngineRequest rq) {
    String workflowName = rq.getWorkflowName();
    LOGGER.info("Deleting DAG {} in Airflow", workflowName);

    try {
      String deleteDAGEndpoint = String.format("api/experimental/dags/%s", workflowName);
      callAirflowApi(deleteDAGEndpoint, HttpMethod.DELETE, null,
          String.format(AIRFLOW_DELETE_DAG_ERROR_MESSAGE, workflowName));
    } catch (AppException e) {
      if (e.getError().getCode() != 404) {
        throw e;
      }
    }

    String fileName = getFileNameFromWorkflow(workflowName);
    LOGGER.info("Deleting DAG file {} from file share", fileName);
    dagsFileShareStore.deleteFile(fileName);
  }

  @Override
  public void saveCustomOperator(final String customOperatorDefinition, final String fileName) {
    customOperatorsFileShareStore.createFile(customOperatorDefinition, fileName);
  }

  private ClientResponse triggerWorkflowBase(final String runId, final String workflowId,
      String workflowName, final Map<String, Object> inputData) {
    String triggerDAGEndpoint = String.format("api/experimental/dags/%s/dag_runs", workflowName);

    JSONObject requestBody = new JSONObject();
    requestBody.put(RUN_ID_PARAMETER_NAME, runId);
    requestBody.put(AIRFLOW_PAYLOAD_PARAMETER_NAME, inputData);
    requestBody.put(AIRFLOW_MICROSECONDS_FLAG, "false");

    return callAirflowApi(triggerDAGEndpoint, HttpMethod.POST, requestBody.toString(),
        String.format(AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE, workflowId, workflowName));
  }

  private ClientResponse triggerWorkflowUsingController(final String runId, final String workflowId,
      String workflowName, Map<String, Object> inputData) {
    String triggerDAGEndpoint = String
        .format("api/experimental/dags/%s/dag_runs", airflowConfig.getControllerDagId());

    JSONObject requestBody = new JSONObject();
    String parentRunId = "PARENT_" + runId;
    requestBody.put(RUN_ID_PARAMETER_NAME, parentRunId);

    Map<String, String> triggerParams = new HashMap<>();
    triggerParams.put(AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_WORKFLOW_ID, workflowName);
    triggerParams.put(AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_WORKFLOW_RUN_ID, runId);

    inputData.put(AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_TRIGGER_CONFIGURATION, triggerParams);
    requestBody.put(AIRFLOW_PAYLOAD_PARAMETER_NAME, inputData);
    requestBody.put(AIRFLOW_MICROSECONDS_FLAG, "false");

    return callAirflowApi(triggerDAGEndpoint, HttpMethod.POST, requestBody.toString(),
        String.format(AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE, workflowId, workflowName));
  }

  @Override
  public TriggerWorkflowResponse triggerWorkflow(WorkflowEngineRequest rq,
      Map<String, Object> inputData) {
    String workflowName = rq.getWorkflowName();
    String runId = rq.getRunId();
    String workflowId = rq.getWorkflowId();
    LOGGER.info("Submitting ingestion with Airflow with dagName: {}", workflowName);
    ClientResponse response = null;
    if (airflowConfig.isDagRunAbstractionEnabled()) {
      response = triggerWorkflowUsingController(runId, workflowId, workflowName, inputData);
    } else {
      response = triggerWorkflowBase(runId, workflowId, workflowName, inputData);
    }

    try {
      final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
      final TriggerWorkflowResponse triggerWorkflowResponse = OBJECT_MAPPER
          .readValue(response.getEntity(String.class), TriggerWorkflowResponse.class);
      LOGGER.info("Airflow response: {}.", triggerWorkflowResponse);
      return triggerWorkflowResponse;
    } catch (JsonProcessingException e) {
      final String error = "Unable to Process(Parse, Generate) JSON value";
      throw new AppException(500, error, e.getMessage());
    }
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
    String workflowName = rq.getWorkflowName();
    String executionDate = rq.getWorkflowEngineExecutionDate();
    LOGGER.info("getting status of WorkflowRun of Workflow {} executed on {}", workflowName,
        executionDate);
    String getDAGRunStatusEndpoint = String.format("api/experimental/dags/%s/dag_runs/%s",
        workflowName, executionDate);
    ClientResponse response = callAirflowApi(getDAGRunStatusEndpoint, HttpMethod.GET, null,
        String.format(AIRFLOW_WORKFLOW_RUN_NOT_FOUND, workflowName, executionDate));
    try {
      final ObjectMapper objectMapper = new ObjectMapper();
      final AirflowGetDAGRunStatus airflowResponse =
          objectMapper.readValue(response.getEntity(String.class),
              AirflowGetDAGRunStatus.class);
      return airflowResponse.getStatusType();
    } catch (JsonProcessingException e) {
      String errorMessage = String.format("Unable to Process Json Received. " + e.getMessage());
      LOGGER.error(errorMessage + e.getStackTrace());
      throw new AppException(500, "Failed to Get Status from Airflow", errorMessage);
    }
  }

  private String getExecutionDateInFormat(final Long executionTimeStamp, final String format) {
    Instant instant = Instant.ofEpochMilli(executionTimeStamp);
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    return zonedDateTime.format(DateTimeFormatter.ofPattern(format));
  }

  private String getFileNameFromWorkflow(String workflowName) {
    return workflowName + FILE_NAME_PREFIX;
  }
}
