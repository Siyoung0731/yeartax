package com.example.fintax.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DEPENDENT")
@Getter
@Setter
public class Dependent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "DEPENDENT_ID")
  private Long dependentId;       //부양가족 고유 번호(자동 증가)
  
  private String name;                    // 가족 이름
  
  private String relation;                // 관계(배우자, 자녀, 부모 등)
  
  private String identityNo;              // 주민등록번호 (인적공제 검증용)
  
  private String isPathfinder;            // 경로 우대 여부(Y/N)
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "JOB_ID")
  private YearTax yearTax;
}
