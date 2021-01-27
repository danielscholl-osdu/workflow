package org.opengroup.osdu.workflow.swagger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerDocumentationConfig {
  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String DEFAULT_INCLUDE_PATTERN = "/.*";

  @Bean
  public Docket api() {
    ParameterBuilder builder = new ParameterBuilder();
    List<Parameter> parameters = new ArrayList<>();
    builder.name(DpsHeaders.DATA_PARTITION_ID)
      .description("Which tenant to use")
      .defaultValue("opendes")
      .modelRef(new ModelRef("string"))
      .parameterType("header")
      .required(true)
      .build();
    parameters.add(builder.build());
    builder.name("Content-Type")
      .description("The file type/format of the request body")
      .defaultValue("application/json")
      .modelRef(new ModelRef("string"))
      .parameterType("header")
      .required(true)
      .build();
    parameters.add(builder.build());
    return new Docket(DocumentationType.SWAGGER_2)
      .globalOperationParameters(parameters)
      .select()
      .apis(RequestHandlerSelectors.basePackage("org.opengroup.osdu.workflow.aws.api"))
      .build()
      .securityContexts(Collections.singletonList(securityContext()))
      .securitySchemes(Collections.singletonList(apiKey()));
  }

  private ApiKey apiKey() {
    return new ApiKey("JWT", AUTHORIZATION_HEADER, "header");
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder()
      .securityReferences(defaultAuth())
      .forPaths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN))
      .build();
  }

  List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope
      = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes =
      new AuthorizationScope[]{authorizationScope};
    return Collections.singletonList(
      new SecurityReference("JWT", authorizationScopes));
  }
}
