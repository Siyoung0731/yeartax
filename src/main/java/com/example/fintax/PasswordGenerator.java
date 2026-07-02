package com.example.fintax;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PasswordGenerator implements CommandLineRunner {
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
        System.out.println("==============================");
        System.out.println(passwordEncoder.encode("pwd1234"));  
        System.out.println("==============================");
  }

}
