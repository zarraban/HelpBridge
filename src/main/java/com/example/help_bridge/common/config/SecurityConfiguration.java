package com.example.help_bridge.common.config;

import com.example.help_bridge.common.security.filter.JwtAuthenticationFilter;
import com.example.help_bridge.common.security.filter.JwtLoginFilter;
import com.example.help_bridge.common.security.service.JwtService;
import com.example.help_bridge.common.security.service.JwtValidatorService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import com.example.help_bridge.common.web.WebLoginFailureHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsChecker accountStatusChecker() {
        return new AccountStatusUserDetailsChecker();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtValidatorService jwtValidatorService,
                                                   JwtService jwtService,
                                                   UserDetailsService userDetailsService,
                                                   AuthenticationManager authenticationManager,
                                                   ObjectMapper objectMapper,
                                                   AuthenticationEntryPoint authenticationEntryPoint,
                                                   AccessDeniedHandler accessDeniedHandler,
                                                   UserDetailsChecker accountStatusChecker) throws Exception {
        JwtLoginFilter jwtLoginFilter = new JwtLoginFilter(authenticationManager, jwtService, objectMapper, authenticationEntryPoint);
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtValidatorService, userDetailsService, authenticationEntryPoint, accountStatusChecker);
        return http
                .securityMatcher("/api/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http,
                                              AuthenticationManager authenticationManager) throws Exception {
        return http
                .authenticationManager(authenticationManager)
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/js/**", "/error", "/error/**", "/actuator/health").permitAll()
                        .requestMatchers("/system-admins", "/system-admins/**", "/fund-representatives",
                                "/fund-representatives/**", "/donors", "/donors/**", "/funds/new",
                                "/funds/*/status", "/funds/*/delete").hasRole("SYSTEM_ADMIN")
                        .requestMatchers("/funds", "/funds/**").hasAnyRole("SYSTEM_ADMIN", "FUND_REPRESENTATIVE")
                        .requestMatchers("/fundraisers/*/mail").hasRole("FUND_REPRESENTATIVE")
                        .requestMatchers("/my/statistics").hasRole("VOLUNTEER")
                        .requestMatchers("/requests/new").hasRole("APPLICANT")
                        .requestMatchers(HttpMethod.POST, "/requests").hasRole("APPLICANT")
                        .requestMatchers(HttpMethod.POST, "/requests/*/review").hasRole("SYSTEM_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/requests")
                        .hasAnyRole("APPLICANT", "FUND_REPRESENTATIVE", "SYSTEM_ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureHandler(new WebLoginFailureHandler()))
                .sessionManagement(session -> session.invalidSessionUrl("/login?expired"))
                .logout(logout -> logout
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login?logout"))
                .build();
    }
}
