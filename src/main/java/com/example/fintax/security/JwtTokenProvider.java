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
  //실제 서비스에서는 환경변수로 관리해야 할 비밀키
  private final Key key = Keys.hmacShaKeyFor("my-secret-key-very-long-and-secure-value-32-chars-minimum".getBytes());
  private final long validityInMilliseconds = 3600000;    // 1시간

  // 1. Token 생성
  public String createToken(String jobId) {
    Claims claims = Jwts.claims().setSubject(jobId);
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  // 2. Token 검증
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token);   //5. 수정
      return true;
    } catch(Exception e) {
      return false;
    }
  }
  // 3. Token 에서 데이터 추출
  public String getJobId(String token) {
    return Jwts.parserBuilder()
               .setSigningKey(key)
               .build()
               .parseClaimsJws(token)
               .getBody()
               .getSubject();
  }
}
