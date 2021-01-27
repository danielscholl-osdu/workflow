package org.opengroup.osdu.workflow.provider.interfaces;

import java.util.Map;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;

public interface IWorkflowEngineService {
  /**
   * Saves the workflow definition into workflow engine accessible location.
   * @param registrationInstruction Definition of workflow.
   * @param workflowName Name of the workflow for which workflow definition must be saved.
   */
  void createWorkflow(final Map<String, Object> registrationInstruction, final String workflowName);

  /**
   * Deletes the workflow definition in workflow engine accessible location.
   * @param workflowName Name of the workflow which needs to be deleted
   */
  void deleteWorkflow(final String workflowName);

  /**
   * Saves the custom operator into workflow engine accessible location.
   * @param customOperatorDefinition Custom operator definition.
   * @param fileName Name of the file with which workflow definition must be saved.
   */
  void saveCustomOperator(final String customOperatorDefinition, final String fileName);

  /**
   * Triggers given workflow by workflowName
   * @param runId Id for this specific run of workflow.
   * @param workflowName Name of the workflow to trigger.
   * @param inputData Input data used by workflow.
   * @return
   */
  void triggerWorkflow(String runId, String workflowId, String workflowName, Map<String, Object> inputData, long executionDate);

  /**
   * Gets Status of the workflowRun
   * @param workflowName Name of the workflow for which run is checked
   * @param executionTimeStamp Execution date time stamp for the workflow run
   * @return Status of the particular workflowRun
   */
  WorkflowStatusType getWorkflowRunStatus(final String workflowName, final long executionTimeStamp);
}
