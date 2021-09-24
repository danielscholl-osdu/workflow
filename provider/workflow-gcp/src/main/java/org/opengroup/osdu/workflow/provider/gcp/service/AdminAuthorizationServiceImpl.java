package org.opengroup.osdu.workflow.provider.gcp.service;

import org.opengroup.osdu.workflow.provider.interfaces.IAdminAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthorizationServiceImpl implements IAdminAuthorizationService {
  @Override
  public boolean isDomainAdminServiceAccount() {
    return false;
  }
}
