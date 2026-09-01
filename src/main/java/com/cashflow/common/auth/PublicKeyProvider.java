package com.cashflow.common.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class PublicKeyProvider {

    private static final Logger logger = LoggerFactory.getLogger(PublicKeyProvider.class);

    private final Resource publicKeyResource;
    private PublicKey cachedPublicKey;

    public PublicKeyProvider(@Value("${jwt.public-key:classpath:public-key.pem}") Resource publicKeyResource) {
        this.publicKeyResource = publicKeyResource;
    }

    public PublicKey getPublicKey() {
        if (cachedPublicKey != null) {
            return cachedPublicKey;
        }

        try {
            Resource resource = this.publicKeyResource;
            if (resource == null || !resource.exists()) {
                resource = new ClassPathResource("public-key.pem");
            }

            try (InputStream inputStream = resource.getInputStream()) {
                String key = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");

                byte[] decoded = Base64.getDecoder().decode(key);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
                cachedPublicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
                logger.info("RSA Public Key loaded successfully (Algorithm: {}, Format: {})",
                        cachedPublicKey.getAlgorithm(), cachedPublicKey.getFormat());
                return cachedPublicKey;
            }
        } catch (Exception ex) {
            logger.error("Failed to load RSA public key", ex);
            throw new IllegalStateException("Unable to load RSA public key", ex);
        }
    }
}
