package com.example.fintax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCsvDto {
  private String jobId;   // 사번
  private String name;    // 이름
  private String department;    // 부서

}
