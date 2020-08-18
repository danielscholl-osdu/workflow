package org.opengroup.osdu.workflow.provider.interfaces;

import java.util.List;
import java.util.Map;
import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.model.WorkflowRunsPage;

public interface IWorkflowRunRepository {
  /**
   * Saves information about workflow run in persistence store.
   * @param workflowRun Object representing a workflow run
   * @return Information about workflow run.
   */
  WorkflowRun saveWorkflowRun(final WorkflowRun workflowRun);

  /**
   * Returns Information about workflow run. based on workflowName, runId
   * @param workflowName Name of the workflow for which workflowRun should be checked.
   * @param runId Id of the workflowRun for which metadata should be retrieved.
   * @return Information about workflow run.
   */
  WorkflowRun getWorkflowRun(final String workflowName, final String runId);

  /**
   * Returns information about workflow runs based on workflowName
   * @param workflowName Name of the workflow for which workflowRun should be checked.
   * @return List of workflow runs.
   */
  WorkflowRunsPage getWorkflowRunsByWorkflowName(final String workflowName, final Integer limit,
                                               final String cursor);

  /**
   * Deletes all workflow runs associated with runIds.
   * @param runIds Run ids to delete
   */
  void deleteWorkflowRuns(final String workflowId, final List<String> runIds);

  /**
   * Updates workflow run in persistence store.
   * @param workflowRun WorkflowRun for which update is called.
   * @return Information about workflow run.
   */
  WorkflowRun updateWorkflowRun(final WorkflowRun workflowRun);

  List<WorkflowRun> getAllRunInstancesOfWorkflow(final String workflowName, Map<String, Object> params);

}
