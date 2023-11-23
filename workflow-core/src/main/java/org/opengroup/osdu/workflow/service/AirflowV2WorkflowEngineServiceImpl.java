package org.opengroup.osdu.workflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.model.*;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

@Service
@Slf4j
@ConditionalOnProperty(name = "osdu.airflow.version2", havingValue = "true", matchIfMissing=false)
public class AirflowV2WorkflowEngineServiceImpl implements IWorkflowEngineService {

  private static final String RUN_ID_PARAMETER_NAME_STABLE = "dag_run_id";
  private static final String AIRFLOW_EXECUTION_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
  private static final String AIRFLOW_PAYLOAD_PARAMETER_NAME = "conf";
  private static final String EXECUTION_DATE_PARAMETER_NAME = "execution_date";
  private static final String TRIGGER_AIRFLOW_ENDPOINT_STABLE = "api/v1/dags/%s/dagRuns";
  private static final String AIRFLOW_RUN_ENDPOINT_STABLE = "api/v1/dags/%s/dagRuns/%s";
  private static final String AIRFLOW_VERSION_ENDPOINT = "api/v1/version";
  private static final String NOT_AVAILABLE = "N/A";
  public static final String VERSION = "version";
  private static final String KEY_USER_ID = "userId";
  private static final String KEY_EXECUTION_CONTEXT = "execution_context";

  private static final String AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE =
      "Failed to trigger workflow with id %s and name %s";
  private static final String AIRFLOW_WORKFLOW_RUN_NOT_FOUND =
      "No WorkflowRun executed for Workflow: %s on %s ";

  private final Client restClient;
	private final AirflowConfig airflowConfig;
	private final DpsHeaders dpsHeaders;


