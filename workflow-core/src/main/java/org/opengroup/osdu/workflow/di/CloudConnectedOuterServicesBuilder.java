package org.opengroup.osdu.workflow.di;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.info.ConnectedOuterServicesBuilder;
import org.opengroup.osdu.core.common.model.info.ConnectedOuterService;
import org.opengroup.osdu.workflow.service.AirflowV2WorkflowEngineServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "osdu.airflow.version2", havingValue = "true", matchIfMissing = false)
public class CloudConnectedOuterServicesBuilder implements ConnectedOuterServicesBuilder {

  public static final String AIRFLOW = "Airflow";
  private final AirflowV2WorkflowEngineServiceImpl airflowV2WorkflowEngineService;

  @Override
  public List<ConnectedOuterService> buildConnectedOuterServices() {
    String airflowVersion = airflowV2WorkflowEngineService.getAirflowVersion();
    return ImmutableList.of(
        ConnectedOuterService.builder()
            .name(AIRFLOW)
            .version(airflowVersion)
            .build()
    );
  }
}
