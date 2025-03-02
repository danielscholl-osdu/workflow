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

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import org.opengroup.osdu.workflow.aws.config.AwsServiceConfig;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WorkflowSqsClient {

  @Inject
  AwsServiceConfig awsConfig;

  // @Value("${WORKFLOW_QUEUE_URL}")
  // private String workflow_queue_url;

  // @Value("${AWS_REGION}")
  // private String awsRegion;

  private AmazonSQS sqs;

  @PostConstruct
  public void init(){
    SqsConfig sqsConfig = new SqsConfig(awsConfig.amazonRegion);
    sqs = sqsConfig.AmazonSQS();
  }

  public void sendMessageToWorkflowQueue(String ref){
    log.info("Sending message");
    log.debug(String.format("S3 reference: %s", ref));
    SendMessageRequest sendMsgRequest = new SendMessageRequest()
        .withQueueUrl(awsConfig.workflowQueueUrl)
        .withMessageBody(ref);
    sqs.sendMessage(sendMsgRequest);
    log.info("Message successfully sent");
  }
}
