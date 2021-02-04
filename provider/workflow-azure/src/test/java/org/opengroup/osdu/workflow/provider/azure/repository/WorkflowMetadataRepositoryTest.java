package org.opengroup.osdu.workflow.provider.azure.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowMetadataDoc;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link WorkflowMetadataRepository}
 */
@ExtendWith(MockitoExtension.class)
public class WorkflowMetadataRepositoryTest {
  private static final String PARTITION_ID = "someId";
  private static final String DATABASE_NAME = "someDbName";
  private static final String WORKFLOW_METADATA_COLLECTION = "someCollection";
  private static final String WORKFLOW_NAME = "HelloWorld";
  private static final String INPUT_WORKFLOW_METADATA = "{\n" +
      "    \"workflowId\": \"foo\",\n" +
      "    \"workflowName\": \"HelloWorld\",\n" +
      "    \"description\": \"This is a test workflow\",\n" +
      "    \"registrationInstructions\": {\n" +
      "    \"concurrentWorkflowRun\": 5,\n" +
      "    \"concurrentTaskRun\": 5,\n" +
      "    \"active\": true\n" +
      "    },\n" +
      "    \"creationTimestamp\": 1600144876028,\n" +
      "    \"createdBy\": \"user@email.com\",\n" +
      "    \"version\": 1\n" +
      "}";
  private static final String OUTPUT_WORKFLOW_METADATA = "{\n" +
      "    \"workflowId\": \"HelloWorld\",\n" +
      "    \"workflowName\": \"HelloWorld\",\n" +
      "    \"description\": \"This is a test workflow\",\n" +
      "    \"registrationInstructions\": {\n" +
      "    \"concurrentWorkflowRun\": 5,\n" +
      "    \"concurrentTaskRun\": 5,\n" +
      "    \"active\": true\n" +
      "    },\n" +
      "    \"creationTimestamp\": 1600144876028,\n" +
      "    \"createdBy\": \"user@email.com\",\n" +
      "    \"version\": 1\n" +
      "}";
  private static final String WORKFLOW_METADATA_DOC = "{\n" +
      "    \"id\": \"HelloWorld\",\n" +
      "    \"partitionKey\": \"HelloWorld\",\n" +
      "    \"workflowName\": \"HelloWorld\",\n" +
      "    \"description\": \"This is a test workflow\",\n" +
      "    \"registrationInstructions\": {\n" +
      "    \"concurrentWorkflowRun\": 5,\n" +
      "    \"concurrentTaskRun\": 5,\n" +
      "    \"active\": true\n" +
      "    },\n" +
      "    \"creationTimestamp\": 1600144876028,\n" +
      "    \"createdBy\": \"user@email.com\",\n" +
      "    \"version\": 1\n" +
      "}";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mock
  private CosmosConfig cosmosConfig;

  @Mock
  private CosmosStore cosmosStore;

  @Mock
  private DpsHeaders dpsHeaders;

  @Mock
  private JaxRsDpsLog jaxRsDpsLog;

  @InjectMocks
  private WorkflowMetadataRepository workflowMetadataRepository;

