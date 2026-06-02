-- ============================================================
-- Database: kanban_db
-- Online Team Collaboration Kanban Board
-- ============================================================
CREATE DATABASE IF NOT EXISTS kanban_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kanban_db;

-- ============================================================
-- 1. user - User accounts
-- ============================================================
CREATE TABLE user (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(100) DEFAULT NULL,
    avatar        VARCHAR(255) DEFAULT NULL COMMENT 'URL of avatar image',
    display_name  VARCHAR(100) DEFAULT NULL COMMENT 'Display name shown in boards',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 2. board - Kanban boards
-- ============================================================
CREATE TABLE board (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    owner_id    BIGINT       NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_board_owner (owner_id),
    CONSTRAINT fk_board_owner FOREIGN KEY (owner_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. board_member - Board membership with roles
-- ============================================================
CREATE TABLE board_member (
    id        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    board_id  BIGINT       NOT NULL,
    user_id   BIGINT       NOT NULL,
    role      VARCHAR(20)  NOT NULL DEFAULT 'EDITOR' COMMENT 'OWNER | EDITOR | VIEWER',
    joined_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_board_user (board_id, user_id),
    INDEX idx_board_member_user (user_id),
    CONSTRAINT fk_member_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user  FOREIGN KEY (user_id)  REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. board_list - Lists/columns within a board
-- NOTE: Table name is 'board_list' to avoid SQL reserved word 'list'
-- ============================================================
CREATE TABLE board_list (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    board_id   BIGINT       NOT NULL,
    name       VARCHAR(100) NOT NULL,
    position   DOUBLE       NOT NULL COMMENT 'Float position for ordering',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_list_board (board_id),
    CONSTRAINT fk_list_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 5. card - Cards within a list
-- ============================================================
CREATE TABLE card (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    list_id        BIGINT       NOT NULL,
    title          VARCHAR(255) NOT NULL,
    description_md TEXT         DEFAULT NULL COMMENT 'Markdown description',
    priority       VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM' COMMENT 'HIGH | MEDIUM | LOW',
    due_date       DATETIME     DEFAULT NULL,
    position       DOUBLE       NOT NULL COMMENT 'Float position for ordering',
    assignee_id    BIGINT       DEFAULT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_card_list (list_id),
    INDEX idx_card_assignee (assignee_id),
    INDEX idx_card_due_date (due_date),
    INDEX idx_card_priority (priority),
    CONSTRAINT fk_card_list     FOREIGN KEY (list_id)     REFERENCES board_list(id) ON DELETE CASCADE,
    CONSTRAINT fk_card_assignee FOREIGN KEY (assignee_id) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 6. card_label - Labels/tags on cards
-- ============================================================
CREATE TABLE card_label (
    id      BIGINT       AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT       NOT NULL,
    name    VARCHAR(50)  NOT NULL,
    color   VARCHAR(7)   NOT NULL COMMENT 'Hex color like #FF5733',
    INDEX idx_label_card (card_id),
    CONSTRAINT fk_label_card FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 7. comment - Comments on cards
-- ============================================================
CREATE TABLE comment (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    card_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    content    TEXT         NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comment_card (card_id),
    INDEX idx_comment_user (user_id),
    CONSTRAINT fk_comment_card FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 8. attachment - File attachments on cards
-- ============================================================
CREATE TABLE attachment (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    card_id     BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    filename    VARCHAR(255) NOT NULL,
    file_path   VARCHAR(500) NOT NULL COMMENT 'Storage path on server',
    file_size   BIGINT       NOT NULL COMMENT 'Size in bytes',
    mime_type   VARCHAR(100) DEFAULT NULL,
    uploaded_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_attachment_card (card_id),
    CONSTRAINT fk_attachment_card FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE,
    CONSTRAINT fk_attachment_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 9. activity_log - Board activity audit trail
-- ============================================================
CREATE TABLE activity_log (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    board_id    BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    action      VARCHAR(50)  NOT NULL COMMENT 'CREATE_BOARD | UPDATE_BOARD | DELETE_BOARD | CREATE_LIST | UPDATE_LIST | DELETE_LIST | MOVE_LIST | CREATE_CARD | UPDATE_CARD | DELETE_CARD | MOVE_CARD | ADD_COMMENT | ADD_ATTACHMENT | ADD_MEMBER | REMOVE_MEMBER | CHANGE_ROLE',
    target_type VARCHAR(50)  DEFAULT NULL COMMENT 'BOARD | LIST | CARD | COMMENT | ATTACHMENT | MEMBER',
    target_id   BIGINT       DEFAULT NULL,
    detail      VARCHAR(500) DEFAULT NULL COMMENT 'Human-readable description',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_activity_board (board_id),
    INDEX idx_activity_user (user_id),
    INDEX idx_activity_time (created_at),
    CONSTRAINT fk_activity_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_user  FOREIGN KEY (user_id)  REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Schema upgrades (for Docker: tables created without status/priority)
-- ============================================================
ALTER TABLE board ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'TODO';
ALTER TABLE board_list ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'TODO';
ALTER TABLE board_list ADD COLUMN priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM';
ALTER TABLE card ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'TODO';
