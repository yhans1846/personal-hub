-- Study plan table refactor: add source/author/url/remark, drop goal/progress
-- Status 3 label becomes "paused" (app-level); numeric codes unchanged.

ALTER TABLE `study_plan`
    ADD COLUMN `source` varchar(100) NULL COMMENT 'source' AFTER `name`,
    ADD COLUMN `author` varchar(100) NULL COMMENT 'author' AFTER `source`,
    ADD COLUMN `url` varchar(500) NULL COMMENT 'url' AFTER `author`,
    ADD COLUMN `remark` text NULL COMMENT 'remark' AFTER `url`;

UPDATE `study_plan`
SET `remark` = COALESCE(`remark`, `goal`)
WHERE `goal` IS NOT NULL AND `goal` != '';

ALTER TABLE `study_plan`
    DROP COLUMN `goal`,
    DROP COLUMN `progress`;
