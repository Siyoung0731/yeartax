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
    System.out.println("[Login jobId]: " + normalizedJobId);

    YearTax yearTax = yearTaxRepo.findById(normalizedJobId)
        .orElseThrow(() -> new RuntimeException("Employee number does not exist."));

    if(!isPasswordMatched(rawPassword, yearTax)) {
      throw new IllegalArgumentException("Password does not match.");
    }

    return jwtTokenProvider.createToken(yearTax.getJobId());
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
