package com.plug.security.config;

import com.plug.security.handler.SecurityExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración del Resource Server para Spring Boot 2.
 * Define las reglas de seguridad para endpoints protegidos y públicos.
 * 
 * Para personalizar las URLs, crea tu propio bean SecurityUrlsConfig:
 * <pre>
 * {@code
 * @Bean
 * public SecurityUrlsConfig securityUrlsConfig() {
 *     return SecurityUrlsConfig.builder()
 *         .addPublicUrls("/public/**", "/health")
 *         .addProtectedUrls("/api/**")
 *         .build();
 * }
 * }
 * </pre>
 */
@Configuration
public class ResourceServerConfig {

    /**
     * Bean de configuración de URLs por defecto.
     * Puede ser sobrescrito definiendo tu propio bean SecurityUrlsConfig.
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityUrlsConfig securityUrlsConfig() {
        return SecurityUrlsConfig.withDefaults().build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                   JwtDecoder jwtDecoder, 
                                                   SecurityExceptionHandler securityExceptionHandler,
                                                   SecurityUrlsConfig securityUrlsConfig) throws Exception {
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
                    .requestMatchers(securityUrlsConfig.getPublicUrlsArray()).permitAll()
                    .requestMatchers(securityUrlsConfig.getProtectedUrlsArray()).authenticated()
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
