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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "IngestionStrategyRepository")
public class IngestionStrategyDoc {

    @DynamoDBHashKey(attributeName = "Id")
    private String id;

    @DynamoDBAttribute(attributeName = "WorkflowType")
    private String workflowType;

    @DynamoDBAttribute(attributeName = "DataType")
    private String dataType;

    @DynamoDBAttribute(attributeName = "UserId")
    private String userId;

    @DynamoDBAttribute(attributeName = "DagName")
    private String dagName;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getWorkflowType() {
      return workflowType;
    }

    public void setWorkflowType(String workflowType) {
      this.workflowType = workflowType;
    }

    public String getDataType() {
      return dataType;
    }

    public void setDataType(String dataType) {
      this.dataType = dataType;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public String getDagName() {
      return dagName;
    }

    public void setDagName(String dagName) {
      this.dagName = dagName;
    }
}
