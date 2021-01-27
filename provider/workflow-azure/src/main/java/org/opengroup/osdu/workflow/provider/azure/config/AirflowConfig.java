package org.opengroup.osdu.workflow.provider.azure.config;

import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.common.Validators;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import sun.misc.BASE64Encoder;

@ConfigurationProperties("osdu.azure.airflow")
@Configuration
@Getter
@Setter
public class AirflowConfig {
  private String url;
  private String username;
  private String password;

  public String getAppKey() {
    Validators.checkNotNull(username, "Airflow username cannot be null");
    Validators.checkNotNull(password, "Airflow password cannot be null");
    String airflowAuthString = username + ":" + password;
    return new BASE64Encoder().encode(airflowAuthString.getBytes());
  }
}
