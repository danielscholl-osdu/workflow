package org.opengroup.osdu.workflow.provider.azure.gsm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.eventgrid.EventGridTopicStore;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.model.status.StatusDetails;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link AzureEventGridPublisher}
 */
@ExtendWith(MockitoExtension.class)
class AzureEventGridPublisherTest {

  private final static String TOPIC_NAME = "status_topic";
  private final static String CORRELATION_ID = "CORRELATION_ID";
  private final static String DATA_PARTITION_ID = "DATA_PARTITION_ID";

  @Mock
  private EventGridTopicStore eventGridTopicStore;

  private AzureEventGridPublisher publisher;

  @BeforeEach
  void setUp() {
    publisher = new AzureEventGridPublisher(eventGridTopicStore);
    publisher.setTopicName(TOPIC_NAME);
    publisher.setIsEventGridEnabled(true);
  }

  @Test
  void shouldPublishGSMMessage() {
    //given
    doNothing().when(eventGridTopicStore).publishToEventGridTopic(any(), any(), any());
    Message[] messages = new Message[]{new StatusDetails()};
    Map<String, String> attributesMap = new HashMap<String, String>() {{
      put(DpsHeaders.CORRELATION_ID, CORRELATION_ID);
      put(DpsHeaders.DATA_PARTITION_ID, DATA_PARTITION_ID);
    }};

    //when
    publisher.publish(messages, attributesMap);

    //then
    verify(eventGridTopicStore, times(1))
        .publishToEventGridTopic(any(), any(), any());
  }

  @Test
  void shouldTrowCoreExceptionOnDisabledPublishing() {
    //given
    publisher.setIsEventGridEnabled(false);
    Message[] messages = new Message[]{new StatusDetails()};
    Map<String, String> attributesMap = new HashMap<String, String>() {{
      put(DpsHeaders.CORRELATION_ID, CORRELATION_ID);
      put(DpsHeaders.DATA_PARTITION_ID, DATA_PARTITION_ID);
    }};

    //when & then
    Assertions.assertThrows(CoreException.class,
        () -> publisher.publish(messages, attributesMap)
    );

    //then
    verify(eventGridTopicStore, times(0))
        .publishToEventGridTopic(any(), any(), any());
  }

  @Test
  void shouldTrowCoreExceptionOnMissingMessages() {
    //given
    Map<String, String> attributesMap = new HashMap<String, String>() {{
      put(DpsHeaders.CORRELATION_ID, CORRELATION_ID);
      put(DpsHeaders.DATA_PARTITION_ID, DATA_PARTITION_ID);
    }};

    //when & then
    Assertions.assertThrows(CoreException.class,
        () -> publisher.publish(null, attributesMap)
    );

    //then
    verify(eventGridTopicStore, times(0))
        .publishToEventGridTopic(any(), any(), any());
  }

  @Test
  void shouldTrowCoreExceptionOnEmptyMessages() {
    //given
    Message[] messages = new Message[]{};
    Map<String, String> attributesMap = new HashMap<String, String>() {{
      put(DpsHeaders.CORRELATION_ID, CORRELATION_ID);
      put(DpsHeaders.DATA_PARTITION_ID, DATA_PARTITION_ID);
    }};

    //when & then
    Assertions.assertThrows(CoreException.class,
        () -> publisher.publish(messages, attributesMap)
    );

    //then
    verify(eventGridTopicStore, times(0))
        .publishToEventGridTopic(any(), any(), any());
  }

  @Test
  void shouldTrowCoreExceptionOnMissingAttributes() {
    //given
    Message[] messages = new Message[]{new StatusDetails()};

    //when & then
    Assertions.assertThrows(CoreException.class,
        () -> publisher.publish(messages, null)
    );

    //then
    verify(eventGridTopicStore, times(0))
        .publishToEventGridTopic(any(), any(), any());
  }

  @Test
  void shouldTrowCoreExceptionOnEmptyAttributes() {
    //given
    Message[] messages = new Message[]{new StatusDetails()};
    Map<String, String> attributesMap = new HashMap<>();

    //when & then
    Assertions.assertThrows(CoreException.class,
        () -> publisher.publish(messages, attributesMap)
    );

    //then
    verify(eventGridTopicStore, times(0))
        .publishToEventGridTopic(any(), any(), any());
  }

  @Test
  void shouldTrowCoreExceptionOnMissingCorrelationId() {
    //given
    Message[] messages = new Message[]{new StatusDetails()};
    Map<String, String> attributesMap = new HashMap<String, String>() {{
      put(DpsHeaders.DATA_PARTITION_ID, DATA_PARTITION_ID);
    }};

    //when & then
    Assertions.assertThrows(CoreException.class,
        () -> publisher.publish(messages, attributesMap)
    );

    //then
    verify(eventGridTopicStore, times(0))
        .publishToEventGridTopic(any(), any(), any());
  }

  @Test
  void shouldTrowCoreExceptionOnMissingDataPartitionId() {
    //given
    Message[] messages = new Message[]{new StatusDetails()};
    Map<String, String> attributesMap = new HashMap<String, String>() {{
      put(DpsHeaders.CORRELATION_ID, CORRELATION_ID);
    }};

    //when & then
    Assertions.assertThrows(CoreException.class,
        () -> publisher.publish(messages, attributesMap)
    );

    //then
    verify(eventGridTopicStore, times(0))
        .publishToEventGridTopic(any(), any(), any());
  }
}
