-- 将 user_layout.layout_type 与设置页 Tab key 对齐
-- preview → reading；notification → advanced；backup → data
-- 若目标 type 已存在则跳过（避免 UK 冲突）

UPDATE user_layout u
LEFT JOIN user_layout exist ON exist.user_id = u.user_id AND exist.layout_type = 'reading' AND exist.is_deleted = 0
SET u.layout_type = 'reading', u.updated_at = NOW()
WHERE u.layout_type = 'preview' AND u.is_deleted = 0 AND exist.id IS NULL;

UPDATE user_layout u
LEFT JOIN user_layout exist ON exist.user_id = u.user_id AND exist.layout_type = 'advanced' AND exist.is_deleted = 0
SET u.layout_type = 'advanced', u.updated_at = NOW()
WHERE u.layout_type = 'notification' AND u.is_deleted = 0 AND exist.id IS NULL;

UPDATE user_layout u
LEFT JOIN user_layout exist ON exist.user_id = u.user_id AND exist.layout_type = 'data' AND exist.is_deleted = 0
SET u.layout_type = 'data', u.updated_at = NOW()
WHERE u.layout_type = 'backup' AND u.is_deleted = 0 AND exist.id IS NULL;
