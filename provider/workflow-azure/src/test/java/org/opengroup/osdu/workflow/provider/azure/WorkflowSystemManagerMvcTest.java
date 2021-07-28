package org.opengroup.osdu.workflow.provider.azure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.provider.azure.interfaces.IAuthorizationServiceSP;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.exception.handler.ConflictApiError;
import org.opengroup.osdu.workflow.provider.azure.security.AuthorizationFilterSP;
import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import  org.opengroup.osdu.workflow.provider.azure.api.WorkflowSystemManagerApi;
import org.opengroup.osdu.workflow.provider.azure.interfaces.IWorkflowSystemManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link WorkflowSystemManagerApi}
 */
@WebMvcTest({WorkflowSystemManagerApi.class})
@AutoConfigureMockMvc
@Import({AuthorizationFilterSP.class, DpsHeaders.class})
@Disabled
class WorkflowSystemManagerMvcTest {
  private static final String TEST_AUTH = "Bearer bla";
  private static final String PARTITION = "partition";
  private static final String CORRELATION_ID = "sample-correlation-id";
  private static final String WORKFLOW_RESPONSE = "{\n" +
      "  \"workflowId\": \"2afccfb8-1351-41c6-9127-61f2d7f22ff8\",\n" +
      "  \"workflowName\": \"HelloWorld\",\n" +
      "  \"description\": \"This is a test workflow\",\n" +
      "  \"creationTimestamp\": 1600144876028,\n" +
      "  \"createdBy\": \"user@email.com\",\n" +
      "  \"version\": 1\n" +
      "}";
  private static final String WORKFLOW_METADATA_LIST_RESPONSE = "[\n" +
      "    {\n" +
      "        \"workflowId\": \"2afccfb8-1351-41c6-9127-61f2d7f22ff8\",\n" +
      "        \"workflowName\": \"HelloWorld\",\n" +
      "        \"description\": \"This is a test workflow\",\n" +
      "        \"creationTimestamp\": 1600144876028,\n" +
      "        \"version\": 1,\n" +
      "        \"registrationInstructions\": {\n" +
      "            \"active\": true,\n" +
      "            \"concurrentWorkflowRun\": 5,\n" +
      "            \"concurrentTaskRun\": 5\n" +
      "        }\n" +
      "    }\n" +
      "]";
  private static final String WORKFLOW_REQUEST = "{\n" +
      "  \"workflowName\": \"HelloWorld\",\n" +
      "  \"description\": \"This is a test workflow\"\n" +
      "}";
  private static final String WORKFLOW_ENDPOINT = "/v1/workflow/system";
  private static final String EXISTING_WORKFLOW_ID = "existing-id";
  private static final String WORKFLOW_NAME = "test-dag-name";
  private static final String WORKFLOW_ID = "2afccfb8-1351-41c6-9127-61f2d7f22ff8";
  private static final String EMPTY_PREFIX_ERROR =
      "Prefix cannot be Null or Empty. Please provide a value.";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper mapper;

  @MockBean
  private IWorkflowSystemManagerService workflowSystemManagerService;

  @MockBean
  private IAuthorizationServiceSP authorizationServiceSP;

  @MockBean
  private JaxRsDpsLog log;

  @MockBean
  private DpsHeaders dpsHeaders;

  @Mock
  private AuthorizationResponse authorizationResponse;

