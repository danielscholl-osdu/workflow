/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.workflow.aws.service.s3;

import javax.inject.Inject;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.aws.s3.IS3ClientFactory;
import org.opengroup.osdu.core.aws.s3.S3ClientWithBucket;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class S3Client {

  @Inject
  private IS3ClientFactory s3ClientFactory;

  @Value("${aws.s3.recordsBucket.ssm.relativePath}")
  private String s3RecordsBucketParameterRelativePath;

  private S3ClientWithBucket getS3ClientWithBucket(String dataPartition) {
      return s3ClientFactory.getS3ClientForPartition(dataPartition, s3RecordsBucketParameterRelativePath);
  }

  public String save(String runId, String content, String dataPartition){
      log.info(String.format("Saving %s content to s3 for data partition: %s", runId, content));

    String s3Url = "";

      try {
          String keyName = java.util.UUID.randomUUID().toString();

          S3ClientWithBucket s3ClientWithBucket = getS3ClientWithBucket(dataPartition);
          AmazonS3 s3 = s3ClientWithBucket.getS3Client();
          String workflowBucketName = s3ClientWithBucket.getBucketName();
          s3.putObject(workflowBucketName, keyName, content);
          s3Url = String.format("s3://%s/%s", workflowBucketName, keyName);
      } catch(Exception e){
          log.error(String.format("Couldn't save content to s3: %s", e.getMessage()), e);
          throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Couldn't process request", "Failure to kick off request");
      }

      return s3Url;
  }

}
