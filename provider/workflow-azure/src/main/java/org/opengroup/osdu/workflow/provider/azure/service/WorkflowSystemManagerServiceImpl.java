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

package org.opengroup.osdu.workflow.provider.azure.service;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.workflow.logging.AuditLogger;
import org.opengroup.osdu.workflow.model.CreateWorkflowRequest;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowManagerService;
import org.opengroup.osdu.workflow.provider.azure.interfaces.IWorkflowSystemManagerService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowSystemManagerServiceImpl implements IWorkflowSystemManagerService {

  @Autowired
  private IWorkflowMetadataRepository workflowMetadataRepository;

  @Autowired
  private IWorkflowManagerService workflowManagerService;

  @Autowired
  private IWorkflowEngineService workflowEngineService;

  @Autowired
  private IWorkflowRunService workflowRunService;

  @Autowired
  private AuditLogger auditLogger;

  @Override
  public WorkflowMetadata createSystemWorkflow(CreateWorkflowRequest request) {
    if (StringUtils.isEmpty(request.getWorkflowName())) {
      throw new BadRequestException("Invalid workflow name provided");
    }
    return workflowManagerService.createWorkflow(request);
  }

  @Override
  public void deleteSystemWorkflow(String workflowName) {
    final WorkflowMetadata workflowMetadata = workflowMetadataRepository.getWorkflow(workflowName);
    workflowMetadataRepository.deleteWorkflow(workflowName);
    WorkflowEngineRequest rq = new WorkflowEngineRequest(workflowName, workflowMetadata.isDeployedThroughWorkflowService());
    workflowEngineService.deleteWorkflow(rq);
    auditLogger.workflowDeleteEvent(Collections.singletonList(workflowName));
  }
}
