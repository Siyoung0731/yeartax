package com.example.fintax.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
  private String jobId;   // 사번
  private String password;      // 비밀번호
}
