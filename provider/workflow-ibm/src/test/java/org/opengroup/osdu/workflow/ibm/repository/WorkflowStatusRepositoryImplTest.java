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

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.ibm.cloudant.CloudantFactory;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes={WorkflowStatusRepositoryImpl.class, CloudantFactory.class})
class WorkflowStatusRepositoryImplTest {

	private static final String TEST_WORKFLOW_ID = "test-workflow-id";
	private static final String TEST_AIRFLOW_RUN_ID = "test-airflow-run-id";
	private static final String USER = "user-1";

	@Autowired
	private WorkflowStatusRepositoryImpl workflowStatusRepository  = new WorkflowStatusRepositoryImpl();

	@Test
	void WorkflowStatusRepositorySave() {
		WorkflowStatus workflowStatus = new WorkflowStatus();
		workflowStatus.setAirflowRunId(TEST_AIRFLOW_RUN_ID);
		workflowStatus.setSubmittedBy(USER);
		workflowStatus.setWorkflowId(TEST_WORKFLOW_ID);
		workflowStatus.setWorkflowStatusType(WorkflowStatusType.SUBMITTED);

		WorkflowStatus response = workflowStatusRepository.save(workflowStatus);
		assertNotNull("WorkflowStatusRepository was not save in the database", response);

	}


	@Test
	void shouldFindWorkflowStatusByWorkflowId() {
		WorkflowStatus workflowStatus = workflowStatusRepository.findWorkflowStatus(TEST_WORKFLOW_ID);
		assertNotNull("Could not find the workflow status in the database", workflowStatus);
	}

}
