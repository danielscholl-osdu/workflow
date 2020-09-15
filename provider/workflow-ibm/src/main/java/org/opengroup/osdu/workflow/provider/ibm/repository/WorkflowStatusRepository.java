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


import java.net.MalformedURLException;
import java.util.Date;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.auth.ServiceCredentials;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cloudant.client.api.Database;





@Repository
@Slf4j
@RequiredArgsConstructor
public class WorkflowStatusRepository implements IWorkflowStatusRepository {

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
	
	@Autowired
	private DpsHeaders headers;
	
	@Autowired
	private TenantInfo tenant;

	
	private WorkflowStatusDoc document ;

	private IBMCloudantClientFactory cloudantFactory;
	private Database db;
  private static final String COLLECTION_NAME = "workflow-status";

  @PostConstruct
	public void init() throws MalformedURLException {
		
			cloudantFactory = new IBMCloudantClientFactory(new ServiceCredentials(dbUrl,dbUser,dbPassword));
		
		db = cloudantFactory.getDatabase(dbNamePrefix, COLLECTION_NAME);
				
	// TODO-- need to chk if indexing needs to be done here.	
	//	db.createIndex(JsonIndex.builder().name("kind-json-index").asc("kind").definition());
	//	db.createIndex(JsonIndex.builder().name("legalTagsNames-json-index").asc("legalTagsNames").definition());
	}

  @Override
  public WorkflowStatus findWorkflowStatus(String workflowId) {
	  log.debug("Requesting workflow status by workflow id - {}", workflowId);
	  
	  tenant.getName();
	  
	 
	try {
		document = db.find(WorkflowStatusDoc.class,workflowId);	
		
	} catch (Exception e) {
		// TODO Auto-generated catch block
		throw new WorkflowStatusQueryException( String.format("Failed to find a workflow status by Workflow id - %s", workflowId));
	}
	  	   
	if (document== null) {
		  throw new WorkflowNotFoundException(
		          String.format("No  workflow status found in"
		                  + " Workflow id - %s",
		               workflowId));
		  
	 }
		 
	  WorkflowStatus workflowstatus = buildWorkflowStatus(document);
		    	
      return workflowstatus;
  }

  @Override
  public WorkflowStatus saveWorkflowStatus(WorkflowStatus workflowStatus) {

	  tenant.getName();
	  log.info("Saving workflow status  location : {}", workflowStatus);
	  
	  // TODO -- chk for date   
	  final String errorMsg = "Exceptions during saving  workflow status: " + workflowStatus;
	       
	  workflowStatus.setSubmittedBy(headers.getUserEmail());
	  workflowStatus.setSubmittedAt(new Date());
	    
	  WorkflowStatusDoc workflowstatusdoc = new WorkflowStatusDoc(workflowStatus);
	    
	  try {
		  	  db.save(workflowstatusdoc);
		  
	  }catch(Exception e) {
		
		  throw new WorkflowStatusNotSavedException( errorMsg);
			
	  }
	 	 	     
	  return workflowStatus;
    
  }

  @Override
  public WorkflowStatus updateWorkflowStatus(String workflowId,
      WorkflowStatusType workflowStatusType) {

	  tenant.getName();
	  log.info("Update workflow status  for workflow id: {}, new status: {}", workflowId,
		        workflowStatusType);
	
	 
		try {
			document = db.find(WorkflowStatusDoc.class,workflowId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new WorkflowStatusQueryException( String.format("Failed to find a workflow status by Workflow id - %s", workflowId));
		}
	   
	  
	  if (document== null) {
		  throw new WorkflowStatusNotFoundException(
		          String.format("Workflow status for Workflow id: %s not found", workflowId));
		  
	  }
	  
	  if (document.getWorkflowStatusType().equals(workflowStatusType)) {
	      throw new WorkflowStatusNotUpdatedException(String.format(
	          "Workflow status for workflow id: %s already has status:%s and can not be updated",
	          workflowId, workflowStatusType));
	    }
	  
	  document.setWorkflowStatusType(workflowStatusType);
	
	 try {
	  
	  db.update(document);
	  
	 }catch (Exception e) {
		 
		 throw new WorkflowStatusQueryException( String.format("Failed to update a workflow status by Workflow id - %s", workflowId));
	 }
	  
    return buildWorkflowStatus(document);
  }

 
  

  private WorkflowStatus buildWorkflowStatus(WorkflowStatusDoc document) {
	  
	return WorkflowStatus.builder()
		        .workflowId(document.get_id())
		        .airflowRunId(document.getAirflowRunId())
		        .workflowStatusType(document.getWorkflowStatusType())
		        .submittedAt(document.getSubmittedAt())
		        .submittedBy(document.getSubmittedBy()).build();
	  
	 	  
	  }
 
}
