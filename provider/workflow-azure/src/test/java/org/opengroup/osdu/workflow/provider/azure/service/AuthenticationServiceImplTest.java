package org.opengroup.osdu.workflow.provider.azure.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {
  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  
  @InjectMocks
  private AuthenticationServiceImpl authenticationService;

  @Test
  public void shouldCheckAuthentication() {
    authenticationService.checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
  }

  @Test
  public void should_throw_unauthorized_exception_when_token_isNull() {
    assertThrows(UnauthorizedException.class, () -> {
        authenticationService.checkAuthentication(null, PARTITION);
    });
  }

  @Test
  public void should_throw_unauthorized_exception_when_partition_isNull() {
    assertThrows(UnauthorizedException.class, () -> {
        authenticationService.checkAuthentication(AUTHORIZATION_TOKEN, null);
    });
  }

  @Test
  public void shouldThrowWhenNothingIsSpecified() {
    assertThrows(UnauthorizedException.class, () -> {
        authenticationService.checkAuthentication(null, null);
    });
  }
}
