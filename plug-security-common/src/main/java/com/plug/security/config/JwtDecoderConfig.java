package com.plug.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configuración del decodificador JWT.
 * Utiliza una clave secreta para validar tokens firmados con HS256.
 */
@Configuration
public class JwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${TOKEN_SIGNER_KEY}") String signerKey) {
        if (signerKey == null || signerKey.isBlank()) {
            throw new IllegalStateException("TOKEN_SIGNER_KEY vacío (TOKEN_SIGNER_KEY)");
        }

        JwtDecoder primary = hs256Decoder(signerKey);    

        return token -> primary.decode(token);
    }

    private JwtDecoder hs256Decoder(String secret) {
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
