// Copyright 2017-2019, Schlumberger
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
package org.opengroup.osdu.azure.workflow.workflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opengroup.osdu.azure.workflow.framework.workflow.GetWorkflowRunByIdIntegrationTests;
import org.opengroup.osdu.azure.workflow.utils.HTTPClientAzure;

public class TestGetWorkflowRunByIdIntegration extends GetWorkflowRunByIdIntegrationTests {

  @BeforeEach
  @Override
  public void setup() throws Exception {
    super.setup();
    this.client = new HTTPClientAzure();
    this.headers = client.getCommonHeader();
    this.initializeTriggeredWorkflow();
  }

  @AfterEach
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    this.client = null;
    this.headers = null;
  }
}
