// 사용자가 버튼을 클릭 시 무슨 일이 일어날지 담당
// 사용자가 파일을 고르고 버튼을 누르면 화면을 바꾸고, 서버에 Ajax 요청 보냄
// 업로드 -> 로딩 -> 결과

//이 전역 변수들은 반복 작업을 관리
let pollingTimer = null;
let loadingTextTimer = null;
let loadingTextIndex = 0;

// 로딩 문구 바꾸는 함수
const loadingMessages = [
  "PDF 분석 중...",
  "소득공제 확인 중...",
  "환급액 계산 중...",
  "결과 정리 중..."
];

//화면 전환 함수 - 나중에 작업이 끝나면 반복작업을 멈춰야 하니까 따로 저장
function showStep(steId) {
  $(".step").hide();
  $(steId).fadeIn(200);
}
//숫자를 한국 원화처럼 보기 좋게 바꿔주는 함수입니다. - 원화 포맷 함수
function formatWon(amount) {
  return amount.toLocaleString("Ko-KR") + "원";
}

function startLoadingMessages() {
  // loadingTextIndex : 현재 몇번째 로딩 문구를 보여주는 지 저장
  loadingTextIndex = 0;
  $("#statusText").text(loadingMessages[loadingTextIndex]);

  // loadingTextTimer : 1.5초 마다 로딩 문구를 바꾸는 작업 기억
  loadingTextTimer = setInterval(function() {
    loadingTextIndex++;

    if(loadingTextIndex >= loadingMessages.length) {
      loadingTextIndex = 0;
    }
    $("#statusText").text(loadingMessages[loadingTextIndex]);
  }, 1500);
}

// 로딩이 끝났거나 에러가 나면 문구 변경 멈춤
// setInterval -> clearInterval
function stopLoadingMessages() {
  if(loadingTextTimer) {
    clearInterval(loadingTextTimer);
    loadingTextTimer = null;
  }
}

function startPolling(jobId) {
  pollingTimer = setInterval(function() {
    $.ajax({  // 서버에 요청
      url: "/api/status",
      method: "GET",
      data: {
        jobId: jobId  // /api/status/jobId=?
      },
      success: function(response) {
        if(response.status === "COMPLETED") {   // 계산이 끝났다는 뜻
          clearInterval(pollingTimer)
          pollingTimer = null; // 작업 stop

          stopLoadingMessages();

          // 환급액을 화면에 넣고 결과 화면으로 변환
          $("#refundAmount").text(formatWon(response.refundAmount));  // 환급액 표시
          $("#jobIdText").text(jobId);  // 처리 번호 표시
          showStep("#step-result");   // 결과 화면으로 전환
        } 
      },
      error: function() {
        clearInterval(pollingTimer);
        pollingTimer = null;

        stopLoadingMessages();

        alert("상태 확인 중 오류가 발생했습니다.");
        showStep("#step-upload");
      }
    });
  }, 2000);
} 

$(function() {
  $("#uploadBtn").on("click", function() { //업로드 버튼을 클릭 시
    // 사용자가 선택한 PDF 파일을 가져옴
    // JQuery로 찾은 결과에서 실제 DOM 요소를 꺼내기 위해[0]을 씀
    const fileInput = $("#pdfFile")[0];

    // 파일 검사
    if(!fileInput) {
      alert("파일 input을 찾을 수 없습니다. index.html을 확인해주세요.");
      return;
    }

    const file = fileInput.files[0];

    // 파일을 선택하지 않았으면 경고창을 띄우고 여기서 멈춤
    if(!file) {
      alert("PDF 파일을 선택해주세요");
      return;
    }
    // pdf 가 아니면 파일 선택을 비우고 멈춤
    if(file.type !== "application/pdf") {
      alert("PDF 파일만 업로드할 수 있습니다.");
      $("#pdfFile").val("");
      return;
    }

    //파일을 서버에 보내기 위해 FormData 에 저장
    //파일 업로드는 일반 문자열 데이터와 다르게 FormData 를 쓰는 게 정석
    const formData = new FormData();
    formData.append("file", file);

    // 로딩 화면으로 전환 - text : 파일을 업로드 하는 중입니다 출력
    showStep("#step-loading");
    startLoadingMessages();

    // /api/upload 주소로 파일을 보내는 코드
    $.ajax({
      url: "/api/upload",
      method: "POST",
      data: formData,
      // JQuery 가 마음대로 변환하지 않게 해야 해서 추가
      // 파일 업로드할 때는 거의 항상 추가
      processData: false, 
      contentType: false,
      // 업로드가 성공하면 서버가 jobId를 줌
      success: function(response) {
        // jobId 데이터로 상태확인 시작
        startPolling(response.jobId);
      },
      error: function() {
        startLoadingMessages();

        alert("업로드 중 오류가 발생했습니다.");
        showStep("#step-upload");
      }
    });
  });
  $("#restartBtn").on("click", function() {
    if(pollingTimer) {
      clearInterval(pollingTimer);  // 선택 확인 반복 중지
      // pollingTimer : 2초마다 서버에 상태를 물어보는 작업을 기억하는 변수
      pollingTimer = null;  // 로딩 문구 반복 중지
    }

    stopLoadingMessages();

    $("#pdfFile").val("");  // 파일 선택 초기화
    $("#refundAmount").text("0원"); //환급액 초기화
    $("#jobIdText").text("-");  // 처리번호 초기화
    $("#statusText").text("PDF 분석 중...");  // 처음 화면으로 이동

    showStep("#step-upload");
  });
});
