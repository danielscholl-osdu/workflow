/**
 * Copyright 2020 IBM Corp. All Rights Reserved.
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

package org.opengroup.osdu.ibm.workflow.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_STATUS_URL;

import javax.ws.rs.HttpMethod;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.ibm.workflow.util.HTTPClientIBM;
import org.opengroup.osdu.workflow.consts.DefaultVariable;
import org.opengroup.osdu.workflow.util.PayloadBuilder;
import org.opengroup.osdu.workflow.workflow.PostGetStatusIntegrationTests;

import com.sun.jersey.api.client.ClientResponse;

public class TestPostGetStatusIntegration extends PostGetStatusIntegrationTests{

	@BeforeEach
	@Override
	public void setup() throws Exception {
		this.client = new HTTPClientIBM();
		this.headers = client.getCommonHeader();
	}

	@AfterEach
	@Override
	public void tearDown() throws Exception {
		this.client = null;
		this.headers = null;
	}
	
	

}
