# 💰 FinTax - 사내 연말정산 패키지 시스템 (B2B SaaS)

> **FinTax**는 기업 임직원들이 번거로운 연말정산 서류 제출을 혁신적으로 간소화하고, 실시간으로 예상 환급액을 시각화하여 관리할 수 있도록 지원하는 실무형 연말정산 관리 패키지(B2B SaaS) 시스템입니다.

---

## 🏛️ 1. 아키텍처 특장점 (Architecture Highlights)

* **인증 및 정산 데이터 통합 싱글 엔티티 모델**: 복잡한 인사(HR) 시스템과의 연동 비용을 줄이기 위해 별도의 User 테이블을 분리하지 않고, `YearTax` 엔티티 단일 테이블을 사용하여 인증 정보(로그인)와 정산 데이터를 통합 관리하는 고성능 패키지형 아키텍처를 채택했습니다.
* **멀티 테넌시(Multi-tenancy) 확장성**: 추후 다중 기업을 대상으로 한 SaaS 서비스 확장을 고려하여 단일 테이블 격리 구조 기반의 비즈니스 레이어를 마련했습니다.
* **JWT 기반 무상태(Stateless) 보안인증**: 클라이언트 측 `localStorage`에 JWT 토큰을 보관하고, 모든 API 요청 시 HTTP 헤더에 `Authorization: Bearer {token}`을 실어 전송하는 표준 보안 패턴을 준수합니다.
* **유기적 웹 컴포넌트 연동**: `ViewController`가 Thymeleaf 화면 이동을 통제하고, 비즈니스 데이터 처리는 완벽한 REST API 컨트롤러 분리 구조(`DashboardController`, `DependentController`, `EvidenceController` 등)로 설계하여 유지보수성을 극대화했습니다.

---

## ✨ 2. 핵심 기능 및 비즈니스 로직 (Core Features)

### 🧑‍💼 사원(임직원) 기능
1. **최초 로그인 온보딩 통제**: 사원 일괄 등록 시 초기 비밀번호를 `사번@fin24` 형식으로 발급하고, 최초 로그인 여부(`isFirstLogin = 1`)를 판별하여 비밀번호 강제 변경 페이지로 이동시키는 안전한 온보딩 흐름을 지원합니다.
2. **실시간 환급액 대시보드**: 로그인 성공 시 발급받은 JWT를 기반으로 본인의 정산 데이터만 조회하며, `Chart.js` 도넛 차트를 활용해 실시간 예상 환급액 변화 추이를 비동기(Ajax)로 시각화합니다.
3. **국세청 간소화 PDF 업로드**: 업로드된 자료를 기반으로 의료비, 교육비, 신용카드 공제 금액을 백엔드에서 계산 및 추출하고 `pdfDeductionAmount`에 실시간 가산하여 연동합니다. (중복 업로드 원천 차단)
4. **부양가족(인적공제) 등록 엔진**: `YearTax`와 `Dependent` 테이블 간의 1:N 연관관계를 JPA로 매핑하여가족이 등록될 때마다 세법 규정에 따른 기본공제액(1인당 1,500,000원)이 `resultAmount`에 즉시 반영됩니다. (동일 사번 내 주민등록번호 중복 등록 방어)
5. **수동 증빙 제출 및 완벽한 인터락(Lock)**: 누락분을 위한 수동 영수증(JPG, PNG, PDF) 업로드 기능을 제공하며, 사원이 **[최종 제출]** 완료 시 화면 새로고침 없이 즉시 퀵 메뉴 및 제출 버튼을 완전 잠금(`pointer-events: none` 및 부트스트랩 `disabled` 처리)하여 데이터 무결성을 보장합니다.

### 🔒 관리자(인사/회계팀) 기능
1. **사원 정보 일괄 등록**: 인사담당자가 사원 명부 CSV 파일을 업로드하여 다량의 임직원 계정을 일괄 생성하고 안전하게 DB에 영속화합니다.
2. **수동 영수증 결재함**: 사원이 올린 `PENDING` 상태의 영수증 목록을 검토 후 **[승인]** 처리 시 즉시 해당 금액이 사원의 `resultAmount`에 자동 반영됩니다.
3. **유기적 반려 프로세스**: 증빙 부적합으로 **[반려]** 처리 시 영수증 상태는 `REJECTED`로 바뀌며 사유를 저장하고, 사원의 정산 상태를 다시 `PROCESSING`으로 되돌려 대시보드 잠금을 해제함으로써 수정 제출을 가능하게 만듭니다.
4. **정산 최종 마감 & 엑셀 통계**: 전 사원의 연말정산 서류를 일괄 최종 마감(`FINALIZED`)하고, 국세청 세무 신고를 위한 전체 정산 통계 데이터를 다운로드할 수 있습니다.

---

## 🛠️ 3. 기술 스펙 (Tech Stack)

