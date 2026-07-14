-- Personal Hub V4 - Profile 个人资料模块
-- 为 sys_user 表添加个人资料扩展字段

ALTER TABLE sys_user
    ADD COLUMN gender     tinyint      DEFAULT 0  COMMENT '性别 0-保密 1-男 2-女' AFTER email,
    ADD COLUMN birthday   date         DEFAULT NULL COMMENT '出生日期' AFTER gender,
    ADD COLUMN phone      varchar(20)  DEFAULT NULL COMMENT '手机号' AFTER birthday,
    ADD COLUMN country    varchar(50)  DEFAULT NULL COMMENT '国家' AFTER phone,
    ADD COLUMN province   varchar(50)  DEFAULT NULL COMMENT '省份' AFTER country,
    ADD COLUMN city       varchar(50)  DEFAULT NULL COMMENT '城市' AFTER province,
    ADD COLUMN district   varchar(50)  DEFAULT NULL COMMENT '区/县' AFTER city,
    ADD COLUMN website    varchar(200) DEFAULT NULL COMMENT '个人网站' AFTER district,
    ADD COLUMN github     varchar(200) DEFAULT NULL COMMENT 'GitHub 地址' AFTER website,
    ADD COLUMN bio        varchar(500) DEFAULT NULL COMMENT '个人简介' AFTER github;
