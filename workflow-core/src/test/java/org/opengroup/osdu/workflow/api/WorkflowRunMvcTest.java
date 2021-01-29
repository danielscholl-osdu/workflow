package org.opengroup.osdu.workflow.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.workflow.exception.handler.RestExceptionHandler;
import org.opengroup.osdu.workflow.sucurity.AuthorizationFilter;
import org.opengroup.osdu.workflow.model.TriggerWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunService;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link WorkflowRunApi}
 */
@WebMvcTest(WorkflowRunApi.class)
@AutoConfigureMockMvc
@Import({AuthorizationFilter.class, DpsHeaders.class})
class WorkflowRunMvcTest {
  private static final String TEST_AUTH = "Bearer bla";
  private static final String PARTITION = "partition";
  private static final String WORKFLOW_NAME = "test-dag-name";
  private static final String RUN_ID = "d13f7fd0-d27e-4176-8d60-6e9aad86e347";
  private static final String TRIGGER_WORKFLOW_ENDPOINT = String
      .format("/v1/workflow/%s/workflowRun", WORKFLOW_NAME);
  private static final String TRIGGER_WORKFLOW_REQUEST = "{\n" +
      "  \"runId\": \"d13f7fd0-d27e-4176-8d60-6e9aad86e347\",\n" +
      "  \"executionContext\": {\n" +
      "    \"id\": \"someid\",\n" +
      "    \"kind\": \"somekind\",\n" +
      "    \"dataPartitionId\": \"someId\"\n" +
      "  }\n" +
      "}";
  private static final String WORKFLOW_RUN_RESPONSE = "{\n" +
      "  \"workflowId\": \"2afccfb8-1351-41c6-9127-61f2d7f22ff8\",\n" +
      "  \"runId\": \"d13f7fd0-d27e-4176-8d60-6e9aad86e347\",\n" +
      "  \"startTimeStamp\": 1600145420675,\n" +
      "  \"endTimeStamp\": 1600145420675,\n" +
      "  \"status\": \"submitted\",\n" +
      "  \"submittedBy\": \"user@mail.com\"\n" +
      "}";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper mapper;

  @MockBean
  private IWorkflowRunService workflowRunService;
  @MockBean
  private IAuthorizationService authorizationService;
  @MockBean
  private RestExceptionHandler restExceptionHandler;

  @Mock
  private AuthorizationResponse authorizationResponse;

  @Test
  void testTriggerWorkflowApiWithSuccess() throws Exception {
    final TriggerWorkflowRequest request = mapper
        .readValue(TRIGGER_WORKFLOW_REQUEST, TriggerWorkflowRequest.class);
    final WorkflowRun workflowRun = mapper.readValue(WORKFLOW_RUN_RESPONSE, WorkflowRun.class);
    when(workflowRunService.triggerWorkflow(eq(WORKFLOW_NAME), eq(request)))
        .thenReturn(workflowRun);
    when(authorizationService.authorizeAny(any(), any())).thenReturn(authorizationResponse);
    final MvcResult mvcResult = mockMvc.perform(
        post(TRIGGER_WORKFLOW_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .headers(getHttpHeaders())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .content(TRIGGER_WORKFLOW_REQUEST))
        .andExpect(status().isOk())
        .andReturn();
    verify(workflowRunService, times(1)).triggerWorkflow(eq(WORKFLOW_NAME), eq(request));
    verify(authorizationService, times(1)).authorizeAny(any(), any());
    final WorkflowRun response = mapper
        .readValue(mvcResult.getResponse().getContentAsByteArray(), WorkflowRun.class);
    assertThat(workflowRun, equalTo(response));
  }

  @Test
  void testGetWorkflowRunApiWithSuccess() throws Exception {
    final WorkflowRun workflowRun = mapper.readValue(WORKFLOW_RUN_RESPONSE, WorkflowRun.class);
    when(workflowRunService.getWorkflowRunByName(eq(WORKFLOW_NAME), eq(RUN_ID)))
        .thenReturn(workflowRun);
    when(authorizationService.authorizeAny(any(), any())).thenReturn(authorizationResponse);
    final MvcResult mvcResult = mockMvc.perform(
        get("/v1/workflow/{workflow_name}/workflowRun/{runId}", WORKFLOW_NAME, RUN_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .headers(getHttpHeaders())
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isOk())
        .andReturn();
    verify(workflowRunService).getWorkflowRunByName(eq(WORKFLOW_NAME), eq(RUN_ID));
    verify(authorizationService).authorizeAny(any(), any());
    final WorkflowRun responseWorkflowRun =
        mapper.readValue(mvcResult.getResponse().getContentAsByteArray(), WorkflowRun.class);
    assertThat(workflowRun, equalTo(responseWorkflowRun));

  }

  private HttpHeaders getHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(DpsHeaders.AUTHORIZATION, TEST_AUTH);
    headers.add(DpsHeaders.DATA_PARTITION_ID, PARTITION);
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
