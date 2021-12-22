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

package org.opengroup.osdu.workflow.provider.gcp.osm.datastore.legacy.config;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

import com.google.cloud.datastore.Key;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.HashMap;
import org.opengroup.osdu.core.gcp.osm.persistence.IdentityTranslator;
import org.opengroup.osdu.core.gcp.osm.translate.Instrumentation;
import org.opengroup.osdu.core.gcp.osm.translate.TypeMapper;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.provider.gcp.osm.datastore.legacy.model.OsmWorkflowMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

//TODO can be removed if the data structure in the Datastore is changed to fit the model
@Primary
@Component
@Scope(SCOPE_SINGLETON)
@ConditionalOnProperty(name = "datastore.legacy.data.structure", havingValue = "true")
public class DatastoreTypeMapperImpl extends TypeMapper {

  public DatastoreTypeMapperImpl() {
    super(ImmutableList.of(
        new Instrumentation<>(OsmWorkflowMetadata.class,
            new HashMap<String, String>() {{
              put("workflowName", "WorkflowName");
              put("createdBy", "CreatedBy");
              put("creationTimestamp", "CreationTimestamp");
              put("description", "Description");
              put("version", "Version");
              put("dagName", "DagName");
            }},
            null,
            new IdentityTranslator<>(
                WorkflowMetadata::getWorkflowId,
                ((w, o) -> w.setWorkflowId(((Key) o).getName()))
            ),
            Collections.singletonList("workflowId")
        ),
        new Instrumentation<>(WorkflowRun.class,
            new HashMap<String, String>() {{
              put("workflowId", "WorkflowID");
              put("workflowName", "WorkflowName");
              put("startTimeStamp", "StartTimeStamp");
              put("endTimeStamp", "EndTimeStamp");
              put("status", "Status");
              put("submittedBy", "SubmittedBy");
            }},
            null,
            new IdentityTranslator<>(
                WorkflowRun::getRunId,
                ((w, o) -> w.setRunId(((Key) o).getName()))
            ),
            Collections.singletonList("runId")),
        new Instrumentation<>(WorkflowStatus.class,
            new HashMap<String, String>() {{
              put("workflowId", "WorkflowID");
              put("airflowRunId", "AirflowRunID");
              put("workflowStatusType", "Status");
            }},
            null,
            new IdentityTranslator<>(
                WorkflowStatus::getAirflowRunId,
                ((w, o) -> w.setAirflowRunId(((Key) o).getName()))
            ),
            Collections.singletonList("airflowRunId")))
    );
  }
}