	public AirflowV2WorkflowEngineServiceImpl(Client restClient, AirflowConfig airflowConfig, DpsHeaders dpsHeaders){
		this.restClient = restClient;
		this.airflowConfig = airflowConfig;
		this.dpsHeaders = dpsHeaders;
	}

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
	public TriggerWorkflowResponse triggerWorkflow(WorkflowEngineRequest rq, Map<String, Object> context) {
		log.info("IBM : Submitting ingestion with Airflow 2 with dagName: {}", rq.getDagName());
		String url = "";
		addUserIdToExecutionContext(context, rq);
		final JSONObject requestBody = new JSONObject();
		requestBody.put(AIRFLOW_PAYLOAD_PARAMETER_NAME, context);
			url = format(TRIGGER_AIRFLOW_ENDPOINT_STABLE, rq.getDagName());
			requestBody.put(RUN_ID_PARAMETER_NAME_STABLE, rq.getRunId());

		final String errMsg = format(AIRFLOW_TRIGGER_DAG_ERROR_MESSAGE, rq.getWorkflowId(), rq.getWorkflowName());
		ClientResponse airflowRs = callAirflow(
				HttpMethod.POST,
				url,
				requestBody.toString(),
				rq,
				errMsg
				);
		try {
			ObjectMapper om = new ObjectMapper();
				String body = airflowRs.getResponseBody().toString();
				JsonNode jsonNode = om.readValue(body, JsonNode.class);
				String execution_date = "";
				String dag_run_id = "";
				if(jsonNode.has(EXECUTION_DATE_PARAMETER_NAME))
					execution_date = jsonNode.get(EXECUTION_DATE_PARAMETER_NAME).asText();
				if(jsonNode.has(RUN_ID_PARAMETER_NAME_STABLE))
					dag_run_id = jsonNode.get(RUN_ID_PARAMETER_NAME_STABLE).asText();

				return new TriggerWorkflowResponse(execution_date, "", dag_run_id );

		} catch (JsonProcessingException e) {
			log.info("Airflow response: {}.", airflowRs);
			final String error = "Unable to Process(Parse, Generate) JSON value";
			throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), error, e.getMessage());
		}
	}

	@Override
	public WorkflowStatusType getWorkflowRunStatus(WorkflowEngineRequest rq) {
		log.info("getting status of WorkflowRun of Workflow {} executed on {}", rq.getWorkflowName(),
				rq.getExecutionTimeStamp());
		final String executionDate = executionDate(rq.getExecutionTimeStamp());
		String url = format(AIRFLOW_RUN_ENDPOINT_STABLE, rq.getDagName(), rq.getRunId());
		final String errMsg = String.format(AIRFLOW_WORKFLOW_RUN_NOT_FOUND, rq.getWorkflowName(), executionDate);
		final ClientResponse response = callAirflow(
				HttpMethod.GET,
				url,
				null,
				rq,
				errMsg);
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			final AirflowGetDAGRunStatus airflowResponse =
					objectMapper.readValue(response.getResponseBody().toString(),
							AirflowGetDAGRunStatus.class);
			return airflowResponse.getStatusType();
		} catch (JsonProcessingException e) {
			final String errorMessage = format("Unable to Process Json Received. %s", e.getMessage());
			log.error(errorMessage, e);
			throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to Get Status from Airflow", errorMessage);
		}
	}

  public String getAirflowVersion() {
    ClientResponse clientResponse =
        callAirflow(
            HttpMethod.GET,
            AIRFLOW_VERSION_ENDPOINT,
            null,
            null,
            null);
    try {
      ObjectMapper om = new ObjectMapper();
      String body = clientResponse.getResponseBody().toString();
      JsonNode jsonNode = om.readValue(body, JsonNode.class);
      if (jsonNode.has(VERSION)) {
        return jsonNode.get(VERSION).asText();
      } else {
        log.error("Unable to locate version in Airflow response. Airflow response: {}.",
            clientResponse);
        return NOT_AVAILABLE;
      }
    } catch (JsonProcessingException e) {
      log.error("Unable to Process(Parse, Generate) JSON value. Airflow response: {}.",
          clientResponse);
      return NOT_AVAILABLE;
    }
  }

	protected ClientResponse callAirflow(String httpMethod, String apiEndpoint, String body,
			WorkflowEngineRequest rq, String errorMessage) {
		String url = format("%s/%s", airflowConfig.getUrl(), apiEndpoint);
		log.info("Calling airflow 2 endpoint {} with method {}", url, httpMethod);

		WebResource webResource = restClient.resource(url);
		com.sun.jersey.api.client.ClientResponse response = webResource
				.type(MediaType.APPLICATION_JSON)
				.header("Authorization", "Basic " + airflowConfig.getAppKey())
				.method(httpMethod, com.sun.jersey.api.client.ClientResponse.class, body);

		final int status = response.getStatus();
		log.info("Received response status: {}.", status);

		if (status != HttpStatus.OK.value()) {
			String responseBody = response.getEntity(String.class);
			throw new AppException(status, responseBody, errorMessage);
		}

		return ClientResponse.builder()
				.contentType(String.valueOf(response.getType()))
				.responseBody(response.getEntity(String.class))
				.status(HttpStatus.OK)
				.statusCode(response.getStatus())
				.statusMessage(response.getStatusInfo().getReasonPhrase())
				.build();
	}

	protected String executionDate(final Long executionTimeStamp){
		Instant instant = Instant.ofEpochMilli(executionTimeStamp);
		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
		return zonedDateTime.format(DateTimeFormatter.ofPattern(AIRFLOW_EXECUTION_DATE_FORMAT));
	}

  protected void addUserIdToExecutionContext(
      Map<String, Object> inputData, WorkflowEngineRequest rq) {
    if (Objects.isNull(inputData)) {
      throw new AppException(
          HttpStatus.BAD_REQUEST.value(),
          "Failed to trigger workflow run",
          "data is null or empty");
    }
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> executionContext =
        objectMapper.convertValue(inputData.get(KEY_EXECUTION_CONTEXT), Map.class);
    if (Objects.isNull(executionContext)) {
      throw new AppException(
          HttpStatus.BAD_REQUEST.value(),
          "Failed to trigger workflow run",
          "execution_context is null or empty");
    }
    if (executionContext.containsKey(KEY_USER_ID)) {
      String errorMessage =
          String.format(
              "Request to trigger workflow with name %s failed because execution context contains reserved key 'userId'",
              rq.getWorkflowName());
      throw new AppException(400, "Failed to trigger workflow run", errorMessage);
    }
    log.debug(
        String.format("putting user email: %s in execution context", dpsHeaders.getUserEmail()));
    executionContext.put(KEY_USER_ID, dpsHeaders.getUserEmail());
    inputData.put(KEY_EXECUTION_CONTEXT, executionContext);
  }
}

