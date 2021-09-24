package org.opengroup.osdu.workflow.api;

import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/workflow/system")
public class WorkflowSystemManagerApi {
  @Autowired
  private IWorkflowManagerService workflowManagerService;

  /**
   * API to create a system workflow.
   * @param request Request object which has information to create workflow.
   * @return Workflow metadata.
   */
  @PostMapping
  @PreAuthorize("@authorizationFilter.hasRootPermission()")
  public WorkflowMetadata createSystemWorkflow(@RequestBody final CreateWorkflowRequest request) {
    return workflowManagerService.createSystemWorkflow(request);
  }

  /**
   * Deletes system workflow by workflowName
   * @param workflowName Name of the workflow which needs to be deleted.
   */
  @DeleteMapping("/{workflow_name}")
  @PreAuthorize("@authorizationFilter.hasRootPermission()")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSystemWorkflowById(@PathVariable("workflow_name") final String workflowName) {
    workflowManagerService.deleteSystemWorkflow(workflowName);
  }
}
