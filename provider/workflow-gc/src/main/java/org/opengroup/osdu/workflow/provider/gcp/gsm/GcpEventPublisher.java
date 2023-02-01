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

package org.opengroup.osdu.workflow.provider.gcp.gsm;

import com.google.gson.Gson;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.core.gcp.oqm.driver.OqmDriver;
import org.opengroup.osdu.core.gcp.oqm.model.OqmDestination;
import org.opengroup.osdu.core.gcp.oqm.model.OqmMessage;
import org.opengroup.osdu.core.gcp.oqm.model.OqmTopic;
import org.opengroup.osdu.workflow.provider.gcp.config.EventMessagingPropertiesConfig;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcpEventPublisher implements IEventPublisher {

  private final EventMessagingPropertiesConfig eventMessagingPropertiesConfig;
  private final TenantInfo tenantInfo;
  private final OqmDriver driver;
  private OqmTopic oqmTopic;

  @PostConstruct
  void postConstruct() {
    oqmTopic = OqmTopic.builder().name(eventMessagingPropertiesConfig.getTopicName()).build();
  }

  @Override
  public void publish(Message[] messages, Map<String, String> attributesMap) throws CoreException {
    if (eventMessagingPropertiesConfig.isMessagingEnabled()) {
      validateInput(messages, attributesMap);

      OqmDestination oqmDestination = OqmDestination.builder()
          .partitionId(tenantInfo.getDataPartitionId())
          .build();

      OqmMessage oqmMessage = createMessage(messages, attributesMap);
      driver.publish(oqmMessage, oqmTopic, oqmDestination);
    } else {
      log.info("Event publishing disabled");
    }
  }

  private OqmMessage createMessage(Message[] messages, Map<String, String> attributesMap) {
    String data = new Gson().toJson(messages);
    attributesMap.put(DpsHeaders.ACCOUNT_ID, this.tenantInfo.getName());
    return OqmMessage.builder()
        .data(data)
        .attributes(attributesMap)
        .build();
  }

  private void validateInput(Message[] messages, Map<String, String> attributesMap) {
    validateMsg(messages);
    validateAttributesMap(attributesMap);
  }

  private void validateMsg(Message[] messages) {
    if (Objects.isNull(messages) || messages.length == 0) {
      log.warn("Nothing in message to publish");
    }
  }

  private void validateAttributesMap(Map<String, String> attributesMap) {
    if (Objects.isNull(attributesMap) || attributesMap.isEmpty()) {
      throw new IllegalArgumentException(
          "data-partition-id and correlation-id are required to publish status event");
    } else if (attributesMap.get(DpsHeaders.DATA_PARTITION_ID) == null) {
      throw new IllegalArgumentException("data-partition-id is required to publish status event");
    } else if (attributesMap.get(DpsHeaders.CORRELATION_ID) == null) {
      throw new IllegalArgumentException("correlation-id is required to publish status event");
    }
  }

}
