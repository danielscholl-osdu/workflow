//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.workflow.provider.azure.repository;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.azure.CosmosFacade;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowStatusDoc;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.azure.cosmos.CosmosContainer;

import javax.inject.Named;

@Repository
@Slf4j
@RequiredArgsConstructor
public class WorkflowStatusRepository implements IWorkflowStatusRepository {

  private static Logger logger = Logger.getLogger(WorkflowStatusRepository.class.getName());

  @Autowired
  @Named("WORKFLOW_STATUS_CONTAINER")
  private CosmosContainer workflowStatusContainer;

  @Override
  public WorkflowStatus findWorkflowStatus(String workflowId) {
    logger.log(Level.INFO, String.format("Requesting workflow status by workflow Id :{%s}",
      workflowId));

    Optional<WorkflowStatusDoc> document = CosmosFacade.findItem(
      workflowStatusContainer,
      workflowId,
      workflowId,
      WorkflowStatusDoc.class);

    WorkflowStatusDoc workflowStatusDoc = !document.isPresent() ? null : document.get();

    if(workflowStatusDoc == null) {
      throw new WorkflowNotFoundException(
        String.format("Workflow for workflow id {%s} not found", workflowId));
    }

    WorkflowStatus workflowStatus = buildWorkflowStatus(workflowStatusDoc);
    logger.log(Level.INFO, String.format("Found workflow: {%s}", workflowStatusDoc));

    return workflowStatus;
  }

  @Override
  public WorkflowStatus saveWorkflowStatus(WorkflowStatus workflowStatus) {
    logger.log(Level.INFO, String.format("Saving new workflow status: {%s}",
      workflowStatus));


    Optional<WorkflowStatusDoc> existingDoc = CosmosFacade.findItem(
      workflowStatusContainer,
      workflowStatus.getWorkflowId(),
      workflowStatus.getWorkflowId(),
      WorkflowStatusDoc.class);

    if (!existingDoc.isPresent()) {
      WorkflowStatusDoc newStatusDoc = buildWorkflowStatusDoc(workflowStatus);
      CosmosFacade.upsertItem(workflowStatusContainer, newStatusDoc);
    }

    logger.log(Level.INFO, String.format("Fetch saved workflow status: {%s}", workflowStatus));

    return workflowStatus;
  }

  @Override
  public WorkflowStatus updateWorkflowStatus(String workflowId,
                                             WorkflowStatusType workflowStatusType) {
    logger.log(Level.INFO, String.format("Update workflow status for workflow id: {%s}, new status: {%s}",
      workflowId, workflowStatusType));

    Optional<WorkflowStatusDoc> existingDoc = CosmosFacade.findItem(
      workflowStatusContainer,
      workflowId,
      workflowId,
      WorkflowStatusDoc.class);

    WorkflowStatusDoc workflowStatusDoc = !existingDoc.isPresent() ? null : existingDoc.get();

    if(workflowStatusDoc == null) {
      throw new WorkflowNotFoundException(
        String.format("Workflow for workflow id {%s} not found", workflowId));
    }

    logger.log(Level.INFO, String.format("Found workflow status : {%s}", workflowStatusDoc));
    workflowStatusDoc.workflowStatusType = WorkflowStatusType.valueOf(workflowStatusType.toString());

    CosmosFacade.upsertItem(workflowStatusContainer, workflowStatusDoc);

    WorkflowStatus workflowStatus = buildWorkflowStatus(workflowStatusDoc);
    logger.log(Level.INFO, String.format("Updated workflow status : {%s}", workflowStatus));

    return workflowStatus;
  }

  private WorkflowStatus buildWorkflowStatus(WorkflowStatusDoc workflowStatusDoc) {
    logger.log(Level.INFO, String.format("Build workflow status. workflow status doc : {%s}",
      workflowStatusDoc.toString()));

    return WorkflowStatus.builder()
      .workflowId(workflowStatusDoc.workflowId)
      .airflowRunId(workflowStatusDoc.airflowRunId)
      .workflowStatusType(WorkflowStatusType.valueOf(workflowStatusDoc.workflowStatusType.toString()))
      .submittedAt(workflowStatusDoc.submittedAt)
      .submittedBy(workflowStatusDoc.submittedBy)
      .build();
  }

  private WorkflowStatusDoc buildWorkflowStatusDoc(WorkflowStatus workflowStatus) {
    logger.log(Level.INFO, String.format("Build workflow status doc. workflow status : {%s}",
      workflowStatus.toString()));

    return WorkflowStatusDoc.builder()
      .id(workflowStatus.getWorkflowId())
      .workflowId(workflowStatus.getWorkflowId())
      .airflowRunId(workflowStatus.getAirflowRunId())
      .workflowStatusType(workflowStatus.getWorkflowStatusType())
      .submittedAt(workflowStatus.getSubmittedAt())
      .build();
  }
}
