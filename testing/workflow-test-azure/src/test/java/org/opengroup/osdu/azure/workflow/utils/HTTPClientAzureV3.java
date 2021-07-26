package org.opengroup.osdu.azure.workflow.utils;

import com.google.common.base.Strings;
import lombok.extern.java.Log;
import org.opengroup.osdu.azure.util.AzureServicePrincipal;
import org.opengroup.osdu.workflow.util.HTTPClient;

import java.util.HashMap;
import java.util.Map;

import static org.opengroup.osdu.workflow.consts.DefaultVariable.DEFAULT_DATA_PARTITION_ID_TENANT1;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.getEnvironmentVariableOrDefaultKey;
import static org.opengroup.osdu.workflow.consts.TestConstants.HEADER_DATA_PARTITION_ID;

@Log
public class HTTPClientAzureV3 extends HTTPClient {

  @Override
  public synchronized String getAccessToken() throws Exception {
    if (Strings.isNullOrEmpty(accessToken)) {
      String sp_id = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
      String sp_secret = System.getProperty("TESTER_SERVICEPRINCIPAL_SECRET", System.getenv("TESTER_SERVICEPRINCIPAL_SECRET"));
      String tenant_id = System.getProperty("AZURE_AD_TENANT_ID", System.getenv("AZURE_AD_TENANT_ID"));
      String app_resource_id = System.getProperty("AZURE_AD_APP_RESOURCE_ID", System.getenv("AZURE_AD_APP_RESOURCE_ID"));
      accessToken = new AzureServicePrincipal().getIdToken(sp_id, sp_secret, tenant_id, app_resource_id);
    }
    return "Bearer " + accessToken;
  }

  @Override
  public synchronized String getNoDataAccessToken() throws Exception {
    if (Strings.isNullOrEmpty(noDataAccessToken)) {
      String sp_id = System.getProperty("NO_DATA_ACCESS_TESTER", System.getenv("NO_DATA_ACCESS_TESTER"));
      String sp_secret = System.getProperty("NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET", System.getenv("NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET"));
      String tenant_id = System.getProperty("AZURE_AD_TENANT_ID", System.getenv("AZURE_AD_TENANT_ID"));
      String app_resource_id = System.getProperty("AZURE_AD_APP_RESOURCE_ID", System.getenv("AZURE_AD_APP_RESOURCE_ID"));
      noDataAccessToken = new AzureServicePrincipal().getIdToken(sp_id, sp_secret, tenant_id, app_resource_id);
    }
    return "Bearer " + noDataAccessToken;
  }

  public Map<String, String> getCommonHeader() {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_DATA_PARTITION_ID, getEnvironmentVariableOrDefaultKey(DEFAULT_DATA_PARTITION_ID_TENANT1));
    return headers;
  }
}
