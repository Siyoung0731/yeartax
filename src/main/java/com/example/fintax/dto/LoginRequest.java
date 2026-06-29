package com.example.fintax.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
  private String jobId;
  private String password;
}
