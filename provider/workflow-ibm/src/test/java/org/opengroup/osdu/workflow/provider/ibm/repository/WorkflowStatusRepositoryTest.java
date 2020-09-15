/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.provider.ibm.repository;


import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.ibm.exception.WorkflowStatusNotFoundException;
import org.opengroup.osdu.workflow.provider.ibm.exception.WorkflowStatusNotSavedException;
import org.opengroup.osdu.workflow.provider.ibm.exception.WorkflowStatusNotUpdatedException;
import org.opengroup.osdu.workflow.provider.ibm.exception.WorkflowStatusQueryException;
import org.opengroup.osdu.workflow.provider.ibm.model.WorkflowStatusDoc;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusRepository;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.Assert.assertNotNull;
import com.cloudant.client.api.Database;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import com.cloudant.client.api.model.Response;


@ExtendWith(MockitoExtension.class)
class WorkflowStatusRepositoryTest {
	
  @Mock
 IBMCloudantClientFactory cloudantFactory;
	 
	  @Mock
	  Database db;
	  
	  @Mock
	  DpsHeaders headers;
	  
	  @Mock
	  TenantInfo tenant;
	  
	  
	  WorkflowStatusDoc workflowStatusdoc;
	  
	  
	  WorkflowStatus workflowStatus;
	  
	  @Mock
	  private Response response;


  private static final String COLLECTION_NAME = "workflow-status";
  private static final String TEST_WORKFLOW_ID = "test-workflow-id";
  private static final String TEST_WORKFLOW_ID_2 = "test-workflow-id-2";
  private static final String TEST_AIRFLOW_RUN_ID = "test-airflow-run-id";
  private static final String USER = "user-1";

  
  @InjectMocks
  private WorkflowStatusRepository workflowStatusRepository = new WorkflowStatusRepository();

 

  @Nested
  class FindWorkflowStatus {

    @Test
    void shouldFindWorkflowStatusByWorkflowId() throws MalformedURLException {
    Date createdDate = new Date();
    WorkflowStatusDoc workflowStatusdoc = new WorkflowStatusDoc();
	workflowStatusdoc.set_id(TEST_WORKFLOW_ID);
	workflowStatusdoc.setAirflowRunId(TEST_AIRFLOW_RUN_ID);
	workflowStatusdoc.setSubmittedAt(createdDate);
	workflowStatusdoc.setWorkflowStatusType(null);
	workflowStatusdoc.setSubmittedBy("shri");
	workflowStatusdoc.setWorkflowId(TEST_WORKFLOW_ID);
  
	
	Mockito.when(db.find(WorkflowStatusDoc.class, TEST_WORKFLOW_ID)).thenReturn(workflowStatusdoc);
	
	 WorkflowStatusDoc document = db.find(WorkflowStatusDoc.class,TEST_WORKFLOW_ID);
	 
	 Assert.assertEquals(document.get_id(), workflowStatusdoc.get_id());
	 }

    @Test
    void shouldThrowExceptionWhenQueryFailed() {
    	
    	Date createdDate = new Date();
        WorkflowStatusDoc workflowStatusdoc = new WorkflowStatusDoc();
    	workflowStatusdoc.set_id(TEST_WORKFLOW_ID);
    	workflowStatusdoc.setAirflowRunId(TEST_AIRFLOW_RUN_ID);
    	workflowStatusdoc.setSubmittedAt(createdDate);
    	workflowStatusdoc.setWorkflowStatusType(null);
    	workflowStatusdoc.setSubmittedBy("shri");
    	workflowStatusdoc.setWorkflowId(TEST_WORKFLOW_ID);
    	
    	Mockito.when(db.find(WorkflowStatusDoc.class, TEST_WORKFLOW_ID)).thenThrow(new WorkflowStatusQueryException(String.format("Failed to find a workflow status by Workflow id - %s", TEST_WORKFLOW_ID)));
    	
    	
    	
    	Mockito.when(tenant.getName()).thenReturn("shri");
    	
    	try {
    	workflowStatusRepository.findWorkflowStatus(TEST_WORKFLOW_ID);
    	
    	}catch(WorkflowStatusQueryException e) {
    		assertEquals(String.format("Failed to find a workflow status by Workflow id - %s",
	                  TEST_WORKFLOW_ID), e.getMessage());
    	}
    }

   
    @Test
    void shouldThrowExceptionWhenItFindsFewDocuments() {
    		
    }

