/*
  Copyright 2021 Google LLC
  Copyright 2021 EPAM Systems, Inc

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

package org.opengroup.osdu.workflow.provider.gcp.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.provider.gcp.config.GcpPropertiesConfiguration;
import org.opengroup.osdu.workflow.provider.interfaces.IAdminAuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collections;
import java.util.Objects;

@Slf4j
@Service
@RequestScope
@RequiredArgsConstructor
public class AdminAuthorizationServiceImpl implements IAdminAuthorizationService {

  private final DpsHeaders headers;
  private final GcpPropertiesConfiguration configuration;

  @Override
  public boolean isDomainAdminServiceAccount() {
    if (Objects.isNull(headers.getAuthorization()) || headers.getAuthorization().isEmpty()) {
      throw AppException.createUnauthorized("No JWT token. Access is Forbidden");
    }
    String email = null;
    try {
      GoogleIdTokenVerifier verifier =
          new GoogleIdTokenVerifier.Builder(
              GoogleNetHttpTransport.newTrustedTransport(),
              JacksonFactory.getDefaultInstance())
              .setAudience(Collections.singleton(configuration.getGoogleAudiences()))
              .build();

      String authorization = headers.getAuthorization().replace("Bearer ", "");
      GoogleIdToken googleIdToken = verifier.verify(authorization);
      if (Objects.isNull(googleIdToken)) {
        log.warn("Not valid token provided");
        throw AppException.createUnauthorized("Unauthorized. The JWT token could not be validated");
      }
      email = googleIdToken.getPayload().getEmail();
      String workflowAdminAccount = configuration.getWorkflowAdminAccount();

      log.debug("workflowAdminAccount: " + workflowAdminAccount);
      if (Objects.nonNull(workflowAdminAccount) && !workflowAdminAccount.isEmpty()) {
        if (email.equals(workflowAdminAccount)) {
          return true;
        } else {
          throw AppException
              .createUnauthorized(String.format("Unauthorized. The user %s is untrusted.", email));
        }
      } else {
        if (StringUtils.endsWithIgnoreCase(email, "gserviceaccount.com")) {
          return true;
        } else {
          throw AppException.createUnauthorized(
              String.format("Unauthorized. The user %s is not Service Principal", email));
        }
      }
    } catch (Exception ex) {
      log.warn(String.format("User %s is not unauthorized. %s.", email, ex));
      throw AppException.createUnauthorized("Unauthorized. The JWT token could not be validated");
    }
  }
}

