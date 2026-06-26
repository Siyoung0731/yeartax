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
@Table(name = "MANUAL_EVIDENCE")
@Getter
@Setter
public class ManualEvidence {       // 자식클래스
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "EVIDENCE_ID")
  private Long evidenceId;      // 영수증 고유 번호(자동 증가 +1)

  //YearTax에 연결(N:1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "JOB_ID")    // 외래키(Fk)
  private YearTax yearTax;

  private String category;    // 안경비, 기부금 등
  private String fileUrl;     // 실제 이미지가 저장된 서버 경로 (UUID_파일명 형식)
  private Long amount;        // 영수증 금액
  private String status = "PENDING"; // 대기, 승인, 반려 상태
}
