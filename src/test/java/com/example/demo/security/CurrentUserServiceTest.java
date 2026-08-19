package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.exception.DomainException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentUserServiceTest {

  private final CurrentUserService currentUserService = new CurrentUserService();

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void currentUser_returnsTheUser_whenPrincipalIsAUserInstance() {
    User user = new User();
    user.setRole(Role.ADMIN);
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    assertTrue(currentUserService.currentUser().isPresent());
    assertEquals(user, currentUserService.currentUser().get());
  }

  @Test
  void currentUser_returnsEmpty_whenNoAuthenticationIsSet() {
    assertFalse(currentUserService.currentUser().isPresent());
  }

  @Test
  void currentUser_returnsEmpty_whenPrincipalIsNotAUserInstance() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    assertFalse(currentUserService.currentUser().isPresent());
  }

  @Test
  void requireUser_returnsTheUser_whenAuthenticated() {
    User user = new User();
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    assertEquals(user, currentUserService.requireUser());
  }

  @Test
  void requireUser_throwsForbidden_whenNotAuthenticated() {
    assertThrows(DomainException.class, currentUserService::requireUser);
  }
}