    @Test
    void shouldReturnNullWhenNothingWasFound() {
    	
    	
    	Mockito.when(db.find(WorkflowStatusDoc.class, TEST_WORKFLOW_ID)).thenReturn(null);
    	Mockito.when(tenant.getName()).thenReturn("shri");
    	
    	try {
    		
    		workflowStatusRepository.findWorkflowStatus(TEST_WORKFLOW_ID);
    		}catch(WorkflowNotFoundException e) {
    		
    		assertEquals(String.format("No  workflow status found in"
	                  + " Workflow id - %s",
	                  TEST_WORKFLOW_ID), e.getMessage());
    		}
    	
    }

  }

  @Nested
  class SaveWorkflowStatus {

    @Captor
    ArgumentCaptor<Map<String, Object>> dataCaptor;

    @Test
    void shouldSaveWorkflowStatusAndReturnSavedEntity() {
    	
    	Date createdDate = new Date();
    	WorkflowStatus workflow = new WorkflowStatus();
    	
    	workflow.setAirflowRunId(TEST_AIRFLOW_RUN_ID);
    	workflow.setWorkflowStatusType(WorkflowStatusType.SUBMITTED);
    	workflow.setWorkflowId(TEST_WORKFLOW_ID);
    	workflow.setSubmittedAt(createdDate);

    	workflow.setSubmittedBy("shri");
    	
    	WorkflowStatusDoc workflowStatusDoc = new WorkflowStatusDoc(workflow);
    	Mockito.when(tenant.getName()).thenReturn("shri");
    	
    	
    	Mockito.when(headers.getUserEmail()).thenReturn("shri");
    	Mockito.when(tenant.getName()).thenReturn("shri");
    	
    	
    	Mockito.when(db.save(workflowStatusDoc)).thenReturn(response);
    	
    	
    	assertNotNull(workflowStatusRepository.saveWorkflowStatus(workflow));
    	    	
    }

    
    //chk as we are inserting new date
    @Test
    void shouldUseServerTimestampWhenCreateAtIsNotSpecified() {}

    @Test
    void shouldThrowExceptionWhenSaveQueryFailed() {
    	
    	
    	Date createdDate = new Date();
    	WorkflowStatus workflow = new WorkflowStatus();
    	
    	workflow.setAirflowRunId(TEST_AIRFLOW_RUN_ID);
    	workflow.setWorkflowStatusType(WorkflowStatusType.SUBMITTED);
    	workflow.setWorkflowId(TEST_WORKFLOW_ID);
    	workflow.setSubmittedAt(createdDate);

    	workflow.setSubmittedBy("shri");
    	final String errorMsg = "Exceptions during saving  workflow status: " + workflow;
    	WorkflowStatusDoc workflowStatusDoc = new WorkflowStatusDoc(workflow);
    	
    	
    	Mockito.when(headers.getUserEmail()).thenReturn("shri");
    	Mockito.when(tenant.getName()).thenReturn("shri");
    	
    	Mockito.when(db.save(workflowStatusDoc)).thenThrow(new WorkflowStatusNotSavedException(errorMsg));
    	
    	try {
    		workflowStatusRepository.saveWorkflowStatus(workflow);
    		
    	}catch(WorkflowStatusNotSavedException e) {
    		
    		assertEquals(errorMsg, e.getMessage());
    		
    	}
    	
    	
    }
    
   // NA 
    @Test
    void shouldThrowExceptionWhenUnableToFetchSavedEntity() {}
  }

  @Nested
  class UpdateWorkflowStatus {

    @Test
    void shouldUpdateWorkflowStatusAndReturnSavedEntity() {
    	Date createdDate = new Date();
    	
    	WorkflowStatusDoc workflowStatusdoc = new WorkflowStatusDoc();
    	workflowStatusdoc.set_id(TEST_WORKFLOW_ID);
    	workflowStatusdoc.setAirflowRunId(TEST_AIRFLOW_RUN_ID);
    	workflowStatusdoc.setSubmittedAt(createdDate);
    	workflowStatusdoc.setWorkflowStatusType(WorkflowStatusType.SUBMITTED);
    	workflowStatusdoc.setSubmittedBy("shri");
    	workflowStatusdoc.setWorkflowId(TEST_WORKFLOW_ID);
    	
    	Mockito.when(tenant.getName()).thenReturn("shri");
     
    	Mockito.when(db.find(WorkflowStatusDoc.class, TEST_WORKFLOW_ID)).thenReturn(workflowStatusdoc);
        WorkflowStatusDoc document = db.find(WorkflowStatusDoc.class,TEST_WORKFLOW_ID);
    	 
    	Mockito.when(db.update(document)).thenReturn(response);
    	
    	assertNotNull(workflowStatusRepository.updateWorkflowStatus(TEST_WORKFLOW_ID, WorkflowStatusType.FINISHED));
    	
    
    	
    }

