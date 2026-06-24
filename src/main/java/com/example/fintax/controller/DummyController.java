package com.example.fintax.controller;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController     // 데이터를 응답하는 컨트롤러 - JSON을 보내는 용도
public class DummyController {  
  private final Map<String, Integer> statusCount = new ConcurrentHashMap<>();

  // jobId 발급 - 브라우저에서 파일을 업로드하면 이 메서드 실행
  @PostMapping("/api/upload")
  public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
    // UUID : 이름의 중복을 제거하기 위해 랜덤 일련번호
    String jobId = UUID.randomUUID().toString();  
    
    // 이 작업의 상태 확인 횟수를 기록
    statusCount.put(jobId, 0);

    // 브라우저에게 JSON으로 응답
    return Map.of(
      "jobId", jobId,
      "message", "UPLOAD_ACCEPTED"
    );
  } 
  // 처음 두번은 PROCESSING
  // 세번째부터 COMPLETED
  // 브라우저가 작업 상태를 물어보면 실행
  @GetMapping("/api/status")
  public Map<String, Object> status(@RequestParam("jobId") String jobId) {
    // 상태 확인 요청이 몇번째인지 체크
    int count = statusCount.getOrDefault(jobId, 0) + 1;
    statusCount.put(jobId, count);

    if(count >= 3) {    // 세번째부터는 완료된 척
      return Map.of(
        "status", "COMPLETED",
        "refundAmount", 124800
      ); // 완료 응답 전달
    }
    return Map.of(
      "status", "PROCESSING"
    ); // 완료 중이 아닐 시 처리 중 전달
  }
}
