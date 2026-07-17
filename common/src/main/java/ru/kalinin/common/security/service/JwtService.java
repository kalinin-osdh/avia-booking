package ru.kalinin.common.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import ru.kalinin.common.security.dto.JwtUserPrincipal;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret:8c2a7b4f9e1d6a3c5f8b0d2e4a6c8e0f2a4b6d8f0e2c4a6b8d0f2e4a6c8e0f}")
    private String jwtSecret;

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public JwtUserPrincipal extractUserPrincipal(String token){
        return new JwtUserPrincipal(extractUserId(token), extractUsername(token));
    }

    private Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public boolean isTokenValid(String token) {
        Claims claims = extractAllClaims(token);
        return !isTokenExpired(claims);
    }

    private boolean isTokenExpired(Claims claims) {
        Date date = claims.getExpiration();
        return date.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] jwtTokenBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(jwtTokenBytes);
    }


}
