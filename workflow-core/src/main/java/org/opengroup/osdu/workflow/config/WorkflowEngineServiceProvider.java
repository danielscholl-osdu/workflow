/*
 *  Copyright 2020-2025 Google LLC
 *  Copyright 2020-2025 EPAM Systems, Inc
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

package org.opengroup.osdu.workflow.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.provider.interfaces.IAirflowApiClient;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowEngineService;
import org.opengroup.osdu.workflow.service.AirflowV2WorkflowEngineServiceImpl;
import org.opengroup.osdu.workflow.service.AirflowWorkflowEngineServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WorkflowEngineServiceProvider {

  private final IAirflowApiClient airflowApiClient;
  private final DpsHeaders dpsHeaders;

  @Value("${osdu.airflow.version2:#{null}}")
  private Boolean airflowVersion2;

  @Bean
  IWorkflowEngineService workflowEngineService() {
    if (Boolean.TRUE.equals(airflowVersion2)) {
      return new AirflowV2WorkflowEngineServiceImpl(airflowApiClient, dpsHeaders);
    } else {
      return new AirflowWorkflowEngineServiceImpl(airflowApiClient);
    }
  }
}
