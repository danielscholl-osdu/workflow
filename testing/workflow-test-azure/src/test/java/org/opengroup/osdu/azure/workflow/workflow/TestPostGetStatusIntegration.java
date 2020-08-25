package org.opengroup.osdu.azure.workflow.workflow;


import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.workflow.utils.DummyRecordsHelper;
import org.opengroup.osdu.azure.workflow.utils.HTTPClientAzure;
import org.opengroup.osdu.azure.workflow.utils.AzurePayLoadBuilder;
import org.opengroup.osdu.workflow.consts.DefaultVariable;
import org.opengroup.osdu.workflow.util.HTTPClient;
import org.opengroup.osdu.workflow.util.PayloadBuilder;
import org.opengroup.osdu.workflow.workflow.PostGetStatusIntegrationTests;

import javax.ws.rs.HttpMethod;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.workflow.consts.TestConstants.GET_STATUS_URL;

public class TestPostGetStatusIntegration extends PostGetStatusIntegrationTests {
  protected static final DummyRecordsHelper RECORDS_HELPER = new DummyRecordsHelper();

	@BeforeEach
	@Override
	public void setup() {
		this.client = new HTTPClientAzure();
		this.headers = client.getCommonHeader();
	}

  @Test
  @Override
  public void should_returnUnauthorized_when_notGivenAccessToken()  {
    ClientResponse response = client.send(
        HttpMethod.POST,
        GET_STATUS_URL,
        PayloadBuilder.buildWorkflowIdPayload(DefaultVariable.getEnvironmentVariableOrDefaultKey(DefaultVariable.FINISHED_WORKFLOW_ID)),
        headers,
        null
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }
  @Test
  public void should_returnUnauthorized_when_givenInvalidPartition() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        GET_STATUS_URL,
        PayloadBuilder.buildWorkflowIdPayload(DefaultVariable.getEnvironmentVariableOrDefaultKey(DefaultVariable.FINISHED_WORKFLOW_ID)),
        HTTPClient.overrideHeader(headers, "invalid-partition"),
        client.getAccessToken()
    );

    assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void should_returnBadRequest_when_givenInvalidFormatWorkflowId() throws Exception {
    ClientResponse response = client.send(
        HttpMethod.POST,
        GET_STATUS_URL,
        AzurePayLoadBuilder.buildInvalidWorkflowIdPayload(DefaultVariable.getEnvironmentVariableOrDefaultKey(DefaultVariable.FINISHED_WORKFLOW_ID)),
        headers,
        client.getAccessToken()
    );
    Assert.assertEquals(HttpStatus.SC_BAD_REQUEST,response.getStatus());
    DummyRecordsHelper.BadRequestMock responseObject = RECORDS_HELPER.getRecordsMockFromBadRequestResponse(response);
   String resp="Unrecognized field \"Workflow\"";
    assertThat(responseObject.message,containsString(resp));

  }
	@AfterEach
	@Override
	public void tearDown()  {
		this.client = null;
		this.headers = null;
	}
}
