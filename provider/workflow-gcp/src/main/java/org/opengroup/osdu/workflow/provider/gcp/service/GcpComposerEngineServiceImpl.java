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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.multitenancy.IDatastoreFactory;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.model.ClientResponse;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.gcp.config.WorkflowPropertiesConfiguration;

import org.opengroup.osdu.workflow.service.AirflowWorkflowEngineServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.opengroup.osdu.workflow.logging.LoggerUtils.getTruncatedData;

@Service
@Slf4j
@Primary
public class GcpComposerEngineServiceImpl extends AirflowWorkflowEngineServiceImpl {
  private static final String KEY_AIRFLOW_RUN_ID = "AirflowRunID";
  private static final String KEY_WORKFLOW_ID = "WorkflowID";
  private static final String KEY_RUN_ID = "RunID";
  private static final String KEY_STATUS = "Status";

  private final AirflowConfig airflowConfig;
  private final WorkflowPropertiesConfiguration workflowConfig;
  private final GoogleIapHelper googleIapHelper;
  private final ObjectMapper objectMapper;
  private final TenantInfo tenantInfo;
  private final Map<String, Datastore> tenantRepositories = new HashMap<>();
  private final IDatastoreFactory datastoreFactory;
  private final ITenantFactory tenantFactory;


  public GcpComposerEngineServiceImpl(AirflowConfig airflowConfig,
                                      WorkflowPropertiesConfiguration workflowConfig,
                                      GoogleIapHelper googleIapHelper,
                                      ObjectMapper objectMapper,
                                      TenantInfo tenantInfo,
                                      IDatastoreFactory datastoreFactory,
                                      ITenantFactory tenantFactory
  ) {
    super(null, airflowConfig);
    this.airflowConfig = airflowConfig;
    this.workflowConfig = workflowConfig;
    this.googleIapHelper = googleIapHelper;
    this.objectMapper = objectMapper;
    this.tenantInfo = tenantInfo;
    this.datastoreFactory = datastoreFactory;
    this.tenantFactory = tenantFactory;
  }


  private ClientResponse sendAirflowRequest(
      String httpMethod, String url, String stringData, WorkflowEngineRequest rq) {
    log.info(
        "Calling airflow endpoint with Google API. Http method: {}, Endpoint: {}, request body: {}",
        httpMethod, url, getTruncatedData(stringData));
    String airflowUrl = this.airflowConfig.getUrl();
    String iapClientId = this.googleIapHelper.getIapClientId(airflowUrl);

    try {
      HttpRequest httpRequest;
      if (HttpMethod.POST.name().equals(httpMethod)) {
        Map<String, Object> data = objectMapper.readValue(stringData, new TypeReference<Map<String, Object>>() {});
        httpRequest = this.googleIapHelper.buildIapPostRequest(url, iapClientId, data);
      } else if (HttpMethod.GET.name().equals(httpMethod)) {
        httpRequest = this.googleIapHelper.buildIapGetRequest(url, iapClientId);
      } else {
        throw new BadRequestException(
            String.format("This method is not supported. Method: %s", httpMethod));
      }
      HttpResponse response = httpRequest.execute();
      String content = IOUtils.toString(response.getContent(), UTF_8);
      if (HttpMethod.POST.name().equals(httpMethod) && response.getStatusCode() == 200) {
        saveWorkflowStatus(content, rq);
      }
      return ClientResponse.builder()
          .contentEncoding(response.getContentEncoding())
          .contentType(response.getContentType())
          .responseBody(content)
          .status(HttpStatus.OK)
          .statusCode(response.getStatusCode())
          .statusMessage(response.getStatusMessage())
          .build();
    } catch (IOException e) {
      String errorMessage = format("Unable to send request to Airflow. %s", e.getMessage());
      log.error(errorMessage, e);
      throw new AppException(500, "Failed to send request.", errorMessage);
    }
  }

  @Override
  protected ClientResponse callAirflow(String httpMethod, String apiEndpoint, String body,
      WorkflowEngineRequest rq, String errorMessage) {
    String url = format("%s/%s", airflowConfig.getUrl(), apiEndpoint);
    log.info("Calling airflow endpoint {} with method {}", url, httpMethod);

    ClientResponse response = sendAirflowRequest(httpMethod, url, body, rq);
    int status = response.getStatusCode();
    log.info("Received response status: {}.", status);
    if (status != 200) {
      throw new AppException(status, (String) response.getResponseBody(), errorMessage);
    }
    return response;
  }

  private void saveWorkflowStatus(String content, WorkflowEngineRequest rq) {
    log.info("Saving workflow status. Workflow status id : {}", rq.getWorkflowId());
    String airflowRunId = getRunIdFromResponse(content);
    if (isNullOrEmpty(airflowRunId)) {
      airflowRunId = rq.getRunId();
    }
    Datastore ds = getDatastore();
    Transaction txn = ds.newTransaction();
    try {
      Entity entity = buildStatusEntity(airflowRunId, rq.getWorkflowName(), rq.getRunId());
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

  private Entity buildStatusEntity(String airflowRunId,
      String workflowName, String runId) {
    Datastore ds = getDatastore();
    Key newKey = ds.newKeyFactory()
        .setKind(workflowConfig.getWorkflowStatusKind())
        .newKey(runId);
    return Entity.newBuilder(newKey)
        .set(KEY_WORKFLOW_ID, workflowName)
        .set(KEY_AIRFLOW_RUN_ID, airflowRunId)
        .set(KEY_RUN_ID, runId)
        .set(KEY_STATUS, WorkflowStatusType.SUBMITTED.name()).build();
  }

  private String getRunIdFromResponse(String response) {
    String[] responsePath = response.split(" ");
    if (responsePath.length < 10) {
      log.warn(String.format("Incorrect response from airflow. Response: %s", response));
      return "";
    }
    return responsePath[6].replace(",", "");
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
