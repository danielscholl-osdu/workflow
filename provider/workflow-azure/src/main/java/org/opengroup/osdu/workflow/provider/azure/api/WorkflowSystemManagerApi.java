// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.workflow.provider.azure.api;

import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.azure.interfaces.IWorkflowSystemManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/workflow/system")
public class WorkflowSystemManagerApi {
  @Autowired
  private IWorkflowSystemManagerService workflowSystemManagerService;
  /**
   * API to create a workflow.
   * @param request Request object which has information to create workflow.
   * @return Workflow metadata.
   */
  @PostMapping
  @PreAuthorize("@authorizationFilterSP.hasPermissions()")
  public WorkflowMetadata createSystemWorkflow(@RequestBody final CreateWorkflowRequest request) {
    return workflowSystemManagerService.createSystemWorkflow(request);
  }


  /**
   * Deletes workflow by workflowName
   * @param workflowName Name of the workflow which needs to be deleted.
   */
  @DeleteMapping("/{workflow_name}")
  @PreAuthorize("@authorizationFilterSP.hasPermissions()")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSystemWorkflowById(@PathVariable("workflow_name") final String workflowName) {
    workflowSystemManagerService.deleteSystemWorkflow(workflowName);
  }

}
