package org.opengroup.osdu.workflow.provider.interfaces;

import java.util.Map;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;

public interface IWorkflowEngineService {
  /**
   * Saves the workflow definition into workflow engine accessible location.
   * @param rq request parameters required to make a call to Workflow Engine.
   * @param registrationInstruction Definition of workflow.
   */
  void createWorkflow(final WorkflowEngineRequest rq, final Map<String, Object> registrationInstruction);

  /**
   * Deletes the workflow definition in workflow engine accessible location.
   * @param rq request parameters required to make a call to Workflow Engine.
   */
  void deleteWorkflow(final WorkflowEngineRequest rq);

  /**
   * Saves the custom operator into workflow engine accessible location.
   * @param customOperatorDefinition Custom operator definition.
   * @param fileName Name of the file with which workflow definition must be saved.
   */
  void saveCustomOperator(final String customOperatorDefinition, final String fileName);

  /**
   * Triggers given workflow by workflowName
   * @param rq request parameters required to make a call to Workflow Engine.
   * @param context context data object used by Workflow.
   * @return
   */
  void triggerWorkflow(WorkflowEngineRequest rq, Map<String, Object> context);

  /**
   * Gets Status of the workflowRun
   * @param rq request parameters required to make a call to Workflow Engine.
   * @return Status of the particular workflowRun
   */
  WorkflowStatusType getWorkflowRunStatus(WorkflowEngineRequest rq);
}
