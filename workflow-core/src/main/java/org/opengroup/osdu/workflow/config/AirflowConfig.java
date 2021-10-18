package org.opengroup.osdu.workflow.config;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.core.common.model.http.AppException;
import sun.misc.BASE64Encoder;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Configuration
@ConfigurationProperties("osdu.airflow")
public class AirflowConfig {
  private final static String USERNAME_ERROR = "Airflow username cannot be null";
  private final static String PASSWORD_ERROR = "Airflow password cannot be null";

  private String url;
  private String username;
  private String password;
  private boolean dagRunAbstractionEnabled;
  private String controllerDagId;
  private boolean version2;

  public String getAppKey() {
    if (Objects.isNull(username)) {
      throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
          USERNAME_ERROR, USERNAME_ERROR);
    }

    if (Objects.isNull(password)) {
      throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
          PASSWORD_ERROR, PASSWORD_ERROR);
    }

    String airflowAuthString = username + ":" + password;
    return new BASE64Encoder().encode(airflowAuthString.getBytes());
  }
}
