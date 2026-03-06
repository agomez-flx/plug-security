package com.plug.security.config;

import com.plug.security.handler.SecurityExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Configuración del Resource Server para Spring Boot 2.
 * Define las reglas de seguridad para endpoints protegidos y públicos.
 */
@Configuration
public class ResourceServerConfig {

    private static final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(
            new AntPathRequestMatcher("/oauth/**"),
            new AntPathRequestMatcher("/actuator/**"),
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/v3/api-docs/**"),
            new AntPathRequestMatcher("/swagger-ui/index.html/**"));

    private static final RequestMatcher PROTECTED_URLS = new OrRequestMatcher(
            new AntPathRequestMatcher("/webhooks/**"));

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder, 
                                                   SecurityExceptionHandler securityExceptionHandler) throws Exception {
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable()
                .cors()
                .and()
                .exceptionHandling()
                    .authenticationEntryPoint(securityExceptionHandler)
                    .accessDeniedHandler(securityExceptionHandler)
                .and()
                .authorizeRequests()
                    .requestMatchers(PUBLIC_URLS).permitAll()
                    .requestMatchers(PROTECTED_URLS).authenticated()
                    .anyRequest().denyAll()
                .and()
                .oauth2ResourceServer()
                    .jwt()
                        .decoder(jwtDecoder)
                    .and()
                    .authenticationEntryPoint(securityExceptionHandler);

        return http.build();
    }
}
