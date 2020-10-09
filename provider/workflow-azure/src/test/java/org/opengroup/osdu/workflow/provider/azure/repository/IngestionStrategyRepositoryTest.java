package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.cosmos.CosmosClientException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.provider.azure.WorkflowApplication;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.IngestionStrategyDoc;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
public class IngestionStrategyRepositoryTest {
  private static final String DATABASE_NAME = "someDatabase";
  private static final String INGESTION_STRATEGY_COLLECTION_NAME = "someIngestionStrategyName";
  private static final String PARTITION_ID = "somePartition";

  @Mock
  private CosmosStore cosmosStore;

  @Mock
  private CosmosConfig cosmosConfig;

  @Mock
  private DpsHeaders dpsHeaders;

  @InjectMocks
  private IngestionStrategyRepository repository;

  @Before
  public void initMocks() throws Exception {
    when(cosmosConfig.getDatabase()).thenReturn(DATABASE_NAME);
    when(cosmosConfig.getIngestionStrategyCollection()).thenReturn(INGESTION_STRATEGY_COLLECTION_NAME);
    when(dpsHeaders.getPartitionId()).thenReturn(PARTITION_ID);
  }

  @Test
  public void findByWorkflowTypeAndDataTypeAndUserId() throws CosmosClientException, IOException {
    IngestionStrategyDoc ingestionStrategyDoc = new IngestionStrategyDoc();
    ingestionStrategyDoc.setDagName("osdu_python_sdk_well_log_ingestion");
    ingestionStrategyDoc.setDataType("well_log");
    ingestionStrategyDoc.setWorkflowType(WorkflowType.OSDU.name());
    final String dataType = "well_log";
    when(cosmosStore.findItem(eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(INGESTION_STRATEGY_COLLECTION_NAME),
        eq(String.format("%s-%s", WorkflowType.OSDU.toString().toLowerCase(), dataType.toLowerCase())),
        eq(WorkflowType.OSDU.toString().toLowerCase()),
        eq(IngestionStrategyDoc.class))).thenReturn(Optional.of(ingestionStrategyDoc));
    IngestionStrategy ingestionStrategy = repository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.OSDU, "well_log", "");

    Assert.assertNotNull(ingestionStrategy);
    Assert.assertEquals(getIngestionStrategy().getDagName(), ingestionStrategy.getDagName());
    Assert.assertEquals(getIngestionStrategy().getDataType(), ingestionStrategy.getDataType());
    Assert.assertEquals(getIngestionStrategy().getWorkflowType(), ingestionStrategy.getWorkflowType());

  }

  @Test
  public void shouldReturnNullWhenRecordNotFound() throws CosmosClientException {
    when(cosmosStore.findItem(any(), any(), any(), any(), any(), any()))
        .thenReturn(Optional.empty());
    Assert.assertNull(repository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.OSDU, "test", ""));
  }

  @Test(expected = AppException.class)
  public void shouldThrowExceptionWhenCosmosException() throws CosmosClientException {
    doThrow(AppException.class)
        .when(cosmosStore)
        .findItem(any(), any(), any(), any(), any(), any());
    repository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.INGEST, "well_log", "");
  }

  private IngestionStrategy getIngestionStrategy() {
    return IngestionStrategy.builder()
        .workflowType(WorkflowType.OSDU)
        .dataType("well_log")
        .userId("")
        .dagName("osdu_python_sdk_well_log_ingestion")
        .build();
  }

  private boolean throwException() {
    throw new NullPointerException();
  }
}
