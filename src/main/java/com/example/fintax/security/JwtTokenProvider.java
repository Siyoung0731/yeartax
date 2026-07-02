package com.example.fintax.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
  private final Key key = Keys.hmacShaKeyFor(
      "my-secret-key-very-long-and-secure-value-32-chars-minimum".getBytes());
  private final long validityInMilliseconds = 3600000;

  private Claims getClaims(String token) {
    return Jwts.parserBuilder()
             .setSigningKey(key)
             .build()
             .parseClaimsJws(token)
             .getBody();
  }

  public String createToken(String jobId, String role) {
    Claims claims = Jwts.claims().setSubject(jobId);
    // role 추가 260702
    claims.put("role", role);

    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token);
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  public String getJobId(String token) {
    return getClaims(token).getSubject();
  }
  public String getRole(String token) {
    return getClaims(token).get("role", String.class);
  }
}
