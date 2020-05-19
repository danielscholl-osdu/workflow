/*
 * Copyright 2020 IBM Corp
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

package org.opengroup.osdu.workflow.ibm.repository;

import static com.cloudant.client.api.query.Expression.eq;
import static org.opengroup.osdu.workflow.model.WorkflowStatus.Fields.WORKFLOW_ID;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.opengroup.osdu.core.ibm.cloudant.DBUtil;
import org.opengroup.osdu.core.ibm.cloudant.ICloudantFactory;
import org.opengroup.osdu.workflow.ibm.exception.WorkflowStatusQueryException;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.repository.WorkflowStatusRepository;
import org.springframework.stereotype.Repository;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.JsonIndex;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.api.query.Selector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// TODO Will be moved to registry service
@Repository
@Slf4j
@RequiredArgsConstructor
public class WorkflowStatusRepositoryImpl implements WorkflowStatusRepository {

	@Inject
	private ICloudantFactory cloudantFactory;
	private Database db;

	public final static String DB_NAME = "workflow-status";

	@PostConstruct
	public void init(){
		db = cloudantFactory.getDatabase(DB_NAME);
		System.out.println("creating indexes...");
		db.createIndex(JsonIndex.builder().
				name(DB_NAME+"-index0").
				asc(WORKFLOW_ID).
				definition());
	}

	@Override
	public WorkflowStatus findWorkflowStatus(String workflowId) {
		log.debug("Requesting workflow status by workflow id - {}", workflowId);
		Selector selector = eq(WORKFLOW_ID, workflowId);
		QueryResult<WorkflowStatusDoc> documentsResult = db.query(new QueryBuilder(selector).build(), WorkflowStatusDoc.class);
		List<WorkflowStatusDoc> documents = documentsResult.getDocs();
		if (documents.size() > 1) {
			throw new WorkflowStatusQueryException(
					String.format(
							"Find workflow status returned %s documents(s), expected 1, query by Workflow id - %s",
							documents.size(), workflowId));
		}
		WorkflowStatus workflowStatus = documents.isEmpty()
				? null
						: WorkflowStatusDoc.getWorkflowStatusFromWorkflowStatusDoc(documents.get(0));

		log.debug("Found workflow status : {}", workflowStatus);
		return workflowStatus;
	}

	@Override
	public WorkflowStatus save(WorkflowStatus workflowStatus) {
		log.info("Saving workflow status  location : {}", workflowStatus);
		WorkflowStatusDoc doc = WorkflowStatusDoc.getWorkflowStatusDocFromWorkflowStatus(workflowStatus);
		boolean success = DBUtil.saveIfDoesNotExist(db, doc, WorkflowStatusDoc.class, WORKFLOW_ID, doc.getWorkflowId());
		if (success) {
			return workflowStatus;
		} else {
			return null;
		}
	}
	

	
	
	
	


}
