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



import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.aws.dynamodb.QueryPageResult;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.aws.service.s3.S3Client;
import org.opengroup.osdu.workflow.aws.util.dynamodb.converters.WorkflowRunDoc;
import org.opengroup.osdu.workflow.exception.WorkflowRunNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.model.WorkflowRunsPage;
import org.springframework.boot.test.context.SpringBootTest;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes={S3Client.class})
public class AwsWorkflowRunRepositoryTest {

    private final String partition = "data-partition-id";
    private final String workflowRunHashKey = "runId";
    private final String workflowName = "workflowName";
    private final String runId = "runId";
    private final String cursor = "cursor";

    @InjectMocks
    private AwsWorkflowRunRepository repo = new AwsWorkflowRunRepository();

    @Mock
    private DpsHeaders headers;

    @Mock
    private DynamoDBQueryHelperFactory dynamoDBQueryHelperFactory;

    @Mock
    private DynamoDBQueryHelperV2 queryHelper;

    @Test
    public void testSaveWorkflowRun()
    {
        WorkflowRun workflowRun = Mockito.mock(WorkflowRun.class);

        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        repo.saveWorkflowRun(workflowRun);

        Mockito.verify(queryHelper, Mockito.times(1)).saveWithHashCondition(Mockito.any(WorkflowRunDoc.class), Mockito.eq(workflowRunHashKey));

    }

    @Test (expected = AppException.class)
    public void testSaveWorkflowRunConditionalCheckFailedException()
    {
        WorkflowRun workflowRun = Mockito.mock(WorkflowRun.class);

        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        Mockito.doThrow(new ConditionalCheckFailedException("exception")).when(queryHelper).saveWithHashCondition(Mockito.any(WorkflowRunDoc.class), Mockito.eq(workflowRunHashKey));

        repo.saveWorkflowRun(workflowRun);

        Mockito.verify(queryHelper, Mockito.times(1)).saveWithHashCondition(Mockito.any(WorkflowRunDoc.class), Mockito.eq(workflowRunHashKey));

    }

    @Test (expected = AppException.class)
    public void testSaveWorkflowRunException()
    {
        WorkflowRun workflowRun = Mockito.mock(WorkflowRun.class);

        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        Mockito.doThrow(new UnsupportedOperationException()).when(queryHelper).saveWithHashCondition(Mockito.any(WorkflowRunDoc.class), Mockito.eq(workflowRunHashKey));

        repo.saveWorkflowRun(workflowRun);

        Mockito.verify(queryHelper, Mockito.times(1)).saveWithHashCondition(Mockito.any(WorkflowRunDoc.class), Mockito.eq(workflowRunHashKey));

    }

    @Test
    public void testGetWorkflow()
    {

        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        Mockito.when(queryHelper.loadByPrimaryKey(Mockito.any(), Mockito.anyString(), Mockito.anyString())).thenReturn(new WorkflowRunDoc(runId, partition, workflowName, workflowName, 1l, 1l, null, "", ""));

        WorkflowRun result = repo.getWorkflowRun(workflowName, runId);

        Assert.assertNotNull(result);
    }

    @Test (expected = WorkflowRunNotFoundException.class)
    public void testGetWorkflowRunWorkflowRunNotFound()
    {

        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        Mockito.when(queryHelper.loadByPrimaryKey(Mockito.any(), Mockito.anyString(), Mockito.anyString())).thenReturn(null);

        repo.getWorkflowRun(workflowName, runId);
    }

    @Test (expected = AppException.class)
    public void testGetWorkflowRunInvalidWorkflowRun()
    {
        WorkflowRunDoc doc = Mockito.mock(WorkflowRunDoc.class);

        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        Mockito.when(queryHelper.loadByPrimaryKey(Mockito.any(), Mockito.anyString(), Mockito.anyString())).thenReturn(doc);

        Mockito.when(doc.getWorkflowName()).thenReturn(workflowName);

        Mockito.when(doc.getDataPartitionId()).thenReturn("");

        repo.getWorkflowRun(workflowName, runId);
    }

