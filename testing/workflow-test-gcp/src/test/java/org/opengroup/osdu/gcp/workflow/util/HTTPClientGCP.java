package org.opengroup.osdu.gcp.workflow.util;

import static org.opengroup.osdu.workflow.consts.DefaultVariable.GOOGLE_AUDIENCE;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.INTEGRATION_TESTER;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.NO_DATA_ACCESS_TESTER;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.getEnvironmentVariableOrDefaultKey;

import lombok.extern.java.Log;
import org.opengroup.osdu.workflow.util.HTTPClient;

@Log
public class HTTPClientGCP extends HTTPClient {
	@Override
	public synchronized String getAccessToken() throws Exception {
		if (accessToken == null || accessToken.isEmpty()) {
		  log.info("Get INTEGRATION_TESTER credentials");
			accessToken = new GoogleServiceAccount(getEnvironmentVariableOrDefaultKey(INTEGRATION_TESTER))
					.getAuthToken(getEnvironmentVariableOrDefaultKey(GOOGLE_AUDIENCE));
		}
		return "Bearer " + accessToken;
	}

	@Override
	public synchronized String getNoDataAccessToken() throws Exception {
		if (noDataAccessToken == null || noDataAccessToken.isEmpty()) {
      log.info("Get NO_DATA_ACCESS_TESTER credentials");
			noDataAccessToken = new GoogleServiceAccount(getEnvironmentVariableOrDefaultKey(NO_DATA_ACCESS_TESTER))
					.getAuthToken(getEnvironmentVariableOrDefaultKey(GOOGLE_AUDIENCE));

		}
		return "Bearer " + noDataAccessToken;
	}
}
