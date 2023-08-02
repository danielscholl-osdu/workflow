package org.opengroup.osdu.workflow.provider.azure.security;
import com.azure.spring.autoconfigure.aad.AADAppRoleStatelessAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ConditionalOnProperty(value = "azure.istio.auth.enabled", havingValue = "false", matchIfMissing = false)
public class AadSecurityConfig extends WebSecurityConfigurerAdapter {
  public static final String[] AUTH_ALLOWLIST = {"/", "/index.html",
      "/api-docs.yaml",
      "/api-docs/swagger-config",
      "/api-docs/**",
      "/swagger",
      "/swagger-ui.html",
      "/swagger-ui/**",
  };

  @Autowired
  private AADAppRoleStatelessAuthenticationFilter aadAppRoleStatelessAuthenticationFilter;

@Override
  protected void configure(HttpSecurity security) throws Exception{
  security.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
      .and()
      .authorizeRequests()
      .antMatchers("/v1/workflow/*")
      .permitAll()
      .anyRequest().authenticated()
      .antMatchers(AUTH_ALLOWLIST).permitAll()
      .and()
      .addFilterBefore(aadAppRoleStatelessAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
  }
}
