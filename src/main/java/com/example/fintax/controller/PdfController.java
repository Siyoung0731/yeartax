package com.example.fintax.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.fintax.dto.PdfDeductionResult;
import com.example.fintax.entity.YearTax;
import com.example.fintax.repository.YearTaxRepository;
import com.example.fintax.service.PdfParsingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {
  private final YearTaxRepository yearTaxRepo;
  private final PdfParsingService pdfParsingService;

  @PostMapping("/upload")
  public ResponseEntity<String> uploadHometaxPdf(
    Authentication authentication,
    @RequestParam("file") MultipartFile file) {

    String jobId = authentication.getName();

    //1. 유효성 검증(Validation)
    String filename = file.getOriginalFilename();
    
    // accept=".pdf" 는 사용자 편의, 진짜 검증은 서버
    if(filename == null || !filename.toLowerCase().endsWith(".pdf")) {
      return ResponseEntity.badRequest().body("PDF 파일만 업로드할 수 있습니다.");
    }

    YearTax parentTax = yearTaxRepo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("정산내역을 찾을 수 없습니다."));
                
    if ("SUBMITTED".equals(parentTax.getStatus()) || "FINALIZED".equals(parentTax.getStatus())) {
      return ResponseEntity.badRequest().body("최종 제출 이후에는 수정할 수 없습니다.");
    }

    if(parentTax.getPdfDeductionAmount() != null && parentTax.getPdfDeductionAmount() > 0) {
      return ResponseEntity.badRequest().body("이미 PDF 공제 내역이 반영되었습니다.");
    }

    // 시뮬레이션 : PDF 안에서 신용카드, 의료비 등 총 총 4,500,000원의 공제 내역을 스캔했다고 가정
    PdfDeductionResult result = pdfParsingService.parse(file);

    int currentAmount = parentTax.getResultAmount() != null ? parentTax.getResultAmount() : 0;
    int pdfAmount = result.getTotalDeductionAmount();

    parentTax.setPdfDeductionAmount(pdfAmount);
    parentTax.setResultAmount(currentAmount + pdfAmount);
    parentTax.setStatus("PROCESSING");

    yearTaxRepo.save(parentTax);

    return ResponseEntity.ok(
      "PDF 분석 완료! 의료비"
      + result.getMedicalAmount()
      + "원, 교육비"
      + result.getMedicalAmount()
      + "원, 신용카드"
      + result.getCreditCardAmount()
      + "원이 반영되었습니다."
    );
  }
}
