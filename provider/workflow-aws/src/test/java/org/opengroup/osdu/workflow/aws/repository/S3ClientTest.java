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

 package org.opengroup.osdu.workflow.aws.repository;

 import com.amazonaws.services.s3.AmazonS3;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.opengroup.osdu.core.aws.s3.IS3ClientFactory;
 import org.opengroup.osdu.core.aws.s3.S3ClientWithBucket;
 import org.opengroup.osdu.workflow.aws.service.s3.S3Client;
 import org.springframework.boot.test.context.SpringBootTest;

 import static org.mockito.MockitoAnnotations.initMocks;

 @RunWith(MockitoJUnitRunner.class)
 @SpringBootTest(classes={S3Client.class})
 public class S3ClientTest {

     @InjectMocks
     S3Client CUT = new S3Client();

     @Mock
     private IS3ClientFactory s3ClientFactory;

     @Before
     public void setUp() {
         initMocks(this);
     }

     @Test
     public void save()
     {
       // Arrange
       AmazonS3 s3 = Mockito.mock(AmazonS3.class);
       S3ClientWithBucket s3ClientWithBucket = Mockito.mock(S3ClientWithBucket.class);
       Mockito.when(s3ClientWithBucket.getS3Client())
           .thenReturn(s3);
       Mockito.when(s3ClientFactory.getS3ClientForPartition(Mockito.anyString(), Mockito.anyString()))
           .thenReturn(s3ClientWithBucket);

       // Act
       String guid = CUT.save("runId", "content", "data-partition");

       // Assert
       Mockito.verify(s3, Mockito.times(1)).putObject(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
     }
 }
