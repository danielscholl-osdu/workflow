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

package org.opengroup.osdu.aws.workflow.util;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.Date;

@DynamoDBTable(tableName = "WorkflowStatusRepository")
public class WorkflowStatusDoc {

    @DynamoDBHashKey(attributeName = "WorkflowId")
    private String workflowId;

    @DynamoDBAttribute(attributeName = "AirflowRunId")
    private String airflowRunId;

    @DynamoDBAttribute(attributeName = "WorkflowStatusType")
    private String workflowStatusType;

    @DynamoDBAttribute(attributeName = "SubmittedAt")
    private Date submittedAt;

    @DynamoDBAttribute(attributeName = "SubmittedBy")
    private String submittedBy;

  public String getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  public String getAirflowRunId() {
    return airflowRunId;
  }

  public void setAirflowRunId(String airflowRunId) {
    this.airflowRunId = airflowRunId;
  }

  public String getWorkflowStatusType() {
    return workflowStatusType;
  }

  public void setWorkflowStatusType(String workflowStatusType) {
    this.workflowStatusType = workflowStatusType;
  }

  public Date getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(Date submittedAt) {
    this.submittedAt = submittedAt;
  }

  public String getSubmittedBy() {
    return submittedBy;
  }

  public void setSubmittedBy(String submittedBy) {
    this.submittedBy = submittedBy;
  }
}
