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
import java.util.*;
import java.util.function.Predicate;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import lombok.extern.java.Log;

@Log
public class GoogleServiceAccount {
  private final static Predicate<String> contentAcceptanceTester = s -> s.trim().startsWith("{");
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

  public String getAuthToken(String audience) throws IOException {
    JwtBuilder jwtBuilder = Jwts.builder();

    Map<String, Object> header = new HashMap<>();
    header.put("type", "JWT");
    header.put("alg", "RS256");
    jwtBuilder.setHeader(header);

    Map<String, Object> claims = new HashMap<>();
    claims.put("target_audience", audience);
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
