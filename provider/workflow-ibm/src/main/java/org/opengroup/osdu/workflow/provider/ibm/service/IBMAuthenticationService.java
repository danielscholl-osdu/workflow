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

package org.opengroup.osdu.workflow.provider.ibm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.exception.UnauthorizedException;
import org.opengroup.osdu.workflow.model.ClientResponse;
import org.opengroup.osdu.workflow.model.WorkflowEngineRequest;
import org.opengroup.osdu.workflow.provider.interfaces.IAuthenticationService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IBMAuthenticationService implements IAuthenticationService {

  @Override
  public void checkAuthentication(String authorizationToken, String partitionID) {
    log.debug("Start checking authentication. Authorization: {}, partitionID: {}",
        authorizationToken, partitionID);

    checkPreconditions(authorizationToken, partitionID);

    // TODO: add check of user permissions

    log.debug("Finished checking authentication.");
  }

  @Override
  public ClientResponse sendAirflowRequest(
      String httpMethod, String url, String body,
      WorkflowEngineRequest rq) {
    return null;
  }

  private void checkPreconditions(String authorizationToken, String partitionID) {
    if (authorizationToken == null) {
      throw new UnauthorizedException("Missing authorization token");
    }

    if (partitionID == null) {
      throw new UnauthorizedException("Missing partitionID");
    }
  }

}
