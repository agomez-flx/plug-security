package com.plug.security.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuración de URLs públicas y protegidas para la seguridad de la aplicación.
 * Utiliza el patrón Builder para permitir personalización flexible.
 * 
 * Ejemplo de uso:
 * <pre>
 * SecurityUrlsConfig config = SecurityUrlsConfig.builder()
 *     .addPublicUrls("/public/**", "/health")
 *     .addProtectedUrls("/api/**")
 *     .build();
 * </pre>
 */
public class SecurityUrlsConfig {
    
    private final List<String> publicUrls;
    private final List<String> protectedUrls;

    private SecurityUrlsConfig(Builder builder) {
        this.publicUrls = new ArrayList<>(builder.publicUrls);
        this.protectedUrls = new ArrayList<>(builder.protectedUrls);
    }

    public List<String> getPublicUrls() {
        return new ArrayList<>(publicUrls);
    }

    public List<String> getProtectedUrls() {
        return new ArrayList<>(protectedUrls);
    }
    
    public String[] getPublicUrlsArray() {
        return publicUrls.toArray(new String[0]);
    }
    
    public String[] getProtectedUrlsArray() {
        return protectedUrls.toArray(new String[0]);
    }

    /**
     * Crea un builder con URLs por defecto comunes para aplicaciones REST.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Crea un builder con las URLs por defecto estándar de la librería.
     */
    public static Builder withDefaults() {
        return new Builder()
                .addPublicUrls(
                        "/oauth/**",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/index.html/**"
                )
                .addProtectedUrls("/webhooks/**");
    }

    public static class Builder {
        private final List<String> publicUrls = new ArrayList<>();
        private final List<String> protectedUrls = new ArrayList<>();

        private Builder() {
        }

        /**
         * Agrega una o más URLs públicas (no requieren autenticación).
         * 
         * @param urls Patrones de URL (ej: "/public/**", "/health")
         * @return este builder para encadenamiento
         */
        public Builder addPublicUrls(String... urls) {
            this.publicUrls.addAll(Arrays.asList(urls));
            return this;
        }

        /**
         * Agrega una lista de URLs públicas.
         * 
         * @param urls Lista de patrones de URL
         * @return este builder para encadenamiento
         */
        public Builder addPublicUrls(List<String> urls) {
            this.publicUrls.addAll(urls);
            return this;
        }

        /**
         * Agrega una o más URLs protegidas (requieren autenticación).
         * 
         * @param urls Patrones de URL (ej: "/api/**", "/webhooks/**")
         * @return este builder para encadenamiento
         */
        public Builder addProtectedUrls(String... urls) {
            this.protectedUrls.addAll(Arrays.asList(urls));
            return this;
        }

        /**
         * Agrega una lista de URLs protegidas.
         * 
         * @param urls Lista de patrones de URL
         * @return este builder para encadenamiento
         */
        public Builder addProtectedUrls(List<String> urls) {
            this.protectedUrls.addAll(urls);
            return this;
        }

        /**
         * Limpia todas las URLs públicas configuradas.
         * 
         * @return este builder para encadenamiento
         */
        public Builder clearPublicUrls() {
            this.publicUrls.clear();
            return this;
        }

        /**
         * Limpia todas las URLs protegidas configuradas.
         * 
         * @return este builder para encadenamiento
         */
        public Builder clearProtectedUrls() {
            this.protectedUrls.clear();
            return this;
        }

        /**
         * Construye la configuración de URLs.
         * 
         * @return configuración inmutable de URLs
         */
        public SecurityUrlsConfig build() {
            return new SecurityUrlsConfig(this);
        }
    }
}
