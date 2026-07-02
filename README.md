### 🚀 사내 연말정산 시스템 (FinTax)

기업의 연말정산 업무를 자동화하고 효율적으로 관리하기 위한 사내 전용 웹 애플리케이션입니다.  
사원은 본인의 연말정산 자료를 등록하고 예상 환급액을 확인할 수 있으며, 인사담당자는 사원 정보 일괄 등록, 증빙 영수증 결재, 정산 최종 마감 업무를 처리할 수 있습니다.

---

### 🛠 Tech Stack

Backend: Spring Boot, Spring Security, Spring Data JPA  
Database: Oracle DB  
Frontend: HTML5, Bootstrap, JavaScript, jQuery, AJAX  
Security/Auth: JWT, BCrypt Password Encoder  
File Upload: MultipartFile, Local File Storage

---

### 📌 Core Features & Business Logic

#### 1. B2B 사내 계정 관리 및 보안 로그인

- 별도의 회원가입 없이 인사담당자가 CSV 파일로 사원 정보를 일괄 등록합니다.
- 사번, 이름, 부서 정보를 기반으로 `YearTax` 계정이 생성됩니다.
- 초기 비밀번호는 `사번@fin24` 형식으로 자동 발급되며 BCrypt로 암호화되어 저장됩니다.
- 최초 로그인 시 `isFirstLogin = 1`이면 비밀번호 변경 화면으로 강제 이동합니다.
- 비밀번호 변경 완료 후 `isFirstLogin = 0`으로 변경되어 정상 대시보드 접근이 가능합니다.
- 로그인 성공 시 JWT를 발급하고, 이후 API 요청은 `Authorization: Bearer {token}` 헤더를 통해 인증합니다.

#### 2. 사원 전용 대시보드

- JWT에서 현재 로그인한 사번을 추출하여 본인의 정산 정보만 조회합니다.
- 사원 이름, 부서, 사번, 현재 예상 환급액을 대시보드에 표시합니다.
- PDF 업로드, 부양가족 등록, 수동 영수증 제출 페이지로 이동할 수 있는 메뉴를 제공합니다.
- 관리자 전용 메뉴로 영수증 결재함, 사원 정보 일괄 등록, 정산 최종 마감 화면을 제공합니다.
- 정산 상태가 `SUBMITTED` 또는 `FINALIZED`인 경우 최종 제출 버튼을 비활성화합니다.

#### 3. 부양가족 인적공제 등록

- `YearTax`와 `Dependent`는 1:N 관계로 구성됩니다.
- 사원이 부양가족을 등록하면 `Dependent` 테이블에 저장됩니다.
- 동일 사번과 동일 주민등록번호 조합의 중복 등록을 방지합니다.
- 부양가족 1명 등록 시 `YearTax.resultAmount`에 1,500,000원이 자동 반영됩니다.
- 정산 상태가 `SUBMITTED` 또는 `FINALIZED`인 경우 추가 등록을 차단합니다.

#### 4. 국세청 간소화 PDF 업로드 및 공제 금액 반영

- 사원이 PDF 파일을 업로드하면 서버에서 파일 형식을 검증합니다.
- 현재는 테스트용 Mock 파싱 로직을 통해 의료비, 교육비, 신용카드 공제 금액을 계산합니다.
- 계산된 PDF 공제 금액은 `pdfDeductionAmount`에 저장되고 `resultAmount`에 누적됩니다.
- 이미 PDF 공제 내역이 반영된 경우 중복 업로드를 차단합니다.
- 추후 PDFBox 기반 실제 PDF 텍스트 파싱 로직으로 교체할 수 있도록 Service 구조로 분리했습니다.

#### 5. 수동 영수증 증빙 업로드

- 사원이 누락된 영수증 파일을 JPG, PNG, PDF 형식으로 업로드할 수 있습니다.
- 업로드된 파일은 서버의 `uploads/evidences` 경로에 UUID 파일명으로 저장됩니다.
- DB에는 원본 파일명, 저장 파일 경로, 업로드 시간, 금액, 카테고리, 상태값이 저장됩니다.
- 영수증 최초 상태는 `PENDING`입니다.
- 수동 영수증은 업로드 즉시 환급액에 반영하지 않고, 인사담당자 승인 후 반영됩니다.

#### 6. 인사담당자 영수증 결재

- 인사담당자는 `PENDING` 상태의 영수증 목록을 조회할 수 있습니다.
- 승인 시 영수증 상태가 `APPROVED`로 변경되고, 해당 금액이 사원의 `resultAmount`에 반영됩니다.
- 반려 시 영수증 상태가 `REJECTED`로 변경되며 반려 사유가 저장됩니다.
- 반려된 경우 사원의 정산 상태를 다시 `PROCESSING`으로 돌려 수정 제출이 가능하도록 합니다.

#### 7. 정산 최종 제출 및 마감 프로세스

