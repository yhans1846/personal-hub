-- Re-add progress column for study_plan Product UI
ALTER TABLE `study_plan`
    ADD COLUMN `progress` int NOT NULL DEFAULT 0 COMMENT 'progress 0-100' AFTER `remark`;
