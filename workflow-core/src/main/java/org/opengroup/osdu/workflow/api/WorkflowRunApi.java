package org.opengroup.osdu.workflow.api;

import java.util.List;
import java.util.Map;
import org.opengroup.osdu.workflow.model.TriggerWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowRole;
import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.model.UpdateWorkflowRunRequest;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/workflow")
public class WorkflowRunApi {
  @Autowired
  private IWorkflowRunService workflowRunService;

  /**
   * API to trigger a workflow.
   * @param workflowName Workflow to trigger.
   * @param request Request object which has information to trigger workflow.
   * @return Information about workflow run.
   */
  @PostMapping("/{workflow_name}/workflowRun")
  @PreAuthorize("@authorizationFilter.hasPermission('" + WorkflowRole.ADMIN + "')")
  public WorkflowRun triggerWorkflow(@PathVariable("workflow_name") String workflowName,
      @RequestBody TriggerWorkflowRequest request) {
    return workflowRunService.triggerWorkflow(workflowName, request);
  }

  /**
   * Returns Information about workflow run. based on workflowName, runId
   * @param workflowName Name of the workflow for which workflowRun should be checked.
   * @param runId Id of the workflowRun for which metadata should be retrieved.
   * @return Information about workflow run.
   */
  @GetMapping("/{workflow_name}/workflowRun/{runId}")
  @PreAuthorize("@authorizationFilter.hasPermission('" + WorkflowRole.CREATOR + "')")
  public WorkflowRun getWorkflowRunById(@PathVariable("workflow_name") final String workflowName,
      @PathVariable("runId") final String runId) {
    return workflowRunService.getWorkflowRunByName(workflowName, runId);
  }

  /**
   * Get all run instances of a workflow.
   * @param workflowName Workflow to trigger.
   * @return Information list about workflow run.
   */
  @GetMapping("/{workflow_name}/workflowRun")
  @PreAuthorize("@authorizationFilter.hasPermission('" + WorkflowRole.CREATOR + "')")
  public List<WorkflowRun> getAllRunInstances(
      @PathVariable("workflow_name") String workflowName,
      @RequestParam Map<String, Object> params) {
    return workflowRunService.getAllRunInstancesOfWorkflow(workflowName, params);
  }

  /**
   * Update the workflow run instance. based on workflowName, runId
   * @param workflowName Name of the workflow for which workflowRun should be checked.
   * @param runId Id of the workflowRun for which metadata should be retrieved.
   * @return Information about workflow run.
   */
  @PutMapping("/{workflow_name}/workflowRun/{runId}")
  @PreAuthorize("@authorizationFilter.hasPermission('" + WorkflowRole.CREATOR + "')")
  public WorkflowRun updateWorkflowRun(
      @PathVariable("workflow_name") String workflowName,
      @PathVariable("runId") String runId,
      @RequestBody UpdateWorkflowRunRequest body) {
    return workflowRunService.updateWorkflowRunStatus(workflowName, runId, body.getStatus());
  }
}
