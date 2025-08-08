-- 기존 스키마에서 User Service 관련 테이블만
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE p_time
(
    p_time_id  UUID PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_by VARCHAR(100) NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);

CREATE TABLE p_admins
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(20) UNIQUE NOT NULL,
    email        VARCHAR(255)       NOT NULL,
    password     VARCHAR(255)       NOT NULL,
    phone_number VARCHAR(18),
    position     VARCHAR(50),
    p_time_id    UUID               NOT NULL,
    CONSTRAINT fk_admins_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_customer
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(20) UNIQUE NOT NULL,
    nickname     VARCHAR(100),
    email        VARCHAR(255),
    password     VARCHAR(255)       NOT NULL,
    phone_number VARCHAR(18),
    points       INT,
    p_time_id    UUID               NOT NULL,
    CONSTRAINT fk_p_users_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);

CREATE TABLE p_managers
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(20) UNIQUE NOT NULL,
    email        VARCHAR(255)       NOT NULL,
    password     VARCHAR(255)       NOT NULL,
    phone_number VARCHAR(18),
    store_id     UUID,
    p_time_id    UUID               NOT NULL,
    CONSTRAINT fk_managers_p_time FOREIGN KEY (p_time_id) REFERENCES p_time (p_time_id)
);