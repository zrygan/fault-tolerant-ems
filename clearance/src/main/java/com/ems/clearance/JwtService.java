package com.ems.clearance;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    public record Caller(Long id, String role) {}

    private final byte[] secret;

    public JwtService(@Value("${jwt_secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }
    
    public Caller verify(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(secret))) return null;

            var claims = jwt.getJWTClaimsSet();
            var exp = claims.getExpirationTime();
            if (exp == null || exp.before(new Date())) return null;

            return new Caller(Long.valueOf(claims.getSubject()), claims.getStringClaim("role"));
        } catch (Exception e) {
            return null;
        }
    }
}