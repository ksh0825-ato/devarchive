-- 맨 윗줄에 추가
SET FOREIGN_KEY_CHECKS = 0;

-- -- 1. 유저 데이터 생성 (Account)
-- -- 비밀번호는 평문으로 작성했으나, 실제 Spring Security 사용 시에는 암호화된 값을 넣어야 합니다.
-- INSERT INTO account (username, password, email, nickname, role, created_at) 
-- VALUES ('user1', '$2a$10$w8.9i2P9J.q6eR1iP5Y4u.k.r3Q1xL.aW1jZ4H5X6J7K8L9M0N1O2', 'user1@test.com', '테스터1', 'ROLE_USER', NOW());
-- --- pw : 1234

-- INSERT INTO account (username, password, email, nickname, role, created_at) 
-- VALUES ('company1', '$2a$10$w8.9i2P9J.q6eR1iP5Y4u.k.r3Q1xL.aW1jZ4H5X6J7K8L9M0N1O2', 'comp1@test.com', '삼성전자채용팀', 'ROLE_COMPANY', NOW());
-- --- pw : 1234

-- 2. 채용 공고 생성 (JobPost)
-- user_id 1번(user1)이 작성한 공고들
INSERT INTO job_post (user_id, job_post_title, company_name, position, description, url, deadline, created_at) 
VALUES (1, '백엔드 개발자 채용', '삼성전자', '백엔드', '대규모 트래픽 처리를 위한 서버 개발', 'https://samsung.com', '2026-07-20', NOW());

INSERT INTO job_post (user_id, job_post_title, company_name, position, description, url, deadline, created_at) 
VALUES (1, '프론트엔드 인턴', '네이버', '프론트엔드', 'React를 활용한 서비스 개발', 'https://naver.com', '2026-07-08', NOW());

INSERT INTO job_post (user_id, job_post_title, company_name, position, description, url, deadline, created_at) 
VALUES (1, '데이터 엔지니어', '카카오', '데이터', '빅데이터 파이프라인 구축', 'https://kakao.com', '2026-08-01', NOW());

-- 3. 학습 기록 생성 (Article)
-- 공고와 연결된 학습 기록
INSERT INTO article (user_id, job_id, title, content, view_count, created_at, updated_at) 
VALUES (1, 1, '삼성전자 백엔드 공고 분석', '대규모 트래픽 아키텍처 학습 필요.', 5, NOW(), NOW());

INSERT INTO article (user_id, job_id, title, content, view_count, created_at, updated_at) 
VALUES (1, 2, '네이버 인턴 준비사항', 'React Query와 상태 관리 전략 정리.', 12, NOW(), NOW());

-- 공고와 연결되지 않은 자유 학습 기록
INSERT INTO article (user_id, job_id, title, content, view_count, created_at, updated_at) 
VALUES (1, NULL, 'Java Stream API 마스터하기', 'Stream API 성능 이슈 및 최적화.', 3, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;