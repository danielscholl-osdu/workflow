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

package org.opengroup.osdu.workflow.aws.service;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AirflowClient  {
    public void makeRequestToAirflow(String airflowDagURL, String body, String dagName) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        HttpURLConnection connection = getConnection(body, headers, airflowDagURL);

        sendRequest(connection, body);
        final String response = getResponse(connection).toString();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
          log.info(String.format("Response from airflow for dag %s: %s", dagName, response));
        } else {
          log.info(String.format("Something is wrong and we didn't get a good response for %s. It was a %s response", dagName, connection.getResponseCode()));
        }
    }

    private HttpURLConnection getConnection(String body, Map<String, String> headers, String targetURL) throws IOException {
        URL url = new URL(targetURL);
        HttpURLConnection connection =  (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Length",
                Integer.toString(body.getBytes().length));

        for(Map.Entry<String, String> entry: headers.entrySet()){
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.setUseCaches(false);
        connection.setDoOutput(true);
        return connection;
    }

  public void sendRequest(HttpURLConnection connection, String body) throws IOException {
    DataOutputStream writer = new DataOutputStream (
        connection.getOutputStream());
    writer.writeBytes(body);
    writer.close();
  }

  public StringBuilder getResponse(HttpURLConnection connection) throws IOException {
    StringBuilder response = new StringBuilder();
    InputStream is = connection.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = reader.readLine()) != null) {
      response.append(line);
      response.append('\r');
    }
    reader.close();
    return response;
  }
}
