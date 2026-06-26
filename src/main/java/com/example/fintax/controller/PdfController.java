package com.example.fintax.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.fintax.entity.YearTax;
import com.example.fintax.repository.YearTaxRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {
  private final YearTaxRepository yearTaxRepo;

  @PostMapping("/upload")
  public ResponseEntity<String> uploadHometaxPdf(
    @RequestParam("jobId") String jobId,
    @RequestParam("file") MultipartFile file) {

    //1. 유효성 검증(Validation)
    String filename = file.getOriginalFilename();
    if(filename == null || !filename.toLowerCase().endsWith(".pdf")) {
      return ResponseEntity.badRequest().body("국세청 간소화 서비스에서 다운로드한 올바른 PDF 파일이 아닙니다.");
    }

    YearTax parentTax = yearTaxRepo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("정산내역을 찾을 수 없습니다."));

    // 시뮬레이션 : PDF 안에서 신용카드, 의료비 등 총 총 4,500,000원의 공제 내역을 스캔했다고 가정
    int mockParseDeduction = 4500000;

    parentTax.setResultAmount(parentTax.getResultAmount() + mockParseDeduction);
    parentTax.setStatus("COMPLETED");

    yearTaxRepo.save(parentTax);

    return ResponseEntity.ok("국세청 간소화 PDF 분석 완료! 총 4,500,000원의 공제 내역이 자동 반영되어 정산 상태가 'COMPLETED'로 변경되었습니다.");
  }
}
