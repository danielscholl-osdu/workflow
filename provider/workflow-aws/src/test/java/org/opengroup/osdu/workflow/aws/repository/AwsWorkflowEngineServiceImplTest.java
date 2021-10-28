package org.opengroup.osdu.workflow.aws.repository;

import com.sun.jersey.api.client.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.aws.service.AwsWorkflowEngineServiceImpl;
import org.opengroup.osdu.workflow.aws.service.airflow.sqs.WorkflowRequestBodyFactory;
import org.opengroup.osdu.workflow.aws.service.airflow.sqs.WorkflowSqsClient;
import org.opengroup.osdu.workflow.aws.service.s3.S3Client;
import org.springframework.boot.test.context.SpringBootTest;
import org.opengroup.osdu.workflow.aws.config.AwsAirflowApiMode;
import org.opengroup.osdu.workflow.aws.config.AwsServiceConfig;
import org.opengroup.osdu.workflow.config.AirflowConfig;
import org.opengroup.osdu.workflow.model.TriggerWorkflowResponse;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes={S3Client.class})
public class AwsWorkflowEngineServiceImplTest {
    @InjectMocks
    AwsWorkflowEngineServiceImpl CUT = new AwsWorkflowEngineServiceImpl();

    @Mock
    private AwsServiceConfig config;

    @Mock
    private DpsHeaders dpsHeaders;

    @Mock
    AwsServiceConfig awsConfig;

    @Mock
    private AirflowConfig airflowConfig;

    @Mock
    private Client restClient;

    @Mock
    DpsHeaders headers;

    @Mock
    WorkflowSqsClient sqsClient;

    @Mock
    S3Client s3Client;

    @Mock
    private WorkflowRequestBodyFactory workflowRequestBodyFactory;

    @Mock
    AwsWorkflowRunRepository awsWorkflowRunRepository;

    @Before
    public void setUp() {
      initMocks(this);
    }

    @Test
    public void save()
    {
        // Arrange
        String runId = "test-run-id";
        String dagName = "test-dag-name";
        String serializedData = "{some-serialized-data}";
        String partitionId = "test-partition";
        String ref = "test-ref";


        WorkflowEngineRequest request = WorkflowEngineRequest.builder().runId(runId).workflowId("workflowId").dagName(dagName).workflowName(dagName).workflowEngineExecutionDate("date")
          .isSystemWorkflow(false).isDeployedThroughWorkflowService(false).build();

        Map<String, Object> inputData = new HashMap<>();

        // to mock
        Mockito.when(awsWorkflowRunRepository.runExists(Mockito.eq(runId)))
            .thenReturn(false);

        Mockito.when(awsConfig.getAirflowApiMode())
            .thenReturn(AwsAirflowApiMode.SQS);

        Mockito.when(workflowRequestBodyFactory.getSerializedWorkflowRequest(Mockito.anyMap(), Mockito.eq(dagName),
            Mockito.eq(runId), Mockito.any(DpsHeaders.class), Mockito.anyBoolean()))
            .thenReturn(serializedData);

        Mockito.when(dpsHeaders.getPartitionId())
            .thenReturn(partitionId);

        Mockito.when(s3Client.save(Mockito.eq(runId), Mockito.eq(serializedData), Mockito.eq(partitionId)))
            .thenReturn(ref);

        Mockito.doNothing().when(sqsClient).sendMessageToWorkflowQueue(Mockito.eq(ref));

        // Act
        TriggerWorkflowResponse response = CUT.triggerWorkflow(request, inputData);

        // Assert
        Mockito.verify(awsWorkflowRunRepository, Mockito.times(1)).runExists(Mockito.eq(runId));

        Mockito.verify(workflowRequestBodyFactory, Mockito.times(1)).getSerializedWorkflowRequest(Mockito.anyMap(), Mockito.eq(dagName),
            Mockito.eq(runId), Mockito.any(DpsHeaders.class), Mockito.anyBoolean());

        Mockito.verify(s3Client, Mockito.times(1)).save(Mockito.eq(runId), Mockito.eq(serializedData), Mockito.eq(partitionId));

        Mockito.verify(sqsClient, Mockito.times(1)).sendMessageToWorkflowQueue(Mockito.eq(ref));
    }
}
