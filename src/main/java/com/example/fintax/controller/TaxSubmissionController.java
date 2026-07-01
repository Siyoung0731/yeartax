package com.example.fintax.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fintax.entity.YearTax;
import com.example.fintax.repository.YearTaxRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxSubmissionController {
  private final YearTaxRepository yearTaxRepo;

  @PostMapping("/submit")
  public ResponseEntity<String> submitTax(Authentication authentication) {
    String jobId = authentication.getName();

    YearTax yearTax = yearTaxRepo.findById(jobId)
              .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다."));
    
    if("SUBMITTED".equals(yearTax.getStatus())) {
      return ResponseEntity.badRequest().body("이미 최종 제출된 정산입니다.");
    }

    if("FINALIZED".equals(yearTax.getStatus())) {
      return ResponseEntity.badRequest().body("이미 마감된 정산입니다.");
    }

    yearTax.setStatus("SUBMITTED");
    yearTaxRepo.save(yearTax);

    return ResponseEntity.ok("최종 제출이 완료되었습니다.");
  }

  @PostMapping("/admin/finalize/{jobId}")
  public ResponseEntity<String> finalizeTax(@PathVariable("jobId") String jobId) {
    YearTax yearTax = yearTaxRepo.findById(jobId)
              .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다."));

    // 마감은 검토가 끝난 제출 건만 해야 한다.
    if(!"SUBMITTED".equals(yearTax.getStatus())) {
      return ResponseEntity.badRequest().body("제출 완료 상태만 최종 마감할 수 있습니다.");
    }

    yearTax.setStatus("FINALIZED");
    yearTaxRepo.save(yearTax);

    return ResponseEntity.ok("연말정산이 최종 마감되었습니다.");
  }
}
