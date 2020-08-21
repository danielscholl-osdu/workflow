// Copyright © 2020 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.workflow.aws.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.workflow.aws.util.dynamodb.converters.IngestionStrategyDoc;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.provider.interfaces.IIngestionStrategyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

// TODO Will be moved to registry service
@Repository
@Slf4j
@RequiredArgsConstructor
public class IngestionStrategyRepositoryImpl implements IIngestionStrategyRepository {

  @Value("${aws.dynamodb.table.prefix}")
  String tablePrefix;
  @Value("${aws.dynamodb.region}")
  String dynamoDbRegion;
  @Value("${aws.dynamodb.endpoint}")
  String dynamoDbEndpoint;
  private DynamoDBQueryHelper queryHelper;

  @PostConstruct
  public void init() {
    queryHelper = new DynamoDBQueryHelper(dynamoDbEndpoint, dynamoDbRegion, tablePrefix);
  }

  @Override
  public IngestionStrategy findByWorkflowTypeAndDataTypeAndUserId(WorkflowType workflowType, String dataType, String userId) {

    String compositeKey = String.join(":", workflowType.toString(), dataType, userId);
    IngestionStrategyDoc doc = queryHelper.loadByPrimaryKey(IngestionStrategyDoc.class, compositeKey);

    if (doc != null) {
      IngestionStrategy ingestionStrategy = new IngestionStrategy();
      ingestionStrategy.setWorkflowType(WorkflowType.valueOf(doc.getWorkflowType()));
      ingestionStrategy.setDataType(dataType);
      ingestionStrategy.setUserId(doc.getUserId());
      ingestionStrategy.setDagName(doc.getDagName());

      return ingestionStrategy;
    } else {
      return null;
    }
  }
}
