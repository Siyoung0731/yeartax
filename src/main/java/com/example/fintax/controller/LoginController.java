package com.example.fintax.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.fintax.dto.LoginRequest;
import com.example.fintax.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LoginController {
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/api/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest loginData) {
    String jobId = loginData.getJobId();
    String password = loginData.getPassword();

    System.out.println("===로그인 요청===");
    System.out.println("입력받은 사번: " + jobId);
    System.out.println("입력받은 비밀번호: " + password);

    //[핵심] 실제 서비스라면 DB에서 유저 정보를 조회해서 비밀번호를 비교해야합니다.
    //현재는 구현을 위해 "1234" 사변인 경우 무조건 로그인을 성공시키는 로직
    if("1234".equals(jobId) && "password123".equals(password)) {
      //토큰 발급
      String token = jwtTokenProvider.createToken(jobId);
      return ResponseEntity.ok(Map.of("token", token, "message", "로그인 성공!"));
    } else {
      return ResponseEntity.status(401).body("로그인 실패: 사번 또는 비밀번호 확인하세요!");
    }
  }
}
