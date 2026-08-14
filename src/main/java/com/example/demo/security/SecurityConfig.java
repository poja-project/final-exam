package com.example.demo.security;

import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomUserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    RequestMatcher webMatcher = new AntPathRequestMatcher("/web/**");

    AuthenticationEntryPoint loginEntryPoint = new LoginUrlAuthenticationEntryPoint("/web/login");
    AuthenticationEntryPoint basicEntryPoint =
        new BasicAuthenticationEntryPoint() {
          {
            setRealmName("prog4");
          }
        };

    LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
    entryPoints.put(webMatcher, loginEntryPoint);
    var delegatingEntryPoint =
        new org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint(
            entryPoints);
    delegatingEntryPoint.setDefaultEntryPoint(basicEntryPoint);

    http.csrf(
            csrf ->
                csrf.ignoringRequestMatchers(
                    "/auth/login", "/grades", "/exams")) // adjust per POJA/Thymeleaf needs
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/login", "/web/login", "/web/css/**")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/cohorts/{id}/graduates/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/exams")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers("/grades")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers("/students/**", "/web/**", "/cohorts")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/web/login")
                    .loginProcessingUrl("/web/login")
                    .defaultSuccessUrl("/web/cohorts", true)
                    .permitAll())
        .httpBasic(basic -> basic.realmName("prog4")) // BasicAuthenticationFilter stays active
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                    delegatingEntryPoint)) // overrides default entry-point resolution
        .authenticationProvider(authenticationProvider());

    return http.build();
  }
}