- 사원은 모든 자료 입력을 마친 뒤 최종 제출할 수 있습니다.
- 최종 제출 시 `YearTax.status`가 `PROCESSING`에서 `SUBMITTED`로 변경됩니다.
- `SUBMITTED` 상태에서는 PDF 업로드, 부양가족 등록, 영수증 업로드 등 쓰기 API가 차단됩니다.
- 인사담당자는 제출 완료된 정산을 검토한 뒤 `FINALIZED`로 최종 마감할 수 있습니다.
- `FINALIZED` 상태에서는 더 이상 수정할 수 없습니다.

---

### 🧱 Architecture Highlights

- `ViewController`: Thymeleaf 화면 이동 담당
- `LoginController`: 로그인, 비밀번호 변경, JWT 발급 담당
- `DashboardController`: 사원 대시보드 데이터 조회 담당
- `DependentController`: 부양가족 등록 및 인적공제 반영 담당
- `PdfController`: PDF 업로드 및 공제 금액 반영 담당
- `EvidenceController`: 수동 영수증 업로드, 승인, 반려 담당
- `HrAdminController`: 사원 CSV 일괄 등록 담당
- `TaxSubmissionController`: 최종 제출 및 관리자 마감 처리 담당

---

### 🔐 Status Flow

YearTax status:

```text
PROCESSING → SUBMITTED → FINALIZED

ManualEvidence status:

```text
PENDING -> APPROVED
PENDING -> REJECTED

▶️ How to Run

### 🚀 [Update] 사내 연말정산 시스템 실무 고도화 및 인증/대시보드 연동 완료
1. 🏢 B2B SaaS 패키지 최적화 (아키텍처 설계)

단일 엔티티 기반 인증 통합: 복잡한 인사(HR) 시스템 연동 비용을 줄이기 위해 별도의 User 테이블을 생성하지 않고, YearTax 테이블을 인증(로그인)과 정산 데이터 통합 테이블로 활용하도록 아키텍처를 재설계했습니다.


멀티 테넌시(Multi-tenancy) 확장성 고려: 추후 다중 기업을 대상으로 한 SaaS 서비스 확장을 대비하여 기업을 구분할 수 있는 단일 테이블 격리 구조 기반을 마련했습니다.

2. 🔐 보안 및 계정 통제 고도화

BCrypt 암호화 적용: 비밀번호 평문 저장을 방지하기 위해 BCryptPasswordEncoder를 도입하여 DB 내 비밀번호를 안전하게 해시 암호화 처리했습니다.


스마트 마이그레이션(Fallback) 로직: LoginService에 기존 평문 데이터(rawPassword.equals)로 로그인 시, 자동으로 BCrypt 암호화로 변환 후 DB를 갱신(UPDATE)해 주는 실무형 마이그레이션 코드를 적용했습니다.


입력값 정제: 비밀번호 복사/붙여넣기 시 발생하는 공백 에러를 원천 차단하기 위해 rawPassword.strip() 등 입력값 전처리 로직을 추가했습니다.

3. 📊 실시간 환급액 대시보드 연동 (Front-Back 통신)

REST API 설계: 메인 대시보드에서 특정 사원의 현재 환급액 데이터를 JSON 형태로 반환하는 DashboardController(/api/dashboard/{jobId})를 구축했습니다.


비동기 렌더링 (Ajax + Chart.js): 백엔드에서 받아온 RESULT_AMOUNT 데이터를 바탕으로 Chart.js 도넛 그래프가 실시간으로 렌더링되도록 프론트엔드 비동기 통신(Ajax)을 완벽하게 연동했습니다.

4. 🛠️ 핵심 트러블슈팅 및 DB 무결성 확보
JPA/Oracle DB 제약조건 충돌 해결:

기존 데이터가 존재하는 상태에서 role 컬럼 타입을 변경 시도할 때 발생한 ORA-01439 및 ORA-01463 에러를, 테이블 초기화 및 안전한 VARCHAR2 타입 수정을 통해 완벽히 해결했습니다.

문자열('USER')을 숫자형 컬럼에 넣으려다 발생한 ORA-01722 에러를 디버깅하여 컬럼 설정을 정상화했습니다.


IS_FIRST_LOGIN 및 PDF_DEDUCTION_AMOUNT 등 필수 컬럼의 NOT NULL(ORA-01400) 제약조건 위반을 방어하기 위해 완벽한 디폴트 값이 포함된 샘플 INSERT 스크립트를 재구성했습니다.

API 통신 및 컴파일러 에러 해결:

Spring Boot 컨트롤러 파라미터 매핑 에러(500 Error)를 @PathVariable("jobId") 명시를 통해 컴파일러 옵션 의존성 없이 해결했습니다.

프론트엔드 Ajax 요청 시 발생한 404 Not Found 에러를 URL 경로 슬래시(/) 누락 분석을 통해 신속히 바로잡았습니다.

차트가 그려지지 않는 타이밍 이슈를, 데이터 수신 성공 직후(비동기 콜백 내부)에 캔버스를 초기화하고 new Chart()를 생성하도록 강제 렌더링 로직을 추가하여 해결했습니다.


1. Oracle DB 실행 및 접속 확인
2. application.properties 에서 DB 접속 정보 설정
3. Spring Boot 애플리케이션 실행
4. 브라우저에서 접속
