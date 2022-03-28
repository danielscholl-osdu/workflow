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

import static org.opengroup.osdu.core.gcp.osm.model.where.predicate.Eq.eq;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

import com.google.api.client.http.HttpStatusCodes;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.osm.model.query.GetQuery;
import org.opengroup.osdu.core.gcp.osm.service.Context;
import org.opengroup.osdu.core.gcp.osm.translate.TranslatorException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.gcp.config.WorkflowPropertiesConfiguration;
import org.opengroup.osdu.workflow.provider.gcp.osm.config.IDestinationProvider;
import org.opengroup.osdu.workflow.provider.gcp.repository.ICommonMetadataRepository;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
@Scope(SCOPE_SINGLETON)
@Slf4j
@Setter
@RequiredArgsConstructor
public class GcpOsmCommonMetadataRepository implements ICommonMetadataRepository {

  private static final String KEY_DAG_NAME = "dagName";
  public static final String WORKFLOW_NAME = "workflowName";
  private final WorkflowPropertiesConfiguration workflowConfig;
  private final IDestinationProvider destinationProvider;
  private final Context context;
  private final TenantInfo tenantInfo;
  private Class clazz = WorkflowMetadata.class;

  @Override
  public WorkflowMetadata createWorkflow(WorkflowMetadata workflowMetadata,
      boolean isSystemWorkflow) {

    Map<String, Object> instructions = workflowMetadata.getRegistrationInstructions();
    String workflowName = workflowMetadata.getWorkflowName();
    instructions.putIfAbsent(KEY_DAG_NAME,workflowName);

    if (Objects.isNull(workflowMetadata.getWorkflowId()) || workflowMetadata.getWorkflowId()
        .isEmpty()) {
      workflowMetadata.setWorkflowId(UUID.randomUUID().toString());
    }
    String tenant = getTenantName(isSystemWorkflow);

    boolean workflowExist = getWorkflowMetadataByWorkflowName(workflowName, tenant).stream()
        .findFirst()
        .isPresent();

    if (workflowExist) {
      throw new AppException(HttpStatus.CONFLICT.value(), "Conflict",
          String.format("Workflow with name %s already exists.",
              workflowName));
    }

    return context.upsertAndGet(workflowMetadata,
        destinationProvider.getDestination(tenant, workflowConfig.getWorkflowKind()));
  }

  @Override
  public WorkflowMetadata getWorkflow(String workflowName, boolean isSystemWorkflow) {
    log.info("Get details for workflow. Workflow name : {}", workflowName);
    String tenant = getTenantName(isSystemWorkflow);
    List<WorkflowMetadata> results = getWorkflowMetadataByWorkflowName(workflowName, tenant);
    return results.stream().findFirst()
        .orElseThrow(() -> new WorkflowNotFoundException(
            String.format("Workflow entity for workflow name: %s not found.", workflowName)));
  }


  @Override
  public void deleteWorkflow(String workflowName, boolean isSystemWorkflow) {
    log.info("Delete workflow. Workflow name : {}", workflowName);
    String tenant = getTenantName(isSystemWorkflow);
    try {
      context.delete(
          clazz,
          destinationProvider.getDestination(tenant, workflowConfig.getWorkflowKind()),
          eq(WORKFLOW_NAME, workflowName)
      );
    } catch (TranslatorException ex) {
      throw new PersistenceException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR,
          "Internal server error", ex.getMessage());
    }
  }

  @Override
  public List<WorkflowMetadata> getAllWorkflowForTenant(String prefix, boolean isSystemWorkflow) {
    log.info("Get all workflows. Prefix {}", prefix);
    String tenant = getTenantName(isSystemWorkflow);
    GetQuery<WorkflowMetadata> getQuery =
        new GetQuery<>(clazz,
            destinationProvider.getDestination(tenant, workflowConfig.getWorkflowKind()));
    List<WorkflowMetadata> workflowMetadataList = context.getResultsAsList(getQuery);

    return workflowMetadataList.stream().filter(c ->
            Objects.isNull(prefix) || c.getWorkflowName().startsWith(prefix))
        .collect(Collectors.toList());
  }

  private List<WorkflowMetadata> getWorkflowMetadataByWorkflowName(String workflowName,
      String tenant) {
    GetQuery<WorkflowMetadata> workflowMetadata =
        new GetQuery<>(
            clazz,
            destinationProvider.getDestination(tenant, workflowConfig.getWorkflowKind()),
            eq(WORKFLOW_NAME, workflowName)
        );
    return context.getResultsAsList(workflowMetadata);
  }

  private String getTenantName(boolean isSystemWorkflow) {
    return isSystemWorkflow ? workflowConfig.getSharedTenantName() : this.tenantInfo.getName();
  }
}
