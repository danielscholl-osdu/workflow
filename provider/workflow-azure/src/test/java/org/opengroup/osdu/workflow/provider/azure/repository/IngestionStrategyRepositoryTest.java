package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.cosmos.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import org.mockito.junit.MockitoJUnitRunner;

import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.workflow.model.IngestionStrategy;
import org.opengroup.osdu.workflow.provider.azure.WorkflowApplication;
import org.opengroup.osdu.workflow.provider.azure.model.IngestionStrategyDoc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import javax.inject.Named;

import java.io.IOException;


@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
public class IngestionStrategyRepositoryTest {
  @Mock
  private CosmosItem cosmosItem;

  @Mock
  private CosmosItemResponse cosmosResponse;

  @Mock
  private CosmosItemProperties cosmosItemProperties;

  @Mock
  @Named("INGESTION_STRATEGY_CONTAINER")
  private CosmosContainer ingestionStrategyContainer;

  @InjectMocks
  private IngestionStrategyRepository repository;

  @Before
  public void initMocks() throws Exception {
    doReturn(cosmosItem).when(ingestionStrategyContainer).getItem(any(), any());
    doReturn(cosmosResponse).when(cosmosItem).read(any());
    doReturn(cosmosItemProperties).when(cosmosResponse).getProperties();
  }

  @Test
  public void findByWorkflowTypeAndDataTypeAndUserId() throws CosmosClientException, IOException {
    IngestionStrategyDoc ingestionStrategyDoc = new IngestionStrategyDoc();
    ingestionStrategyDoc.setDagName("osdu_python_sdk_well_log_ingestion");
    ingestionStrategyDoc.setDataType("well_log");
    ingestionStrategyDoc.setWorkflowType(WorkflowType.OSDU.name());
    doReturn(ingestionStrategyDoc)
        .when(cosmosItemProperties)
        .getObject(any());
    IngestionStrategy ingestionStrategy = repository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.OSDU, "well_log", "");

    Assert.assertNotNull(ingestionStrategy);
    Assert.assertEquals(getIngestionStrategy().getDagName(), ingestionStrategy.getDagName());
    Assert.assertEquals(getIngestionStrategy().getDataType(), ingestionStrategy.getDataType());
    Assert.assertEquals(getIngestionStrategy().getWorkflowType(), ingestionStrategy.getWorkflowType());

  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowExceptionWhenRecordNotFound() throws CosmosClientException {
    doThrow(NullPointerException.class)
        .when(cosmosItem)
        .read(any());
    repository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.OSDU, "test", "");
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowExceptionWhenDataTypeAndWorkflowTypeNotFound() throws Throwable {
    doThrow(NotFoundException.class)
        .when(cosmosItem)
        .read(any());
    repository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.OSDU, "well_log111", "opendes11");
    Assert.assertFalse(throwException());
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowExceptionWhenDocumentisMalformed() throws IOException {
    doThrow(IOException.class)
        .when(cosmosItemProperties)
        .getObject(any());
    repository.findByWorkflowTypeAndDataTypeAndUserId(WorkflowType.OSDU, "well_log111", "opendes11");
    Assert.assertFalse(throwException());
  }

  @Test(expected = AppException.class)
  public void shouldThrowExceptionWhenCosmosException() throws CosmosClientException {
    doThrow(CosmosClientException.class)
        .when(cosmosItem)
        .read(any());
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
