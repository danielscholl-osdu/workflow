package org.opengroup.osdu.workflow.swagger;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "${api.title}",
        description = "${api.description}",
        version = "${api.version}",
        contact = @Contact(name = "${api.contact.name}", email = "${api.contact.email}"),
        license = @License(name = "${api.license.name}", url = "${api.license.url}")),
    security = @SecurityRequirement(name = "Authorization"),
    tags = {
        @Tag(name = "workflow-run-api", description = "WorkflowRun related endpoints"),
        @Tag(name = "workflow-manager-api", description = "WorkflowManager related endpoints"),
        @Tag(name = "workflow-system-manager-api", description = "Workflow System Manager related endpoints"),
        @Tag(name = "health", description = "Health related endpoints"),
        @Tag(name = "info", description = "Version info endpoint")
    }
)
@SecurityScheme(name = "Authorization", scheme = "bearer", bearerFormat = "Authorization", type = SecuritySchemeType.HTTP)
@Configuration
public class SwaggerConfiguration {
  @Bean
  public OperationCustomizer customize() {
    return (operation, handlerMethod) -> {
      Parameter dataPartitionId = new Parameter()
          .name(DpsHeaders.DATA_PARTITION_ID)
          .description("Tenant Id")
          .in("header")
          .required(true)
          .schema(new StringSchema());
      return operation.addParametersItem(dataPartitionId);
    };
  }

}
