/*
  Copyright © 2021 Amazon Web Services

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.opengroup.osdu.workflow.aws.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.aws.config.AwsServiceConfig;
import org.opengroup.osdu.workflow.aws.util.dynamodb.converters.WorkflowMetadataDoc;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
public class AwsWorkflowMetadataRepository implements IWorkflowMetadataRepository {

    @Inject
    private AwsServiceConfig config;

    @Inject 
    DpsHeaders headers;

    private DynamoDBQueryHelper queryHelper;

    @PostConstruct
    public void init() {
      queryHelper = new DynamoDBQueryHelper(config.dynamoDbEndpoint, config.dynamoDbRegion, config.dynamoDbTablePrefix);
    }


    @Override
    public WorkflowMetadata createWorkflow(WorkflowMetadata workflowMetadata) {   

      //should be removed once the validation occurs in common code
      if (!workflowMetadata.getWorkflowName().matches("^[a-zA-Z0-9._-]{1,64}$")) {
        throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid workflow name",
        String.format("Invalid workflowName. Must match pattern '%s'", "^[a-zA-Z0-9._-]{1,64}$"));
      }

      String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();     
      workflowMetadata.setWorkflowId(generateWorkflowId(workflowMetadata.getWorkflowName(), dataPartitionId)); //name should be unique. Enforce it via using name/id as same field
      
      if (workflowExists(workflowMetadata.getWorkflowName(), dataPartitionId)) { //don't update if workflow already exists
        throw new ResourceConflictException(workflowMetadata.getWorkflowName(), "Workflow with same name already exists");
      }

      WorkflowMetadataDoc doc = WorkflowMetadataDoc.create(workflowMetadata, dataPartitionId);
      
      try {
        queryHelper.save(doc);
      }
      catch(Exception e) {
        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                              HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), 
                              "Failed to save workflowMetadata to db");
      }
      
      
      return doc.convertToWorkflowMetadata(); //eliminate fields that might exist that aren't stored
    }


    private String generateWorkflowId(String workflowName, String dataPartitionId) {
      return String.format("%s:%s", dataPartitionId, workflowName);
    }

    private boolean workflowExists(String workflowId) {
      // Set GSI hash key
      WorkflowMetadataDoc workflowMetadataDoc = new WorkflowMetadataDoc();
      workflowMetadataDoc.setWorkflowId(workflowId);

      // Check if the tenant exists in the table by name
      return queryHelper.keyExistsInTable(WorkflowMetadataDoc.class, workflowMetadataDoc);
    }

    private boolean workflowExists(String workflowName, String dataPartitionId) {
       return workflowExists(generateWorkflowId(workflowName, dataPartitionId));
    }

    @Override
    public WorkflowMetadata getWorkflow(String workflowName) {

        String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();
        String workflowId = generateWorkflowId(workflowName, dataPartitionId);

        WorkflowMetadataDoc doc = queryHelper.loadByPrimaryKey(WorkflowMetadataDoc.class, workflowId, dataPartitionId);

        if (doc == null) {
          throw new WorkflowNotFoundException(String.format("Workflow: '%s' not found", workflowName));
        }

       return doc.convertToWorkflowMetadata();
    }

    @Override
    public void deleteWorkflow(String workflowName) {
        String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();
        String workflowId = generateWorkflowId(workflowName, dataPartitionId);
        
        queryHelper.deleteByPrimaryKey(WorkflowMetadataDoc.class, workflowId, dataPartitionId);

    }

    @Override
    public List<WorkflowMetadata> getAllWorkflowForTenant(String prefix) {

        String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();
        String filterExpression = "dataPartitionId = :partitionId";
        AttributeValue attributeValue = new AttributeValue(dataPartitionId);
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":partitionId", attributeValue);

        ArrayList<WorkflowMetadataDoc> docs = queryHelper.scanTable(WorkflowMetadataDoc.class, filterExpression, eav);

        if (docs.size() > 0) {
          List<WorkflowMetadata> metadatas = docs.stream().map(x -> x.convertToWorkflowMetadata()).collect(Collectors.toList());  
          return metadatas;
        }
        else {
          return new ArrayList<WorkflowMetadata>();
        }        
        
    }
    
}
