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
