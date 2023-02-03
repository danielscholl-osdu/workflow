package org.opengroup.osdu.workflow.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import java.util.Collections;

@Configuration
public class SwaggerDocumentationConfig {
  @Bean
  public OpenAPI openApi(ServletContext servletContext) {
    Server server = new Server().url(servletContext.getContextPath());
    return new OpenAPI()
        .servers(Collections.singletonList(server))
        .info(new Info()
                  .title("Workflow Service")
                  .version("1.0"))
        .components(new Components()
                        .addSecuritySchemes("Authorization",
                                            new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("Authorization")
                                                .in(SecurityScheme.In.HEADER)
                                                .name("Authorization")))
        .addSecurityItem(
            new SecurityRequirement()
                .addList("Authorization"));
  }
}
