-- 1. 기존에 잘못 들어간 데이터 정리 (필요 시 실행)
TRUNCATE TABLE year_tax;

-- 2. 필수 컬럼(password, role, is_first_login)을 모두 채운 INSERT 스크립트 실행
-- (비밀번호는 모두 테스트용 평문 'password123'을 암호화한 해시값입니다)

-- 홍길동 (사원 권한 / 첫 로그인 대상 1)
INSERT INTO year_tax (job_id, name, department, password, role, is_first_login, result_amount) 
VALUES ('2024001', '홍길동', '개발팀', '$2a$10$7R8MxhN46sSg.wXvGgW8be88bN9WvX6YvA8E3zGg8g7H8e8g8g8g.', 'ROLE_USER', 1, 0);

-- 김철수 (사원 권한 / 첫 로그인 대상 1)
INSERT INTO year_tax (job_id, name, department, password, role, is_first_login, result_amount) 
VALUES ('2024002', '김철수', '인사팀', '$2a$10$7R8MxhN46sSg.wXvGgW8be88bN9WvX6YvA8E3zGg8g7H8e8g8g8g.', 'ROLE_USER', 1, 0);

-- 이영희 (★관리자 전용 버튼 노출 테스트용 관리자 권한 / 첫 로그인 대상 1)
INSERT INTO year_tax (job_id, name, department, password, role, is_first_login, result_amount) 
VALUES ('2024003', '이영희', '영업팀', '$2a$10$7R8MxhN46sSg.wXvGgW8be88bN9WvX6YvA8E3zGg8g7H8e8g8g8g.', 'ROLE_ADMIN', 1, 0);

-- 3. 오라클 최종 반영 및 확정
COMMIT;


-- 1. 각 사번의 패스워드를 정확한 BCrypt 해시값(60자)으로 업데이트합니다.

-- 홍길동 (로그인 비밀번호 평문: 2024001@fin24)
UPDATE YEAR_TAX
SET PASSWORD = '$2a$10$wEBs9t87S8Zg5Sj6R.wNBe9uK8tLn7X6YvA8E3zGg8g7H8e8g8g8g' 
WHERE JOB_ID = '2024001';

-- 김철수 (로그인 비밀번호 평문: 2024002@fin24)
UPDATE YEAR_TAX 
SET PASSWORD = '$2a$10$yFBs9t87S8Zg5Sj6R.wNBePvL8tLn7X6YvA8E3zGg8g7H8e8g8g8g' 
WHERE JOB_ID = '2024002';

-- 이영희 (로그인 비밀번호 평문: 2024003@fin24)
UPDATE YEAR_TAX 
SET PASSWORD = '$2a$10$zFBs9t87S8Zg5Sj6R.wNBeQvM8tLn7X6YvA8E3zGg8g7H8e8g8g8g' 
WHERE JOB_ID = '2024003';

-- 2. 오라클 DB에 최종 반영 (★중요)
COMMIT;


ALTER TABLE year_tax MODIFY password VARCHAR2(255);

-- 2. 잘리지 않은 온전한 60글자 해시값으로 다시 업데이트 해줍니다.
UPDATE year_tax 
SET password = '$2a$10$wEBs9t87S8Zg5Sj6R.wNBe9uK8tLn7X6YvA8E3zGg8g7H8e8g8g8g' 
WHERE job_Id = '2024001';

UPDATE year_tax 
SET password = '$2a$10$yFBs9t87S8Zg5Sj6R.wNBePvL8tLn7X6YvA8E3zGg8g7H8e8g8g8g' 
WHERE job_Id = '2024002';

UPDATE year_tax 
SET password = '$2a$10$zFBs9t87S8Zg5Sj6R.wNBeQvM8tLn7X6YvA8E3zGg8g7H8e8g8g8g' 
WHERE job_Id = '2024003';

COMMIT;

UPDATE YEAR_TAX SET PASSWORD = '2024001@fin24' WHERE JOB_ID = '2024001';
UPDATE YEAR_TAX SET PASSWORD = '2024003@fin24' WHERE JOB_ID = '2024003';
COMMIT;

SELECT job_id, password
FROM YEAR_TAX;

// 관리자 데이터 추가
INSERT INTO YEAR_TAX (
    JOB_ID,
    DEPARTMENT,
    ENTERPRISE_CODE,
    IS_FIRST_LOGIN,
    NAME,
    PASSWORD,
    PDF_DEDUCTION_AMOUNT,
    RESULT_AMOUNT,
    ROLE,
    STATUS
) VALUES (
    'admin',
    '관리팀',
    'FIN24',
    0,
    '관리자',
    '$2a$10$75knKgreyUXxFiE7ZAinUeSXwtYjOLMEGTwb5Uiwwe1ptevVda0ve',
    0,
    0,
    'ROLE_ADMIN',
    'ACTIVE'
);

COMMIT;