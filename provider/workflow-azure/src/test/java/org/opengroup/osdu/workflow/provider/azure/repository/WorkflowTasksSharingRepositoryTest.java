package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.storage.blob.sas.BlobContainerSasPermission;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.provider.azure.WorkflowApplication;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowTasksSharingDoc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
public class WorkflowTasksSharingRepositoryTest {

  private static final String TEST_WORKFLOW_NAME = "test-workflow-name";
  private static final String TEST_RUN_ID = "test-run-id";
  private static final String DATABASE_NAME = "database";
  private static final String PARTITION_ID = "partition-id";
  private static final String WORKFLOW_TASKS_SHARING_COLLECTION_NAME = "workflow-tasks-sharing-collection";
  private static final String CONTAINER_ID = "container-id";

  @Mock
  BlobStore blobStore;

  @Mock
  private CosmosStore cosmosStore;

  @Mock
  private CosmosConfig cosmosConfig;

  @Mock
  private DpsHeaders dpsHeaders;

  @Mock
  private JaxRsDpsLog logger;

  @InjectMocks
  private WorkflowTasksSharingRepository sut;

  @Before
  public void init() {
    doReturn(PARTITION_ID).when(dpsHeaders).getPartitionId();
    doReturn(DATABASE_NAME).when(cosmosConfig).getDatabase();
    doReturn(WORKFLOW_TASKS_SHARING_COLLECTION_NAME).when(cosmosConfig).getWorkflowTasksSharingCollection();
  }

  @Test
  public void testGetSignedUrl_whenContainerExists_thenReturnsSignedUrlForExistingContainer() {
    WorkflowTasksSharingDoc workflowTasksSharingDoc = mock(WorkflowTasksSharingDoc.class);

    doReturn(CONTAINER_ID).when(workflowTasksSharingDoc).getContainerId();
    doReturn(Optional.of(workflowTasksSharingDoc)).when(cosmosStore).findItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_TASKS_SHARING_COLLECTION_NAME),
        eq(TEST_RUN_ID),
        eq(TEST_WORKFLOW_NAME),
        eq(WorkflowTasksSharingDoc.class));

    sut.getSignedUrl(TEST_WORKFLOW_NAME, TEST_RUN_ID);

    ArgumentCaptor<String> containerIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BlobContainerSasPermission> blobContainerSasPermissionArgumentCaptor = ArgumentCaptor.forClass(BlobContainerSasPermission.class);

    verify(blobStore).generatePreSignedUrlWithUserDelegationSas(eq(PARTITION_ID), containerIdCaptor.capture(), any(), any(), blobContainerSasPermissionArgumentCaptor.capture());

    String containerId = containerIdCaptor.getValue();
    BlobContainerSasPermission blobContainerSasPermission = blobContainerSasPermissionArgumentCaptor.getValue();

    assertEquals(containerId, workflowTasksSharingDoc.getContainerId());
    checkBlobContainerSasPermission(blobContainerSasPermission);
  }

  @Test
  public void testGetSignedUrl_whenContainerDoesNotExist_thenCreateContainerAndReturnSignedUrl() {
    sut.getSignedUrl(TEST_WORKFLOW_NAME, TEST_RUN_ID);

    ArgumentCaptor<String> containerIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<WorkflowTasksSharingDoc> workflowTasksSharingDocArgumentCaptor = ArgumentCaptor.forClass(WorkflowTasksSharingDoc.class);
    ArgumentCaptor<BlobContainerSasPermission> blobContainerSasPermissionArgumentCaptor = ArgumentCaptor.forClass(BlobContainerSasPermission.class);

    verify(blobStore).createBlobContainer(eq(PARTITION_ID), containerIdCaptor.capture());
    String containerId = containerIdCaptor.getValue();
    verify(blobStore).generatePreSignedUrlWithUserDelegationSas(eq(PARTITION_ID), eq(containerId), any(), any(), blobContainerSasPermissionArgumentCaptor.capture());
    BlobContainerSasPermission blobContainerSasPermission = blobContainerSasPermissionArgumentCaptor.getValue();
    verify(cosmosStore).createItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_TASKS_SHARING_COLLECTION_NAME),
        eq(TEST_WORKFLOW_NAME),
        workflowTasksSharingDocArgumentCaptor.capture()
    );
    WorkflowTasksSharingDoc workflowTasksSharingDoc = workflowTasksSharingDocArgumentCaptor.getValue();
    assertEquals(workflowTasksSharingDoc.getContainerId(), containerId);
    assertEquals(workflowTasksSharingDoc.getWorkflowName(), TEST_WORKFLOW_NAME);
    assertEquals(workflowTasksSharingDoc.getRunId(), TEST_RUN_ID);
    assertEquals(workflowTasksSharingDoc.getPartitionKey(), TEST_WORKFLOW_NAME);
    assertEquals(workflowTasksSharingDoc.getId(), TEST_RUN_ID);
    checkBlobContainerSasPermission(blobContainerSasPermission);
  }

  @Test
  public void testDeleteTasksSharingInfoContainer() {
    WorkflowTasksSharingDoc workflowTasksSharingDoc = mock(WorkflowTasksSharingDoc.class);

    doReturn(CONTAINER_ID).when(workflowTasksSharingDoc).getContainerId();
    doReturn(Optional.of(workflowTasksSharingDoc)).when(cosmosStore).findItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_TASKS_SHARING_COLLECTION_NAME),
        eq(TEST_RUN_ID),
        eq(TEST_WORKFLOW_NAME),
        eq(WorkflowTasksSharingDoc.class));

    sut.deleteTasksSharingInfoContainer(PARTITION_ID, TEST_WORKFLOW_NAME, TEST_RUN_ID);

    verify(blobStore, times(1)).deleteBlobContainer(eq(PARTITION_ID), eq(CONTAINER_ID));
    verify(cosmosStore, times(1)).deleteItem(
        eq(PARTITION_ID),
        eq(DATABASE_NAME),
        eq(WORKFLOW_TASKS_SHARING_COLLECTION_NAME),
        eq(TEST_RUN_ID),
        eq(TEST_WORKFLOW_NAME)
    );
  }

  private void checkBlobContainerSasPermission(BlobContainerSasPermission blobContainerSasPermission) {
    assertEquals(blobContainerSasPermission.hasAddPermission(), false);
    assertEquals(blobContainerSasPermission.hasCreatePermission(), true);
    assertEquals(blobContainerSasPermission.hasDeletePermission(), false);
    assertEquals(blobContainerSasPermission.hasDeleteVersionPermission(), false);
    assertEquals(blobContainerSasPermission.hasListPermission(), true);
    assertEquals(blobContainerSasPermission.hasReadPermission(), true);
    assertEquals(blobContainerSasPermission.hasTagsPermission(), false);
    assertEquals(blobContainerSasPermission.hasWritePermission(), true);
  }
}
