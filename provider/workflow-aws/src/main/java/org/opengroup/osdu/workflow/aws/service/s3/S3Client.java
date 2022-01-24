package org.opengroup.osdu.workflow.aws.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.aws.s3.IS3ClientFactory;
import org.opengroup.osdu.core.aws.s3.S3ClientWithBucket;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

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
