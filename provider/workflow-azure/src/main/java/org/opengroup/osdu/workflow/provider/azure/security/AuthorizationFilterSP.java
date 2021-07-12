package org.opengroup.osdu.workflow.provider.azure.security;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.core.common.exception.UnauthorizedException;
import org.opengroup.osdu.workflow.provider.azure.interfaces.IAuthorizationServiceSP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component("authorizationFilterSP")
@RequestScope
public class AuthorizationFilterSP {

  @Autowired
  private IAuthorizationServiceSP authorizationService;
  final DpsHeaders headers;

  public boolean hasPermissions() {
    validateMandatoryHeaders();
    headers.put(DpsHeaders.USER_EMAIL, "ServicePrincipalUser");
    return authorizationService.isDomainAdminServiceAccount();
  }

  private void validateMandatoryHeaders() {
    if (StringUtils.isEmpty(this.headers.getAuthorization())) {
      throw new UnauthorizedException("Authorization header is mandatory");
    }
    if (!StringUtils.isEmpty(this.headers.getPartitionId())) {
      throw new BadRequestException("data-partition-id header should not be passed");
    }
  }
}
