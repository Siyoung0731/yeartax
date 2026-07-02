package com.example.fintax.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          // cors 설정 적용
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(csrf -> csrf.disable())   // REST API이므로 CSRF 보안 무효화

          // 폼 로그인 및 기본 HTTP 로그인 비활성화 (우리는 Ajax/JSON 로그인을 사용)
          .formLogin(form -> form.disable())
          .httpBasic(basic -> basic.disable())

          // 세션을 생성하지 않도록 설정 (JWT 사용 필수 설정)
          .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
          )
          .authorizeHttpRequests(auth -> auth
                // 1. 화면(View) 컨트롤러 주소 완벽 허용
                .requestMatchers("/", "/login", "/view/**").permitAll()
                
                // 2. JWT 로그인 API 주소 허용
                .requestMatchers("/api/login").permitAll()
                
                // 3. 정적 리소스(디자인, 스크립트, 파비콘 등) 완벽 허용 (⭐ 이 부분이 누락되었을 확률이 높습니다)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()
                
                // 4. 나머지는 모두 인증 필요
                .anyRequest().authenticated()
          )
          .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
      return http.build();
  }

  // CORS 기본 설정 빈 등록 (프론트엔드 통신 허용)
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));     // 모든 도메인 허용
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  // 비밀번호 암호화
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
