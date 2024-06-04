package org.opengroup.osdu.workflow.provider.azure.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.exception.UnauthorizedException;
import org.opengroup.osdu.workflow.provider.azure.WorkflowAzureApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = {WorkflowAzureApplication.class})
public class AuthenticationServiceImplTest {
  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  @Mock
  private AuthenticationServiceImpl authenticationService;

  @Test
  public void shouldCheckAuthentication() {
    authenticationService.checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
  }

  @Test(expected = UnauthorizedException.class)
  public void should_throw_unauthorized_exception_when_token_isNull() {

    doThrow(UnauthorizedException.class).when(authenticationService).checkAuthentication(null, PARTITION);
    authenticationService.checkAuthentication(null, PARTITION);
  }

  @Test(expected = UnauthorizedException.class)
  public void should_throw_unauthorized_exception_when_partition_isNull() {

    doThrow(UnauthorizedException.class).when(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, null);
    authenticationService.checkAuthentication(AUTHORIZATION_TOKEN, null);
  }

  @Test(expected = UnauthorizedException.class)
  public void shouldThrowWhenNothingIsSpecified() {
    doThrow(UnauthorizedException.class).when(authenticationService).checkAuthentication(null, null);
    authenticationService.checkAuthentication(null, null);

  }
}
