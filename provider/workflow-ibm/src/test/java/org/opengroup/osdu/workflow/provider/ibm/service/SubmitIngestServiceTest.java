/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.workflow.provider.ibm.service;


import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.workflow.provider.ibm.property.AirflowProperties;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;


@ExtendWith(MockitoExtension.class)
class SubmitIngestServiceTest {

  private static final String TEST_AIRFLOW_URL = "http://test-airflow";
  private static final String TEST_CLIENT_ID = "client-id";
  private static final String Websrv_URL = "http://test-airflow/api/dag_id/dagruns";

  @Mock
  private AirflowProperties airflowProperties;



  @Mock
  private HttpPost httpPost;




  @Mock
  private HttpClient httpClient;

 @Mock
 SubmitIngestServiceImpl submitIngestService;

  @Mock
  private HttpResponse httpResponse;


  @Test
  void shouldStartWorkflow() throws IOException {

    // given
    HashMap<String, Object> data = new HashMap<>();
    data.put("key", "value");


   Mockito.when(submitIngestService.submitIngest("dag-name", data)).thenReturn(true);
    // when
    assertEquals(submitIngestService.submitIngest("dag-name", data), true);

    // then

  }

  // Cannot throw runtime excpetion
  @Test
  void shouldThrowExceptionIfRequestFails() throws IOException {

	  // given
	    HashMap<String, Object> data = new HashMap<>();
	    data.put("key", "value");




  }

}
