package org.opengroup.osdu.workflow.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import springfox.documentation.oas.web.OpenApiTransformationContext;
import springfox.documentation.spi.DocumentationType;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SpringfoxSwaggerHostResolverTest {
  private static final String TEST_NON_HTTPS_URL = "dummy-url";
  private static final String TEST_HTTPS_URL = "dummy-url:443";
  @InjectMocks SpringfoxSwaggerHostResolver springfoxSwaggerHostResolver;

  @Test
  void testOAS30DelimiterProvided() {
    DocumentationType delimiter = DocumentationType.OAS_30;
    assertEquals(true, springfoxSwaggerHostResolver.supports(delimiter));
  }

  @Test
  void testNonOAS30DelimiterProvided() {
    DocumentationType delimiter1 = DocumentationType.SWAGGER_2;
    DocumentationType delimiter2 = DocumentationType.SWAGGER_12;
    assertEquals(false, springfoxSwaggerHostResolver.supports(delimiter1));
    assertEquals(false, springfoxSwaggerHostResolver.supports(delimiter2));
  }

  @Test
  void testTransformWhenNonHTTPSUrl() {
    OpenApiTransformationContext<HttpServletRequest> contextMock = mock(OpenApiTransformationContext.class);
    OpenAPI swaggerMock = mock(OpenAPI.class);
    Server server = new Server();
    server.setUrl(TEST_NON_HTTPS_URL);
    List<Server> servers = Arrays.asList(server);
    when(contextMock.getSpecification()).thenReturn(swaggerMock);
    when(swaggerMock.getServers()).thenReturn(servers);

    OpenAPI swaggerObtained = springfoxSwaggerHostResolver.transform(contextMock);

    verify(swaggerMock, times(1)).getServers();
    assertEquals(swaggerMock, swaggerObtained);
    assertEquals(server.getUrl(), TEST_NON_HTTPS_URL);
  }

  @Test
  void testTransformWhenHTTPSUrl() {
    OpenApiTransformationContext<HttpServletRequest> contextMock = mock(OpenApiTransformationContext.class);
    OpenAPI swaggerMock = mock(OpenAPI.class);
    Server server = new Server();
    server.setUrl(TEST_HTTPS_URL);
    List<Server> servers = Arrays.asList(server);
    when(contextMock.getSpecification()).thenReturn(swaggerMock);
    when(swaggerMock.getServers()).thenReturn(servers);

    OpenAPI swaggerObtained = springfoxSwaggerHostResolver.transform(contextMock);

    verify(swaggerMock, times(1)).getServers();
    assertEquals(swaggerMock, swaggerObtained);
    assertEquals(server.getUrl(), TEST_NON_HTTPS_URL);
  }
}
