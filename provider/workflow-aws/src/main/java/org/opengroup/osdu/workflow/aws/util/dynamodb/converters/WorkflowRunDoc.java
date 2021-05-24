// Copyright © 2021 Amazon Web Services
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

package org.opengroup.osdu.workflow.aws.util.dynamodb.converters;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;

import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamoDBTable(tableName = "WorkflowRunRepository")
public class WorkflowRunDoc {

    @DynamoDBHashKey(attributeName = "runId")
    private String runId;

    @DynamoDBRangeKey(attributeName = "dataPartitionId")
    @DynamoDBIndexRangeKey(attributeName = "dataPartitionId", globalSecondaryIndexName = "workflowName-tenant-index")
    private String dataPartitionId;

    @DynamoDBAttribute(attributeName = "workflowId")
    private String workflowId;

    @DynamoDBAttribute(attributeName = "workflowName")
    @DynamoDBIndexHashKey(attributeName = "workflowName",  globalSecondaryIndexName = "workflowName-tenant-index")
    private String workflowName;

    @DynamoDBAttribute(attributeName = "startTimeStamp")    
    private Long startTimeStamp;
    
    @DynamoDBAttribute(attributeName = "endTimeStamp")
    private Long endTimeStamp;

    @DynamoDBAttribute(attributeName = "status")
    @DynamoDBTyped(DynamoDBAttributeType.S)
    private WorkflowStatusType status;

    @DynamoDBAttribute(attributeName = "submittedBy")
    private String submittedBy;

    @DynamoDBAttribute(attributeName = "workflowEngineExecutionDate")
    private String workflowEngineExecutionDate;


    public static WorkflowRunDoc create(WorkflowRun workflowRun, String dataPartitionId) {
        WorkflowRunDoc doc = WorkflowRunDoc.builder()
            .runId(workflowRun.getRunId())    
            .dataPartitionId(dataPartitionId)
            .workflowId(workflowRun.getWorkflowId())
            .workflowName(workflowRun.getWorkflowName())            
            .startTimeStamp(workflowRun.getStartTimeStamp())
            .endTimeStamp(workflowRun.getEndTimeStamp())
            .status(workflowRun.getStatus())
            .submittedBy(workflowRun.getSubmittedBy())
            .workflowEngineExecutionDate(workflowRun.getWorkflowEngineExecutionDate())
            .build();

        return doc;

    }

    public WorkflowRun convertToWorkflowRun() {
        WorkflowRun workflowRun = WorkflowRun.builder()
            .runId(runId)    
            .workflowId(workflowId)
            .workflowName(workflowName)            
            .startTimeStamp(startTimeStamp)
            .endTimeStamp(endTimeStamp)
            .status(status)
            .submittedBy(submittedBy)
            .workflowEngineExecutionDate(workflowEngineExecutionDate)
            .build();

        return workflowRun;
    }


}
