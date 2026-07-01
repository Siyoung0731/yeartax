package com.example.fintax.controller;

import com.example.fintax.service.HrAdminService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.fintax.dto.EmployeeCsvDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class HrAdminController {

  // 관리자가 CSV 파일을 업로드하는 API 엔드포인트
  private final HrAdminService hrAdminService;

  @PostMapping("/employees/upload")
  public ResponseEntity<?> uploadEmployeeCsv(@RequestParam("file") MultipartFile file) {
    if(file.isEmpty()) {
      return ResponseEntity.badRequest().body("파일이 비어있습니다.");
    }

    List<EmployeeCsvDto> empList = new ArrayList<>();

    // MultipartFile을 읽어서 텍스트 라인 단위로 분해하는 과정
    try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      boolean isFirstLine = true;   // 엑셀의 첫 번째 줄(헤더)을 건너뛰기 위한 플래그

      while((line = br.readLine()) != null) {
        // 1. 첫번째 줄(사원, 이름, 부서 등 헤더 텍스트)은 스킵
        if(isFirstLine) {
          isFirstLine = false;
          continue;
        }

        // 2. 콤마(,)를 기준으로 문자열을 자릅니다
        String[] data = line.split(",");

        // 3. 데이터가 정상적으로 3칸(사원, 이름, 부서)이 있는지 확인
        if(data.length >= 3) {
          EmployeeCsvDto dto = EmployeeCsvDto.builder()
                   .jobId(data[0].trim())
                   .name(data[1].trim())
                   .department(data[2].trim())
                   .build();

          empList.add(dto);       // empList 에 저장
        }
      }

      // 4. 리스트가 꽉 차면 서비스 클래스로 넘겨 DB 저장 & 암호화 수행
      hrAdminService.registerEmployee(empList);
      
      return ResponseEntity.ok("총 " + empList.size() + "명의 사원 정보가 성공적으로 등록되었습니다.");
    } catch(Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError().body("CSV 파일 처리 중 오류가 발생했습니다.");
    }
  } 
}