    @Test
    public void testGetWorkflowRunsByWorkflowName() throws IllegalArgumentException, UnsupportedEncodingException
    {
        List<WorkflowRunDoc> results = new ArrayList<WorkflowRunDoc>();
        results.add(Mockito.mock(WorkflowRunDoc.class));

        QueryPageResult<WorkflowRunDoc> docs = new QueryPageResult<WorkflowRunDoc>(cursor, results);

        Mockito.when(queryHelper.queryByGSI(Mockito.any(), Mockito.any(WorkflowRunDoc.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString())).thenReturn(docs);

        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        WorkflowRunsPage result = repo.getWorkflowRunsByWorkflowName(workflowName, 1, cursor);

        Assert.assertNotNull(result);
    }

    @Test
    public void testGetWorkflowRunsByWorkflowNameNullDocs()
    {
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        WorkflowRunsPage result = repo.getWorkflowRunsByWorkflowName(workflowName, 1, cursor);

        Assert.assertNotNull(result);
    }

    @Test (expected = AppException.class)
    public void testGetWorkflowRunsByWorkflowNameException() throws IllegalArgumentException, UnsupportedEncodingException
    {
        Mockito.when(queryHelper.queryByGSI(Mockito.any(), Mockito.any(WorkflowRunDoc.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString())).thenThrow(new IllegalArgumentException());

        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        repo.getWorkflowRunsByWorkflowName(workflowName, 1, cursor);
    }

    @Test
    public void testDeleteWorkflowRuns()
    {
        List<String> list = new ArrayList<String>();

        list.add("id1");
        list.add("id2");

        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        repo.deleteWorkflowRuns(workflowName, list);

        Mockito.verify(queryHelper, Mockito.times(2)).deleteByPrimaryKey(Mockito.any(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testUpdateWorkflowRun()
    {
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        Mockito.when(queryHelper.keyExistsInTable(Mockito.any(), Mockito.any(WorkflowRunDoc.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    
        WorkflowRun workflowRun = Mockito.mock(WorkflowRun.class);

        WorkflowRun result = repo.updateWorkflowRun(workflowRun);

        Assert.assertNotNull(result);

        Mockito.verify(queryHelper, Mockito.times(1)).save(Mockito.any(WorkflowRunDoc.class));
    }

    @Test (expected = AppException.class)
    public void testUpdateWorkflowRunNonExist()
    {
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        Mockito.when(queryHelper.keyExistsInTable(Mockito.any(), Mockito.any(WorkflowRunDoc.class), Mockito.anyString(), Mockito.anyString())).thenReturn(false);
    
        WorkflowRun workflowRun = Mockito.mock(WorkflowRun.class);

        repo.updateWorkflowRun(workflowRun);
    }

    @Test (expected = AppException.class)
    public void testUpdateWorkflowRunException()
    {
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        Mockito.when(queryHelper.keyExistsInTable(Mockito.any(), Mockito.any(WorkflowRunDoc.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        Mockito.doThrow(new UnsupportedOperationException()).when(queryHelper).save(Mockito.any(WorkflowRunDoc.class));
    
        WorkflowRun workflowRun = Mockito.mock(WorkflowRun.class);

        repo.updateWorkflowRun(workflowRun);
    }

    @Test
    public void testGetAllRunInstancesOfWorkflow()
    {
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partition);

        List<WorkflowRun> result = repo.getAllRunInstancesOfWorkflow(workflowName, null);

        Assert.assertNotNull(result);
    }

    @Test
    public void testRunExist()
    {
        repo.runExists(runId);
        
        Mockito.verify(queryHelper, Mockito.times(1)).keyExistsInTable(Mockito.any(), Mockito.any(WorkflowRunDoc.class));
    }
}
