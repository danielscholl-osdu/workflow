package org.opengroup.osdu.workflow.provider.ibm.service;

import org.opengroup.osdu.workflow.provider.interfaces.IAdminAuthorizationService;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthorizationServiceImpl implements IAdminAuthorizationService {
  @Override
  public boolean isDomainAdminServiceAccount() {
    return false;
  }
}
