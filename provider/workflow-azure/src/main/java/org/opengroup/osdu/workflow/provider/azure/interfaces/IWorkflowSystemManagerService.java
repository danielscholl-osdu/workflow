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

package org.opengroup.osdu.workflow.provider.azure.interfaces;

import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;

public interface IWorkflowSystemManagerService {
  /**
   * Creates workflow with given request.
   * @param request Request object which has information to create workflow.
   * @return Workflow metadata.
   */
  WorkflowMetadata createSystemWorkflow(final CreateWorkflowRequest request);
  /**
   * Deletes workflow based on workflowName
   * @param workflowName Id of the workflow which needs to be deleted.
   */
  void deleteSystemWorkflow(final String workflowName);

}
