package org.opengroup.osdu.workflow.di;

import java.util.ArrayList;
import java.util.List;
import org.opengroup.osdu.core.common.info.ConnectedOuterServicesBuilder;
import org.opengroup.osdu.core.common.model.info.ConnectedOuterService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(type = "ConnectedOuterServicesBuilder")
public class CloudConnectedOuterServicesBuilder implements ConnectedOuterServicesBuilder {

  // TODO Need to implement this functionality after upgrading Airflow version,
  // currently Airflow doesn't have public version info endpoint.

  @Override
  public List<ConnectedOuterService> buildConnectedOuterServices() {
    return new ArrayList<>();
  }
}
