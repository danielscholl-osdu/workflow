package org.opengroup.osdu.workflow.swagger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class HomeControllerTest {

  private static final String SWAGGER_UI = "redirect:swagger-ui/index.html";

  @InjectMocks
  private HomeController homeController;

  @Test
  void testSwaggerUI() {
    assertEquals(SWAGGER_UI, homeController.swagger());
  }

  @Test
  void testSwaggerHTMLUI() {
    assertEquals(SWAGGER_UI, homeController.swaggerhtml());
  }
}
