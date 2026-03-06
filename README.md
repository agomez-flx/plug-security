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

### 3. Personalización de URLs (Opcional)

La librería proporciona un builder para personalizar fácilmente las URLs públicas y protegidas:

```java
@Configuration
public class CustomSecurityConfig {
    
    @Bean
    public SecurityUrlsConfig securityUrlsConfig() {
        return SecurityUrlsConfig.builder()
            // URLs públicas (no requieren autenticación)
            .addPublicUrls("/public/**", "/health", "/info")
            .addPublicUrls("/swagger-ui/**", "/v3/api-docs/**")
            
            // URLs protegidas (requieren JWT válido)
            .addProtectedUrls("/api/**", "/webhooks/**")
            .addProtectedUrls("/admin/**")
            
            .build();
    }
}
```

**Ejemplo 1: Partir desde cero (sin URLs por defecto)**

```java
@Bean
public SecurityUrlsConfig securityUrlsConfig() {
    return SecurityUrlsConfig.builder()
        .addPublicUrls("/health")
        .addProtectedUrls("/api/**")
        .build();
}
```

**Ejemplo 2: Usar URLs por defecto y agregar más**

```java
@Bean
public SecurityUrlsConfig securityUrlsConfig() {
    return SecurityUrlsConfig.withDefaults()
        // Agregar URLs adicionales
        .addPublicUrls("/public/**", "/health")
        .addProtectedUrls("/api/**")
        .build();
}
```

**Ejemplo 3: Usar URLs por defecto y limpiar selectivamente**

```java
@Bean
public SecurityUrlsConfig securityUrlsConfig() {
    return SecurityUrlsConfig.withDefaults()
        .clearProtectedUrls()  // Limpiar las protegidas por defecto
        .addProtectedUrls("/api/**")  // Agregar las propias
        .build();
}
```

**URLs por defecto de la librería:**
- **Públicas:** `/oauth/**`, `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- **Protegidas:** `/webhooks/**`

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
