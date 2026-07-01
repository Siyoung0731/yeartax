package com.example.fintax.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.example.fintax.entity.Dependent;
import com.example.fintax.entity.YearTax;
import com.example.fintax.repository.DependentRepository;
import com.example.fintax.repository.YearTaxRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dependent")
@RequiredArgsConstructor
public class DependentController {
  private final DependentRepository dependentRepo;
  private final YearTaxRepository yearTaxRepo;

  // 부양 가족 추가
  @PostMapping("/add")
  public ResponseEntity<String> addDependent(
    Authentication authentication,
    @RequestParam("relation") String relation,
    @RequestParam("name") String name,
    @RequestParam("identityNo") String identityNo,
    @RequestParam("isPathfinder") String isPathfinder) {

    String jobId = authentication.getName();  

    YearTax parentTax = yearTaxRepo.findById(jobId)
              .orElseThrow(() -> new IllegalArgumentException("정산 내역을 찾을 수 없습니다."));

    if ("SUBMITTED".equals(parentTax.getStatus()) || "FINALIZED".equals(parentTax.getStatus())) {
      return ResponseEntity.badRequest().body("최종 제출 이후에는 수정할 수 없습니다.");
    }

    Dependent dependent = new Dependent();
    dependent.setYearTax(parentTax);
    dependent.setRelation(relation);
    dependent.setName(name);
    dependent.setIdentityNo(identityNo);
    dependent.setIsPathfinder(isPathfinder);

    // 부양가족 등록 중복 방지 로직
    if(dependentRepo.existsByYearTax_JobIdAndIdentityNo(jobId, identityNo)) {
      return ResponseEntity.badRequest().body("이미 등록된 부양가족입니다.");
    }

    dependentRepo.save(dependent);

    int currentAmount = parentTax.getResultAmount() != null ? parentTax.getResultAmount() : 0;
    // 핵심
    parentTax.setResultAmount(currentAmount + 15000000);
    
    yearTaxRepo.save(parentTax);

    return ResponseEntity.ok(name + "님이 부양가족으로 등록되었습니다. 예상 환급액 +1,500,000원 반영");
  }
}
