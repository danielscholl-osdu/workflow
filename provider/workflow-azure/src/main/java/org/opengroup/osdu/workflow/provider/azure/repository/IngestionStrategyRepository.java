//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.workflow.provider.azure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.IngestionStrategyDoc;
import org.opengroup.osdu.workflow.provider.interfaces.IIngestionStrategyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
@Slf4j
@RequiredArgsConstructor
public class IngestionStrategyRepository implements IIngestionStrategyRepository {

  private static Logger logger = Logger.getLogger(IngestionStrategyRepository.class.getName());

  @Autowired
  private CosmosStore cosmosStore;

  @Autowired
  private DpsHeaders dpsHeaders;

  @Autowired
  private CosmosConfig cosmosConfig;

  @Override
  public IngestionStrategy findByWorkflowTypeAndDataTypeAndUserId(WorkflowType workflowType,
                                                                  String dataType, String userId) {
    logger.log(Level.INFO, String.format("Requesting dag selection. Workflow type: {%s}, Data type: {%s}, User id: {%s}",
      workflowType, dataType, userId));
    Optional<IngestionStrategyDoc> document = cosmosStore.findItem(
        dpsHeaders.getPartitionId(),
        cosmosConfig.getDatabase(),
        cosmosConfig.getIngestionStrategyCollection(),
        String.format("%s-%s", workflowType.toString().toLowerCase(), dataType.toLowerCase()),
        workflowType.toString().toLowerCase(),
        IngestionStrategyDoc.class);

    IngestionStrategyDoc ingestionStrategyDoc = !document.isPresent() ? null : document.get();

    IngestionStrategy ingestionStrategy = null;

    if(ingestionStrategyDoc != null) {
      logger.log(Level.INFO, String.format("Found dag: {%s}", ingestionStrategyDoc.dagName));
      ingestionStrategy = buildIngestionStrategy(ingestionStrategyDoc);
    }

    return ingestionStrategy;
  }

  private IngestionStrategy buildIngestionStrategy(IngestionStrategyDoc ingestionStrategyDoc) {
    logger.log(Level.INFO, String.format("Build ingestion strategy. Ingestion Strategy Doc: {%s}",
      ingestionStrategyDoc.toString()));

    return IngestionStrategy.builder()
      .workflowType(WorkflowType.valueOf(ingestionStrategyDoc.workflowType.toUpperCase()))
      .dataType(ingestionStrategyDoc.dataType)
      .userId(ingestionStrategyDoc.userId)
      .dagName(ingestionStrategyDoc.dagName)
      .build();
  }
}
