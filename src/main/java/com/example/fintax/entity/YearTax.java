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
  @Column(name = "JOB_ID", length = 30)
  private String jobId;       // 진동벨 번호(UUID)

  private String status = "PROCESSING";     //현재 상태
  private Integer resultAmount = 0;         //총 예상 환급액

  //핵심 : ManualEvidence 랑 1:N 관계
  // mappedBy 는 자식 클래스에서 나를 부르는 변수명("yearTax")과 같아야 함
  // JsonIgnore : Json으로 변환 시 영수증 리스트는 무시
  @JsonIgnore
  @OneToMany(mappedBy = "yearTax", cascade = CascadeType.ALL)
  private List<ManualEvidence> evidences = new ArrayList<>();
}
