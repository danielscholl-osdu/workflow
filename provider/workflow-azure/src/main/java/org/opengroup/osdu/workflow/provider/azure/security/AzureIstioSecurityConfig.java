package org.opengroup.osdu.workflow.provider.azure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.opengroup.osdu.workflow.provider.azure.security.AadSecurityConfig.AUTH_ALLOWLIST;
import static org.springframework.security.config.Customizer.withDefaults;


@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnProperty(value = "azure.istio.auth.enabled", havingValue = "true", matchIfMissing = true)
public class AzureIstioSecurityConfig {
  @Autowired
  AzureIstioSecurityFilter azureIstioSecurityFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      return http
          .csrf(AbstractHttpConfigurer::disable)
          .sessionManagement((sess) -> sess.sessionCreationPolicy(SessionCreationPolicy.NEVER))
          .authorizeHttpRequests(request -> request.requestMatchers(AUTH_ALLOWLIST).permitAll())
          .addFilterBefore(azureIstioSecurityFilter, UsernamePasswordAuthenticationFilter.class)
          .build();
    }

}
