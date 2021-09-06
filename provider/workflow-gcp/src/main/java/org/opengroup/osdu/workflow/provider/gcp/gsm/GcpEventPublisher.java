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

import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PubsubMessage.Builder;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.workflow.provider.gcp.config.EventMessagingPropertiesConfig;
import org.springframework.stereotype.Service;
import org.threeten.bp.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcpEventPublisher implements IEventPublisher {

  private final EventMessagingPropertiesConfig eventMessagingPropertiesConfig;
  private Publisher publisher;
  private final TenantInfo tenantInfo;

  private static final RetrySettings RETRY_SETTINGS = RetrySettings.newBuilder()
      .setTotalTimeout(Duration.ofSeconds(10))
      .setInitialRetryDelay(Duration.ofMillis(5))
      .setRetryDelayMultiplier(2)
      .setMaxRetryDelay(Duration.ofSeconds(3))
      .setInitialRpcTimeout(Duration.ofSeconds(10))
      .setRpcTimeoutMultiplier(2)
      .setMaxRpcTimeout(Duration.ofSeconds(10))
      .build();


  @Override
  public void publish(Message[] messages,
      Map<String, String> attributesMap) throws CoreException {
    if (eventMessagingPropertiesConfig.isMessagingEnabled()) {
      validateInput(messages, attributesMap);

      if (Objects.isNull(this.publisher)) {
        try {
          this.publisher = Publisher.newBuilder(
                  ProjectTopicName.newBuilder()
                      .setProject(this.tenantInfo.getProjectId())
                      .setTopic(this.eventMessagingPropertiesConfig.getTopicName()).build())
              .setRetrySettings(RETRY_SETTINGS).build();
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
      }
      Map<String, Object> messageMap = createMessageMap(messages, attributesMap);
      PubsubMessage pubSubMessage = createPubSubMessageList(messageMap);

      publisher.publish(pubSubMessage);
    }
  }

  private Map<String, Object> createMessageMap(Message[] messages,
      Map<String, String> attributesMap) {
    String dataPartitionId = attributesMap.get(DpsHeaders.DATA_PARTITION_ID);
    String correlationId = attributesMap.get(DpsHeaders.CORRELATION_ID);
    Map<String, Object> message = new HashMap<>();
    message.put("data", messages);
    message.put(DpsHeaders.DATA_PARTITION_ID, dataPartitionId);
    message.put(DpsHeaders.CORRELATION_ID, correlationId);
    message.put(DpsHeaders.ACCOUNT_ID, this.tenantInfo.getName());
    return message;
  }

  private PubsubMessage createPubSubMessageList(Map<String, Object> message) {
    Builder messageBuilder = PubsubMessage.newBuilder();
    messageBuilder.putAttributes(DpsHeaders.ACCOUNT_ID,
        String.valueOf(message.get(DpsHeaders.ACCOUNT_ID)));
    messageBuilder.putAttributes(DpsHeaders.DATA_PARTITION_ID,
        String.valueOf(message.get(DpsHeaders.DATA_PARTITION_ID)));
    messageBuilder.putAttributes(DpsHeaders.CORRELATION_ID,
        String.valueOf(message.get(DpsHeaders.CORRELATION_ID)));

    Message[] messagesArray = (Message[]) message.get("data");

    ByteString data = ByteString.copyFromUtf8(Arrays.toString(messagesArray));
    messageBuilder.setData(data);

    return messageBuilder.build();
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
