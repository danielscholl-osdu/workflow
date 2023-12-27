/*
 *  Copyright 2020-2023 Google LLC
 *  Copyright 2020-2023 EPAM Systems, Inc
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

package org.opengroup.osdu.workflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.config.BasicAuthAirflowClientCondition;
import org.opengroup.osdu.workflow.model.ClientResponse;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineExtension;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.HttpMethod;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.String.format;

@Slf4j
@Service
@Primary
@Conditional(BasicAuthAirflowClientCondition.class)
public class AirflowV2WorkflowEngineExtension extends AirflowV2WorkflowEngineServiceImpl
    implements IWorkflowEngineExtension {

  public static final String GET_RUN_TASKS_ERROR_MESSAGE =
      "Failed to fetch run tasks with id %s and name %s";
  public static final String GET_TASKS_XCOM_ERROR_MESSAGE =
      "Failed to fetch task xcom entries, task id %s";
  public static final String GET_XCOM_VALUES_ERROR_MESSAGE =
      "Failed to fetch xcom values for task id %s";
  public static final String TASK_INSTANCES = "api/v1/dags/%s/dagRuns/%s/taskInstances";
  public static final String XCOM_ENTRIES = TASK_INSTANCES + "/%s/xcomEntries";
  public static final String XCOM_VALUES = XCOM_ENTRIES + "/%s";
  public static final String TASK_INSTANCES_ELEMENT = "task_instances";
  public static final String END_DATE_ELEMENT = "end_date";
  public static final String TASK_ID_ELEMENT = "task_id";
  public static final String XCOM_ENTRIES_ELEMENT = "xcom_entries";
  public static final String KEY_ELEMENT = "key";
  public static final String XCOM_VALUE = "value";
  public static final String XCOM_RESP = "xcom";

  private final ObjectMapper om = new ObjectMapper();

  protected AirflowV2WorkflowEngineExtension(
      Client restClient, AirflowConfig airflowConfig, DpsHeaders dpsHeaders) {
    super(restClient, airflowConfig, dpsHeaders);
  }

  @Override
  public Object getLatestTaskDetails(String workflowName, String runId) {
    try {
      List<Map<String, String>> tasksMap = getDagRunTasks(workflowName, runId);

      Map<String, String> latestTask = findLatestTask(tasksMap);

      if (Objects.isNull(latestTask) || latestTask.isEmpty()) {
        log.info("Task instances not found for DAG: {} with runId: {}", workflowName, runId);
        throw new AppException(HttpStatus.NOT_FOUND.value(), "Tasks not found", "Empty response.");
      }
      String latestTaskId = latestTask.get(TASK_ID_ELEMENT);

      List<String> xcomKeys = getTaskXcomKeys(workflowName, runId, latestTaskId);

      Map<String, String> xcomKeyVal =
          getXcomKeyValues(workflowName, runId, latestTaskId, xcomKeys);
      ObjectNode xcomKeyValJson = om.convertValue(xcomKeyVal, ObjectNode.class);
      ObjectNode taskJson = om.convertValue(latestTask, ObjectNode.class);
      taskJson.set(XCOM_RESP, xcomKeyValJson);
      return taskJson;
    } catch (JsonProcessingException e) {
      log.error("Unable to process Airflow response.", e);
      final String error = "Unable to Process(Parse, Generate) JSON value";
      throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), error, e.getMessage());
    }
  }

  private List<Map<String, String>> getDagRunTasks(String workflowName, String runId)
      throws JsonProcessingException {
    String taskInstancesEndpoint = format(TASK_INSTANCES, workflowName, runId);
    String tasksErrMsg = format(GET_RUN_TASKS_ERROR_MESSAGE, runId, workflowName);

    ClientResponse taskListResponse =
        this.callAirflow(HttpMethod.GET, taskInstancesEndpoint, null, null, tasksErrMsg);

    JsonNode taskInstancesJson =
        om.readValue(taskListResponse.getResponseBody().toString(), JsonNode.class)
            .get(TASK_INSTANCES_ELEMENT);

    if (!taskInstancesJson.isArray()) {
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          "Unexpected response format for Task list request.",
          "Airflow response:" + taskListResponse);
    }
    List<Map<String, String>> tasksMap = new ArrayList<>();
    for (JsonNode jsonNode : taskInstancesJson) {
      tasksMap.add(om.convertValue(jsonNode, Map.class));
    }
    return tasksMap;
  }

  private List<String> getTaskXcomKeys(String workflowName, String runId, String latestTaskId)
      throws JsonProcessingException {
    String taskXcomEntriesEndpoint = format(XCOM_ENTRIES, workflowName, runId, latestTaskId);
    String xcomEntriesErrMsg = format(GET_TASKS_XCOM_ERROR_MESSAGE, latestTaskId);

    ClientResponse entriesResponse =
        this.callAirflow(HttpMethod.GET, taskXcomEntriesEndpoint, null, null, xcomEntriesErrMsg);

    JsonNode entriesNode =
        om.readValue(entriesResponse.getResponseBody().toString(), JsonNode.class);

    JsonNode xcomEntries = entriesNode.get(XCOM_ENTRIES_ELEMENT);
    ArrayList<String> xcomKeys = new ArrayList<>();
    if (!xcomEntries.isArray()) {
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          "Unexpected response format for Xcom keys request.",
          "Airflow response:" + entriesResponse);
    }
    for (JsonNode xcomEntry : xcomEntries) {
      xcomKeys.add(xcomEntry.get(KEY_ELEMENT).textValue());
    }
    return xcomKeys;
  }

  private Map<String, String> getXcomKeyValues(
      String workflowName, String runId, String latestTaskId, List<String> xcomKeys)
      throws JsonProcessingException {
    String xcomValErrMsg = format(GET_XCOM_VALUES_ERROR_MESSAGE, latestTaskId);
    Map<String, String> xcomKeyVal = new HashMap<>();
    for (String xcomKey : xcomKeys) {
      String xcomValueEndpoint = format(XCOM_VALUES, workflowName, runId, latestTaskId, xcomKey);
      ClientResponse xcomValueResponse =
          callAirflow(HttpMethod.GET, xcomValueEndpoint, null, null, xcomValErrMsg);
      JsonNode xcomValue =
          om.readValue(xcomValueResponse.getResponseBody().toString(), JsonNode.class);

      xcomKeyVal.put(xcomKey, xcomValue.get(XCOM_VALUE).textValue());
    }
    return xcomKeyVal;
  }

  private static Map<String, String> findLatestTask(List<Map<String, String>> tasksMap) {
    return tasksMap.stream()
        .max(
            Comparator.comparing(
                map ->
                    LocalDateTime.parse(
                        map.get(END_DATE_ELEMENT), DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
        .orElseThrow(
            () ->
                new AppException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unable to locate latest task.",
                    String.format("Provided tasks: %s", StringUtils.join(tasksMap))));
  }
}
