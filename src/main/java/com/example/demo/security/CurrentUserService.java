package com.example.demo.security;

import com.example.demo.exception.DomainException;
import com.example.demo.model.User;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

  public Optional<User> currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof User user) {
      return Optional.of(user);
    }
    return Optional.empty();
  }

  public User requireUser() {
    return currentUser().orElseThrow(() -> DomainException.forbidden("Authentication required"));
  }
}
