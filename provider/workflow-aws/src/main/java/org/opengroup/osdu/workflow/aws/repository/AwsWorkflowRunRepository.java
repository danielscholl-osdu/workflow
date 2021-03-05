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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.aws.dynamodb.QueryPageResult;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.aws.config.AwsServiceConfig;
import org.opengroup.osdu.workflow.aws.util.dynamodb.converters.WorkflowRunDoc;
import org.opengroup.osdu.workflow.exception.WorkflowRunNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.model.WorkflowRunsPage;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
public class AwsWorkflowRunRepository implements IWorkflowRunRepository {

    @Inject
    private AwsServiceConfig config;

    @Inject
    DpsHeaders headers;

    private DynamoDBQueryHelper queryHelper;

    @PostConstruct
    public void init() {
        queryHelper = new DynamoDBQueryHelper(config.dynamoDbEndpoint, config.dynamoDbRegion,
                config.dynamoDbTablePrefix);
    }

    @Override
    public WorkflowRun saveWorkflowRun(WorkflowRun workflowRun) {

        String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();                    

        WorkflowRunDoc doc = WorkflowRunDoc.create(workflowRun, dataPartitionId);

        try {
            queryHelper.save(doc);
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Failed to save workflowRun to db");
        }

        return workflowRun;

    }

    @Override
    public WorkflowRun getWorkflowRun(String workflowName, String runId) {

        String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();        

        WorkflowRunDoc doc = queryHelper.loadByPrimaryKey(WorkflowRunDoc.class, runId, dataPartitionId);

        if (doc == null) {
            throw new WorkflowRunNotFoundException(
                    String.format("Workflow Run: '%s' for Workflow '%s' not found", runId, workflowName));
        }

        return doc.convertToWorkflowRun();

    }

    @Override
    public WorkflowRunsPage getWorkflowRunsByWorkflowName(String workflowName, Integer limit, String cursor) {

        String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();        

        WorkflowRunDoc queryDoc = WorkflowRunDoc.builder().workflowName(workflowName).dataPartitionId(dataPartitionId)
                .build();

        QueryPageResult<WorkflowRunDoc> docs;
        try {
            docs = queryHelper.queryByGSI(WorkflowRunDoc.class, queryDoc, "dataPartitionId", dataPartitionId, limit, cursor);
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {            
            e.printStackTrace();
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                   HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), 
                                   "Failed to query workflow runs by name");
            
        }

        List<WorkflowRun> items;
        String docsCursor = null;

        if (docs != null && docs.results != null && docs.results.size() > 0) {
            items = docs.results.stream().map(x -> x.convertToWorkflowRun()).collect(Collectors.toList());
            docsCursor = docs.cursor;
        }
        else {
            items = new ArrayList<WorkflowRun>();
        }
        
        return WorkflowRunsPage.builder()
                        .cursor(docsCursor)
                        .items(items)
                        .build();
    }

    @Override
    public void deleteWorkflowRuns(String workflowName, List<String> runIds) {

        String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();
        
        for (String runId : runIds) {            

            queryHelper.deleteByPrimaryKey(WorkflowRunDoc.class, runId, dataPartitionId);

        }

    }

    @Override
    public WorkflowRun updateWorkflowRun(WorkflowRun workflowRun) {
        
        String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();

        if (!workflowRunExists(workflowRun.getRunId(), dataPartitionId)) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(),
                                   HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                   "WorkflowRun not found, cannot update");
        }
        
        WorkflowRunDoc doc = WorkflowRunDoc.create(workflowRun, dataPartitionId);

        try {
            queryHelper.save(doc);
          }
          catch(Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                  HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), 
                                  "Failed to save workflowRun to db");
          }                  

        return workflowRun;
    }

    @Override
    public List<WorkflowRun> getAllRunInstancesOfWorkflow(String workflowName, Map<String, Object> params) {
        
        String cursor = null;

        List<WorkflowRun> runs = new ArrayList<>();

        do {
            
            WorkflowRunsPage page = getWorkflowRunsByWorkflowName(workflowName, 100, cursor);
            runs.addAll(page.getItems());
            cursor = page.getCursor();
        }
        while (cursor != null);

        return runs;
        
    }

    private boolean workflowRunExists(String runId, String dataPartitionId) {
        // Set GSI hash key
        WorkflowRunDoc workflowRunDoc = new WorkflowRunDoc();
        workflowRunDoc.setRunId(runId);
  
        // Check if the tenant exists in the table by name
        return queryHelper.keyExistsInTable(WorkflowRunDoc.class, workflowRunDoc, "dataPartitionId", dataPartitionId);
      }
    
}
