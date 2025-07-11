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

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import org.opengroup.osdu.core.aws.v2.sqs.AmazonSQSConfig;
import org.opengroup.osdu.workflow.aws.config.AwsServiceConfig;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.sqs.SqsClient;

@Component
public class WorkflowSqsClient {

  private final AwsServiceConfig awsConfig;
  private final SqsClient sqs;
  private final JaxRsDpsLog log;

  @Autowired
  public WorkflowSqsClient(
      AwsServiceConfig awsServiceConfig,
      JaxRsDpsLog log
  ){
    this.awsConfig = awsServiceConfig;
    AmazonSQSConfig amazonSQSConfig = new AmazonSQSConfig(awsConfig.getAmazonRegion());
    sqs = amazonSQSConfig.AmazonSQS();
    this.log = log;
  }

  public void sendMessageToWorkflowQueue(String ref){
    log.info("Sending message");
    SendMessageRequest sendMsgRequest  = SendMessageRequest
        .builder()
        .queueUrl(awsConfig.getWorkflowQueueUrl())
        .messageBody(ref)
        .build();
    sqs.sendMessage(sendMsgRequest);
    log.info("Message successfully sent");
  }
}
