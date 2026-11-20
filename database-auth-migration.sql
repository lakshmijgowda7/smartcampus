USE smart_campus_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(80) NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_email (email),
    KEY idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @smart_campus_username_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'username'
);
SET @smart_campus_username_column_sql = IF(
    @smart_campus_username_column_exists = 0,
    'ALTER TABLE users ADD COLUMN username VARCHAR(80) NULL AFTER id',
    'SELECT 1'
);
PREPARE smart_campus_username_column_stmt
    FROM @smart_campus_username_column_sql;
EXECUTE smart_campus_username_column_stmt;
DEALLOCATE PREPARE smart_campus_username_column_stmt;

SET @smart_campus_username_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND index_name = 'uq_users_username'
);
SET @smart_campus_username_index_sql = IF(
    @smart_campus_username_index_exists = 0,
    'CREATE UNIQUE INDEX uq_users_username ON users (username)',
    'SELECT 1'
);
PREPARE smart_campus_username_index_stmt
    FROM @smart_campus_username_index_sql;
EXECUTE smart_campus_username_index_stmt;
DEALLOCATE PREPARE smart_campus_username_index_stmt;
