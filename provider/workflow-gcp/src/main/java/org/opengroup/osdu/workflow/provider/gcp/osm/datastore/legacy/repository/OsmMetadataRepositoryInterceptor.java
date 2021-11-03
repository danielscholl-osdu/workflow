/*
 *  Copyright 2020-2021 Google LLC
 *  Copyright 2020-2021 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.workflow.provider.gcp.osm.datastore.legacy.repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.gcp.osm.datastore.legacy.model.OsmWorkflowMetadata;
import org.opengroup.osdu.workflow.provider.gcp.osm.repository.GcpOsmCommonMetadataRepository;
import org.opengroup.osdu.workflow.provider.gcp.repository.ICommonMetadataRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

//TODO can be removed if the data structure in the Datastore is changed to fit the model
@Primary
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "datastore.legacy.data.structure", havingValue = "true")
public class OsmMetadataRepositoryInterceptor implements ICommonMetadataRepository {

  private static final String KEY_DAG_NAME = "dagName";
  private final GcpOsmCommonMetadataRepository commonMetadataRepository;

  @PostConstruct
  public void setUpRepository() {
    commonMetadataRepository.setClazz(OsmWorkflowMetadata.class);
  }

  @Override
  public WorkflowMetadata createWorkflow(WorkflowMetadata workflowMetadata,
      boolean isSystemWorkflow) {
    Map<String, Object> instructions = workflowMetadata.getRegistrationInstructions();
    String dagName = (Objects.nonNull(instructions) 
      ? (String) instructions.get(KEY_DAG_NAME) 
      : workflowMetadata.getWorkflowName());
    WorkflowMetadata metadata = commonMetadataRepository.createWorkflow(
        new OsmWorkflowMetadata(workflowMetadata, dagName), isSystemWorkflow);
    if (metadata instanceof OsmWorkflowMetadata) {
      return ((OsmWorkflowMetadata) metadata).getWorkflowMetadataFromOsmModel();
    } else {
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal server error",
          "Misconfigured repository");
    }
  }

  @Override
  public WorkflowMetadata getWorkflow(String workflowName, boolean isSystemWorkflow) {
    WorkflowMetadata metadata =
        commonMetadataRepository.getWorkflow(workflowName, isSystemWorkflow);
    if (metadata instanceof OsmWorkflowMetadata) {
      return ((OsmWorkflowMetadata) metadata).getWorkflowMetadataFromOsmModel();
    } else {
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal server error",
          "Misconfigured repository");
    }
  }

  @Override
  public void deleteWorkflow(String workflowName, boolean isSystemWorkflow) {
    commonMetadataRepository.deleteWorkflow(workflowName, isSystemWorkflow);
  }

  @Override
  public List<WorkflowMetadata> getAllWorkflowForTenant(String prefix, boolean isSystemWorkflow) {
    return commonMetadataRepository.getAllWorkflowForTenant(prefix, isSystemWorkflow).stream()
        .filter(OsmWorkflowMetadata.class::isInstance)
        .map(OsmWorkflowMetadata.class::cast)
        .map(OsmWorkflowMetadata::getWorkflowMetadataFromOsmModel)
        .collect(Collectors.toList());
  }
}
