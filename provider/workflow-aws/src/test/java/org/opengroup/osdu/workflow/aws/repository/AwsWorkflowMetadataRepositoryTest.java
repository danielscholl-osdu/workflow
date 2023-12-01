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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.aws.util.dynamodb.converters.WorkflowMetadataDoc;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;

@RunWith(MockitoJUnitRunner.class)
public class AwsWorkflowMetadataRepositoryTest {

    private final String workflowName = "workflowName";
    private final String prefix = "prefix";

    @InjectMocks
    private AwsWorkflowMetadataRepository repo = new AwsWorkflowMetadataRepository();

    @Mock
    private DynamoDBQueryHelperV2 queryHelper;

    @Mock
    private DpsHeaders headers;

    @Test
    public void testCreateWorkflow()
    {
        WorkflowMetadata workflowMetadata = Mockito.mock(WorkflowMetadata.class);
        
        repo.createWorkflow(workflowMetadata);

        Mockito.verify(queryHelper, Mockito.times(1)).save(Mockito.any(WorkflowMetadataDoc.class));
    }

    @Test (expected = ResourceConflictException.class)
    public void testCreateWorkflowKeyExist()
    {
        WorkflowMetadata workflowMetadata = Mockito.mock(WorkflowMetadata.class);

        Mockito.when(queryHelper.keyExistsInTable(Mockito.any(), Mockito.any())).thenReturn(true);
        
        repo.createWorkflow(workflowMetadata);
    }

    @Test
    public void testGetWorkflow()
    {
        WorkflowMetadataDoc doc = Mockito.mock(WorkflowMetadataDoc.class);

        Mockito.when(queryHelper.loadByPrimaryKey(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(doc);

        repo.getWorkflow(workflowName);

        Mockito.verify(doc, Mockito.times(1)).convertToWorkflowMetadata();
    }

    @Test (expected = WorkflowNotFoundException.class)
    public void testGetWorkflowNullDoc()
    {
        repo.getWorkflow(workflowName);
    }

    @Test
    public void testDeleteWorkflow()
    {
        repo.deleteWorkflow(workflowName);

        Mockito.verify(queryHelper, Mockito.times(1)).deleteByPrimaryKey(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetAllWorkflowForTenant()
    {
        List<WorkflowMetadata> result = repo.getAllWorkflowForTenant(prefix);

        Assert.assertNotNull(result);
    }

}