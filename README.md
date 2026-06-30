### 🏢 사내 연말정산 시스템 (FinTax)

기업의 연말정산 업무를 자동화하고 효율적으로 관리하기 위한 사내 전용 웹 애플리케이션입니다.
기존의 복잡한 서류 제출 방식을 개선하고, 인사팀(관리자)과 임직원(사용자) 모두에게 직관적이고 안전한 정산 프로세스를 제공합니다.

🛠️ Tech Stack

Backend: Spring Boot, Spring Security, Spring Data JPA

Database: Oracle DB (Free)

Frontend: HTML5, CSS3 (Bootstrap), JavaScript (jQuery, AJAX)

Security/Auth: JWT (JSON Web Token), BCrypt Password Encoder

🌟 Core Features & Business Logic

1. 🔐 B2B 최적화 계정 및 보안 시스템 (No Sign-up)

사내 시스템의 특성을 반영하여 불특정 다수의 '회원가입' 기능을 전면 폐지하고, 데이터의 정합성을 보장하는 중앙 통제식 계정 관리를 구현했습니다.

인사팀 CSV 일괄 등록: 관리자가 사원 정보(사번, 이름, 부서 등)를 CSV로 업로드하면 시스템이 자동으로 계정을 생성합니다.

초기 비밀번호 자동 발급: 계정 생성 시 사번 + 특정문자열 조합으로 초기 비밀번호를 자동 부여하며, 반드시 BCrypt로 암호화하여 DB에 저장합니다.

최초 로그인 감지 및 변경 강제: 사원이 초기 비밀번호로 최초 로그인(isFirstLogin == true) 시, 시스템 사용을 차단하고 비밀번호 변경 화면으로 강제 리다이렉트 시켜 보안을 강화합니다.

Stateless JWT 인증: 세션(Session)을 배제하고 JWT를 활용하여 API 통신을 수행합니다. 프론트엔드는 발급받은 토큰을 localStorage에 보관하고, 이후 모든 요청의 Authorization 헤더에 담아 전송합니다.

2. 👨‍👩‍👧‍👦 부양가족(인적공제) 등록 및 자동 계산

1:N 데이터 매핑: 사원(YearTax)과 부양가족(Dependent) 간의 일대다 관계를 JPA로 구성했습니다.

실시간 예상 환급액 반영: 부양가족이 등록될 때마다 세법 기준(1인당 기본공제 150만 원)을 적용하여 예상 환급액을 자동으로 재계산합니다.

3. 📄 국세청 간소화 PDF 및 수동 영수증 관리

국세청 PDF 파싱 및 데이터베이스 저장 (진행 중)

누락된 의료비/기부금 등 수동 영수증 제출 기능 제공

4. 💼 [관리자 전용] 영수증 결재 대기함 (Pending List)

보안 API 통신: 관리자 권한(/api/admin/)이 필요한 데이터 요청 시, 프론트엔드($.ajax)에서 JWT 토큰을 헤더에 동적으로 삽입하여 403 Forbidden을 방지합니다.

사원들이 제출한 수동 영수증을 검토하고 즉시 승인(Approve) 처리할 수 있는 비동기 대시보드를 제공합니다.

🏛️ Architecture Highlights

관심사의 분리 (Separation of Concerns): * ViewController (@Controller): 클라이언트의 화면 이동(View Routing)만을 전담.

ApiController (@RestController): 데이터 조회 및 비즈니스 로직(JSON 반환)만을 전담하여 유지보수성을 극대화.

통합 인증 테이블 구조: 별도의 User 테이블을 두지 않고 YearTax 엔티티를 '인증'과 '정산'의 통합 테이블로 활용하여 JOIN 연산을 최소화하고 성능을 높였습니다.

🚀 How to Run

Oracle DB (FREEPDB1) 구동 및 접속 확인

application.properties 내 DB 접속 정보(username, password) 세팅

Spring Boot Application 실행 (FinTaxApplication.java)

브라우저에서 http://localhost:9090/login 접속

Developed by [본인 이름/닉네임]
