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


import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.provider.ibm.exception.IngestionStrategyQueryException;
import org.opengroup.osdu.workflow.provider.ibm.repository.IngestionStrategyRepository;


import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;

@ExtendWith(MockitoExtension.class)
class IngestionStrategyRepositoryTest {

  private static final String COLLECTION_NAME = "ingestion-strategy";
  private static final String USER = "user-1";
 // private static final String workflowType = "ingest";
  private static final String dataType = "ingest";
 
  @InjectMocks
  private IngestionStrategyRepository ingestionStrategyRepository;

  @Mock
  Database db;
  
  @Mock
  QueryBuilder querybuilder;
  
  @Mock
  IBMCloudantClientFactory cloudantFactory;
 
  @Mock
  QueryResult<IngestionStrategy> results;
  
  @Mock
  QueryResult queryResult;
  
  @Mock
  IngestionStrategy ingestion;
  
  


  @Nested
  class FindIngestionStrategy {

    @Test
    void shouldFindIngestionStrategyByWorkflowId() throws MalformedURLException {
      
    	
    	ingestion.setDagName("welllog");
    	ingestion.setDataType("osdu");
    	ingestion.setUserId("shri");
    	ingestion.setWorkflowType(WorkflowType.INGEST);
    	
    	List<IngestionStrategy> ingestionStrategyList = new <IngestionStrategy>ArrayList();
    	ingestionStrategyList.add(ingestion);
    	
    	Mockito.when(db.query(Mockito.any(),Mockito.any())).thenReturn(queryResult);
    	Mockito.when(queryResult.getDocs()).thenReturn(ingestionStrategyList);

    	Assert.assertNotNull(ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.INGEST, "default", "shri"));
    	
    	
    }

    @Test
    void shouldThrowExceptionWhenQueryFailed() {
    	
    	
    	
    	Mockito.when(db.query(Mockito.any(),Mockito.any())).thenThrow(new IngestionStrategyQueryException(
				 format("Failed to find a dag by Workflow type - %s, Data type - %s and User id - %s",
						 WorkflowType.INGEST, dataType, USER)));
    	
    	try {
    	ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.INGEST, dataType, USER);
    	}catch(IngestionStrategyQueryException e) {
    		
    		assertEquals(String.format("Failed to find a dag by Workflow type - %s, Data type - %s and User id - %s",
					 WorkflowType.INGEST, dataType, USER), e.getMessage());
    	}
    	
    	
    }

    @Test
    void shouldThrowExceptionWhenFutureFailed() throws Exception {}

    @Test
    void shouldThrowExceptionWhenItFindsFewDocuments() {
    	
    	ingestion.setDagName("welllog");
    	ingestion.setDataType("osdu");
    	ingestion.setUserId("shri");
    	ingestion.setWorkflowType(WorkflowType.INGEST);
    	
    	List<IngestionStrategy> ingestionStrategyList = new <IngestionStrategy>ArrayList();
    	ingestionStrategyList.add(ingestion);
    	Mockito.when(db.query(Mockito.any(),Mockito.any())).thenReturn(queryResult);
    	Mockito.when(queryResult.getDocs()).thenReturn(ingestionStrategyList);
    	
    	
    	
    	try {
        	ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.INGEST, dataType, USER);
        	}catch(IngestionStrategyQueryException e) {
        		
        		assertEquals(String.format("Find dag selection returned %s documents(s), expected 1, query by Workflow"
		                  + " type - %s, Data type - %s and User id - %s",
		                  "2", WorkflowType.INGEST, dataType, USER), e.getMessage());
        	}
    	
    }

    @Test
    void shouldReturnNullWhenNothingWasFound() {
    	
    	Mockito.when(db.query(Mockito.any(),Mockito.any())).thenReturn(null);
    	
    	try {
        	ingestionStrategyRepository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.INGEST, dataType, USER);
        	}catch(IngestionStrategyQueryException e) {
        		
        		assertEquals(String.format("Failed to find a dag by Workflow type - %s, Data type - %s and User id - %s",
        				WorkflowType.INGEST, dataType, USER), e.getMessage());
        	}
    	
    }

  }

  private IngestionStrategy getIngestionStrategy() {
    return IngestionStrategy.builder()
        .workflowType(WorkflowType.INGEST)
        .dataType("well_log")
        .userId(USER)
        .build();
  }

  
}
