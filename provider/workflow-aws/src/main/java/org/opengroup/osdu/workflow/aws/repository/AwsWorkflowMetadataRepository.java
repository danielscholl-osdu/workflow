/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.aws.config.AwsServiceConfig;
import org.opengroup.osdu.workflow.aws.util.dynamodb.converters.WorkflowMetadataDoc;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.RequestScope;

@Repository
@RequestScope
public class AwsWorkflowMetadataRepository implements IWorkflowMetadataRepository {

    @Inject
    private AwsServiceConfig config;

    @Inject
    DpsHeaders headers;

    @Inject
    private DynamoDBQueryHelperFactory dynamoDBQueryHelperFactory;

    private DynamoDBQueryHelperV2 queryHelper;

    @Value("${aws.dynamodb.workflowMetadataTable.ssm.relativePath}")
    String workflowMetadataTableParameterRelativePath;

    @PostConstruct
    public void init() {
      queryHelper = dynamoDBQueryHelperFactory.getQueryHelperForPartition(headers, workflowMetadataTableParameterRelativePath);
    }


    @Override
    public WorkflowMetadata createWorkflow(WorkflowMetadata workflowMetadata) {
      String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();
      workflowMetadata.setWorkflowId(generateWorkflowId(workflowMetadata.getWorkflowName(), dataPartitionId)); //name should be unique. Enforce it via using name/id as same field

      if (workflowExists(workflowMetadata.getWorkflowName(), dataPartitionId)) { //don't update if workflow already exists
        throw new ResourceConflictException(workflowMetadata.getWorkflowName(), "Workflow with same name already exists");
      }

      WorkflowMetadataDoc doc = WorkflowMetadataDoc.create(workflowMetadata, dataPartitionId);

      // try {
        queryHelper.save(doc);
      // }
      // catch(Exception e) {
      //   System.out.println(">>>>>>>>>>>>>")
      //   System.out.println
      //   throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
      //                         HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
      //                         "Failed to save workflowMetadata to db");
      // }


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

        AttributeValue dataPartitionAttributeValue = new AttributeValue(dataPartitionId);


        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":partitionId", dataPartitionAttributeValue);

        if (StringUtils.isNotBlank(prefix)) {
          filterExpression += " AND begins_with ( workflowName, :workflowNamePrefix )";
          AttributeValue workflowNamePrefixAttributeValue = new AttributeValue(prefix);
          eav.put(":workflowNamePrefix", workflowNamePrefixAttributeValue);
        }

        ArrayList<WorkflowMetadataDoc> docs = queryHelper.scanTable(WorkflowMetadataDoc.class, filterExpression, eav);

        if (!docs.isEmpty()) {
          return docs.stream().map(x -> x.convertToWorkflowMetadata()).collect(Collectors.toList());
        }
        else {
          return new ArrayList<WorkflowMetadata>();
        }

    }

}
