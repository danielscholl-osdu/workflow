// Copyright © 2020 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.aws.workflow.util;

import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;

public class WorkflowStatusUtil {

  private final static String ENVIRONMENT = "ENVIRONMENT";
  private final static String DYNAMO_DB_REGION = "DYNAMO_DB_REGION";
  private final static String DYNAMO_DB_ENDPOINT = "DYNAMO_DB_ENDPOINT";

  private final static int MAX_WAIT_TIME_SECONDS = 10;

  private final static String DYNAMO_ITEM_RETRIEVAL_ERROR_MSG = "Failed to retrieve testing workflow status in allotted time.";

  DynamoDBQueryHelper queryHelper;

  public String insertWorkflowStatus() throws Exception {
    String tablePrefix = String.format("%s%s", System.getenv(ENVIRONMENT), "-");
    String dynamoDbRegion = System.getenv(DYNAMO_DB_REGION);
    String dynamoDbEndpoint = System.getenv(DYNAMO_DB_ENDPOINT);
    queryHelper = new DynamoDBQueryHelper(dynamoDbEndpoint, dynamoDbRegion, tablePrefix);

    String workflowId = "int-test-" + java.util.UUID.randomUUID();
    WorkflowStatusDoc workflowStatusDoc = new WorkflowStatusDoc();
    workflowStatusDoc.setWorkflowId(workflowId);
    workflowStatusDoc.setWorkflowStatusType("FINISHED");
    queryHelper.save(workflowStatusDoc);

    waitUntilFound(workflowStatusDoc);

    return workflowId;
  }

  public void deleteWorkflow(String workflowId){
    queryHelper.deleteByPrimaryKey(WorkflowStatusDoc.class, workflowId);
  }

  // necessary due to dynamo's eventual consistency
  // if not included, int tests would be flakey
  private void waitUntilFound(WorkflowStatusDoc workflowStatusDoc) throws Exception {
    boolean isFound = false;

    long startTime = System.currentTimeMillis();
    while(!isFound){
      WorkflowStatusDoc doc = queryHelper.loadByGSI(WorkflowStatusDoc.class, workflowStatusDoc);
      if(doc != null){
        isFound = true;
      } else {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long elapsedSeconds = elapsedTime / 1000;
        if(elapsedSeconds > MAX_WAIT_TIME_SECONDS){
         throw new Exception(DYNAMO_ITEM_RETRIEVAL_ERROR_MSG);
        }
      }
    }
  }
}
