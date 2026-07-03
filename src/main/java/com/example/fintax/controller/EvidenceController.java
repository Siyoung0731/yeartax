package com.example.fintax.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
import org.springframework.security.core.Authentication;
import java.time.LocalDateTime;

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
  private final Path uploadRoot = Paths.get("uploads", "evidences");

  //1. 사원이 영수증을 업로드할 때 받는 API
  @PostMapping("/upload")
  public ResponseEntity<String> uploadEvidence(
    Authentication authentication,
    @RequestParam("category") String category,
    @RequestParam("amount") Long amount,
    @RequestParam("file") MultipartFile file) {
      try {
        String jobId = authentication.getName();
        
        //[B] JPA를 이용해 DB에 데이터 저장
        //먼저 부모 클래스를 찾아옴(없으면 에러 처리)
        YearTax parentTax = yearTaxRepo.findById(jobId)
                  .orElseThrow(() -> new IllegalArgumentException("정산 내역을 찾을 수 없습니다."));

        if ("SUBMITTED".equals(parentTax.getStatus()) || "FINALIZED".equals(parentTax.getStatus())) {
          return ResponseEntity.badRequest().body("최종 제출 이후에는 수정할 수 없습니다.");
        }
        
        if(file.isEmpty()) {
          return ResponseEntity.badRequest().body("파일이 비어 있습니다.");
        }         
        
        String originalFilename = file.getOriginalFilename();

        if(originalFilename == null || originalFilename.isBlank()) {
          return ResponseEntity.badRequest().body("파일명이 올바르지 않습니다.");
        }

        String lowerName = originalFilename.toLowerCase();

        if(!lowerName.endsWith(".jpg")
          && !lowerName.endsWith(".jpeg")
          && !lowerName.endsWith("png")
          && !lowerName.endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("JPG, PNG, PDF 파일만 업로드 할 수 있습니다.");
          }
        
        Files.createDirectories(uploadRoot);

        //[A] 파일 저장 로직(UUID로 이름 변경)
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = UUID.randomUUID().toString() + extension;      // 실제 저장 파일명은 서버가 직적 만들어야 안전
        Path savedPath = uploadRoot.resolve(savedFilename);

        Files.copy(file.getInputStream(), savedPath, StandardCopyOption.REPLACE_EXISTING);

        ManualEvidence evidence = new ManualEvidence();
        evidence.setYearTax(parentTax);     // 부모 클래스 연결
        evidence.setCategory(category);
        evidence.setAmount(amount);
        evidence.setOriginalFileName(originalFilename);
        evidence.setFilePath(savedPath.toString());       //DB에는 저장된 파일명만 기록
        evidence.setUploadedAt(LocalDateTime.now());
        evidence.setStatus("PENDING");
        //status는 엔티티에서 default="PENDING" 설정

        evidenceRepo.save(evidence);      // DB에 INSERT;

        return ResponseEntity.ok("영수증 업로드 완료. 인사팀 승인 대기 중입니다.");

      } catch(IOException e) {
        return ResponseEntity.internalServerError().body("파일 저장 실패"); 
      }

  }
  // 3. 관리자 : 영수증 승인 처리 (POST 요청으로 상태 변경)
  @PostMapping("/admin/approve/{evidenceId}")
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


