/*
  Copyright 2021 Google LLC
  Copyright 2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.opengroup.osdu.workflow.provider.gcp.repository;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.EntityQuery.Builder;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.core.common.exception.NotFoundException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.multitenancy.IDatastoreFactory;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.exception.IntegrationException;
import org.opengroup.osdu.workflow.exception.RuntimeException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.gcp.config.WorkflowPropertiesConfiguration;
import org.opengroup.osdu.workflow.provider.gcp.service.GoogleIapHelper;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcpWorkflowMetadataRepository implements IWorkflowMetadataRepository {
  private static final String KEY_DAG_NAME = "dagName";
  private static final String DAG_NAME = "DagName";
  private static final String WORKFLOW_NAME = "WorkflowName";
  private static final String DESCRIPTION = "Description";
  private static final String CREATED_BY = "CreatedBy";
  private static final String CREATION_TIME_STAMP = "CreationTimestamp";
  private static final String VERSION = "Version";

  private Map<String, Datastore> tenantRepositories = new HashMap<>();
  private final AirflowConfig airflowConfig;
  private final WorkflowPropertiesConfiguration workflowConfig;
  private final GoogleIapHelper googleIapHelper;
  private final TenantInfo tenantInfo;
  private final IDatastoreFactory datastoreFactory;
  private final ITenantFactory tenantFactory;


  @Override
  public WorkflowMetadata createWorkflow(WorkflowMetadata workflowMetadata) {
    log.info("Saving workflow : {}", workflowMetadata);

    String dagName = null;
    Map<String, Object> instructions = workflowMetadata.getRegistrationInstructions();
    if (Objects.nonNull(instructions)) {
      dagName = (String) instructions.get(KEY_DAG_NAME);
    }
    if (Objects.isNull(dagName)) {
      dagName = workflowMetadata.getWorkflowName();
    }
    // validate DAG name
    try {
      String airflowUrl = this.airflowConfig.getUrl();
      String iapClientId = this.googleIapHelper.getIapClientId(airflowUrl);
      String webServerUrl = String.format("%s/api/experimental/latest_runs", airflowUrl);

      HttpRequest httpRequest = this.googleIapHelper.buildIapGetRequest(webServerUrl, iapClientId);
      HttpResponse response = httpRequest.execute();
      String content = IOUtils.toString(response.getContent(), UTF_8);

      if (Objects.nonNull(content)) {
        JSONParser parser = new JSONParser(content);
        LinkedHashMap<String, ArrayList<LinkedHashMap<String, String>>> dagList =
            (LinkedHashMap<String, ArrayList<LinkedHashMap<String, String>>>) parser.parse();
        List<LinkedHashMap<String, String>> items = dagList.get("items");
        String finalDagName = dagName;
        if (items.stream().noneMatch(c -> c.get("dag_id").equals(finalDagName))) {
          throw new BadRequestException(
              String.format("DAG with name %s doesn't exist.", dagName));
        }
      }
    } catch (HttpResponseException e) {
      throw new IntegrationException("Airflow request fail", e);
    } catch (IOException | ParseException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    saveWorkflowMetadata(workflowMetadata, dagName);
    resetWorkflowMetadata(workflowMetadata);
    log.info("Fetch saved workflow : {}. DAG name {}", workflowMetadata, dagName);
    return workflowMetadata;
  }

  @Override
  public WorkflowMetadata getWorkflow(String workflowName) {
    log.info("Get details for workflow. Workflow name : {}", workflowName);
    Datastore ds = getDatastore();
    EntityQuery.Builder queryBuilder = getBaseQueryBuilder(workflowName);
    QueryResults<Entity> tasks = ds.run(queryBuilder.build());
    if (tasks.hasNext()) {
      return convertEntityToWorkflowMetadata(tasks.next());
    }
    throw new NotFoundException(
        String.format("Workflow entity for workflow name: %s not found.", workflowName));
  }

  @Override
  public void deleteWorkflow(String workflowName) {
    log.info("Delete workflow. Workflow name : {}", workflowName);
    Transaction txn = null;
    Datastore ds = getDatastore();
    try {
      EntityQuery.Builder queryBuilder = getBaseQueryBuilder(workflowName);
      QueryResults<Entity> tasks = ds.run(queryBuilder.build());
      if (tasks.hasNext()) {
        Key key = tasks.next().getKey();
        txn = ds.newTransaction();
        txn.delete(key);
        txn.commit();
      }
    } catch (DatastoreException ex) {
      throw new PersistenceException(ex.getCode(), ex.getMessage(), ex.getReason());
    } finally {
      if (txn != null && txn.isActive()) {
        txn.rollback();
      }
    }
  }

  @Override
  public List<WorkflowMetadata> getAllWorkflowForTenant(final String prefix) {
    log.info("Get all workflows. Prefix {}", prefix);
    Datastore ds = getDatastore();
    List<WorkflowMetadata> responseList = new ArrayList<>();
    EntityQuery.Builder queryBuilder = Query.newEntityQueryBuilder()
        .setKind(this.workflowConfig.getWorkflowKind());
    QueryResults<Entity> tasks = ds.run(queryBuilder.build());
    while (tasks.hasNext()) {
      responseList.add(convertEntityToWorkflowMetadata(tasks.next()));
    }
    return responseList.stream().filter(c -> {
      if (Objects.isNull(prefix) ||
          Objects.nonNull(prefix) && c.getWorkflowName().startsWith(prefix)) {
        return true;
      }
      return false;
    }).collect(Collectors.toList());
  }

  private void saveWorkflowMetadata(WorkflowMetadata workflowMetadata, String dagName) {
    Datastore ds = getDatastore();
    Transaction txn = ds.newTransaction();
    String workflowName = workflowMetadata.getWorkflowName();
    try {
      EntityQuery.Builder queryBuilder = getBaseQueryBuilder(workflowName);
      QueryResults<Entity> tasks = ds.run(queryBuilder.build());
      if (!tasks.hasNext()) {
        Entity entity = convertWorkflowMetadataToEntity(workflowMetadata, dagName);
        txn.put(entity);
        txn.commit();
        workflowMetadata.setWorkflowId(entity.getKey().getName());
      } else {
        throw new AppException(HttpStatus.CONFLICT.value(), "Conflict", String.format("Workflow with name %s already exists.", workflowName));
      }
    } catch (DatastoreException ex) {
      throw new PersistenceException(ex.getCode(), ex.getMessage(), ex.getReason());
    } finally {
      if (txn.isActive()) {
        txn.rollback();
      }
    }
  }

  private void resetWorkflowMetadata(WorkflowMetadata workflowMetadata) {
    workflowMetadata.setRegistrationInstructions(null);
  }

  private Entity convertWorkflowMetadataToEntity(WorkflowMetadata workflowMetadata, String dagName) {
    Datastore ds = getDatastore();
    Key taskKey = ds.newKeyFactory()
        .setKind(this.workflowConfig.getWorkflowKind())
        .newKey(UUID.randomUUID().toString());
    return Entity.newBuilder(taskKey)
        .set(DAG_NAME, dagName == null ? "" : dagName)
        .set(WORKFLOW_NAME, workflowMetadata.getWorkflowName())
        .set(DESCRIPTION, workflowMetadata.getDescription() == null ?
            "" : workflowMetadata.getDescription())
        .set(CREATED_BY, workflowMetadata.getCreatedBy() == null ?
            "" : workflowMetadata.getCreatedBy())
        .set(CREATION_TIME_STAMP, workflowMetadata.getCreationTimestamp() == null ?
            0 : workflowMetadata.getCreationTimestamp())
        .set(VERSION, workflowMetadata.getVersion() == null ? 0 : workflowMetadata.getVersion())
        .build();
  }

  private WorkflowMetadata convertEntityToWorkflowMetadata(Entity entity) {
    return WorkflowMetadata.builder()
        .workflowId(entity.getKey().getName())
        .workflowName(entity.getString(WORKFLOW_NAME))
        .createdBy(entity.getString(CREATED_BY))
        .creationTimestamp(entity.getLong(CREATION_TIME_STAMP))
        .description(entity.getString(DESCRIPTION))
        .version(entity.getLong(VERSION))
        .registrationInstructions(Collections.singletonMap(KEY_DAG_NAME, entity.getString(DAG_NAME)))
        .build();
  }

  private Builder getBaseQueryBuilder(String workflowName) {
    return Query.newEntityQueryBuilder()
        .setKind(this.workflowConfig.getWorkflowKind())
        .setFilter(PropertyFilter.eq(WORKFLOW_NAME, workflowName));
  }

  private Datastore getDatastore() {
    String tenantName = this.tenantInfo.getName();
    if (!this.tenantRepositories.containsKey(tenantName)) {
      TenantInfo info = this.tenantFactory.getTenantInfo(tenantName);
      Datastore datastore = this.datastoreFactory.getDatastore(info);
      this.tenantRepositories.put(tenantName, datastore);
    }
    return this.tenantRepositories.get(tenantName);
  }
}
