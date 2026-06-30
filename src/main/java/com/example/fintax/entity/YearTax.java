package com.example.fintax.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "YEAR_TAX")
@Getter
@Setter
public class YearTax {    // 부모 클래스 
  @Id
  @Column(name = "JOB_ID", length = 36)
  private String jobId;       // 진동벨 번호(UUID)

  @Column(name = "PASSWORD", nullable = false)
  private String password;  // 암호화된 비밀번호 저장

  @Column(name = "RESULT_AMOUNT", nullable = false)
  private Integer resultAmount = 0;         //총 예상 환급액

  private String enterpriseCode;
  private String status = "PROCESSING";     //현재 상태

  @Column(nullable = false)
  private boolean isFirstLogin = true;      //DB에 처음 들어갈 때는 무조건 '최초(true)' 상태

  //핵심 : ManualEvidence 랑 1:N 관계
  // mappedBy 는 자식 클래스에서 나를 부르는 변수명("yearTax")과 같아야 함
  // JsonIgnore : Json으로 변환 시 영수증 리스트는 무시
  @JsonIgnore
  @OneToMany(mappedBy = "yearTax", cascade = CascadeType.ALL)
  private List<ManualEvidence> evidences = new ArrayList<>();

  public static Object builder() {
    throw new UnsupportedOperationException("Unimplemented method 'builder'");
  }
}
