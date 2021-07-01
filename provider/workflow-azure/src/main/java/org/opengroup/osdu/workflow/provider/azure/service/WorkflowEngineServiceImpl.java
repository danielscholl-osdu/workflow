package org.opengroup.osdu.workflow.provider.azure.service;

import com.azure.storage.file.share.models.ShareStorageException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONObject;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.model.AirflowGetDAGRunStatus;
import org.opengroup.osdu.workflow.model.TriggerWorkflowResponse;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.azure.config.AirflowConfigResolver;
import org.opengroup.osdu.workflow.provider.azure.config.AzureWorkflowEngineConfig;
import org.opengroup.osdu.workflow.provider.azure.fileshare.FileShareConfig;
import org.opengroup.osdu.workflow.provider.azure.fileshare.FileShareStore;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowEngineServiceImpl.class);
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
  private static final String KEY_DAG_CONTENT = "dagContent";

  @Autowired
  private AirflowConfigResolver airflowConfigResolver;

  @Autowired
  private Client restClient;

  @Autowired
  private FileShareConfig fileShareConfig;

  @Autowired
  @Qualifier("IngestFileShareStore")
  private FileShareStore fileShareStore;

  @Autowired
  private DpsHeaders dpsHeaders;

  @Autowired
  private AzureWorkflowEngineConfig workflowEngineConfig;


  @Override
  public void createWorkflow(
      final WorkflowEngineRequest rq, final Map<String, Object> registrationInstruction) {
    String dagContent = (String) registrationInstruction.get(KEY_DAG_CONTENT);
    if(workflowEngineConfig.getIgnoreDagContent()) {
      LOGGER.info("Ignoring input DAG content: {}", dagContent);
      dagContent = "";
    }
    if(dagContent != null && !dagContent.isEmpty()) {
      fileShareStore.writeToFileShare(dpsHeaders.getPartitionId(), fileShareConfig.getShareName(),
          fileShareConfig.getDagsFolder(), getFileNameFromWorkflow(rq.getWorkflowName()),
          dagContent);
    }
  }

  @Override
  public void deleteWorkflow(WorkflowEngineRequest rq) {
    String workflowName = rq.getWorkflowName();
    LOGGER.info("Deleting DAG {} in Airflow", workflowName);

    if (rq.isDeployedThroughWorkflowService()) {
      // Deleting only if dag is deployed through workflow service.
      // Figure out how to only remove the metadata but not the DAG.
      // Because in repeated delete create fashion the dag will not appear for a while
      try {
        String deleteDAGEndpoint = String.format("api/experimental/dags/%s", workflowName);
        callAirflowApi(getAirflowConfig(false), deleteDAGEndpoint, HttpMethod.DELETE, null,
            String.format(AIRFLOW_DELETE_DAG_ERROR_MESSAGE, workflowName));
      } catch (AppException e) {
        if (e.getError().getCode() != 404) {
          throw e;
        }
      }

      String fileName = getFileNameFromWorkflow(workflowName);
      LOGGER.info("Deleting DAG file {} from file share", fileName);
      try {
        fileShareStore.deleteFromFileShare(dpsHeaders.getPartitionId(),
            fileShareConfig.getShareName(), fileShareConfig.getDagsFolder(), fileName);
      } catch (final ShareStorageException e) {
        if (e.getStatusCode() != 404) {
          throw e;
        }
      }
    }
  }

  @Override
  public void saveCustomOperator(final String customOperatorDefinition, final String fileName) {
    fileShareStore.writeToFileShare(dpsHeaders.getPartitionId(), fileShareConfig.getShareName(),
        fileShareConfig.getCustomOperatorsFolder(), fileName, customOperatorDefinition);
  }

  private ClientResponse triggerWorkflowBase(AirflowConfig airflowConfig, final String runId,
                                             final String workflowId, String workflowName,
                                             final Map<String, Object> inputData) {
    String triggerDAGEndpoint = String.format("api/experimental/dags/%s/dag_runs", workflowName);

    JSONObject requestBody = new JSONObject();
    requestBody.put(RUN_ID_PARAMETER_NAME, runId);
    requestBody.put(AIRFLOW_PAYLOAD_PARAMETER_NAME, inputData);
    requestBody.put(AIRFLOW_MICROSECONDS_FLAG, "false");

    return callAirflowApi(airflowConfig, triggerDAGEndpoint, HttpMethod.POST, requestBody.toString(),
        String.format(AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE, workflowId, workflowName));
  }

  private ClientResponse triggerWorkflowUsingController(AirflowConfig airflowConfig,
                                                        final String runId, final String workflowId,
                                                        String workflowName,
                                                        Map<String, Object> inputData) {
    String triggerDAGEndpoint = String
        .format("api/experimental/dags/%s/dag_runs", getAirflowConfig(false).getControllerDagId());

    JSONObject requestBody = new JSONObject();
    String parentRunId = "PARENT_" + runId;
    requestBody.put(RUN_ID_PARAMETER_NAME, parentRunId);

    Map<String, String> triggerParams = new HashMap<>();
    triggerParams.put(AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_WORKFLOW_ID, workflowName);
    triggerParams.put(AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_WORKFLOW_RUN_ID, runId);

    inputData.put(AIRFLOW_CONTROLLER_PAYLOAD_PARAMETER_TRIGGER_CONFIGURATION, triggerParams);
    requestBody.put(AIRFLOW_PAYLOAD_PARAMETER_NAME, inputData);
    requestBody.put(AIRFLOW_MICROSECONDS_FLAG, "false");

    return callAirflowApi(airflowConfig, triggerDAGEndpoint, HttpMethod.POST, requestBody.toString(),
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
    AirflowConfig airflowConfig = getAirflowConfig(false);
    if (airflowConfig.isDagRunAbstractionEnabled()) {
      response = triggerWorkflowUsingController(airflowConfig, runId, workflowId,
          workflowName, inputData);
    } else {
      response = triggerWorkflowBase(airflowConfig, runId, workflowId, workflowName, inputData);
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

  private ClientResponse callAirflowApi(AirflowConfig airflowConfig, String apiEndpoint,
                                        String method, Object body, String errorMessage) {
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
    ClientResponse response = callAirflowApi(getAirflowConfig(false),
        getDAGRunStatusEndpoint, HttpMethod.GET, null,
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

  private AirflowConfig getAirflowConfig(Boolean isSystemDAG) {
    if(isSystemDAG) {
      if(workflowEngineConfig.getIsDPAirflowUsedForSystemDAG()) {
        return airflowConfigResolver.getAirflowConfig(dpsHeaders.getPartitionId());
      } else {
        return airflowConfigResolver.getSystemAirflowConfig();
      }
    } else {
      return airflowConfigResolver.getAirflowConfig(dpsHeaders.getPartitionId());
    }
  }
}
