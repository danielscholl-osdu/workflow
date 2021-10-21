package org.opengroup.osdu.workflow.provider.azure.gsm;

import com.microsoft.azure.eventgrid.models.EventGridEvent;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.opengroup.osdu.azure.eventgrid.EventGridTopicStore;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzureEventGridPublisher implements IEventPublisher {


  private static final String STATUS_CHANGED = "status-changed";
  private static final String EVENT_DATA_VERSION = "1.0";

  @Setter
  @Value("${azure.eventGrid.topicName}")
  private String topicName;

  @Setter
  @Value("${azure.eventGrid.enabled}")
  private Boolean isEventGridEnabled;

  private final EventGridTopicStore eventGridTopicStore;

  @Override
  public void publish(Message[] messages, Map<String, String> attributesMap) throws CoreException {
    validateEventGrid();
    validateInput(messages, attributesMap);

    Map<String, Object> message = createMessageMap(messages, attributesMap);
    List<EventGridEvent> eventsList = createEventGridEventList(message);
    String dataPartitionId = attributesMap.get(DpsHeaders.DATA_PARTITION_ID);

    eventGridTopicStore.publishToEventGridTopic(dataPartitionId, topicName, eventsList);

    String correlationId = attributesMap.get(DpsHeaders.CORRELATION_ID);
    String logMsg = String.format(
        "Event published successfully to topic='%s' with dataPartitionId='%s' and correlationId='%s'.",
        topicName, dataPartitionId, correlationId
    );
    log.info(logMsg);
  }

  private List<EventGridEvent> createEventGridEventList(Map<String, Object> message) {
    String messageId = UUID.randomUUID().toString();
    EventGridEvent eventGridEvent = new EventGridEvent(messageId, STATUS_CHANGED, message, STATUS_CHANGED,
        DateTime.now(), EVENT_DATA_VERSION);

    return Collections.singletonList(eventGridEvent);
  }

  private Map<String, Object> createMessageMap(Message[] messages, Map<String, String> attributesMap) {
    String dataPartitionId = attributesMap.get(DpsHeaders.DATA_PARTITION_ID);
    String correlationId = attributesMap.get(DpsHeaders.CORRELATION_ID);
    Map<String, Object> message = new HashMap<>();
    message.put("data", messages);
    message.put(DpsHeaders.DATA_PARTITION_ID, dataPartitionId);
    message.put(DpsHeaders.CORRELATION_ID, correlationId);

    return message;
  }

  private void validateEventGrid() throws CoreException {
    if (!isEventGridEnabled) {
      throw new CoreException("Event grid is not enabled");
    }
  }

  private void validateInput(Message[] messages, Map<String, String> attributesMap) throws CoreException {
    validateMsg(messages);
    validateAttributesMap(attributesMap);
  }

  private void validateMsg(Message[] messages) throws CoreException {
    if (messages == null || messages.length == 0) {
      throw new CoreException("Nothing in message to publish");
    }
  }

  private void validateAttributesMap(Map<String, String> attributesMap) throws CoreException {
    if (attributesMap == null || attributesMap.isEmpty()) {
      throw new CoreException("data-partition-id and correlation-id are required to publish status event");
    } else if (attributesMap.get(DpsHeaders.DATA_PARTITION_ID) == null) {
      throw new CoreException("data-partition-id is required to publish status event");
    } else if (attributesMap.get(DpsHeaders.CORRELATION_ID) == null) {
      throw new CoreException("correlation-id is required to publish status event");
    }
  }
}
