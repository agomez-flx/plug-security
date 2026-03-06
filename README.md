# Plug Security Library

Java library for JWT token validation with support for Spring Boot 2 and Spring Boot 3.

## Features

- ✅ JWT token validation with HS256 signature
- ✅ Permission evaluation based on claims and scopes
- ✅ Pre-configured security settings
- ✅ Custom JSON error responses
- ✅ Compatible with Spring Boot 2.x and Spring Boot 3.x
- ✅ Spring Boot auto-configuration

## Requirements

- Java 21
- Maven 3.6+
- Spring Boot 2.x or 3.x

## Installation

### For Spring Boot 3.x

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.plug</groupId>
    <artifactId>plug-security-spring-boot-3</artifactId>
    <version>1.0.0</version>
</dependency>
```

### For Spring Boot 2.x

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.plug</groupId>
    <artifactId>plug-security-spring-boot-2</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configuration

### 1. Environment Variable

Configure the JWT token signing key:

```properties
TOKEN_SIGNER_KEY=your-super-secure-secret-key-at-least-256-bits
```

### 2. Protected Endpoints

The library automatically configures the following endpoints:

**Public (no authentication required):**
- `/oauth/**`
- `/actuator/**`
- `/swagger-ui/**`
- `/v3/api-docs/**`

**Protected (require valid JWT):**
- `/webhooks/**`

**Denied:**
- Any other endpoint not configured

### 3. URL Customization (Optional)

The library provides a builder to easily customize public and protected URLs:

```java
@Configuration
public class CustomSecurityConfig {
    
    @Bean
    public SecurityUrlsConfig securityUrlsConfig() {
        return SecurityUrlsConfig.builder()
            // Public URLs (no authentication required)
            .addPublicUrls("/public/**", "/health", "/info")
            .addPublicUrls("/swagger-ui/**", "/v3/api-docs/**")
            
            // Protected URLs (require valid JWT)
            .addProtectedUrls("/api/**", "/webhooks/**")
            .addProtectedUrls("/admin/**")
            
            .build();
    }
}
```

**Example 1: Start from scratch (no default URLs)**

```java
@Bean
public SecurityUrlsConfig securityUrlsConfig() {
    return SecurityUrlsConfig.builder()
        .addPublicUrls("/health")
        .addProtectedUrls("/api/**")
        .build();
}
```

**Example 2: Use default URLs and add more**

```java
@Bean
public SecurityUrlsConfig securityUrlsConfig() {
    return SecurityUrlsConfig.withDefaults()
        // Add additional URLs
        .addPublicUrls("/public/**", "/health")
        .addProtectedUrls("/api/**")
        .build();
}
```

**Example 3: Use default URLs and selectively clear**

```java
@Bean
public SecurityUrlsConfig securityUrlsConfig() {
    return SecurityUrlsConfig.withDefaults()
        .clearProtectedUrls()  // Clear default protected URLs
        .addProtectedUrls("/api/**")  // Add your own
        .build();
}
```

**Library default URLs:**
- **Public:** `/oauth/**`, `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- **Protected:** `/webhooks/**`

## Usage

### Permission Validation with @PreAuthorize

The library provides a `CustomPermissionEvaluator` that allows validating both authorities and JWT scopes:

```java
@RestController
@RequestMapping("/api")
public class MyController {
    
    // Validate privilege OR scope
    @PreAuthorize("hasPermission('', 'ADMIN', 'webhooks.write')")
    @PostMapping("/webhooks")
    public ResponseEntity<?> createWebhook() {
        // Endpoint logic
        return ResponseEntity.ok().build();
    }
    
    // Only validate authority
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/webhooks/{id}")
    public ResponseEntity<?> deleteWebhook(@PathVariable String id) {
        // Endpoint logic
        return ResponseEntity.ok().build();
    }
}
```

### JWT Structure

The library expects the JWT to contain the following claims:

```json
{
  "sub": "user@example.com",
  "scope": "webhooks.read webhooks.write",
  "authorities": ["ROLE_USER", "ADMIN"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

Scopes can be:
- A space-separated string: `"scope": "read write"`
- A collection: `"scope": ["read", "write"]`

## Build

To build the library:

```bash
mvn clean install
```

This will generate three artifacts:
- `plug-security-common-1.0.0.jar` - Common module
- `plug-security-spring-boot-2-1.0.0.jar` - For Spring Boot 2
- `plug-security-spring-boot-3-1.0.0.jar` - For Spring Boot 3

## Project Structure

```
plug-security/
├── pom.xml                           # Parent POM
├── plug-security-common/             # Shared code
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

## Error Responses

The library provides structured JSON responses for authentication and authorization errors:

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

## License

[Specify license]

## Contributing

[Instructions for contributing to the project]
