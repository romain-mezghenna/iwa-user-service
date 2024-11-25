package com.iwa.userservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtTokenUtil {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private final String jwtIssuer = "com.iwa.userservice";

    public String generateAccessToken(Long userId) {
        SecretKey jwtkey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 heures
                .signWith(jwtkey,SignatureAlgorithm.HS512)
                .compact();
    }

    public Long getUserId(String token) {
        SecretKey jwtkey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtkey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validate(String token) {
        SecretKey jwtkey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        try {
            Jwts.parserBuilder().setSigningKey(jwtkey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}