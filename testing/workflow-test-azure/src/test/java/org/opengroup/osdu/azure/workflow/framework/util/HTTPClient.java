package org.opengroup.osdu.azure.workflow.framework.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import lombok.ToString;
import lombok.extern.java.Log;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import static org.opengroup.osdu.workflow.consts.DefaultVariable.DEFAULT_DATA_PARTITION_ID_TENANT1;
import static org.opengroup.osdu.workflow.consts.DefaultVariable.getEnvironmentVariableOrDefaultKey;
import static org.opengroup.osdu.workflow.consts.TestConstants.HEADER_CORRELATION_ID;
import static org.opengroup.osdu.workflow.consts.TestConstants.HEADER_DATA_PARTITION_ID;

@Log
@ToString
public abstract class HTTPClient {

	private final int MAX_ID_SIZE = 50;

	protected static String accessToken;
	protected static String noDataAccessToken;


	public abstract String getAccessToken() throws Exception;
	public abstract String getNoDataAccessToken() throws Exception;

	private static Client getClient() {
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		}};

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception ignored) {
		}
		return Client.create();
	}

	public ClientResponse send(String httpMethod, String url, String payLoad, Map<String, String> headers, String token) {
		ClientResponse response;
		try {
			String correlationId = java.util.UUID.randomUUID().toString();
			log.info(String.format("Request correlation id: %s", correlationId));
			headers.put(HEADER_CORRELATION_ID, correlationId);
			Client client = getClient();
			client.setReadTimeout(180000);
			client.setConnectTimeout(10000);
			WebResource webResource = client.resource(url);
      log.info("URL = " + url);
			response = this.getClientResponse(httpMethod, payLoad, webResource, headers, token);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssertionError("Error: Send request error", e);
		}
		log.info("waiting on response");
		return response;
	}

	private ClientResponse getClientResponse(String httpMethod, String requestBody, WebResource webResource, Map<String, String> headers, String token) {
		final WebResource.Builder builder = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).header("Authorization", token);
		headers.forEach(builder::header);
		log.info("making request to datalake api");
		return builder.method(httpMethod, ClientResponse.class, requestBody);
	}

	public Map<String, String> getCommonHeader() {
		Map<String, String> headers = new HashMap<>();
		headers.put(HEADER_DATA_PARTITION_ID, getEnvironmentVariableOrDefaultKey(DEFAULT_DATA_PARTITION_ID_TENANT1));
		return headers;
	}

	public static Map<String, String> overrideHeader(Map<String, String> currentHeaders, String... partitions) {
		String value = String.join(",", partitions);
		currentHeaders.put(HEADER_DATA_PARTITION_ID, value);
		return currentHeaders;
	}
}
