package org.opengroup.osdu.workflow.provider.azure.config;

import com.microsoft.azure.spring.autoconfigure.aad.AADAppRoleStatelessAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationConfig {

  @Autowired
  private AADAppRoleStatelessAuthenticationFilter aadAppRoleStatelessAuthenticationFilter;

  @Bean
  @ConditionalOnProperty(value = "azure.istio.auth.enabled", havingValue = "true", matchIfMissing = false)
  public FilterRegistrationBean<AADAppRoleStatelessAuthenticationFilter> authFilterIfIstioEnabled() {
    FilterRegistrationBean<AADAppRoleStatelessAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(aadAppRoleStatelessAuthenticationFilter);
    registrationBean.addUrlPatterns("/v1/workflow/system","/v1/workflow/system/*");
    return registrationBean;
  }

  @Bean
  @ConditionalOnProperty(value = "azure.istio.auth.enabled", havingValue = "false", matchIfMissing = false)
  public FilterRegistrationBean<AADAppRoleStatelessAuthenticationFilter> authFilterIfIstioDisabled() {
    FilterRegistrationBean<AADAppRoleStatelessAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(aadAppRoleStatelessAuthenticationFilter);
    registrationBean.addUrlPatterns("/v1/workflow/*");
    return registrationBean;
  }
}
