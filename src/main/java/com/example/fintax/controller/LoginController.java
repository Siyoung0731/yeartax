package com.example.fintax.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fintax.dto.LoginRequest;
import com.example.fintax.entity.YearTax;
import com.example.fintax.repository.YearTaxRepository;
import com.example.fintax.security.JwtTokenProvider;
import com.example.fintax.service.LoginService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")           // 프론트와 포트가 다른 경우를 위한 CORS 허용
public class LoginController {
  private final BCryptPasswordEncoder passwordEncoder;
  private final LoginService loginService;
  private final YearTaxRepository yearTaxRepo;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    // 1. 사번으로 DB에서 정보 조회 및 BCrypt 비밀번호 검증(기존 로직)
    YearTax employee = yearTaxRepo.findById(req.getJobId())
             .orElseThrow(() -> new RuntimeException("사번이 존재하지 않습니다."));

    if(!passwordEncoder.matches(req.getPassword(), employee.getPassword())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
    }

    // 2. JWT 토큰 발급 (기존 로직)
    String token = jwtTokenProvider.createToken(employee.getJobId());

    // 3. 최종 로그인 여부에 따라 응답 데이터(JSON)를 다르게 내려줌
    Map<String, Object> responseData = new HashMap<>();
    responseData.put("token", token);

    if(employee.isFirstLogin()) {
      responseData.put("requiredPasswordChange", true);
      responseData.put("message", "최초 로그인입니다. 비밀번호를 변경해주세요.");
    } else {
      responseData.put("requiredPasswordChage", false);
      responseData.put("message", "로그인 성공");
    }

    return ResponseEntity.ok(responseData);
  }
}
