package com.example.fintax.security;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;

  public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
      
        // 1. 헤더에서 Token을 추출
        String token = request.getHeader("Authorization");

        // 2. 토큰이 있고 유효한지 검사
        if(token != null && token.startsWith("Bearer ")) {
          String jwt = token.substring(7);        // "Bearer  " 제거 후 순수 토큰 추출

          if(jwtTokenProvider.validateToken(jwt)) {
            String jobId = jwtTokenProvider.getJobId(jwt);

            // 3. 토큰이 정상이면 SecurityContext에 인증 정보 저장
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(jobId, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(auth);
          }
        }

        // 4. 다음 필터로 진행
        filterChain.doFilter(request, response);
  }
}
