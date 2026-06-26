package com.example.fintax.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @RequestParam("jobId") String jobId,
    @RequestParam("name") String name,
    @RequestParam("relation") String relation,
    @RequestParam("identityNo") String identityNo,
    @RequestParam("isPathfinder") String isPathfinder) {
    YearTax parentTax = yearTaxRepo.findById(jobId)
              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정산 내역입니다. "));

    Dependent dependent = new Dependent();
    dependent.setYearTax(parentTax);
    dependent.setName(name);
    dependent.setRelation(relation);
    dependent.setIdentityNo(identityNo);
    dependent.setIsPathfinder(isPathfinder);

    dependentRepo.save(dependent);

    //인적공제 : 1인당 1,500,000원 환급 대상 금액(또는 소득공제액) 증가 시뮬레이션
    parentTax.setResultAmount(parentTax.getResultAmount() + 1500000);
    yearTaxRepo.save(parentTax);

    return ResponseEntity.ok(name + "님이 부양가족으로 등록되었습니다. (인적공제 반영)");
  }
}
