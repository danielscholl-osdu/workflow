package org.opengroup.osdu.workflow.provider.azure.service;

import com.azure.storage.file.share.models.ShareStorageException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.azure.fileshare.FileShareConfig;
import org.opengroup.osdu.workflow.provider.azure.config.AirflowConfigResolver;
import org.opengroup.osdu.workflow.provider.azure.config.AzureWorkflowEngineConfig;
import org.opengroup.osdu.workflow.provider.azure.fileshare.FileShareStore;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WorkflowEngineServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
public class WorkflowEngineServiceImplTest {
  private static final String RUN_ID = "4f65d8d2-e40b-4e76-a290-12e2c6fee033";
  private static final String WORKFLOW_NAME = "HelloWorld";
  private static final String WORKFLOW_ID = "SGVsbG9Xb3JsZA==";
  private static final String AIRFLOW_URL = "https://airflow.com/airlfow";
  private static final String AIRFLOW_APP_KEY = "appKey";
  private static final String AIRFLOW_DAG_TRIGGER_URL =
      "https://airflow.com/airlfow/api/experimental/dags/HelloWorld/dag_runs";
  private static final String AIRFLOW_DAG_GET_STATUS_URL =
      "https://airflow.com/airlfow/api/experimental/dags/HelloWorld/dag_runs/2021-01-05T11:36:45+00:00";
  private static final String AIRFLOW_DAG_URL = "https://airflow.com/airlfow/api/experimental/dags/HelloWorld";
  private static final String AIRFLOW_CONTROLLER_DAG_TRIGGER_URL =
      "https://airflow.com/airlfow/api/experimental/dags/controller/dag_runs";
  private static final String CONTROLLER_DAG_ID = "controller";
  private static final String HEADER_AUTHORIZATION_NAME = "Authorization";
  private static final String HEADER_AUTHORIZATION_VALUE = "Basic " + AIRFLOW_APP_KEY;
  private static final int SUCCESS_STATUS_CODE = 200;
  private static final int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;
  private static final String AIRFLOW_INPUT = "{\n" +
      "  \"run_id\": \"4f65d8d2-e40b-4e76-a290-12e2c6fee033\",\n" +
      "  \"conf\": {\n" +
      "    \"Hello\": \"World\"\n" +
      "  },\n" +
      "  \"replace_microseconds\":\"false\"" +
      "}";

  private static final String AIRFLOW_CONTROLLER_DAG_INPUT = "{\n" +
      "  \"run_id\": \"PARENT_4f65d8d2-e40b-4e76-a290-12e2c6fee033\",\n" +
      "  \"conf\": {\n" +
      "    \"Hello\": \"World\"\n," +
      "    \"_trigger_config\": {\n" +
      "       \"trigger_dag_id\": \"HelloWorld\"\n," +
      "       \"trigger_dag_run_id\": \"4f65d8d2-e40b-4e76-a290-12e2c6fee033\"\n" +
      "     },\n" +
      "  },\n" +
      "  \"replace_microseconds\":\"false\"" +
      "}";

  private static final String WORKFLOW_TRIGGER_RESPONSE = "{\n" +
      "  \"execution_date\": \"2021-01-05T11:36:45+00:00\",\n" +
      "  \"message\": \"Created <DagRun HelloWorld @ 2021-01-05 11:36:45+00:00: d13f7fd0-d27e-4176-8d60-6e9aad86e347, externally triggered: True>\",\n" +
      "  \"run_id\": \"d13f7fd0-d27e-4176-8d60-6e9aad86e347\"\n" +
      "}";

