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
import org.opengroup.osdu.core.ibm.cloudant.DBUtil;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes={IngestionStrategyRepositoryImpl.class, CloudantFactory.class, DBUtil.class})
class IngestionStrategyRepositoryImplTest {

  private static final String USER = "user-1";

  @Autowired
  private IngestionStrategyRepositoryImpl ingestionStrategyRepository  = new IngestionStrategyRepositoryImpl();

  @Test
  void shouldFindIngestionStrategyByWorkflowId() {
    IngestionStrategy ingestionStrategy = ingestionStrategyRepository
        .findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.INGEST, DataType.WELL_LOG, USER);
    assertNotNull("Could not find the ingestion strategy in the database", ingestionStrategy);
  }
  
}
