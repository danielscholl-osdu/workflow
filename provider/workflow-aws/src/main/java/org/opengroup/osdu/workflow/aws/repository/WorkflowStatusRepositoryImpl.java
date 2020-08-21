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
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.workflow.aws.util.DateTime;
import org.opengroup.osdu.workflow.aws.util.dynamodb.converters.WorkflowStatusDoc;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

// TODO Will be moved to registry service
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

  private DynamoDBQueryHelper queryHelper;
  private DateTime dateTime;

  @PostConstruct
  public void init() {
    queryHelper = new DynamoDBQueryHelper(dynamoDbEndpoint, dynamoDbRegion, tablePrefix);
    dateTime = new DateTime();
  }

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

  @Override
  public WorkflowStatus saveWorkflowStatus(WorkflowStatus workflowStatus) {
    if (workflowStatus != null) {
      WorkflowStatusDoc doc = mapWorkflowStatusToDoc(workflowStatus);
      queryHelper.save(doc);
    }
    return workflowStatus;
  }

  @Override
  public WorkflowStatus updateWorkflowStatus(String workflowId, WorkflowStatusType workflowStatusType) {
    if (workflowId != null && workflowStatusType != null) {
      WorkflowStatus workflowStatus = findWorkflowStatus(workflowId);
      workflowStatus.setWorkflowStatusType(workflowStatusType);
      WorkflowStatusDoc doc = mapWorkflowStatusToDoc(workflowStatus);
      queryHelper.save(doc);
      return workflowStatus;
    }
    else {
      return null;
    }
  }

  private WorkflowStatusDoc mapWorkflowStatusToDoc(WorkflowStatus workflowStatus) {
    WorkflowStatusDoc doc = new WorkflowStatusDoc();
    doc.setWorkflowId(workflowStatus.getWorkflowId());
    doc.setAirflowRunId(workflowStatus.getAirflowRunId());
    doc.setWorkflowStatusType(workflowStatus.getWorkflowStatusType().toString());
    doc.setSubmittedAt(dateTime.getCurrentDate());
    doc.setSubmittedBy(workflowStatus.getSubmittedBy());
    return doc;
  }
}
