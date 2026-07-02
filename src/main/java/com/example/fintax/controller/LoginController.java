package com.example.fintax.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
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
import org.springframework.security.core.Authentication;
import com.example.fintax.dto.ChangePasswordRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoginController {
  private final BCryptPasswordEncoder passwordEncoder;
  private final YearTaxRepository yearTaxRepo;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {

    
    YearTax employee = yearTaxRepo.findById(req.getJobId().strip())
    .orElseThrow(() -> new RuntimeException("사번이 존재하지 않습니다."));
    
    System.out.println("입력 비밀번호 : " + req.getPassword());
    System.out.println("DB 비밀번호 : " + employee.getPassword());
    System.out.println("matches : " +
    passwordEncoder.matches(req.getPassword(), employee.getPassword()));

    if(!passwordEncoder.matches(req.getPassword(), employee.getPassword())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
    }

    // Jwt token 발급(생성)
    String token = jwtTokenProvider.createToken(
      employee.getJobId(),
      employee.getRole()
    );
    boolean requirePasswordChange = Integer.valueOf(1).equals(employee.getIsFirstLogin());

    Map<String, Object> responseData = new HashMap<>();
    responseData.put("token", token);
    responseData.put("role", employee.getRole());   // 추가
    responseData.put("requirePasswordChange", requirePasswordChange);

    if(requirePasswordChange) {
      responseData.put("message", "초기 비밀번호 변경이 필요합니다.");
      responseData.put("redirectUrl", "/view/change-password");
    } else {
      responseData.put("message", "로그인 성공");
      responseData.put("redirectUrl", "/");
    }

    return ResponseEntity.ok(responseData);
  }

  @PostMapping("/change-password") 
  public ResponseEntity<?> changePassword(
    @RequestBody ChangePasswordRequest req, 
    Authentication authentication) {
      // 유효성 검증
      if(authentication == null || authentication.getName() == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
      }
      if(req.getNewPassword() == null || req.getNewPassword().isBlank()) {
        return ResponseEntity.badRequest().body("새 비밀번호를 입력해주세요.");
      }

      // 프론트에서 사번을 보내지 않아도 됌, JWT 토큰 안에 있는 사번을 서버가 꺼내 쓰는 구조
      String jobId = authentication.getName();

      YearTax employee = yearTaxRepo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("사번이 존재하지 않습니다."));
                
      employee.setPassword(passwordEncoder.encode(req.getNewPassword()));
      employee.setIsFirstLogin(0);

      yearTaxRepo.save(employee);

      return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }
}
