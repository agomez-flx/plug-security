# Ejemplo de uso de Plug Security

Este es un ejemplo básico de cómo integrar la librería Plug Security en una aplicación Spring Boot.

## Estructura del proyecto

```
mi-aplicacion/
├── pom.xml
├── src/main/
│   ├── java/com/miapp/
│   │   ├── Application.java
│   │   ├── controller/
│   │   │   └── WebhookController.java
│   │   └── config/
│   │       └── SecurityConfig.java (opcional)
│   └── resources/
│       └── application.yml
```

## 1. Dependencias (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.miapp</groupId>
    <artifactId>mi-aplicacion</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Plug Security para Spring Boot 3 -->
        <dependency>
            <groupId>com.plug</groupId>
            <artifactId>plug-security-spring-boot-3</artifactId>
            <version>1.0.0</version>
        </dependency>
        
        <!-- Lombok (opcional) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

## 2. Configuración (application.yml)

```yaml
server:
  port: 8080

TOKEN_SIGNER_KEY: ${TOKEN_SIGNER_KEY:mi-clave-secreta-super-segura-de-al-menos-256-bits-para-firmar-tokens-jwt}

logging:
  level:
    com.plug.security: DEBUG
    org.springframework.security: DEBUG
```

## 3. Clase principal (Application.java)

```java
package com.miapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 4. Controlador con validación de permisos (WebhookController.java)

```java
package com.miapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    
    /**
     * Endpoint que requiere el privilege "ADMIN" O el scope "webhooks.read"
     */
    @PreAuthorize("hasPermission('', 'ADMIN', 'webhooks.read')")
    @GetMapping
    public ResponseEntity<?> listWebhooks(Authentication authentication) {
        log.info("Listando webhooks - Usuario: {}", authentication.getName());
        
        return ResponseEntity.ok(Map.of(
            "webhooks", List.of(
                Map.of("id", 1, "url", "https://example.com/hook1"),
                Map.of("id", 2, "url", "https://example.com/hook2")
            ),
            "user", authentication.getName()
        ));
    }
    
    /**
     * Endpoint que requiere el privilege "ADMIN" O el scope "webhooks.write"
     */
    @PreAuthorize("hasPermission('', 'ADMIN', 'webhooks.write')")
    @PostMapping
    public ResponseEntity<?> createWebhook(
            @RequestBody Map<String, String> webhook,
            Authentication authentication) {
        
        log.info("Creando webhook - Usuario: {} - URL: {}", 
                 authentication.getName(), webhook.get("url"));
        
        return ResponseEntity.ok(Map.of(
            "id", 3,
            "url", webhook.get("url"),
            "createdBy", authentication.getName()
        ));
    }
    
    /**
     * Endpoint que solo requiere autenticación (cualquier token válido)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getWebhook(@PathVariable Long id, Authentication authentication) {
        log.info("Obteniendo webhook {} - Usuario: {}", id, authentication.getName());
        
        return ResponseEntity.ok(Map.of(
            "id", id,
            "url", "https://example.com/hook" + id,
            "user", authentication.getName()
        ));
    }
    
    /**
     * Endpoint que requiere específicamente el authority "ADMIN"
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWebhook(@PathVariable Long id, Authentication authentication) {
        log.info("Eliminando webhook {} - Usuario: {}", id, authentication.getName());
        
        return ResponseEntity.ok(Map.of(
            "message", "Webhook eliminado correctamente",
            "id", id
        ));
    }
}
```

## 5. Configuración personalizada (opcional - SecurityConfig.java)

Si necesitas personalizar los endpoints protegidos, puedes crear tu propia configuración:

```java
package com.miapp.config;

import com.plug.security.handler.SecurityExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                   JwtDecoder jwtDecoder,
                                                   SecurityExceptionHandler handler) throws Exception {
        http
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(handler)
                .accessDeniedHandler(handler)
            )
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers(
                    "/public/**",
                    "/health",
                    "/actuator/**"
                ).permitAll()
                
                // Endpoints protegidos
                .requestMatchers("/webhooks/**").authenticated()
                .requestMatchers("/api/**").authenticated()
                
                // Todo lo demás denegado
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.decoder(jwtDecoder))
                .authenticationEntryPoint(handler)
            );
        
        return http.build();
    }
}
```

## 6. Ejecutar la aplicación

```bash
# Exportar la variable de entorno
export TOKEN_SIGNER_KEY="mi-clave-secreta-super-segura-de-al-menos-256-bits"

# Ejecutar la aplicación
mvn spring-boot:run
```

## 7. Probar con curl

### Generar un token JWT de prueba

Puedes usar https://jwt.io para generar un token con la siguiente estructura:

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "usuario@example.com",
  "scope": "webhooks.read webhooks.write",
  "authorities": ["ROLE_USER"],
  "iat": 1678886400,
  "exp": 1999999999
}
```

**Secret:** usa la misma clave que configuraste en `TOKEN_SIGNER_KEY`

### Probar endpoints

```bash
# Sin token (debe fallar con 401)
curl -X GET http://localhost:8080/webhooks

# Con token válido
export JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Listar webhooks
curl -X GET http://localhost:8080/webhooks \
  -H "Authorization: Bearer $JWT_TOKEN"

# Crear webhook
curl -X POST http://localhost:8080/webhooks \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/new-hook"}'

# Obtener webhook por ID
curl -X GET http://localhost:8080/webhooks/1 \
  -H "Authorization: Bearer $JWT_TOKEN"

# Eliminar webhook (requiere ADMIN authority)
curl -X DELETE http://localhost:8080/webhooks/1 \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Respuestas esperadas

### Éxito (200 OK)
```json
{
  "webhooks": [
    {"id": 1, "url": "https://example.com/hook1"},
    {"id": 2, "url": "https://example.com/hook2"}
  ],
  "user": "usuario@example.com"
}
```

### Error 401 (sin token)
```json
{
  "error": "UNAUTHORIZED",
  "message": "Bearer token is missing or invalid"
}
```

### Error 403 (sin permisos)
```json
{
  "error": "FORBIDDEN",
  "message": "The provided token does not have permission for this webhook"
}
```

## Notas adicionales

1. **Token Signer Key**: En producción, usa una clave segura de al menos 256 bits y guárdala en variables de entorno o gestores de secretos (AWS Secrets Manager, HashiCorp Vault, etc.).

2. **Logs**: Configura el nivel de log a `INFO` o `WARN` en producción para evitar exponer información sensible.

3. **CORS**: Si tu aplicación será consumida por un frontend en otro dominio, configura CORS adecuadamente.

4. **HTTPS**: En producción, siempre usa HTTPS para proteger los tokens JWT en tránsito.
