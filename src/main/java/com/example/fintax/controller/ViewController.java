package com.example.fintax.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
  // 로그인
  @GetMapping("/login")
  public String login() {
    return "login";
  }
  // 메인 페이지
  @GetMapping("/")
  public String home() {
    return "index";
  }
  // 1. 국세청 간소화 PDF 업로드 화면 이동
  @GetMapping("/view/pdf-upload")
  public String pdfUpload() {
    return "pdf-upload";
  }
  // 2. 부양가족(인적공제) 등록 화면 이동
  @GetMapping("/view/dependent-register")
  public String dependentRegister() {
    return "dependent-register";
  }
  // 3. 누락분 수동 영수증 제출 화면 이동
  @GetMapping("/view/evidence-upload")
  public String evideceUpload() {
    return "evidence-upload";
  }
  // 4. [관리자 전용] 영수증 결재함 화면 이동
  @GetMapping("/view/admin-evidence")
  public String adminEvidence() {
    return "admin-evidence";
  }

  // 5. [관리자 전용] 사원 CSV 일괄 등록 화면 이동
  @GetMapping("/view/admin-employee")
  public String adminEmployee() {
    return "admin-employee";
  }

  // 6. 비밀번호 변경 페이지 이동
  @GetMapping("/view/change-password")
  public String changePassword() {
    return "change-password";
  }
  // 7. 정산 마감 관리자용 화면 이동
  @GetMapping("/view/admin-tax")
  public String adminTax() {
    return "admin-tax";
  }
}
