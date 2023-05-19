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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.opengroup.osdu.core.aws.iam.IAMConfig;

import org.opengroup.osdu.core.aws.configurationsetup.ConfigSetup;

public class SqsConfig {
  private AWSCredentialsProvider amazonAWSCredentials = IAMConfig.amazonAWSCredentials();
  private String amazonSQSRegion;

  public SqsConfig(String amazonSQSRegion){
    this.amazonSQSRegion = amazonSQSRegion;
  }

  public AmazonSQS AmazonSQS(){
    return AmazonSQSClientBuilder.standard().withCredentials(
        this.amazonAWSCredentials
    ).withRegion(
        this.amazonSQSRegion
    ).withClientConfiguration(ConfigSetup.setUpConfig()).build();
  }
}
