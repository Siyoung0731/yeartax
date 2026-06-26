package com.example.fintax.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fintax.entity.ManualEvidence;
import java.util.List;


public interface ManualEvidenceRepository extends JpaRepository<ManualEvidence, Long> {
  // 관리자가 특정 상태(예: PENDING)인 영수증만 리스트로 뽑을 때 사용할 메서드
  List<ManualEvidence> findByStatus(String status);
}
