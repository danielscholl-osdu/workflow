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
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.provider.azure.fileshare.FileShareStore;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
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
  public void createWorkflow(final WorkflowEngineRequest rq, final Map<String, Object> registrationInstructions) {
    dagsFileShareStore.createFile(dagDescription(registrationInstructions), getFileNameFromWorkflow(rq.getWorkflowName()));
  }

  private String dagDescription(Map<String, Object> instructions) {
    return (String) instructions.get("description");
  }

  @Override
  public void deleteWorkflow(WorkflowEngineRequest rq) {
    LOGGER.info("Deleting DAG {} in Airflow", rq.getWorkflowName());

    try {
      String deleteDAGEndpoint = String.format("api/experimental/dags/%s", rq.getWorkflowName());
      callAirflowApi(deleteDAGEndpoint, HttpMethod.DELETE, null,
          String.format(AIRFLOW_DELETE_DAG_ERROR_MESSAGE, rq.getWorkflowName()));
    } catch (AppException e) {
      if(e.getError().getCode() != 404) {
        throw e;
      }
    }

    String fileName = getFileNameFromWorkflow(rq.getWorkflowName());
    LOGGER.info("Deleting DAG file {} from file share", fileName);
    dagsFileShareStore.deleteFile(fileName);
  }

  @Override
  public void saveCustomOperator(final String customOperatorDefinition, final String fileName) {
    customOperatorsFileShareStore.createFile(customOperatorDefinition, fileName);
  }

  @Override
  public void triggerWorkflow(final WorkflowEngineRequest rq, final Map<String, Object> context) {
    LOGGER.info("Submitting ingestion with Airflow with dagName: {}", rq.getWorkflowName());

    String triggerDAGEndpoint = String.format("api/experimental/dags/%s/dag_runs", rq.getWorkflowName());

    JSONObject requestBody = new JSONObject();
    requestBody.put(RUN_ID_PARAMETER_NAME, rq.getRunId());
    requestBody.put(AIRFLOW_PAYLOAD_PARAMETER_NAME, context);
    requestBody.put(EXECUTION_DATE_PARAMETER_NAME, getExecutionDateInFormat(rq.getExecutionTimeStamp(),
        EXECUTION_DATE_FORMAT));

    callAirflowApi(triggerDAGEndpoint, HttpMethod.POST, requestBody.toString(),
        String.format(AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE, rq.getWorkflowId(), rq.getWorkflowName()));
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
    LOGGER.info("getting status of WorkflowRun of Workflow {} executed on {}", rq.getWorkflowName(),
        rq.getExecutionTimeStamp());
    final String executionDate = getExecutionDateInFormat(rq.getExecutionTimeStamp(), EXECUTION_DATE_FORMAT);
    String getDAGRunStatusEndpoint = String.format("api/experimental/dags/%s/dag_runs/%s",
        rq.getWorkflowName(), executionDate);
    ClientResponse response = callAirflowApi(getDAGRunStatusEndpoint, HttpMethod.GET, null,
        String.format(AIRFLOW_WORKFLOW_RUN_NOT_FOUND, rq.getWorkflowName(), executionDate));
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

  private String getExecutionDateInFormat(final Long executionTimeStamp, final String format){
    Instant instant = Instant.ofEpochMilli(executionTimeStamp);
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    return zonedDateTime.format(DateTimeFormatter.ofPattern(format));
  }

  private String getFileNameFromWorkflow(String workflowName) {
    return workflowName + FILE_NAME_PREFIX;
  }
}
