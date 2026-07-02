package com.example.fintax.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.example.fintax.entity.YearTax;
import com.example.fintax.repository.YearTaxRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final YearTaxRepository yearTaxRepo;
  
  // 대시보드 데이터 불러오기 API
  @GetMapping("/{jobId}")
  public ResponseEntity<Map<String, Object>> getDashboardData(@PathVariable("jobId") String jobId) {
    // 1. DB에서 데이터 조회
    Optional<YearTax> yearTaxOptional = yearTaxRepo.findById(jobId);

    // 2. 데이터를 JSON으로 포장
    Map<String, Object> responseData = new HashMap<>();


    if(yearTaxOptional.isPresent()) {
      YearTax yearTax = yearTaxOptional.get();
      responseData.put("jobId", yearTax.getJobId());
      //RESULT_AMOUNT가 null일 경우 0L 반환
      responseData.put("resultAmount", yearTax.getResultAmount() != null ? yearTax.getResultAmount() : 0L);
      responseData.put("status", "success");
    } else {
      // DB에 데이터가 없어도 에러페이지로 이동하지 않고 데이터 반환
      responseData.put("jobId", jobId);
      responseData.put("resultAmount", 0);
      responseData.put("status", "not_found");
    }

    //3. JSON으로 응답 변환
    return ResponseEntity.ok(responseData);
  }
  
  // 나의 대시보드 데이터 불러오기 API
  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> getMyDashboard(Authentication authentication) {
    // JWT 필터에서 사번을 인증 객체에 넣어놨기 때문에 가능
    String jobId = authentication.getName();

    YearTax yearTax = yearTaxRepo.findById(jobId)
              .orElseThrow(() -> new RuntimeException("대시보드 정보를 찾을 수 없습니다."));

    Map<String, Object> responseData = new HashMap<>();
    responseData.put("jobId", yearTax.getJobId());
    responseData.put("name", yearTax.getName());
    responseData.put("department", yearTax.getDepartment());
    responseData.put("resultAmount", yearTax.getResultAmount() != null ? yearTax.getResultAmount() : 0);
    responseData.put("status", yearTax.getStatus());
    responseData.put("role", yearTax.getRole());

    return ResponseEntity.ok(responseData);
  }
}