    @Test
    void shouldThrowExceptionWhenUpdateQueryFailed() {
    	

    	
    	Date createdDate = new Date();
        WorkflowStatusDoc workflowStatusdoc = new WorkflowStatusDoc();
    	workflowStatusdoc.set_id(TEST_WORKFLOW_ID);
    	workflowStatusdoc.setAirflowRunId(TEST_AIRFLOW_RUN_ID);
    	workflowStatusdoc.setSubmittedAt(createdDate);
    	workflowStatusdoc.setWorkflowStatusType(WorkflowStatusType.SUBMITTED);
    	workflowStatusdoc.setSubmittedBy("shri");
    	workflowStatusdoc.setWorkflowId(TEST_WORKFLOW_ID);
    	
    	Mockito.when(tenant.getName()).thenReturn("shri");
    	Mockito.when(db.find(WorkflowStatusDoc.class, TEST_WORKFLOW_ID)).thenReturn(workflowStatusdoc);
    	
    	Mockito.when(db.update(WorkflowStatusDoc.class)).thenThrow(new WorkflowStatusQueryException(String.format("Failed to update a workflow status by Workflow id - %s", TEST_WORKFLOW_ID)));
    	
    	try {
    		workflowStatusRepository.updateWorkflowStatus(TEST_WORKFLOW_ID, WorkflowStatusType.FINISHED);
    	
    	}catch(WorkflowStatusQueryException e) {
    		assertEquals(String.format("Failed to update a workflow status by Workflow id - %s",
	                  TEST_WORKFLOW_ID), e.getMessage());
    	}
    
    	
    }

   // need to chk if we can save muliple document with same id.
    @Test
    void shouldThrowExceptionWhenItFindsFewDocuments() {
    	 	
    }

    @Test
    void shouldThrowExceptionWhenNothingWasFound() throws MalformedURLException {
    	
    	Mockito.when(tenant.getName()).thenReturn("shri");
    	Mockito.when(db.find(WorkflowStatusDoc.class, TEST_WORKFLOW_ID)).thenReturn(null);
    	
    	try {
    		
    		workflowStatusRepository.updateWorkflowStatus(TEST_WORKFLOW_ID, WorkflowStatusType.SUBMITTED);
    	}catch(WorkflowStatusNotFoundException e) {
    		
    		assertEquals(String.format("Workflow status for Workflow id: %s not found", TEST_WORKFLOW_ID), e.getMessage());
    	}
    	
    	
    	
    }

     @Test
    void shouldThrowExceptionWhenDefinedStatus() {
    	
    	Date createdDate = new Date();
    	WorkflowStatusDoc workflowStatusdoc = new WorkflowStatusDoc();
    	workflowStatusdoc.set_id(TEST_WORKFLOW_ID);
    	workflowStatusdoc.setAirflowRunId(TEST_AIRFLOW_RUN_ID);
    	workflowStatusdoc.setSubmittedAt(createdDate);
    	workflowStatusdoc.setWorkflowStatusType(WorkflowStatusType.FINISHED);
    	workflowStatusdoc.setSubmittedBy("shri");
    	workflowStatusdoc.setWorkflowId(TEST_WORKFLOW_ID);
    	
    	Mockito.when(db.find(WorkflowStatusDoc.class, TEST_WORKFLOW_ID)).thenReturn(workflowStatusdoc);
    	Mockito.when(tenant.getName()).thenReturn("shri");
    	
    	  	
    	try {
    		
    		workflowStatusRepository.updateWorkflowStatus(TEST_WORKFLOW_ID, WorkflowStatusType.FINISHED);
    	}catch(WorkflowStatusNotUpdatedException e) {
    		
    		assertEquals(String.format(
    		          "Workflow status for workflow id: %s already has status:%s and can not be updated",
    		          TEST_WORKFLOW_ID, WorkflowStatusType.FINISHED), e.getMessage());
    	}
    	
    	
    	
    }
  }

  private WorkflowStatus getWorkflowStatus(Date createdDate) {
    return WorkflowStatus.builder()
        .workflowId(TEST_WORKFLOW_ID)
        .airflowRunId(TEST_AIRFLOW_RUN_ID)
        .workflowStatusType(WorkflowStatusType.SUBMITTED)
        .submittedAt(createdDate)
        .submittedBy(USER)
        .build();
  }

  

}
