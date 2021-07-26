package org.opengroup.osdu.azure.workflow.workflow;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opengroup.osdu.azure.workflow.utils.HTTPClientAzureV3;
import org.opengroup.osdu.workflow.workflow.v3.WorkflowRunV3IntegrationTests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.opengroup.osdu.workflow.consts.TestConstants.CREATE_WORKFLOW_WORKFLOW_NAME;

@Slf4j
public class TestWorkflowRunV3Integration extends WorkflowRunV3IntegrationTests {

  @BeforeEach
  @Override
  public void setup() throws Exception {
    this.client = new HTTPClientAzureV3();
    this.headers = client.getCommonHeader();
    deleteTestWorkflows(CREATE_WORKFLOW_WORKFLOW_NAME);
  }

  @AfterEach
  @Override
  public void tearDown() throws Exception {
    try {
      Set<String> completedWorkflowRunIds = new HashSet<>();
      if(createdWorkflowRuns.size() != completedWorkflowRunIds.size()) {
        executeWithWaitAndRetry(() -> {
          waitForWorkflowRunsToComplete(createdWorkflowRuns, completedWorkflowRunIds);
          return null;
        }, 20, 15, TimeUnit.SECONDS);
      }
    } finally {
      Long integrationTestEndTime = System.currentTimeMillis();
      log.info("Completed integration test at {}", integrationTestEndTime);
    }
    deleteAllTestWorkflowRecords();
    this.client = null;
    this.headers = null;
    this.createdWorkflows = new ArrayList<>();
    this.createdWorkflowRuns = new ArrayList<>();
  }

  private void deleteAllTestWorkflowRecords() {
    createdWorkflows.stream().forEach(c -> {
      try {
        deleteTestWorkflows(c.get(WORKFLOW_ID_FIELD));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
}
