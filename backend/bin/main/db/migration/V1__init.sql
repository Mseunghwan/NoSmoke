-- V1__init.sql
-- NoSmoke 프로젝트 초기 스키마 정의
-- 작성된 Entity 클래스 기반으로 생성됨

-- 1. Users 테이블 (User.java)
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       point INT NOT NULL DEFAULT 0,
                       created_at DATETIME(6),
                       modified_at DATETIME(6),
                       CONSTRAINT uk_users_email UNIQUE (email)
);

-- 2. SmokingInfo 테이블 (SmokingInfo.java)
-- @Column(name="quitGoal") 처럼 명시된 컬럼명은 그대로 유지했습니다.
CREATE TABLE smoking_info (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              cigarette_type VARCHAR(100),
                              daily_consumption INT,
                              quit_start_date DATE,
                              target_date DATE,
                              quitGoal VARCHAR(255),  -- Entity의 @Column(name="quitGoal") 반영
                              created_at DATETIME(6),
                              modified_at DATETIME(6)
);

-- 3. QuitSurvey 테이블 (QuitSurvey.java)
-- additionalNotes는 @Column(columnDefinition = "TEXT") 반영
CREATE TABLE quit_survey (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             is_success BIT(1),      -- boolean 타입 매핑
                             stress_level INT,
                             stress_cause VARCHAR(255),
                             craving_level INT,
                             additional_notes TEXT,
                             created_at DATETIME(6),
                             modified_at DATETIME(6)
);

-- 4. MonkeyMessage 테이블 (MonkeyMessage.java)
-- User와 연관관계(@ManyToOne)가 있으므로 FK 제약조건 추가
CREATE TABLE monkey_message (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                content TEXT NOT NULL,
                                message_type VARCHAR(255) NOT NULL, -- ENUM 타입은 문자열로 저장
                                created_at DATETIME(6),
                                modified_at DATETIME(6),
                                CONSTRAINT fk_monkey_message_user FOREIGN KEY (user_id) REFERENCES users (id)
);