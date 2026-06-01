CREATE TABLE IF NOT EXISTS xxl_kettle_group (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(64) NOT NULL,
    group_desc VARCHAR(255),
    add_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS xxl_kettle_file (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(8) NOT NULL,
    file_path VARCHAR(512),
    file_size BIGINT DEFAULT 0,
    version INT DEFAULT 1,
    job_id INT,
    remark VARCHAR(512),
    add_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_group_name (group_id, file_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
