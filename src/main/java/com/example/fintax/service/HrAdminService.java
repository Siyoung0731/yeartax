package com.example.fintax.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fintax.dto.EmployeeCsvDto;
import com.example.fintax.entity.YearTax;
import com.example.fintax.repository.YearTaxRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrAdminService {
  private final YearTaxRepository yearTaxRepo;
  private final PasswordEncoder passwordEncoder;

  // 인사팀이 올린 CSV 데이터를 리스트(employeeDetailList)로 받았다고 가정
  @Transactional
  public void registerEmployee(List<EmployeeCsvDto> empDataList) {
    for(EmployeeCsvDto dto : empDataList) {

      // 초기 비밀번호 생성 : (규칙 = 사번 + "@fin24")
      String rawInitialPassword = dto.getJobId() + "@fin24";

      // 초기 비밀번호 암호화
      String encodedPassword = passwordEncoder.encode(rawInitialPassword);

      // 사원 정보 생성 및 저장
      YearTax newEmployee = YearTax.builder()
                                   .jobId(dto.getJobId())
                                   .name(dto.getName())
                                   .department(dto.getDepartment())
                                   .password(encodedPassword)
                                   .isFirstLogin(1)
                                   .build();
      
      yearTaxRepo.save(newEmployee);                                   
    }
  } 
}