  @Test
  void testCreateApiWithSuccess() throws Exception {
    final CreateWorkflowRequest request = mapper
        .readValue(WORKFLOW_REQUEST, CreateWorkflowRequest.class);
    final WorkflowMetadata metadata = mapper.readValue(WORKFLOW_RESPONSE, WorkflowMetadata.class);
    when(workflowSystemManagerService.createSystemWorkflow(eq(request))).thenReturn(metadata);
    when(authorizationServiceSP.isDomainAdminServiceAccount())
        .thenReturn(true);
    when(dpsHeaders.getAuthorization()).thenReturn(TEST_AUTH);
    when(dpsHeaders.getPartitionId()).thenReturn("");
    when(dpsHeaders.getCorrelationId()).thenReturn(CORRELATION_ID);
    final MvcResult mvcResult = mockMvc.perform(
        post(WORKFLOW_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .headers(getHttpHeaders())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(WORKFLOW_REQUEST))
        .andExpect(status().isOk())
        .andReturn();
    verify(workflowSystemManagerService, times(1)).createSystemWorkflow(eq(request));
    verify(authorizationServiceSP, times(1)).isDomainAdminServiceAccount();
    verify(dpsHeaders).getAuthorization();
    verify(dpsHeaders).getPartitionId();
    final WorkflowMetadata responseMetadata =
        mapper.readValue(mvcResult.getResponse().getContentAsByteArray(), WorkflowMetadata.class);
    assertThat(metadata, equalTo(responseMetadata));
  }

  @Test
  public void testCreateSystemApiWithConflict() throws Exception {
    final CreateWorkflowRequest request = mapper.readValue(WORKFLOW_REQUEST, CreateWorkflowRequest.class);
    when(workflowSystemManagerService.createSystemWorkflow(eq(request)))
        .thenThrow(new ResourceConflictException(EXISTING_WORKFLOW_ID, "conflict"));
    when(authorizationServiceSP.isDomainAdminServiceAccount())
        .thenReturn(true);
    when(dpsHeaders.getAuthorization()).thenReturn(TEST_AUTH);
    when(dpsHeaders.getPartitionId()).thenReturn("");
    when(dpsHeaders.getCorrelationId()).thenReturn(CORRELATION_ID);
    final MvcResult mvcResult = mockMvc.perform(
        post(WORKFLOW_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .headers(getHttpHeaders())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(WORKFLOW_REQUEST))
        .andExpect(status().isConflict())
        .andReturn();
    verify(workflowSystemManagerService, times(1)).createSystemWorkflow(eq(request));
    when(authorizationServiceSP.isDomainAdminServiceAccount())
        .thenReturn(true);
    verify(dpsHeaders).getAuthorization();
    verify(dpsHeaders).getPartitionId();
    final ConflictApiError response =
        mapper.readValue(mvcResult.getResponse().getContentAsByteArray(), ConflictApiError.class);
    Assertions.assertEquals(EXISTING_WORKFLOW_ID, response.getConflictId());
  }

  @Test
  void testDeleteSystemApiWithSuccess() throws Exception {
    doNothing().when(workflowSystemManagerService).deleteSystemWorkflow(eq(WORKFLOW_NAME));
    when(authorizationServiceSP.isDomainAdminServiceAccount())
        .thenReturn(true);
    when(dpsHeaders.getAuthorization()).thenReturn(TEST_AUTH);
    when(dpsHeaders.getPartitionId()).thenReturn("");
    when(dpsHeaders.getCorrelationId()).thenReturn(CORRELATION_ID);
    mockMvc.perform(
        delete("/v1/workflow/system/{workflow_name}", WORKFLOW_NAME)
            .contentType(MediaType.APPLICATION_JSON)
            .headers(getHttpHeaders())
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().is(204))
        .andReturn();
    verify(workflowSystemManagerService).deleteSystemWorkflow(eq(WORKFLOW_NAME));
    verify(authorizationServiceSP, times(1)).isDomainAdminServiceAccount();
    verify(dpsHeaders).getAuthorization();
    verify(dpsHeaders).getPartitionId();
  }

  @Test
  void testDeleteApiWithError() throws Exception {
    doThrow(new WorkflowNotFoundException("not found")).when(workflowSystemManagerService)
        .deleteSystemWorkflow(eq(WORKFLOW_NAME));
    when(authorizationServiceSP.isDomainAdminServiceAccount())
        .thenReturn(true);
    when(dpsHeaders.getAuthorization()).thenReturn(TEST_AUTH);
    when(dpsHeaders.getPartitionId()).thenReturn("");
    when(dpsHeaders.getCorrelationId()).thenReturn(CORRELATION_ID);
    mockMvc.perform(
        delete("/v1/workflow/system/{workflow_name}", WORKFLOW_NAME)
            .contentType(MediaType.APPLICATION_JSON)
            .headers(getHttpHeaders())
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNotFound())
        .andReturn();
    verify(workflowSystemManagerService).deleteSystemWorkflow(eq(WORKFLOW_NAME));
    verify(authorizationServiceSP, times(1)).isDomainAdminServiceAccount();
    verify(dpsHeaders).getAuthorization();
    verify(dpsHeaders).getPartitionId();
  }

  private HttpHeaders getHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(DpsHeaders.AUTHORIZATION, TEST_AUTH);
    //headers.add(DpsHeaders.DATA_PARTITION_ID, PARTITION);
    return headers;
  }

  @TestConfiguration
  @EnableWebSecurity
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  public static class TestSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

      http.httpBasic().disable()
          .csrf().disable();  //disable default authN. AuthN handled by endpoints proxy
    }
  }
}
