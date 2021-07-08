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

import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import org.opengroup.osdu.workflow.model.WorkflowMetadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamoDBTable(tableName = "WorkflowMetadataRepository")
public class WorkflowMetadataDoc {

    @DynamoDBRangeKey(attributeName = "dataPartitionId")
    private String dataPartitionId;

    @DynamoDBHashKey(attributeName = "workflowId")
    private String workflowId;

    @DynamoDBAttribute(attributeName = "workflowName")
    private String workflowName;

    @DynamoDBAttribute(attributeName = "description")
    private String description;

    @DynamoDBAttribute(attributeName = "createdBy")
    private String createdBy;

    @DynamoDBAttribute(attributeName = "creationTimestamp")    
    private Long creationTimestamp;
    
    @DynamoDBAttribute(attributeName = "version")
    private Long version;

    @DynamoDBAttribute(attributeName = "isDeployedThroughWorkflowService")
    private Boolean isDeployedThroughWorkflowService;

    @DynamoDBAttribute(attributeName = "registrationInstructions")
    private Map<String,Object> registrationInstructions;


    public static WorkflowMetadataDoc create(WorkflowMetadata workflowMetadata, String dataPartitionId) {
        WorkflowMetadataDoc doc = WorkflowMetadataDoc.builder()
            .dataPartitionId(dataPartitionId)
            .workflowId(workflowMetadata.getWorkflowId())
            .workflowName(workflowMetadata.getWorkflowName())
            .description(workflowMetadata.getDescription())
            .createdBy(workflowMetadata.getCreatedBy())
            .creationTimestamp(workflowMetadata.getCreationTimestamp())
            .version(workflowMetadata.getCreationTimestamp())
            .isDeployedThroughWorkflowService(false) //we dont support deployment right now
            .registrationInstructions(workflowMetadata.getRegistrationInstructions())
            .build();

        return doc;

    }

    public WorkflowMetadata convertToWorkflowMetadata() {
        WorkflowMetadata metadata = WorkflowMetadata.builder()
            .workflowId(workflowId)
            .workflowName(workflowName)
            .description(description)
            .createdBy(createdBy)
            .creationTimestamp(creationTimestamp)
            .version(version)
            .isDeployedThroughWorkflowService(isDeployedThroughWorkflowService)
            .registrationInstructions(registrationInstructions)
            .build();

        return metadata;
    }


}
