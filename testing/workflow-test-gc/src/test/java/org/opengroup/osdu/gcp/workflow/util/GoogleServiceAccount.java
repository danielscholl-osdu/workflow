/*
  Copyright 2020 Google LLC
  Copyright 2020 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.opengroup.osdu.gcp.workflow.util;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import lombok.extern.java.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

@Log
public class GoogleServiceAccount {
  private final static Predicate<String> contentAcceptanceTester = s -> s.trim().startsWith("{");
  private static final String DEFAULT_TARGET_AUDIENCE = "osdu";
  final ServiceAccountCredentials serviceAccount;


  public GoogleServiceAccount(String serviceAccountValue) throws IOException {

    serviceAccountValue = new DecodedContentExtractor(serviceAccountValue, contentAcceptanceTester).getContent();

    try (InputStream inputStream = new ByteArrayInputStream(serviceAccountValue.getBytes())) {
      this.serviceAccount = ServiceAccountCredentials.fromStream(inputStream);
    }
  }

  public String getEmail() {
    return this.serviceAccount.getClientEmail();
  }

  public String getAuthToken() throws IOException {
    JwtBuilder jwtBuilder = Jwts.builder();

    Map<String, Object> header = new HashMap<>();
    header.put("type", "JWT");
    header.put("alg", "RS256");
    jwtBuilder.setHeader(header);

    Map<String, Object> claims = new HashMap<>();
    claims.put("target_audience", DEFAULT_TARGET_AUDIENCE);
    claims.put("exp", System.currentTimeMillis() / 1000 + 3600);
    claims.put("iat", System.currentTimeMillis() / 1000);
    claims.put("iss", this.getEmail());
    claims.put("aud", "https://www.googleapis.com/oauth2/v4/token");
    jwtBuilder.addClaims(claims);

    jwtBuilder.signWith(SignatureAlgorithm.RS256, this.serviceAccount.getPrivateKey());
    String jwt = jwtBuilder.compact();

    HttpPost httpPost = new HttpPost("https://www.googleapis.com/oauth2/v4/token");

    ArrayList<NameValuePair> postParameters = new ArrayList<>();
    postParameters.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"));
    postParameters.add(new BasicNameValuePair("assertion", jwt));

    HttpClient client = new DefaultHttpClient();

    httpPost.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
    HttpResponse response = client.execute(httpPost);

    String responseEntity = EntityUtils.toString(response.getEntity());
    JsonObject content = new JsonParser().parse(responseEntity).getAsJsonObject();
    return content.get("id_token").getAsString();
  }
}
