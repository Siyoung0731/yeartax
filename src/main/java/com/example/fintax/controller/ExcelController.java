package com.example.fintax.controller;

import java.io.ByteArrayInputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fintax.service.ExcelExportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
public class ExcelController {
  private final ExcelExportService excelExportService;

  @GetMapping("/excel")
  public ResponseEntity<InputStreamResource> downloadExcel() {
    // 서비스에서 엑셀 데이터 생성
    ByteArrayInputStream in = excelExportService.exportTaxDataToExcel();

    // HTTP 헤더 설정 (파일 다운로드용)
    HttpHeaders headers = new HttpHeaders();

    // 다운로드될 파일 이름 지정
    headers.add("Content-Disposition", "attachment; filename=tax_result_2024.xlsx");

    return ResponseEntity
              .ok()
              .headers(headers)
              .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
              .body(new InputStreamResource(in));
  }
}
