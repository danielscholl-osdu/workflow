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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.DatasetDetails;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.model.status.StatusDetails;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.workflow.provider.gcp.config.EventMessagingPropertiesConfig;

@RunWith(MockitoJUnitRunner.class)
public class GcpEventPublisherTest {

  private static final String TENANT_NAME = "tenantName";
  private static final String DATA_PARTITION_VALUE = "partitionValue";
  private static final String CORRELATION_VALUE = "correlationValue";

  @Mock
  private Publisher publisher;

  @Mock
  private TenantInfo tenantInfo;

  @Mock
  private EventMessagingPropertiesConfig eventMessagingPropertiesConfig;

  @InjectMocks
  private GcpEventPublisher gcpEventPublisher;

  @Test
  public void shouldNot_publishEventMessage_WhenFlagIsFalse() {
    when(this.eventMessagingPropertiesConfig.isMessagingEnabled()).thenReturn(false);

    Message[] messages = buildMessageArray();
    Map<String, String> AttributesMap = buildAttributesMap();
    this.gcpEventPublisher.publish(messages, AttributesMap);

    verify(this.publisher, times(0)).publish(any());
  }

  @Test
  public void should_publishEventMessage_WhenFlagIsTrue() {
    when(this.eventMessagingPropertiesConfig.isMessagingEnabled()).thenReturn(true);
    when(this.tenantInfo.getName()).thenReturn(TENANT_NAME);

    Message[] messages = buildMessageArray();
    Map<String, String> AttributesMap = buildAttributesMap();
    this.gcpEventPublisher.publish(messages, AttributesMap);

    verify(this.publisher, times(1)).publish(any(PubsubMessage.class));
  }

  private Message[] buildMessageArray() {
    Message[] messageArray = new Message[2];
    StatusDetails statusDetails = new StatusDetails();
    statusDetails.setKind("testKind1");
    messageArray[0] = statusDetails;
    DatasetDetails datasetDetails = new DatasetDetails();
    datasetDetails.setKind("testKin2");
    messageArray[1] = datasetDetails;
    return messageArray;
  }

  private Map<String, String> buildAttributesMap() {
    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put(DpsHeaders.DATA_PARTITION_ID, DATA_PARTITION_VALUE);
    attributesMap.put(DpsHeaders.CORRELATION_ID, CORRELATION_VALUE);
    return attributesMap;
  }


}
