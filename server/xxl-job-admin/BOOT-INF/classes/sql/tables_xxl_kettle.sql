-- XXL-JOB Kettle 模块建表脚本
-- 适用: MySQL 5.7+ / 8.0+

CREATE TABLE IF NOT EXISTS `xxl_kettle_group` (
    `id`          int(11)     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_name`  varchar(100) NOT NULL COMMENT '分组名称（即存储子目录名）',
    `group_desc`  varchar(255) DEFAULT NULL COMMENT '分组描述',
    `add_time`    datetime    DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime    DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kettle文件分组表';

CREATE TABLE IF NOT EXISTS `xxl_kettle_file` (
    `id`          int(11)     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id`    int(11)     NOT NULL COMMENT '关联分组ID',
    `file_name`   varchar(255) NOT NULL COMMENT '原始文件名',
    `file_type`   varchar(10)  NOT NULL COMMENT '文件类型：KTR/KJB',
    `file_path`   varchar(512) NOT NULL COMMENT '服务器存储相对路径',
    `file_size`   bigint(20)  NOT NULL DEFAULT '0' COMMENT '文件大小（字节）',
    `version`     int(11)     NOT NULL DEFAULT '1' COMMENT '版本号（同名覆盖时自增）',
    `job_id`      int(11)     DEFAULT NULL COMMENT '关联xxl_job_info.id',
    `remark`      varchar(255) DEFAULT NULL COMMENT '备注',
    `add_time`    datetime    DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime    DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `i_group` (`group_id`),
    KEY `i_type`  (`file_type`),
    KEY `i_name`  (`file_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kettle文件管理表';

COMMIT;
