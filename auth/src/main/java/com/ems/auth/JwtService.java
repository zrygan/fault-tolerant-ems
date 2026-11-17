package com.ems.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class JwtService {
    private final byte[] secret;

    public JwtService(@Value("${jwt_secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String create(UserAccount u) {
        try {
            var now = new Date();
            var claims = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(u.getId()))
                    .claim("role", u.getRole())
                    .issueTime(now)
                    .expirationTime(new Date(now.getTime() + 60 * 60 * 1000)) // 1 hour
                    .build();
            var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    public Long verify(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(secret))) return null;
            var claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) return null;
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }
}
