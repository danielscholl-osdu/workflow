/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/


package org.opengroup.osdu.workflow.provider.ibm.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            	.antMatchers("/v1/api-docs",
		            "/configuration/ui",
		            "/swagger-resources/**",
		            "/configuration/security",
		            "/swagger-ui.html",
		            "/swagger",
		            "/webjars/**",
		            "/liveness_check",
		            "/readiness_check").permitAll()
            	.anyRequest()
            		.authenticated()
            		.and()
            	.oauth2ResourceServer().jwt();
    }
}

