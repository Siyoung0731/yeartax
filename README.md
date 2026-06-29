# 📊 사내 연말정산 대시보드 시스템 (Fintax)

## 📝 프로젝트 소개
사원들이 자신의 연말정산 예상 환급액과 진행 현황을 직관적으로 확인할 수 있는 웹 대시보드입니다.
RESTful API 구조를 기반으로 프론트엔드와 백엔드를 분리하였으며, JWT(JSON Web Token)를 활용하여 안전한 무상태(Stateless) 인증 시스템을 구현했습니다.

## 🛠 기술 스택
- **Backend:** Java, Spring Boot, Spring Security, JWT (io.jsonWebToken)
- **Frontend:** HTML5, CSS3, JavaScript(JQuery, Ajax), Bootstrap, Chart.js
- **Database:** Oracle SQL

## 🔐 핵심 보안 및 인증 흐름 (Security & Network Flow)
단순한 세션 방식이 아닌, 실무 표준에 맞춘 토큰 기반 인증 체계를 구축했습니다.
1. **로그인 API (`/api/login`):** 클라이언트가 전송한 DTO 기반의 자격 증명을 검증하고, HS256 알고리즘으로 서명된 JWT 발급.
2. **토큰 저장:** 탈취 위험을 최소화하기 위해 브라우저 `localStorage`에 토큰 보관.
3. **인증 필터 (`JwtAuthenticationFilter`):** 클라이언트의 모든 API 요청 시 HTTP `Authorization` 헤더에 포함된 Bearer 토큰을 가로채어 유효성 및 서명을 검증. 미인증 접근 시 401/403 에러 반환.

## 🚀 주요 기능
- **대시보드시각화:** Chart.js를 활용하여 예상 환급액을 도넛 차트로 구현.
- **REST APT 통신:** @RequestBody와 전용 DTO 패키지를 활용한 깔끔한 JSON 데이터 송수신.
- **안전한 로그아웃:** 클라이언트 측 토큰 즉각 폐기 및 접근 권한 차단 로직 적용.

## 💡 트러블슈팅 (Troubleshooting)
- **문제1: API 통신 중 403 Forbidden 및 401 Unauthorized 에러 지속 발생**
  - **원인:** Spring Security 설정에서 인가 정책 충돌 및 프론트엔드의 데이터 패키징 누락
  - **해결:** SecurityConfig의 `authorizeHttpRequests` 경로를 재설정하고, Ajax 요청 시 데이터를 `JSON.stringify()` 로 변환하여 전송하도록 수정 후 성공적으로 인증 헤더 통과.
- **문제2: 컨트롤러에서 DTO 객체의 필드값이 null로 매핑되는 현상**
  - **원인:** @RequestBody 누락 및 HTML 태그의 ID 선택자(`#`) 누락으로 인한 빈 데이터 전송.
  - **해결:** 프론트엔드 JQuery 선택자 오타를 수정하고, 백엔드에 기본 생성자와 Getter/Setter를 명시한 전용 DTO 클래스를 도입하여 완벽한 데이터 바인딩 성공.
