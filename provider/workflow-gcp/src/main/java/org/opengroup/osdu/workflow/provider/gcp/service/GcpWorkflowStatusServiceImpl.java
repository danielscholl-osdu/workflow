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

package org.opengroup.osdu.workflow.provider.gcp.service;

import com.sun.jersey.api.client.ClientResponse;
import java.util.HashMap;
import java.util.Map;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.multitenancy.IDatastoreFactory;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.gcp.config.WorkflowPropertiesConfiguration;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusService;

import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcpWorkflowStatusServiceImpl implements IWorkflowStatusService {
  private static final String KEY_AIRFLOW_RUN_ID = "AirflowRunID";
  private static final String KEY_WORKFLOW_ID = "WorkflowID";
  private static final String KEY_RUN_ID = "RunID";
  private static final String KEY_STATUS = "Status";

  private final TenantInfo tenantInfo;
  private Map<String, Datastore> tenantRepositories = new HashMap<String, Datastore>();
  private final IDatastoreFactory datastoreFactory;
  private final ITenantFactory tenantFactory;
  private final WorkflowPropertiesConfiguration propertiesConfiguration;

  @Override
  public void saveWorkflowStatus(ClientResponse response, String workflowStatusId,
      String workflowName, String runId) {
    log.info("Saving workflow status. Workflow status id : {}", workflowStatusId);
    String responseEntity = response.getEntity(String.class);
    String airflowRunId = getRunIdFromResponse(responseEntity);
    Datastore ds = getDatastore();
    Transaction txn = ds.newTransaction();
    try {
      Entity entity = buildStatusEntity(workflowStatusId, airflowRunId, workflowName, runId);
      txn.put(entity);
      txn.commit();
    } catch (DatastoreException ex) {
      throw new PersistenceException(ex.getCode(), ex.getMessage(), ex.getReason());
    } finally {
      if (txn.isActive()) {
        txn.rollback();
      }
    }
  }

  private String getRunIdFromResponse(String response) {
    String[] responsePath = response.split(" ");
    if (responsePath.length < 10) {
      log.warn(String.format("Incorrect response from airflow. Response: %s", response));
      return "";
    }
    return responsePath[6].replace(",", "");
  }

  private Entity buildStatusEntity(String workflowStatusId, String airflowRunId,
      String workflowName, String runId) {
    Datastore ds = getDatastore();
    Key newKey = ds.newKeyFactory()
        .setKind(this.propertiesConfiguration.getWorkflowStatusDatastoreKind())
        .newKey(workflowStatusId);
    return Entity.newBuilder(newKey)
        .set(KEY_WORKFLOW_ID, workflowName)
        .set(KEY_AIRFLOW_RUN_ID, airflowRunId)
        .set(KEY_RUN_ID, runId)
        .set(KEY_STATUS, WorkflowStatusType.SUBMITTED.name()).build();
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
