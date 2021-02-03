// Copyright © 2020 Amazon Web Services
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

package org.opengroup.osdu.workflow.aws.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.workflow.aws.interfaces.IWorkflowStatusRepository;
import org.opengroup.osdu.workflow.aws.util.dynamodb.converters.WorkflowStatusDoc;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
@Slf4j
@RequiredArgsConstructor
public class WorkflowStatusRepositoryImpl implements IWorkflowStatusRepository {

  @Value("${aws.dynamodb.table.prefix}")
  String tablePrefix;
  @Value("${aws.dynamodb.region}")
  String dynamoDbRegion;
  @Value("${aws.dynamodb.endpoint}")
  String dynamoDbEndpoint;

  private static String FINISHED_WORKFLOW_BAD_REQUEST_REASON = "Unable to update finished workflow";

  // see integration test "should_returnBadRequest_when_givenFinishedWorkflowId
  private static String FINISHED_WORKFLOW_BAD_REQUEST_MSG = "Workflow status for workflow id: %s already has status:%s and can not be updated";

  private DynamoDBQueryHelper queryHelper;

  /**
   * Spring boot constructor that news up an object to interact with dynamo
   * Also sets the time for the workflow's startdate
   */
  @PostConstruct
  public void init() {
    queryHelper = new DynamoDBQueryHelper(dynamoDbEndpoint, dynamoDbRegion, tablePrefix);
  }

  /**
   * Simple lookup on the workflow dynamo table
   * @param workflowId workflow id
   * @return
   */
  @Override
  public WorkflowStatus findWorkflowStatus(String workflowId) {
    WorkflowStatusDoc doc = queryHelper.loadByPrimaryKey(WorkflowStatusDoc.class, workflowId);
    if (doc != null) {
      WorkflowStatus workflowStatus = new WorkflowStatus();
      workflowStatus.setWorkflowId(doc.getWorkflowId());
      workflowStatus.setAirflowRunId(doc.getAirflowRunId());
      workflowStatus.setWorkflowStatusType((WorkflowStatusType.valueOf(doc.getWorkflowStatusType())));
      workflowStatus.setSubmittedAt(doc.getSubmittedAt());
      workflowStatus.setSubmittedBy(doc.getSubmittedBy());
      return workflowStatus;
    } else {
      return null;
    }
  }

  /**
   * Simple save of a new workflow to the workflow dynamo table
   * @param workflowStatus to save
   * @return
   */
  @Override
  public WorkflowStatus saveWorkflowStatus(WorkflowStatus workflowStatus) {
    if (workflowStatus != null) {
      WorkflowStatusDoc doc = mapWorkflowStatusToDoc(workflowStatus);
      queryHelper.save(doc);
    }
    return workflowStatus;
  }

  /**
   * Simple update of an existing workflow against the workflow dynamo table.
   * This also throws a bad request exception if the workflow is already in a finished state.
   * @param workflowId workflow id
   * @param workflowStatusType
   * @return
   */
  @Override
  public WorkflowStatus updateWorkflowStatus(String workflowId, WorkflowStatusType workflowStatusType) {
    if (workflowId != null && workflowStatusType != null) {
      WorkflowStatus workflowStatus = findWorkflowStatus(workflowId);

      if(workflowStatus.getWorkflowStatusType().equals(WorkflowStatusType.FINISHED)){
        throw new AppException(HttpStatus.SC_BAD_REQUEST, FINISHED_WORKFLOW_BAD_REQUEST_REASON,
            String.format(FINISHED_WORKFLOW_BAD_REQUEST_MSG, workflowId, workflowStatus.getWorkflowStatusType().toString().toUpperCase()));
      }

      workflowStatus.setWorkflowStatusType(workflowStatusType);
      WorkflowStatusDoc doc = mapWorkflowStatusToDoc(workflowStatus);
      queryHelper.save(doc);
      return workflowStatus;
    }
    else {
      return null;
    }
  }

  /**
   * Helper function for converting to dynamo friendly object
   * @param workflowStatus
   * @return
   */
  private WorkflowStatusDoc mapWorkflowStatusToDoc(WorkflowStatus workflowStatus) {
    WorkflowStatusDoc doc = new WorkflowStatusDoc();
    doc.setWorkflowId(workflowStatus.getWorkflowId());
    doc.setAirflowRunId(workflowStatus.getAirflowRunId());
    doc.setWorkflowStatusType(workflowStatus.getWorkflowStatusType().toString());
    doc.setSubmittedAt(workflowStatus.getSubmittedAt());
    doc.setSubmittedBy(workflowStatus.getSubmittedBy());
    return doc;
  }
}
