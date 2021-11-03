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

package org.opengroup.osdu.workflow.provider.gcp.osm.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.osm.service.Context;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.provider.gcp.config.WorkflowPropertiesConfiguration;
import org.opengroup.osdu.workflow.provider.gcp.osm.config.IDestinationProvider;
import org.opengroup.osdu.workflow.provider.gcp.repository.IWorkflowStatusRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcpOsmWorkflowStatusRepository implements IWorkflowStatusRepository {

  private final WorkflowPropertiesConfiguration workflowConfig;
  private final IDestinationProvider destinationProvider;
  private final Context context;
  private final TenantInfo tenantInfo;

  @Override
  public WorkflowStatus saveWorkflowStatus(WorkflowStatus workflowStatus) {
    log.info("Saving workflow status. Workflow status id : {}", workflowStatus.getWorkflowId());
    return context.upsertAndGet(workflowStatus, this.destinationProvider.getDestination(tenantInfo,
        workflowConfig.getWorkflowStatusKind()));
  }


}