* **Backend**: Spring Boot 3.x, Spring Data JPA, Spring Security (BCrypt)
* **Database**: Oracle DB Free (DBeaver 연동 및 OCI Cloud DBaaS 확장 가능)
* **Frontend**: HTML5, CSS3 (Bootstrap 5), JavaScript (jQuery), AJAX, Chart.js
* **Security & Auth**: JWT (JSON Web Token), BCrypt Password Encoder
* **File Upload**: MultipartFile, Local File Storage (`uploads/evidences`)

---

## 📉 4. 최근 업데이트 및 트러블슈팅 이력 (Refactoring & Troubleshooting)

### ① 데이터 무결성을 위한 대시보드 UI 잠금 통제 및 API 1회 통합 최적화
* **현상**: 최종 제출 성공 이후 브라우저 새로고침 전까지 퀵 메뉴가 활성화되어 중복 제출 및 수정 리스크가 존재함. 또한 대시보드 로딩 시 데이터 조회와 상태 조회가 분리되어 중복 네트워크 비용 발생.
* **해결**: `lockDashboardAndHideButtons()` 공통 함수를 구축하고 CSS `pointer-events: none` 스타일을 결합해 하이퍼링크 클릭을 원천 방어함. 백엔드의 대시보드 단건 조회 API에 `status` 필드를 결합하여 **단 1회의 AJAX 통신**만으로 동적 UI 상태 제어가 가능하게끔 아키텍처를 최적화함.

### ② 관리자 영수증 승인 시스템 버그 교정 (`EvidenceController`)
* **현상**: 관리자 전용 영수증 결재함 화면(`admin-evidence.html`)에서 승인 버튼 클릭 시 이벤트가 정상적으로 바인딩되지 않던 현상 및 백엔드 매핑 불일치 에러 발생.
* **해결**: 프론트엔드 버튼 ID 선택자 오류(`btn-approve`)를 클래스 선택자(`.btn-approve`)로 신속히 교정하였고, 백엔드 승인 API 매핑을 `@GetMapping`에서 글로벌 표준에 맞춰 `@PostMapping("/admin/approve/{evidenceId}")` 구조로 전면 변경함. 승인 완료 시 사원의 `YearTax.resultAmount` 세법 연동 가산 로직 검증 완료.

### ③ 보안 컨텍스트 환경에서의 엑셀 다운로드 아키텍처 리팩토링
* **현상**: 정산 최종 마감 화면(`admin-tax.html`)에서 기존 `window.location.href = '/api/admin/export/excel'` 방식을 사용하면서 HTTP 요청 헤더에 JWT 인증 토큰(`Authorization`)이 실리지 않아 `403 Forbidden` 보안 차단 에러 발생.
* **해결**: 단순 링크 이동 방식에서 **AJAX / fetch API 기반 비동기 통신 방식**으로 전면 리팩토링 진행. HTTP Header에 `Authorization: Bearer {token}`을 안정적으로 전송하도록 설계하고, 서버로부터 수신한 스트림 데이터를 브라우저 메모리상에서 `Blob` 객체로 변환하여 동적으로 다운로드를 수행하는 실무형 보안 다운로드 프로세스 안착.

### ④ JPA / Oracle DB 제약조건 충돌 해결
* **현상**: 기존 데이터가 존재하는 상태에서 `role` 컬럼 타입을 변경 시도할 때 `ORA-01439` 및 `ORA-01463` 발생, 혹은 문자열('USER')을 숫자형 컬럼에 오매핑하여 `ORA-01722` 발생. 또한 필수 컬럼에 데이터 누락으로 `ORA-01400` 발생.
* **해결**: 테이블 초기화 및 안전한 `VARCHAR2` 타입 수정을 통해 스키마 구조를 안정화하였고, `IS_FIRST_LOGIN` 및 `PDF_DEDUCTION_AMOUNT` 등 필수 컬럼의 제약조건 위반을 방어할 수 있도록 안전한 디폴트 값이 포함된 샘플 데이터셋 스크립트를 전면 재구성함.

---

## 🔐 5. 데이터 상태 변화 흐름 (Status Flow)

### 임직원 연말정산 상태 (`YearTax.status`)
```text
PROCESSING (작성 중)  ──>  SUBMITTED (제출 완료 / 사원 화면 잠금)  ──>  FINALIZED (인사팀 최종 마감)
```

### 수동 증빙 영수증 상태 (`ManualEvidence.status`)
```text
                     ┌─── APPROVED (승인 / 사원 환급액에 금액 즉시 누적 반영)
PENDING (인사팀 심사 중) ┤
                     └─── REJECTED (반려 / 사유 기록 및 사원 화면 잠금 해제하여 PROCESSING 복귀)
```
## 🏃 6. 시작하기 (How to Run)
# 1. Oracle DB 인스턴스 생성 및 접속 확인
# 2. src/main/resources/application.properties 에서 데이터베이스 접속 정보 최적화
```
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```
# 3. Spring boot 애플리케이션 빌드 및 실행
```
./gradlew bootRun
```
# 4. 브라우저를 통해 사내 네트워크 접속 (http://localhost:9090)
* 초기 계정 생성 시 특정 임직원의 role 컬럼 값을 ROLE_ADMIN으로 수정하면 즉시 관리자 전용 메뉴 통제 권한을 획득할 수 있습니다.


