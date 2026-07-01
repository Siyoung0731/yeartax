package com.example.fintax.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.fintax.dto.PdfDeductionResult;

@Service
public class PdfParsingService {
  // 모킹 기반 파싱 구조
  public PdfDeductionResult parse(MultipartFile file) {
    // TODO: 추후 PDFBox 기반 실제 파싱으로 교체
    return new PdfDeductionResult(
      800000,         // 의료비
      1200000,      // 교통비
      500000       // 신용카드 
    );
  }
}
