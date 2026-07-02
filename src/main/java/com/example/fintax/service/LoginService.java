package com.example.fintax.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.fintax.entity.YearTax;
import com.example.fintax.repository.YearTaxRepository;
import com.example.fintax.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {
  private final YearTaxRepository yearTaxRepo;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  public String login(String jobId, String rawPassword) {
    String normalizedJobId = jobId.strip();
    String normalizedPassword = rawPassword.strip();    // 입력 받은 비밀번호 앞뒤 공백 제거

    System.out.println("[Login Request] jobId: [" + normalizedJobId + "]");
    System.out.println("[Login Request] rawPassword: [" + normalizedPassword + "]");
    

    YearTax yearTax = yearTaxRepo.findById(normalizedJobId)
        .orElseThrow(() -> new RuntimeException("Employee number does not exist."));

    if(!isPasswordMatched(normalizedPassword, yearTax)) {
      throw new IllegalArgumentException("Password does not match.");
    }

    return jwtTokenProvider.createToken(yearTax.getJobId(), yearTax.getRole());
  }

  private boolean isPasswordMatched(String rawPassword, YearTax yearTax) {
    String savedPassword = yearTax.getPassword().strip();

    if(passwordEncoder.matches(rawPassword, savedPassword)) {
      return true;
    }

    if(rawPassword.equals(savedPassword)) {
      yearTax.setPassword(passwordEncoder.encode(rawPassword));
      yearTaxRepo.save(yearTax);
      return true;
    }

    return false;
  }
}
