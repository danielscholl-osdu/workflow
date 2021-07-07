package org.opengroup.osdu.workflow.swagger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableOpenApi
public class SwaggerDocumentationConfig {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String DEFAULT_INCLUDE_PATTERN = "/.*";

    @Bean
    public Docket api() {
        RequestParameterBuilder builder = new RequestParameterBuilder();
        List<RequestParameter> parameters = new ArrayList<>();
        builder.name(DpsHeaders.DATA_PARTITION_ID)
                .description("Which tenant to use")
                .in(ParameterType.HEADER)
                .required(true)
                .build();
        parameters.add(builder.build());
        builder.name("Content-Type")
                .description("The file type/format of the request body")
                .in(ParameterType.HEADER)
                .required(false)
                .build();
        parameters.add(builder.build());
        return new Docket(DocumentationType.OAS_30)
                .globalRequestParameters(parameters)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.opengroup.osdu.workflow.api"))
                .build()
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Collections.singletonList(apiKey()));
    }

    private ApiKey apiKey() {
        return new ApiKey(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER, "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .operationSelector(o -> PathSelectors.regex(DEFAULT_INCLUDE_PATTERN).test(o.requestMappingPattern()))
                .build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes =
                new AuthorizationScope[]{authorizationScope};
        return Collections.singletonList(
                new SecurityReference(AUTHORIZATION_HEADER, authorizationScopes));
    }
}