  @Test
  public void testCreateWorkflow() throws Exception {
    final WorkflowMetadata inputWorkflowMetadata = OBJECT_MAPPER.readValue(INPUT_WORKFLOW_METADATA, WorkflowMetadata.class);
    final WorkflowMetadata outputWorkflowMetadata = OBJECT_MAPPER.readValue(OUTPUT_WORKFLOW_METADATA, WorkflowMetadata.class);
    final WorkflowMetadataDoc workflowMetadataDoc =
        OBJECT_MAPPER.readValue(WORKFLOW_METADATA_DOC, WorkflowMetadataDoc.class);
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowMetadataCollection()).thenReturn(WORKFLOW_METADATA_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    doNothing().when(cosmosStore)
        .createItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_METADATA_COLLECTION), eq(WORKFLOW_NAME), eq(workflowMetadataDoc));
    final WorkflowMetadata response = workflowMetadataRepository.createWorkflow(inputWorkflowMetadata);
    verify(cosmosStore, times(1))
        .createItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_METADATA_COLLECTION), eq(WORKFLOW_NAME), eq(workflowMetadataDoc));
    verify(cosmosConfig, times(1)).getDatabase();
    verify(cosmosConfig, times(1)).getWorkflowMetadataCollection();
    verify(dpsHeaders, times(1)).getPartitionId();
    assertThat(response, equalTo(outputWorkflowMetadata));
  }

  @Test
  public void testCreateWorkflowWithExistingId() throws Exception {
    final WorkflowMetadata inputWorkflowMetadata = OBJECT_MAPPER.readValue(INPUT_WORKFLOW_METADATA, WorkflowMetadata.class);
    final WorkflowMetadataDoc workflowMetadataDoc =
        OBJECT_MAPPER.readValue(WORKFLOW_METADATA_DOC, WorkflowMetadataDoc.class);
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowMetadataCollection()).thenReturn(WORKFLOW_METADATA_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    doThrow(new AppException(409, "conflict", "conflict")).when(cosmosStore)
        .createItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_METADATA_COLLECTION), eq(WORKFLOW_NAME), eq(workflowMetadataDoc));
    boolean isExceptionThrown = false;

    try {
      workflowMetadataRepository.createWorkflow(inputWorkflowMetadata);
    } catch (ResourceConflictException r) {
      isExceptionThrown = true;
    }

    assertThat(isExceptionThrown, equalTo(true));
    verify(cosmosStore)
        .createItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_METADATA_COLLECTION), eq(WORKFLOW_NAME), eq(workflowMetadataDoc));
    verify(cosmosConfig).getDatabase();
    verify(cosmosConfig).getWorkflowMetadataCollection();
    verify(dpsHeaders).getPartitionId();
  }

  @Test
  public void testGetWorkflowWithExistingWorkflowId() throws Exception {
    final WorkflowMetadata workflowMetadata = OBJECT_MAPPER.readValue(OUTPUT_WORKFLOW_METADATA, WorkflowMetadata.class);
    final WorkflowMetadataDoc workflowMetadataDoc =
        OBJECT_MAPPER.readValue(WORKFLOW_METADATA_DOC, WorkflowMetadataDoc.class);
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowMetadataCollection()).thenReturn(WORKFLOW_METADATA_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    when(cosmosStore.findItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_METADATA_COLLECTION),
        eq(WORKFLOW_NAME), eq(WORKFLOW_NAME), eq(WorkflowMetadataDoc.class)))
        .thenReturn(Optional.of(workflowMetadataDoc));
    final WorkflowMetadata response = workflowMetadataRepository.getWorkflow(WORKFLOW_NAME);
    verify(cosmosStore).findItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_METADATA_COLLECTION),
        eq(WORKFLOW_NAME), eq(WORKFLOW_NAME), eq(WorkflowMetadataDoc.class));
    verify(cosmosConfig).getDatabase();
    verify(cosmosConfig).getWorkflowMetadataCollection();
    verify(dpsHeaders).getPartitionId();
    assertThat(response, equalTo(workflowMetadata));
  }

  @Test
  public void testGetWorkflowWithNonExistingWorkflowId() throws Exception {
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowMetadataCollection()).thenReturn(WORKFLOW_METADATA_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    when(cosmosStore.findItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_METADATA_COLLECTION),
        eq(WORKFLOW_NAME), eq(WORKFLOW_NAME), eq(WorkflowMetadataDoc.class)))
        .thenReturn(Optional.empty());
    Assertions.assertThrows(WorkflowNotFoundException.class, () -> {
      workflowMetadataRepository.getWorkflow(WORKFLOW_NAME);
    });
    verify(cosmosStore, times(1)).findItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_METADATA_COLLECTION),
        eq(WORKFLOW_NAME), eq(WORKFLOW_NAME), eq(WorkflowMetadataDoc.class));
    verify(cosmosConfig, times(1)).getDatabase();
    verify(cosmosConfig, times(1)).getWorkflowMetadataCollection();
    verify(dpsHeaders, times(1)).getPartitionId();
  }

  @Test
  public void testDeleteWorkflow() {
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getWorkflowMetadataCollection()).thenReturn(WORKFLOW_METADATA_COLLECTION);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
    doNothing().when(cosmosStore).deleteItem(eq(PARTITION_ID), eq(DATABASE_NAME),
        eq(WORKFLOW_METADATA_COLLECTION), eq(WORKFLOW_NAME), eq(WORKFLOW_NAME));
    workflowMetadataRepository.deleteWorkflow(WORKFLOW_NAME);
    verify(cosmosStore).deleteItem(eq(PARTITION_ID), eq(DATABASE_NAME), eq(WORKFLOW_METADATA_COLLECTION),
        eq(WORKFLOW_NAME), eq(WORKFLOW_NAME));
    verify(cosmosConfig).getDatabase();
    verify(cosmosConfig).getWorkflowMetadataCollection();
    verify(dpsHeaders).getPartitionId();
  }
}
