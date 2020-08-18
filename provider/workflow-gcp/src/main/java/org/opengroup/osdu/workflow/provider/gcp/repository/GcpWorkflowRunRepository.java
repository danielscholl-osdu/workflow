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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.EntityQuery.Builder;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.multitenancy.IDatastoreFactory;
import org.opengroup.osdu.workflow.model.WorkflowRun;
import org.opengroup.osdu.workflow.model.WorkflowRunsPage;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.gcp.config.WorkflowPropertiesConfiguration;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowRunRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcpWorkflowRunRepository implements IWorkflowRunRepository {
  private static final String INCORRECT_RUN_ID_PREFIX = "backfill";
  private static final String KEY_AIRFLOW_RUN_ID = "AirflowRunID";
  private static final String KEY_WORKFLOW_ID = "WorkflowID";
  private static final String KEY_RUN_ID = "RunID";
  private static final String KEY_WORKFLOW_NAME = "WorkflowName";
  private static final String KEY_STATUS = "Status";
  private static final String KEY_START_TIME = "StartTimeStamp";
  private static final String KEY_END_TIME = "EndTimeStamp";
  private static final String KEY_SUBMITTED_BY = "SubmittedBy";

  private final WorkflowPropertiesConfiguration propertiesConfig;
  private final TenantInfo tenantInfo;
  private Map<String, Datastore> tenantRepositories = new HashMap<String, Datastore>();
  private final IDatastoreFactory datastoreFactory;
  private final ITenantFactory tenantFactory;

  @Override
  public WorkflowRun saveWorkflowRun(WorkflowRun workflowRun) {
    Datastore ds = getDatastore();
    Transaction txn = ds.newTransaction();
    String workflowName = workflowRun.getWorkflowName();
    log.info("Saving workflow run. Workflow name : {}", workflowName);
    try {
      EntityQuery.Builder queryBuilder = Query.newEntityQueryBuilder()
          .setKind(this.propertiesConfig.getWorkflowRunDatastoreKind())
          .setFilter(CompositeFilter.and(
              PropertyFilter.eq(KEY_WORKFLOW_NAME, workflowName),
              PropertyFilter.eq(KEY_AIRFLOW_RUN_ID, workflowRun.getRunId())));
      QueryResults<Entity> tasks = ds.run(queryBuilder.build());
      Entity entity;
      if (tasks.hasNext()) {
        Key key = tasks.next().getKey();
        entity = convertWorkflowRunToEntity(workflowRun, key);
      } else {
        entity = convertWorkflowRunToEntity(workflowRun, null);
      }
      txn.put(entity);
      txn.commit();
    } catch (DatastoreException ex) {
      throw new PersistenceException(ex.getCode(), ex.getMessage(), ex.getReason());
    } finally {
      if (txn.isActive()) {
        txn.rollback();
      }
    }
    return workflowRun;
  }

  @Override
  public WorkflowRun getWorkflowRun(String workflowName, String runId) {
    log.info(
        "Get execution instances for workflow. Workflow name : {}, run Id : {}", workflowName,
        runId);
    Datastore ds = getDatastore();
    EntityQuery.Builder queryBuilder = Query.newEntityQueryBuilder()
        .setKind(this.propertiesConfig.getWorkflowRunDatastoreKind())
        .setFilter(CompositeFilter.and(
            PropertyFilter.eq(KEY_WORKFLOW_NAME, workflowName),
            PropertyFilter.eq(KEY_AIRFLOW_RUN_ID, runId)));
    QueryResults<Entity> tasks = ds.run(queryBuilder.build());
    if (tasks.hasNext()) {
      return buildWorkflowRunFromDataStoreEntity(tasks.next());
    }
    throw new BadRequestException(
        String.format("Workflow entity for workflow name: %s and run id: %s not found.",
            workflowName,
            runId));
  }

  @Override
  public WorkflowRunsPage getWorkflowRunsByWorkflowName(String workflowName, Integer limit,
      String cursor) {
    WorkflowRunsPage page = new WorkflowRunsPage();
    page.setItems(new ArrayList<>());

    List<WorkflowRun> pageItems = page.getItems();
    Datastore ds = getDatastore();
    EntityQuery.Builder queryBuilder = getBaseQueryBuilder(workflowName);
    QueryResults<Entity> tasks = ds.run(queryBuilder.build());
    while (tasks.hasNext()) {
      pageItems.add(buildWorkflowRunFromDataStoreEntity(tasks.next()));
    }
    return page;
  }

  @Override
  public void deleteWorkflowRuns(String workflowName, List<String> runIds) {
    log.info("Delete workflow run with id's. Workflow name : {}", workflowName);
    Datastore ds = getDatastore();
    for (String runId : runIds) {
      Transaction txn = null;
      try {
        EntityQuery.Builder queryBuilder = Query.newEntityQueryBuilder()
            .setKind(this.propertiesConfig.getWorkflowRunDatastoreKind())
            .setFilter(CompositeFilter.and(
                PropertyFilter.eq(KEY_WORKFLOW_NAME, workflowName),
                PropertyFilter.eq(KEY_AIRFLOW_RUN_ID, runId)));
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
  }

  @Override
  public WorkflowRun updateWorkflowRun(WorkflowRun workflowRun) {
    log.info("Update called for workflow id: {}, run id: {}",
        workflowRun.getWorkflowId(), workflowRun.getRunId());
    Datastore ds = getDatastore();
    Transaction txn = ds.newTransaction();
    String workflowName = workflowRun.getWorkflowName();
    log.info("Saving workflow run. Workflow name : {}", workflowName);
    try {
      EntityQuery.Builder queryBuilder = Query.newEntityQueryBuilder()
          .setKind(this.propertiesConfig.getWorkflowRunDatastoreKind())
          .setFilter(CompositeFilter.and(
              PropertyFilter.eq(KEY_WORKFLOW_NAME, workflowName),
              PropertyFilter.eq(KEY_AIRFLOW_RUN_ID, workflowRun.getRunId())));
      QueryResults<Entity> tasks = ds.run(queryBuilder.build());
      if (tasks.hasNext()) {
        Key key = tasks.next().getKey();
        Entity entity = convertWorkflowRunToEntity(workflowRun, key);
        txn.put(entity);
        txn.commit();
        return workflowRun;
      }
    } catch (DatastoreException ex) {
      throw new PersistenceException(ex.getCode(), ex.getMessage(), ex.getReason());
    } finally {
      if (txn.isActive()) {
        txn.rollback();
      }
    }
    String errMessage = String.format("Workflow run with name %s exists", workflowName);
    throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), errMessage,
        errMessage);
  }

  @Override
  public List<WorkflowRun> getAllRunInstancesOfWorkflow(String workflowName,
      Map<String, Object> params) {
    log.info("Get all run instances of workflow. Workflow name : {}", workflowName);
    List<WorkflowRun> responseList = new ArrayList<>();
    Datastore ds = getDatastore();
    EntityQuery.Builder queryBuilder = getBaseQueryBuilder(workflowName);
    QueryResults<Entity> tasks = ds.run(queryBuilder.build());
    while (tasks.hasNext()) {
      responseList.add(buildWorkflowRunFromDataStoreEntity(tasks.next()));
    }
    if (!params.isEmpty()) {
      return filterWorkflowEntities(responseList, params);
    }
    return responseList;
  }

  private String getAirflowRunIdToEntity(WorkflowRun workflowRun) {
    Datastore ds = getDatastore();
    EntityQuery.Builder queryBuilder = Query.newEntityQueryBuilder()
        .setKind(this.propertiesConfig.getWorkflowStatusDatastoreKind())
        .setFilter(
            PropertyFilter.eq(KEY_RUN_ID, workflowRun.getRunId()));
    QueryResults<Entity> tasks = ds.run(queryBuilder.build());
    if (tasks.hasNext()) {
      return tasks.next().getString(KEY_AIRFLOW_RUN_ID);
    }
    return "";
  }

  private Builder getBaseQueryBuilder(String workflowName) {
    return Query.newEntityQueryBuilder()
        .setKind(this.propertiesConfig.getWorkflowRunDatastoreKind())
        .setFilter(PropertyFilter.eq(KEY_WORKFLOW_NAME, workflowName));
  }

  private Entity convertWorkflowRunToEntity(WorkflowRun workflowRun, Key newKey) {
    Datastore ds = getDatastore();
    String airflowRunId = workflowRun.getRunId();
    if (Objects.isNull(newKey)) {
      newKey = ds.newKeyFactory()
          .setKind(this.propertiesConfig.getWorkflowRunDatastoreKind())
          .newKey(UUID.randomUUID().toString());
      airflowRunId = getAirflowRunIdToEntity(workflowRun);
    }
    return Entity.newBuilder(newKey)
        .set(KEY_WORKFLOW_ID,
            workflowRun.getWorkflowId() == null ? "" : workflowRun.getWorkflowId())
        .set(KEY_WORKFLOW_NAME,
            workflowRun.getWorkflowName() == null ? "" : workflowRun.getWorkflowName())
        .set(KEY_AIRFLOW_RUN_ID,
            airflowRunId == null ? "" : airflowRunId)
        .set(KEY_SUBMITTED_BY,
            workflowRun.getSubmittedBy() == null ? "" : workflowRun.getSubmittedBy())
        .set(KEY_START_TIME,
            workflowRun.getStartTimeStamp() == null ? 0 : workflowRun.getStartTimeStamp())
        .set(KEY_END_TIME,
            workflowRun.getEndTimeStamp() == null ? 0 : workflowRun.getEndTimeStamp())
        .set(KEY_STATUS, workflowRun.getStatus() == null ? "" :
            workflowRun.getStatus().name())
        .build();
  }

  private List<WorkflowRun> filterWorkflowEntities(List<WorkflowRun> workflowRunList,
      Map<String, Object> params) {
    List<WorkflowRun> resultList;
    String prefix = (String) params.get("prefix");
    String startDate = (String) params.get("startDate");
    String endDate = (String) params.get("endDate");
    String limit = (String) params.get("limit");

    resultList = workflowRunList.stream().filter(c -> {
      if (INCORRECT_RUN_ID_PREFIX.equals(prefix) || Objects.nonNull(prefix) &&
          !c.getRunId().startsWith(prefix)) {
        return false;
      }
      if (Objects.nonNull(startDate) && c.getStartTimeStamp() <= Long.parseLong(startDate)) {
        return false;
      }
      if (Objects.nonNull(endDate) && c.getEndTimeStamp() >= Long.parseLong(endDate)) {
        return false;
      }
      return true;
    }).collect(Collectors.toList());

    if (Objects.nonNull(limit) && resultList.size() > Integer.parseInt(limit)) {
      return resultList.subList(0, Integer.parseInt(limit));
    }
    return resultList;
  }

  private WorkflowRun buildWorkflowRunFromDataStoreEntity(Entity entity) {
    return WorkflowRun.builder()
        .runId(entity.getString(KEY_AIRFLOW_RUN_ID))
        .status(entity.getString(KEY_STATUS) == "" ?
            null : WorkflowStatusType.valueOf(entity.getString(KEY_STATUS)))
        .startTimeStamp(entity.getLong(KEY_START_TIME))
        .endTimeStamp(entity.getLong(KEY_END_TIME))
        .submittedBy(entity.getString(KEY_SUBMITTED_BY))
        .workflowId(entity.getString(KEY_WORKFLOW_ID))
        .workflowName(entity.getString(KEY_WORKFLOW_NAME))
        .build();
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
