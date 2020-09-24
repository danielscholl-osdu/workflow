/**
 * Copyright 2020 IBM Corp. All Rights Reserved.
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

package org.opengroup.osdu.workflow.provider.ibm.repository;

import static java.lang.String.format;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.DAG_NAME;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.DATA_TYPE;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.USER_ID;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.WORKFLOW_TYPE;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.provider.ibm.exception.IngestionStrategyQueryException;
import org.opengroup.osdu.workflow.provider.interfaces.IIngestionStrategyRepository;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import com.cloudant.client.api.Database;
import javax.annotation.PostConstruct;
import org.opengroup.osdu.core.ibm.auth.ServiceCredentials;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;


import static com.cloudant.client.api.query.Expression.eq;
import static com.cloudant.client.api.query.Operation.and;

@Repository
@Slf4j
@RequiredArgsConstructor
public class IngestionStrategyRepository implements IIngestionStrategyRepository {

	@Value("${ibm.db.url}") 
	private String dbUrl;
	@Value("${ibm.db.apikey:#{null}}")
	private String apiKey;
	@Value("${ibm.db.user:#{null}}")
	private String dbUser;
	@Value("${ibm.db.password:#{null}}")
	private String dbPassword;
	
	@Value("${ibm.env.prefix:local-dev}")
	private String dbNamePrefix;

	private IBMCloudantClientFactory cloudantFactory;
	
	@Autowired
	private TenantInfo tenant;

	private Database db;
	private static final String COLLECTION_NAME = "ingestion-strategy";

	@PostConstruct
	public void init() throws MalformedURLException {
		
			cloudantFactory = new IBMCloudantClientFactory(new ServiceCredentials(dbUrl,dbUser,dbPassword));
		
		db = cloudantFactory.getDatabase(dbNamePrefix, COLLECTION_NAME);
		
		
	// TODO-- need to chk if indexing needs to be done here.	
	//	db.createIndex(JsonIndex.builder().name("kind-json-index").asc("kind").definition());
	//	db.createIndex(JsonIndex.builder().name("legalTagsNames-json-index").asc("legalTagsNames").definition());
	}

  @Override
  public IngestionStrategy findByWorkflowTypeAndDataTypeAndUserId(WorkflowType workflowType,
      String dataType, String userId) {
	  
	  log.debug("Requesting dag selection. Workflow type : {}, Data type : {}, User id : {}",
		        workflowType, dataType, userId);
	 
	  QueryResult<IngestionStrategy> results = null;
	try {
		results = db.query(new QueryBuilder(and(eq("workflowType", workflowType.toString().toLowerCase()),eq("dataType", dataType),eq("userId", userId))).build(),
				     		IngestionStrategy.class);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		 throw new IngestionStrategyQueryException(
				 format("Failed to find a dag by Workflow type - %s, Data type - %s and User id - %s",
				            workflowType, dataType, userId));
	}
	
	  if(results == null ) {
			 throw new IngestionStrategyQueryException(
					 format("Failed to find a dag by Workflow type - %s, Data type - %s and User id - %s",
					            workflowType, dataType, userId));
		 }
	  
	  if(results != null && results.getDocs().size() > 1) {
		 throw new IngestionStrategyQueryException(
		          format("Find dag selection returned %s documents(s), expected 1, query by Workflow"
		                  + " type - %s, Data type - %s and User id - %s",
		                  results.getDocs().size(), workflowType, dataType, userId));
	  }
            
	 
	  IngestionStrategy ingestion = results.getDocs().get(0);
	  
      return ingestion;
  }

  private <T> T getSafety(Future<T> future, String errorMsg) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IngestionStrategyQueryException(errorMsg, e);
    } catch (ExecutionException e) {
      throw new IngestionStrategyQueryException(errorMsg, e);
    }
  }
  
 
 

 
}
