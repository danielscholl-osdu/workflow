package org.opengroup.osdu.gcp.workflow.util;

import static org.opengroup.osdu.workflow.consts.DefaultVariable.GOOGLE_AUDIENCE;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.INTEGRATION_TESTER;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.NO_DATA_ACCESS_TESTER;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.getEnvironmentVariableOrDefaultKey;

import org.opengroup.osdu.workflow.util.HTTPClient;

public class HTTPClientGCP extends HTTPClient {
	@Override
	public synchronized String getAccessToken() throws Exception {
		if (accessToken == null || accessToken.isEmpty()) {
			accessToken = new GoogleServiceAccount(getEnvironmentVariableOrDefaultKey(INTEGRATION_TESTER))
					.getAuthToken(getEnvironmentVariableOrDefaultKey(GOOGLE_AUDIENCE));
		}
		return "Bearer " + accessToken;
	}

	@Override
	public synchronized String getNoDataAccessToken() throws Exception {
		if (noDataAccessToken == null || noDataAccessToken.isEmpty()) {
			noDataAccessToken = new GoogleServiceAccount(getEnvironmentVariableOrDefaultKey(NO_DATA_ACCESS_TESTER))
					.getAuthToken(getEnvironmentVariableOrDefaultKey(GOOGLE_AUDIENCE));

		}
		return "Bearer " + noDataAccessToken;
	}
}