  private static final String WORKFLOW_DEFINITION = "Hello World";
  private static final String AIRFLOW_GET_STATUS_RESPONSE = "{\"state\":\"success\"}" ;
  private static final String CUSTOM_OPERATOR_DEFINITION = "This is a sample content";
  private static final Long EXECUTION_TIMESTAMP = 1609932804071L;
  private static final String EXECUTION_DATE = "2021-01-05T11:36:45+00:00";
  private static final String TEST_PARTITION = "test-partition";
  private static final String FILE_SHARE_NAME = "fileShare";
  private static final String FILE_SHARE_DAGS_FOLDER = "dagsFolder";
  private static final String FILE_SHARE_CUSTOM_OPERATORS_FOLDER = "customOperatorsFolder";
  private static final String FILE_NAME = WORKFLOW_NAME + ".py";
  private static final String FILE_CONTENT = "content";

  @Mock
  private FileShareStore fileShareStore;

  @Mock
  private FileShareConfig fileShareConfig;

  @Mock
  private AirflowConfig airflowConfig;

  @Mock
  private AzureWorkflowEngineConfig workflowEngineConfig;

  @Mock
  private AirflowConfigResolver airflowConfigResolver;

  @Mock
  private DpsHeaders dpsHeaders;

  @Mock
  private Client restClient;

  @Mock
  private WebResource webResource;

  @Mock
  private WebResource.Builder webResourceBuilder;

  @Mock
  private ClientResponse clientResponse;

  @InjectMocks
  private WorkflowEngineServiceImpl workflowEngineService;

  @Test
  public void testCreateWorkflowWithDagContent() {
    when(workflowEngineConfig.getIgnoreDagContent()).thenReturn(false);
    doReturn(TEST_PARTITION).when(dpsHeaders).getPartitionId();
    doReturn(FILE_SHARE_NAME).when(fileShareConfig).getShareName();
    doReturn(FILE_SHARE_DAGS_FOLDER).when(fileShareConfig).getDagsFolder();
    doNothing().when(fileShareStore).writeToFileShare(eq(TEST_PARTITION), eq(FILE_SHARE_NAME), eq(FILE_SHARE_DAGS_FOLDER), eq(FILE_NAME), eq(WORKFLOW_DEFINITION));

    workflowEngineService.createWorkflow(workflowEngineRequest(null, true),
        registrationInstructions(WORKFLOW_DEFINITION));
    verify(fileShareStore, times(1)).writeToFileShare(eq(TEST_PARTITION), eq(FILE_SHARE_NAME), eq(FILE_SHARE_DAGS_FOLDER), eq(FILE_NAME), eq(WORKFLOW_DEFINITION));
  }

  @Test
  public void testCreateWorkflowWithDagContentIgnored() {
    when(workflowEngineConfig.getIgnoreDagContent()).thenReturn(true);
    workflowEngineService.createWorkflow(workflowEngineRequest(null, true),
            registrationInstructions(WORKFLOW_DEFINITION));
    verify(fileShareStore, times(0)).writeToFileShare(any(), any(), any(), any(), any());
  }

  @Test
  public void testCreateWorkflowWithNullDagContent() {
    when(workflowEngineConfig.getIgnoreDagContent()).thenReturn(false);
    workflowEngineService.createWorkflow(workflowEngineRequest(null, false),
        registrationInstructions(null));
    verify(fileShareStore, times(0)).writeToFileShare(any(), any(), any(), any(), any());
  }

  @Test
  public void testCreateWorkflowWithEmptyDagContent() {
    when(workflowEngineConfig.getIgnoreDagContent()).thenReturn(false);
    workflowEngineService.createWorkflow(workflowEngineRequest(null, false),
        registrationInstructions(""));
    verify(fileShareStore, times(0)).writeToFileShare(any(), any(), any(), any(), any());
  }

