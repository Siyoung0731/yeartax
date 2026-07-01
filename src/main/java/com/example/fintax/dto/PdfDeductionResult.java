package com.example.fintax.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PdfDeductionResult {
  private int medicalAmount;                      // 의료비 공제 반영액
  private int educationAmount;                    // 교육비 공제 반영액
  private int creditCardAmount;                   // 신용카드 공제 반영액

  public int getTotalDeductionAmount() {
    return medicalAmount + educationAmount + creditCardAmount;
  }
}
