package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private CustomUserDetailsService customUserDetailsService;

  @Test
  void loadUserByUsername_returnsTheUser_whenEmailExists() {
    User user = new User();
    user.setEmail("admin@prog4.local");
    user.setRole(Role.ADMIN);
    when(userRepository.findByEmail("admin@prog4.local")).thenReturn(Optional.of(user));

    var loaded = customUserDetailsService.loadUserByUsername("admin@prog4.local");

    assertEquals("admin@prog4.local", loaded.getUsername());
  }

  @Test
  void loadUserByUsername_throws_whenEmailDoesNotExist() {
    when(userRepository.findByEmail("unknown@prog4.local")).thenReturn(Optional.empty());

    org.junit.jupiter.api.Assertions.assertThrows(
        UsernameNotFoundException.class,
        () -> customUserDetailsService.loadUserByUsername("unknown@prog4.local"));
  }
}