  @Test
  public void testStoreCustomOperator() {
    doReturn(TEST_PARTITION).when(dpsHeaders).getPartitionId();
    doReturn(FILE_SHARE_NAME).when(fileShareConfig).getShareName();
    doReturn(FILE_SHARE_CUSTOM_OPERATORS_FOLDER).when(fileShareConfig).getCustomOperatorsFolder();
    doNothing().when(fileShareStore).writeToFileShare(eq(TEST_PARTITION), eq(FILE_SHARE_NAME), eq(FILE_SHARE_CUSTOM_OPERATORS_FOLDER), eq(FILE_NAME), eq(CUSTOM_OPERATOR_DEFINITION));

    workflowEngineService.saveCustomOperator(CUSTOM_OPERATOR_DEFINITION, FILE_NAME);
    verify(fileShareStore, times(1)).writeToFileShare(eq(TEST_PARTITION), eq(FILE_SHARE_NAME), eq(FILE_SHARE_CUSTOM_OPERATORS_FOLDER), eq(FILE_NAME), eq(CUSTOM_OPERATOR_DEFINITION));
  }

  @Test
  public void testTriggerWorkflowWithSuccessExecution() {
    Map<String, Object> INPUT_DATA = new HashMap<>();
    INPUT_DATA.put("Hello","World");
    final ArgumentCaptor<String> airflowInputCaptor = ArgumentCaptor.forClass(String.class);
    when(dpsHeaders.getPartitionId()).thenReturn(TEST_PARTITION);
    when(airflowConfigResolver.getAirflowConfig(TEST_PARTITION)).thenReturn(airflowConfig);
    when(airflowConfig.getUrl()).thenReturn(AIRFLOW_URL);
    when(airflowConfig.getAppKey()).thenReturn(AIRFLOW_APP_KEY);
    when(airflowConfig.isDagRunAbstractionEnabled()).thenReturn(false);
    when(restClient.resource(eq(AIRFLOW_DAG_TRIGGER_URL))).thenReturn(webResource);
    when(webResource.type(eq(MediaType.APPLICATION_JSON))).thenReturn(webResourceBuilder);
    when(webResourceBuilder.header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE)))
        .thenReturn(webResourceBuilder);
    when(webResourceBuilder.method(eq("POST"), eq(ClientResponse.class),
        airflowInputCaptor.capture())).thenReturn(clientResponse);
    when(clientResponse.getStatus()).thenReturn(SUCCESS_STATUS_CODE);
    when(clientResponse.getEntity(String.class)).thenReturn(WORKFLOW_TRIGGER_RESPONSE);
    workflowEngineService.triggerWorkflow(workflowEngineRequest(null, false), INPUT_DATA);
    verify(restClient).resource(eq(AIRFLOW_DAG_TRIGGER_URL));
    verify(webResource).type(eq(MediaType.APPLICATION_JSON));
    verify(webResourceBuilder).header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE));
    verify(webResourceBuilder).method(eq("POST"), eq(ClientResponse.class), any(String.class));
    verify(clientResponse).getStatus();
    verify(clientResponse).getEntity(String.class);
    verify(airflowConfig).getUrl();
    verify(airflowConfig).getAppKey();
    verify(airflowConfig).isDagRunAbstractionEnabled();
    JSONAssert.assertEquals(AIRFLOW_INPUT, airflowInputCaptor.getValue(), true);
  }

  @Test
  public void testTriggerWorkflowWithExceptionFromAirflow() {
    Map<String, Object> INPUT_DATA = new HashMap<>();
    INPUT_DATA.put("Hello","World");
    final ArgumentCaptor<String> airflowInputCaptor = ArgumentCaptor.forClass(String.class);
    when(dpsHeaders.getPartitionId()).thenReturn(TEST_PARTITION);
    when(airflowConfigResolver.getAirflowConfig(TEST_PARTITION)).thenReturn(airflowConfig);
    when(airflowConfig.getUrl()).thenReturn(AIRFLOW_URL);
    when(airflowConfig.getAppKey()).thenReturn(AIRFLOW_APP_KEY);
    when(airflowConfig.isDagRunAbstractionEnabled()).thenReturn(false);
    when(restClient.resource(eq(AIRFLOW_DAG_TRIGGER_URL))).thenReturn(webResource);
    when(webResource.type(eq(MediaType.APPLICATION_JSON))).thenReturn(webResourceBuilder);
    when(webResourceBuilder.header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE)))
        .thenReturn(webResourceBuilder);
    when(webResourceBuilder.method(eq("POST"), eq(ClientResponse.class),
        airflowInputCaptor.capture())).thenReturn(clientResponse);
    when(clientResponse.getStatus()).thenReturn(INTERNAL_SERVER_ERROR_STATUS_CODE);
    Assertions.assertThrows(AppException.class, () -> {
      workflowEngineService.triggerWorkflow(workflowEngineRequest(null, false), INPUT_DATA);
    });
    verify(restClient).resource(eq(AIRFLOW_DAG_TRIGGER_URL));
    verify(webResource).type(eq(MediaType.APPLICATION_JSON));
    verify(webResourceBuilder).header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE));
    verify(webResourceBuilder).method(eq("POST"), eq(ClientResponse.class), any(String.class));
    verify(clientResponse).getStatus();
    verify(airflowConfig).getUrl();
    verify(airflowConfig).getAppKey();
    verify(airflowConfig).isDagRunAbstractionEnabled();
    JSONAssert.assertEquals(AIRFLOW_INPUT, airflowInputCaptor.getValue(), true);
  }

  @Test
  public void testTriggerWorkflowWithControllerDag() {
    Map<String, Object> INPUT_DATA = new HashMap<>();
    INPUT_DATA.put("Hello","World");
    final ArgumentCaptor<String> airflowInputCaptor = ArgumentCaptor.forClass(String.class);
    when(dpsHeaders.getPartitionId()).thenReturn(TEST_PARTITION);
    when(airflowConfigResolver.getAirflowConfig(TEST_PARTITION)).thenReturn(airflowConfig);
    when(airflowConfig.getUrl()).thenReturn(AIRFLOW_URL);
    when(airflowConfig.getAppKey()).thenReturn(AIRFLOW_APP_KEY);
    when(airflowConfig.isDagRunAbstractionEnabled()).thenReturn(true);
    when(airflowConfig.getControllerDagId()).thenReturn(CONTROLLER_DAG_ID);
    when(restClient.resource(eq(AIRFLOW_CONTROLLER_DAG_TRIGGER_URL))).thenReturn(webResource);
    when(webResource.type(eq(MediaType.APPLICATION_JSON))).thenReturn(webResourceBuilder);
    when(webResourceBuilder.header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE)))
        .thenReturn(webResourceBuilder);
    when(webResourceBuilder.method(eq("POST"), eq(ClientResponse.class), airflowInputCaptor.capture())).thenReturn(clientResponse);
    when(clientResponse.getStatus()).thenReturn(SUCCESS_STATUS_CODE);
    when(clientResponse.getEntity(String.class)).thenReturn(WORKFLOW_TRIGGER_RESPONSE);
    workflowEngineService.triggerWorkflow(workflowEngineRequest(null, false), INPUT_DATA);
    verify(restClient).resource(eq(AIRFLOW_CONTROLLER_DAG_TRIGGER_URL));
    verify(webResource).type(eq(MediaType.APPLICATION_JSON));
    verify(webResourceBuilder).header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE));
    verify(webResourceBuilder).method(eq("POST"), eq(ClientResponse.class), any(String.class));
    verify(clientResponse).getStatus();
    verify(clientResponse).getEntity(String.class);
    verify(airflowConfig).getUrl();
    verify(airflowConfig).getAppKey();
    verify(airflowConfig).isDagRunAbstractionEnabled();
    JSONAssert.assertEquals(AIRFLOW_CONTROLLER_DAG_INPUT, airflowInputCaptor.getValue(), true);
  }

  @Test
  public void testDeleteWorkflowWithSuccess() {
    doReturn(TEST_PARTITION).when(dpsHeaders).getPartitionId();
    doReturn(FILE_SHARE_NAME).when(fileShareConfig).getShareName();
    doReturn(FILE_SHARE_DAGS_FOLDER).when(fileShareConfig).getDagsFolder();
    doNothing().when(fileShareStore).deleteFromFileShare(eq(TEST_PARTITION), eq(FILE_SHARE_NAME), eq(FILE_SHARE_DAGS_FOLDER), eq(FILE_NAME));
    when(airflowConfigResolver.getAirflowConfig(TEST_PARTITION)).thenReturn(airflowConfig);
    when(airflowConfig.getUrl()).thenReturn(AIRFLOW_URL);
    when(airflowConfig.getAppKey()).thenReturn(AIRFLOW_APP_KEY);
    when(restClient.resource(eq(AIRFLOW_DAG_URL))).thenReturn(webResource);
    when(webResource.type(eq(MediaType.APPLICATION_JSON))).thenReturn(webResourceBuilder);
    when(webResourceBuilder.header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE)))
        .thenReturn(webResourceBuilder);
    when(webResourceBuilder.method(eq("DELETE"), eq(ClientResponse.class), eq(null)))
        .thenReturn(clientResponse);
    when(clientResponse.getStatus()).thenReturn(SUCCESS_STATUS_CODE);

    workflowEngineService.deleteWorkflow(workflowEngineRequest(null, true));

    verify(fileShareStore, times(1)).deleteFromFileShare(eq(TEST_PARTITION), eq(FILE_SHARE_NAME), eq(FILE_SHARE_DAGS_FOLDER), eq(FILE_NAME));
    verify(restClient).resource(eq(AIRFLOW_DAG_URL));
    verify(webResource).type(eq(MediaType.APPLICATION_JSON));
    verify(webResourceBuilder).header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE));
    verify(webResourceBuilder).method(eq("DELETE"), eq(ClientResponse.class), eq(null));
    verify(clientResponse).getStatus();
    verify(airflowConfig).getUrl();
    verify(airflowConfig).getAppKey();
  }

  @Test
  public void testDeleteWorkflowWithFailure() {
    when(dpsHeaders.getPartitionId()).thenReturn(TEST_PARTITION);
    when(airflowConfigResolver.getAirflowConfig(TEST_PARTITION)).thenReturn(airflowConfig);
    when(airflowConfig.getUrl()).thenReturn(AIRFLOW_URL);
    when(airflowConfig.getAppKey()).thenReturn(AIRFLOW_APP_KEY);
    when(restClient.resource(eq(AIRFLOW_DAG_URL))).thenReturn(webResource);
    when(webResource.type(eq(MediaType.APPLICATION_JSON))).thenReturn(webResourceBuilder);
    when(webResourceBuilder.header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE)))
        .thenReturn(webResourceBuilder);
    when(webResourceBuilder.method(eq("DELETE"), eq(ClientResponse.class), eq(null)))
        .thenReturn(clientResponse);
    when(clientResponse.getStatus()).thenReturn(INTERNAL_SERVER_ERROR_STATUS_CODE);

    Assertions.assertThrows(AppException.class, () -> {
      workflowEngineService.deleteWorkflow(workflowEngineRequest(null, true));
    });

    verify(fileShareStore, times(0)).deleteFromFileShare(any(), any(), any(), any());
    verify(restClient).resource(eq(AIRFLOW_DAG_URL));
    verify(webResource).type(eq(MediaType.APPLICATION_JSON));
    verify(webResourceBuilder).header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE));
    verify(webResourceBuilder).method(eq("DELETE"), eq(ClientResponse.class), eq(null));
    verify(clientResponse).getStatus();
    verify(airflowConfig).getUrl();
    verify(airflowConfig).getAppKey();
  }

  @Test
  public void testDeleteWorkflowShouldSucceedWhenFileShareReturningNotFound() {
    doReturn(TEST_PARTITION).when(dpsHeaders).getPartitionId();
    doReturn(FILE_SHARE_NAME).when(fileShareConfig).getShareName();
    doReturn(FILE_SHARE_DAGS_FOLDER).when(fileShareConfig).getDagsFolder();
    final ShareStorageException exception = mock(ShareStorageException.class);
    when(exception.getStatusCode()).thenReturn(404);
    doThrow(exception).when(fileShareStore).deleteFromFileShare(eq(TEST_PARTITION), eq(FILE_SHARE_NAME), eq(FILE_SHARE_DAGS_FOLDER), eq(FILE_NAME));
    when(airflowConfigResolver.getAirflowConfig(TEST_PARTITION)).thenReturn(airflowConfig);
    when(airflowConfig.getUrl()).thenReturn(AIRFLOW_URL);
    when(airflowConfig.getAppKey()).thenReturn(AIRFLOW_APP_KEY);
    when(restClient.resource(eq(AIRFLOW_DAG_URL))).thenReturn(webResource);
    when(webResource.type(eq(MediaType.APPLICATION_JSON))).thenReturn(webResourceBuilder);
    when(webResourceBuilder.header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE)))
        .thenReturn(webResourceBuilder);
    when(webResourceBuilder.method(eq("DELETE"), eq(ClientResponse.class), eq(null)))
        .thenReturn(clientResponse);
    when(clientResponse.getStatus()).thenReturn(SUCCESS_STATUS_CODE);

    workflowEngineService.deleteWorkflow(workflowEngineRequest(null, true));

    verify(fileShareStore).deleteFromFileShare(eq(TEST_PARTITION), eq(FILE_SHARE_NAME), eq(FILE_SHARE_DAGS_FOLDER), eq(FILE_NAME));
    verify(restClient).resource(eq(AIRFLOW_DAG_URL));
    verify(webResource).type(eq(MediaType.APPLICATION_JSON));
    verify(webResourceBuilder).header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE));
    verify(webResourceBuilder).method(eq("DELETE"), eq(ClientResponse.class), eq(null));
    verify(clientResponse).getStatus();
    verify(airflowConfig).getUrl();
    verify(airflowConfig).getAppKey();
  }

  @Test
  public void testDeleteWorkflowShouldShouldNotCallFileShareAndAirflowForDAGsNotDeployedThroughWorkflowService() {
//    when(airflowConfig.getUrl()).thenReturn(AIRFLOW_URL);
//    when(airflowConfig.getAppKey()).thenReturn(AIRFLOW_APP_KEY);
//    when(restClient.resource(eq(AIRFLOW_DAG_URL))).thenReturn(webResource);
//    when(webResource.type(eq(MediaType.APPLICATION_JSON))).thenReturn(webResourceBuilder);
//    when(webResourceBuilder.header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE)))
//        .thenReturn(webResourceBuilder);
//    when(webResourceBuilder.method(eq("DELETE"), eq(ClientResponse.class), eq(null)))
//        .thenReturn(clientResponse);
//    when(clientResponse.getStatus()).thenReturn(SUCCESS_STATUS_CODE);

    workflowEngineService.deleteWorkflow(workflowEngineRequest(null, false));

    verify(fileShareStore, times(0)).deleteFromFileShare(eq(TEST_PARTITION), eq(FILE_SHARE_NAME), eq(FILE_SHARE_DAGS_FOLDER), eq(FILE_NAME));
    verify(restClient, times(0)).resource(eq(AIRFLOW_DAG_URL));
    verify(webResource, times(0)).type(eq(MediaType.APPLICATION_JSON));
    verify(webResourceBuilder, times(0)).header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE));
    verify(webResourceBuilder, times(0)).method(eq("DELETE"), eq(ClientResponse.class), eq(null));
    verify(clientResponse, times(0)).getStatus();
    verify(airflowConfig, times(0)).getUrl();
    verify(airflowConfig, times(0)).getAppKey();
  }

  @Test
  public void testGetWorkflowRunStatusWithSuccess() {
    when(dpsHeaders.getPartitionId()).thenReturn(TEST_PARTITION);
    when(airflowConfigResolver.getAirflowConfig(TEST_PARTITION)).thenReturn(airflowConfig);
    when(airflowConfig.getUrl()).thenReturn(AIRFLOW_URL);
    when(airflowConfig.getAppKey()).thenReturn(AIRFLOW_APP_KEY);
    when(restClient.resource(eq(AIRFLOW_DAG_GET_STATUS_URL))).thenReturn(webResource);
    when(webResource.type(eq(MediaType.APPLICATION_JSON))).thenReturn(webResourceBuilder);
    when(webResourceBuilder.header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE)))
        .thenReturn(webResourceBuilder);
    when(webResourceBuilder.method(eq("GET"), eq(ClientResponse.class),
        any())).thenReturn(clientResponse);
    when(clientResponse.getStatus()).thenReturn(SUCCESS_STATUS_CODE);
    when(clientResponse.getEntity(String.class)).thenReturn(AIRFLOW_GET_STATUS_RESPONSE);
    WorkflowStatusType response = workflowEngineService
        .getWorkflowRunStatus(workflowEngineRequest(EXECUTION_DATE, false));
    verify(restClient).resource(eq(AIRFLOW_DAG_GET_STATUS_URL));
    verify(webResource).type(eq(MediaType.APPLICATION_JSON));
    verify(webResourceBuilder).header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE));
    verify(webResourceBuilder).method(eq("GET"), eq(ClientResponse.class), any());
    verify(clientResponse).getStatus();
    verify(airflowConfig).getUrl();
    verify(airflowConfig).getAppKey();
    assertThat(response, equalTo(WorkflowStatusType.SUCCESS));
  }

  @Test
  public void testGetWorkflowRunStatusWithFailure() {
    when(dpsHeaders.getPartitionId()).thenReturn(TEST_PARTITION);
    when(airflowConfigResolver.getAirflowConfig(TEST_PARTITION)).thenReturn(airflowConfig);
    when(airflowConfig.getUrl()).thenReturn(AIRFLOW_URL);
    when(airflowConfig.getAppKey()).thenReturn(AIRFLOW_APP_KEY);
    when(restClient.resource(eq(AIRFLOW_DAG_GET_STATUS_URL))).thenReturn(webResource);
    when(webResource.type(eq(MediaType.APPLICATION_JSON))).thenReturn(webResourceBuilder);
    when(webResourceBuilder.header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE)))
        .thenReturn(webResourceBuilder);
    when(webResourceBuilder.method(eq("GET"), eq(ClientResponse.class),
        any())).thenReturn(clientResponse);
    when(clientResponse.getStatus()).thenReturn(INTERNAL_SERVER_ERROR_STATUS_CODE);
    Assertions.assertThrows(AppException.class, () -> {
      workflowEngineService.getWorkflowRunStatus(workflowEngineRequest(EXECUTION_DATE, false));
    });
    verify(restClient).resource(eq(AIRFLOW_DAG_GET_STATUS_URL));
    verify(webResource).type(eq(MediaType.APPLICATION_JSON));
    verify(webResourceBuilder).header(eq(HEADER_AUTHORIZATION_NAME), eq(HEADER_AUTHORIZATION_VALUE));
    verify(webResourceBuilder).method(eq("GET"), eq(ClientResponse.class), any());
    verify(clientResponse).getStatus();
    verify(airflowConfig).getUrl();
    verify(airflowConfig).getAppKey();
  }

  private WorkflowEngineRequest workflowEngineRequest(String workflowEngineExecutionDate,
                                                      boolean isDeployedThroughWorkflowService) {
    return new WorkflowEngineRequest(RUN_ID, WORKFLOW_ID, WORKFLOW_NAME, EXECUTION_TIMESTAMP,
        workflowEngineExecutionDate, isDeployedThroughWorkflowService);
  }

  private Map<String, Object> registrationInstructions(String dagContent) {
    Map<String, Object> res = new HashMap<>();
    res.put("dagContent", dagContent);
    return res;
  }
}
