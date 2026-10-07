-- Disable FK checks during initialization
SET FOREIGN_KEY_CHECKS = 0;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    lock_until DATETIME NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
) ENGINE=InnoDB;

-- Cases table
CREATE TABLE IF NOT EXISTS cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_number VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    investigator_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (investigator_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Case shares table
CREATE TABLE IF NOT EXISTS case_shares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id BIGINT NOT NULL,
    shared_with_user_id BIGINT NOT NULL,
    shared_by_user_id BIGINT NOT NULL,
    shared_at TIMESTAMP NOT NULL,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (shared_with_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (shared_by_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Screenshots table
CREATE TABLE IF NOT EXISTS screenshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    md5 VARCHAR(64) NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    perceptual_hash VARCHAR(64),
    file_size BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    uploaded_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP NULL,
    error_message TEXT NULL,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Analysis results table
CREATE TABLE IF NOT EXISTS analysis_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screenshot_id BIGINT NOT NULL UNIQUE,
    verdict VARCHAR(20) NOT NULL,
    authenticity_score INT,
    verdict_explanation VARCHAR(1000),
    ela_image_file_name VARCHAR(255),
    metadata_json JSON,
    tamper_heuristics_json JSON,
    ocr_text TEXT,
    analyzed_at TIMESTAMP NOT NULL,
    FOREIGN KEY (screenshot_id) REFERENCES screenshots(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Extracted evidence table (aligned with ExtractedEvidence entity fields)
CREATE TABLE IF NOT EXISTS extracted_evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screenshot_id BIGINT NOT NULL,
    url VARCHAR(500),
    email VARCHAR(255),
    phone_number VARCHAR(50),
    ip_address VARCHAR(45),
    amount VARCHAR(100),
    date VARCHAR(100),
    evidence_type VARCHAR(50),
    confidence VARCHAR(20),
    raw_text TEXT,
    extracted_at TIMESTAMP NOT NULL,
    FOREIGN KEY (screenshot_id) REFERENCES screenshots(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Case notes table
CREATE TABLE IF NOT EXISTS case_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    note TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    description VARCHAR(255),
    ip_address VARCHAR(45),
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Login attempts table
CREATE TABLE IF NOT EXISTS login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    success BOOLEAN NOT NULL,
    timestamp TIMESTAMP NOT NULL
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;