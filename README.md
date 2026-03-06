# Plug Security Library

Librería Java para validación de tokens JWT con soporte para Spring Boot 2 y Spring Boot 3.

## Características

- ✅ Validación de tokens JWT con firma HS256
- ✅ Evaluación de permisos basada en claims y scopes
- ✅ Configuración de seguridad pre-configurada
- ✅ Manejo de excepciones con respuestas JSON personalizadas
- ✅ Compatible con Spring Boot 2.x y Spring Boot 3.x
- ✅ Auto-configuración con Spring Boot

## Requisitos

- Java 21
- Maven 3.6+
- Spring Boot 2.x o 3.x

## Instalación

### Para Spring Boot 3.x

Agregar la dependencia en tu `pom.xml`:

```xml
<dependency>
    <groupId>com.plug</groupId>
    <artifactId>plug-security-spring-boot-3</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Para Spring Boot 2.x

Agregar la dependencia en tu `pom.xml`:

```xml
<dependency>
    <groupId>com.plug</groupId>
    <artifactId>plug-security-spring-boot-2</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configuración

### 1. Variable de entorno

Configurar la clave de firma del token JWT:

```properties
TOKEN_SIGNER_KEY=tu-clave-secreta-super-segura-de-al-menos-256-bits
```

### 2. Endpoints protegidos

La librería configura automáticamente los siguientes endpoints:

**Públicos (no requieren autenticación):**
- `/oauth/**`
- `/actuator/**`
- `/swagger-ui/**`
- `/v3/api-docs/**`

**Protegidos (requieren JWT válido):**
- `/webhooks/**`

**Denegados:**
- Cualquier otro endpoint no configurado

### 3. Personalización (Opcional)

Si necesitas personalizar los endpoints protegidos, puedes sobrescribir el bean `SecurityFilterChain`:

```java
@Configuration
public class CustomSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                   JwtDecoder jwtDecoder,
                                                   SecurityExceptionHandler handler) throws Exception {
        http
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.decoder(jwtDecoder))
            );
        
        return http.build();
    }
}
```

## Uso

### Validación de permisos con @PreAuthorize

La librería proporciona un `CustomPermissionEvaluator` que permite validar tanto authorities como scopes del JWT:

```java
@RestController
@RequestMapping("/api")
public class MyController {
    
    // Validar privilege o scope
    @PreAuthorize("hasPermission('', 'ADMIN', 'webhooks.write')")
    @PostMapping("/webhooks")
    public ResponseEntity<?> createWebhook() {
        // Lógica del endpoint
        return ResponseEntity.ok().build();
    }
    
    // Solo validar authority
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/webhooks/{id}")
    public ResponseEntity<?> deleteWebhook(@PathVariable String id) {
        // Lógica del endpoint
        return ResponseEntity.ok().build();
    }
}
```

### Estructura del JWT

La librería espera que el JWT contenga los siguientes claims:

```json
{
  "sub": "usuario@example.com",
  "scope": "webhooks.read webhooks.write",
  "authorities": ["ROLE_USER", "ADMIN"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

Los scopes pueden ser:
- Un string separado por espacios: `"scope": "read write"`
- Una colección: `"scope": ["read", "write"]`

## Compilación

Para compilar la librería:

```bash
mvn clean install
```

Esto generará tres artefactos:
- `plug-security-common-1.0.0.jar` - Módulo común
- `plug-security-spring-boot-2-1.0.0.jar` - Para Spring Boot 2
- `plug-security-spring-boot-3-1.0.0.jar` - Para Spring Boot 3

## Estructura del proyecto

```
plug-security/
├── pom.xml                           # POM padre
├── plug-security-common/             # Código compartido
│   ├── pom.xml
│   └── src/main/java/com/plug/security/
│       ├── model/ApiError.java
│       ├── evaluator/CustomPermissionEvaluator.java
│       └── config/
│           ├── MethodSecurityConfig.java
│           └── JwtDecoderConfig.java
├── plug-security-spring-boot-2/      # Spring Boot 2 (javax.servlet)
│   ├── pom.xml
│   └── src/main/java/com/plug/security/
│       ├── handler/SecurityExceptionHandler.java
│       └── config/ResourceServerConfig.java
└── plug-security-spring-boot-3/      # Spring Boot 3 (jakarta.servlet)
    ├── pom.xml
    └── src/main/java/com/plug/security/
        ├── handler/SecurityExceptionHandler.java
        └── config/ResourceServerConfig.java
```

## Respuestas de error

La librería proporciona respuestas JSON estructuradas para errores de autenticación y autorización:

**401 Unauthorized:**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Bearer token is missing or invalid"
}
```

**403 Forbidden:**
```json
{
  "error": "FORBIDDEN",
  "message": "The provided token does not have permission for this webhook"
}
```

## Licencia

[Especificar licencia]

## Contribución

[Instrucciones para contribuir al proyecto]
