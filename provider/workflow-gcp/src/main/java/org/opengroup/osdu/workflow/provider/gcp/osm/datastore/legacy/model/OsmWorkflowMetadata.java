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

package org.opengroup.osdu.workflow.provider.gcp.osm.datastore.legacy.model;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;

//TODO can be removed if the data structure in the Datastore is changed to fit the model
@Getter
@Setter
@NonNull
@NoArgsConstructor
@AllArgsConstructor
public class OsmWorkflowMetadata extends WorkflowMetadata {

  private static final String KEY_DAG_NAME = "dagName";
  private String dagName;

  public OsmWorkflowMetadata(WorkflowMetadata workflowMetadata, String dagName) {
    super(
        workflowMetadata.getWorkflowId(),
        workflowMetadata.getWorkflowName(),
        workflowMetadata.getDescription(),
        workflowMetadata.getCreatedBy(),
        workflowMetadata.getCreationTimestamp(),
        workflowMetadata.getVersion(),
        workflowMetadata.isDeployedThroughWorkflowService(),
        workflowMetadata.getRegistrationInstructions(),
        workflowMetadata.isSystemWorkflow());
    this.dagName = dagName;
  }

  public WorkflowMetadata getWorkflowMetadataFromOsmModel() {
    return new WorkflowMetadata(
        this.getWorkflowId(),
        this.getWorkflowName(),
        this.getDescription(),
        this.getCreatedBy(),
        this.getCreationTimestamp(),
        this.getVersion(),
        this.isDeployedThroughWorkflowService(),
        ImmutableMap.of(KEY_DAG_NAME, Optional.ofNullable(dagName).orElse(this.getWorkflowName())),
        this.isSystemWorkflow());
  }
}
