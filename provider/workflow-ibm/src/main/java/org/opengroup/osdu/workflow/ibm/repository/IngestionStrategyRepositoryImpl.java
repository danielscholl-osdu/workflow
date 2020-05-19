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
import static com.cloudant.client.api.query.Operation.and;
import static java.lang.String.format;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.DATA_TYPE;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.USER_ID;
import static org.opengroup.osdu.workflow.model.IngestionStrategy.Fields.WORKFLOW_TYPE;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.ibm.cloudant.ICloudantFactory;
import org.opengroup.osdu.workflow.ibm.exception.IngestionStrategyQueryException;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.repository.IngestionStrategyRepository;
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
public class IngestionStrategyRepositoryImpl implements IngestionStrategyRepository {

	
	@Inject
	private ICloudantFactory cloudantFactory;
	private Database db;

	public final static String DB_NAME = "ingestion-strategy";

	@PostConstruct
	public void init(){
		db = cloudantFactory.getDatabase(DB_NAME);
		log.debug("creating indexes...");
		db.createIndex(JsonIndex.builder().
				name(DB_NAME+"-index0").
				asc(WORKFLOW_TYPE,DATA_TYPE, USER_ID).
				definition());
		
	}

	@Override
	public IngestionStrategy findByWorkflowTypeAndDataTypeAndUserId(WorkflowType workflowType,
			DataType dataType, String userId) {
		log.debug("Requesting dag selection. Workflow type : {}, Data type : {}, User id : {}",
				workflowType, dataType, userId);

		Selector selector =
				and(eq(WORKFLOW_TYPE, workflowType.toString()),
					eq(DATA_TYPE, dataType.toString()),
					eq(USER_ID, userId));

		String query = new QueryBuilder(selector).build();
		QueryResult<IngestionStrategyDoc> documentsResult = db.query(query, IngestionStrategyDoc.class);
		List<IngestionStrategyDoc> documents = documentsResult.getDocs();		
		if (documents.size() > 1) {
			throw new IngestionStrategyQueryException(
					format(
							"Find dag selection returned %s documents(s), expected 1, query by Workflow type - %s, Data type - %s and User id - %s",
							documents.size(), workflowType, dataType, userId));
		}
		IngestionStrategy ingestionStrategy = documents.isEmpty()
				? null
						: IngestionStrategyDoc.getIngestionStrategyFromIngestionStrategyDoc(documents.get(0));

		log.debug("Found dag : {}", ingestionStrategy);
		return ingestionStrategy;
	}



}
