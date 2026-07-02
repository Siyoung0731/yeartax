package com.example.fintax.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.fintax.entity.YearTax;
import com.example.fintax.repository.YearTaxRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelExportService {
  private final YearTaxRepository yearTaxRepo;

  public ByteArrayInputStream exportTaxDataToExcel() {
    // 1. 모든 사원의 정산 데이터 조회 (필요 시 FINALIZED 상태만 가져오도록 수정 기능)
    List<YearTax> taxList = yearTaxRepo.findAll();

    // 2. 새로운 엑셀 워크북(파일)과 시트 생성
    try (Workbook workbook = new XSSFWorkbook();
          ByteArrayOutputStream out = new ByteArrayOutputStream()) {
          Sheet sheet = workbook.createSheet("2024년_연말정산_최종결과");

          // 3. 헤더(첫번째 줄) 만들기
          Row headerRow = sheet.createRow(0);
          String[] columns = {"사번", "이름", "부서", "상태", "추가 공제액(PDF+수동)", "최종 환급/징수액"};

          // 헤더 스타일
          Font headerFont = workbook.createFont();
          headerFont.setBold(true);
          CellStyle headerCellStyle = workbook.createCellStyle();
          headerCellStyle.setFont(headerFont);

          for(int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
          }

          // 4. 데이터 채우기
          int rowIdx = 1;
          for(YearTax tax : taxList) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(tax.getJobId());                 // 사번
            row.createCell(1).setCellValue(tax.getName());                  // 이름
            row.createCell(2).setCellValue(tax.getDepartment());            // 부서명
            row.createCell(3).setCellValue(tax.getStatus());                // 상태(PROCESSING)

            // 공제액(예: PDF 공제액)
            row.createCell(4).setCellValue(tax.getPdfDeductionAmount() != null ? tax.getPdfDeductionAmount() : 0);

            // 최종 예상 환급액
            row.createCell(5).setCellValue(tax.getResultAmount() != null ? tax.getResultAmount() : 0);
          }

          // 5. 엑셀 파일을 Byte 배열로 변환하여 반환
          workbook.write(out);
          return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
          throw new RuntimeException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
  }
}
