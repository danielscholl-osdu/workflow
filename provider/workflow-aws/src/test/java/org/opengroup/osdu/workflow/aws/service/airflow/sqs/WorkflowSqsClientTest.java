/*
 * Copyright © 2021 Amazon Web Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.aws.service.airflow.sqs;

import static org.mockito.Mockito.when;

import com.amazonaws.services.sqs.AmazonSQS;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.workflow.aws.config.AwsServiceConfig;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowSqsClientTest {

  	@InjectMocks
  	WorkflowSqsClient client = new WorkflowSqsClient();

  	@Mock
  	AwsServiceConfig awsConfig;

  	@Test
  	public void test()
	{
		AmazonSQS sqs = Mockito.mock(AmazonSQS.class);

		try (MockedConstruction<SqsConfig> sqsConfig = Mockito.mockConstruction(SqsConfig.class, (mockSqsConfig, context) -> {
            when(mockSqsConfig.AmazonSQS()).thenReturn(sqs);
        })) {   
			client.init();
			client.sendMessageToWorkflowQueue("ref");
			Mockito.verify(sqs, Mockito.times(1)).sendMessage(Mockito.any());
		}
	}

  
}
