package com.cashflow.common.auth;

import com.cashflow.common.exception.InvalidTokenException;
import com.cashflow.common.exception.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Date;

@Component
public class JwtTokenValidator {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidator.class);

    private final PublicKeyProvider publicKeyProvider;
    private final String expectedIssuer;

    public JwtTokenValidator(
            PublicKeyProvider publicKeyProvider,
            @Value("${jwt.issuer:cashflow-auth-service}") String expectedIssuer) {
        this.publicKeyProvider = publicKeyProvider;
        this.expectedIssuer = expectedIssuer;
    }

    public Claims validateAndExtractClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("JWT token is missing or empty");
        }

        try {
            PublicKey publicKey = publicKeyProvider.getPublicKey();
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            validateExpiration(claims);
            validateIssuer(claims);

            return claims;
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT token has expired: {}", ex.getMessage());
            throw new TokenExpiredException("JWT token has expired", ex);
        } catch (MalformedJwtException ex) {
            logger.warn("Malformed JWT token: {}", ex.getMessage());
            throw new InvalidTokenException("Malformed JWT token: " + ex.getMessage(), ex);
        } catch (SecurityException ex) {
            logger.warn("Invalid JWT signature: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid JWT signature: " + ex.getMessage(), ex);
        } catch (UnsupportedJwtException ex) {
            logger.warn("Unsupported JWT token: {}", ex.getMessage());
            throw new InvalidTokenException("Unsupported JWT token: " + ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            logger.warn("JWT claims string is empty or invalid: {}", ex.getMessage());
            throw new InvalidTokenException("JWT claims string is empty or invalid: " + ex.getMessage(), ex);
        } catch (JwtException ex) {
            logger.warn("Invalid JWT token: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid JWT token: " + ex.getMessage(), ex);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            validateAndExtractClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void validateExpiration(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.before(new Date())) {
            throw new TokenExpiredException("JWT token has expired on " + expiration);
        }
    }

    public void validateIssuer(Claims claims) {
        String issuer = claims.getIssuer();
        if (expectedIssuer != null && !expectedIssuer.isBlank() && issuer != null) {
            if (!expectedIssuer.equals(issuer)) {
                logger.warn("JWT issuer mismatch. Expected: {}, Found: {}", expectedIssuer, issuer);
                throw new InvalidTokenException("JWT issuer mismatch: " + issuer);
            }
        }
    }
}
