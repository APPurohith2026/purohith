package com.purohitcommunity.portal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Date;

@Service
public class JwtService {
  private final SecretKey key;
  private final long expirationMinutes;
  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-minutes}") long expirationMinutes){
    this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expirationMinutes=expirationMinutes;
  }
  public String generate(String email, String role){
    Instant now=Instant.now();
    return Jwts.builder().subject(email).claim("role",role).issuedAt(Date.from(now)).expiration(Date.from(now.plus(Duration.ofMinutes(expirationMinutes)))).signWith(key).compact();
  }
  public String subject(String token){ return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject(); }
}
