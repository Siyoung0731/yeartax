package com.example.fintax.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.fintax.entity.ManualEvidence;
import com.example.fintax.entity.YearTax;
import com.example.fintax.repository.ManualEvidenceRepository;
import com.example.fintax.repository.YearTaxRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/evidence")
@RequiredArgsConstructor
public class EvidenceController {
  
  private final ManualEvidenceRepository evidenceRepo;
  private final YearTaxRepository yearTaxRepo;

  // 테스트용 파일 저장 경로
  private final String UPLOAD_DIR = "C:\\Temp\\uploads\\";

  //1. 사원이 영수증을 업로드할 때 받는 API
  @PostMapping("/upload")
  public ResponseEntity<String> uploadEvidence(
    @RequestParam("jobId") String jobId,
    @RequestParam("category") String category,
    @RequestParam("amount") Long amount,
    @RequestParam("file") MultipartFile file) {
      try {
        //[A] 파일 저장 로직(UUID로 이름 변경)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = UUID.randomUUID().toString() + extension;

        File dest = new File(UPLOAD_DIR + savedFilename);
        file.transferTo(dest);    //실제 하드디스크에 파일 저장

        //[B] JPA를 이용해 DB에 데이터 저장
        //먼저 부모 클래스를 찾아옴(없으면 에러 처리)
        YearTax parentTax = yearTaxRepo.findById(jobId)
                  .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정산 내역입니다. "));

        ManualEvidence evidence = new ManualEvidence();
        evidence.setYearTax(parentTax);     // 부모 클래스 연결
        evidence.setCategory(category);
        evidence.setAmount(amount);
        evidence.setFileUrl(savedFilename);       //DB에는 저장된 파일명만 기록
        //status는 엔티티에서 default="PENDING" 설정

        evidenceRepo.save(evidence);      // DB에 INSERT;

        return ResponseEntity.ok("업로드 완료 (대기 중)");

      } catch(IOException e) {
        return ResponseEntity.internalServerError().body("파일 저장 실패"); 
      }

  }
  // 3. 관리자 : 대기 중(PENDING)인 영수증 목록 조회
  @GetMapping("/admin/approve/{evidenceId}")
  public ResponseEntity<String> approveEvidence(@PathVariable("evidenceId") Long evidenceId) {
    //1. 해당 영수증을 DB에서 찾음
    ManualEvidence evidence = evidenceRepo.findById(evidenceId)
                    .orElseThrow(() -> new IllegalArgumentException("영수증을 찾을 수 없습니다."));
    
    //2. 상태를 APPROVED 로 변경
    evidence.setStatus("APPROVED");

    //[핵심] 3. 부모 클래스의 총 환급액에 이 영수증 금액을 더해준다.
    YearTax parentTax = evidence.getYearTax();
    parentTax.setResultAmount(parentTax.getResultAmount() + evidence.getAmount().intValue());
    
    //4. 변경된 내역을 DB에 저장(JPA는 save 하나로 UPDATE까지 처리합니다)
    evidenceRepo.save(evidence);
    yearTaxRepo.save(parentTax);

    return ResponseEntity.ok(evidenceId + "번 영수증이 승인되었습니다. (총 환급액 증가 반영)");
  }

  @GetMapping("/admin/pending-list")
  public ResponseEntity<List<ManualEvidence>> getPendingEvidence() {
    List<ManualEvidence> pendingList = evidenceRepo.findByStatus("PENDING");
    return ResponseEntity.ok(pendingList);
  }
}


