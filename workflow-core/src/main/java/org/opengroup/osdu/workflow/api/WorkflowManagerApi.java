package org.opengroup.osdu.workflow.api;

import java.util.List;

import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.model.WorkflowRole;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowManagerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/workflow")
public class WorkflowManagerApi {
  @Autowired
  private IWorkflowManagerService workflowManagerService;

  /**
   * API to create a workflow.
   * @param request Request object which has information to create workflow.
   * @return Workflow metadata.
   */
  @PostMapping
  @PreAuthorize("@authorizationFilter.hasPermission('" + WorkflowRole.ADMIN + "')")
  public WorkflowMetadata create(@RequestBody final CreateWorkflowRequest request) {
    return workflowManagerService.createWorkflow(request);
  }

  /**
   * Returns workflow metadata based on workflowName
   * @param workflowName Name of the workflow for which metadata should be retrieved.
   * @return Workflow metadata
   */
  @GetMapping("/{workflow_name}")
  @PreAuthorize("@authorizationFilter.hasPermission('" + WorkflowRole.VIEWER + "','" + WorkflowRole.CREATOR + "','" + WorkflowRole.ADMIN + "')")
  public  WorkflowMetadata getWorkflowByName(@PathVariable("workflow_name") final String workflowName) {
    return workflowManagerService.getWorkflowByName(workflowName);
  }

  /**
   * Deletes workflow by workflowName
   * @param workflowName Name of the workflow which needs to be deleted.
   */
  @DeleteMapping("/{workflow_name}")
  @PreAuthorize("@authorizationFilter.hasPermission('" + WorkflowRole.ADMIN + "')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteWorkflowById(@PathVariable("workflow_name") final String workflowName) {
    workflowManagerService.deleteWorkflow(workflowName);
  }

  /**
   * Get List all the workflows for the tenant.
   * @param prefix Filter workflow names which start with the full prefix specified.
   */
  @GetMapping
  @PreAuthorize("@authorizationFilter.hasPermission('" + WorkflowRole.VIEWER + "','" + WorkflowRole.CREATOR + "','" + WorkflowRole.ADMIN + "')")
  public List<WorkflowMetadata> getAllWorkflowForTenant(
      @RequestParam(required = false) String prefix) {
    return workflowManagerService.getAllWorkflowForTenant(prefix);
  }
}

