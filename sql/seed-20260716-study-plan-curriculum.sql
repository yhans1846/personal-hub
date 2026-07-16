-- Seed study plans from spreadsheet (user_id=1)
-- Status: 0=待学习 1=进行中 2=已完成 3=已暂停

-- Ensure category tags
INSERT INTO tag (user_id, name, color, created_at, updated_at)
SELECT 1, 'Java', '#22c55e', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE user_id=1 AND name='Java');

INSERT INTO tag (user_id, name, color, created_at, updated_at)
SELECT 1, '服务器', '#3b82f6', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE user_id=1 AND name='服务器');

INSERT INTO tag (user_id, name, color, created_at, updated_at)
SELECT 1, '中间件', '#a855f7', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE user_id=1 AND name='中间件');

INSERT INTO tag (user_id, name, color, created_at, updated_at)
SELECT 1, '算法', '#f59e0b', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE user_id=1 AND name='算法');

INSERT INTO tag (user_id, name, color, created_at, updated_at)
SELECT 1, 'Python', '#ec4899', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE user_id=1 AND name='Python');

INSERT INTO tag (user_id, name, color, created_at, updated_at)
SELECT 1, 'Vibe Coding', '#14b8a6', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE user_id=1 AND name='Vibe Coding');

-- Soft-delete old sample plans that are not in the curriculum list
UPDATE study_plan SET is_deleted=1
WHERE user_id=1 AND is_deleted=0
  AND name IN ('Spring Boot 3 深入学习', '算法刷题 100 道', '阅读《代码整洁之道》', '英语口语提升计划', '1');

-- Upsert curriculum rows: delete same-name then insert fresh
DELETE FROM tag_rel WHERE entity_type='study_plan' AND entity_id IN (
  SELECT id FROM (SELECT id FROM study_plan WHERE user_id=1 AND name IN (
    '苍穹外卖','Maven','Spring AI','Docker','cicd','nginx','mq','数据结构与算法','python零基础','入门教程'
  )) t
);
DELETE FROM study_plan WHERE user_id=1 AND name IN (
  '苍穹外卖','Maven','Spring AI','Docker','cicd','nginx','mq','数据结构与算法','python零基础','入门教程'
);

INSERT INTO study_plan (user_id, name, source, author, url, remark, start_date, end_date, status, is_deleted, created_at, updated_at) VALUES
(1, '苍穹外卖', 'B站', '黑马',
 'https://www.bilibili.com/video/BV1TP411v7v6?vd_source=1e7d760d0540036329f08257a878b656&spm_id_from=333.788.videopod.episodes',
 '【黑马程序员Java项目实战《苍穹外卖》，最适合新手的SpringBoot+SSM的企业级Java项目实战】',
 '2025-10-20', '2025-12-28', 2, 0, NOW(), NOW()),
(1, 'Maven', 'B站', '黑马',
 'https://www.bilibili.com/video/BV1Ah411S7ZE?p=5&vd_source=1e7d760d0540036329f08257a878b656',
 '【黑马程序员Maven全套教程，maven项目管理从基础到高级，Java项目开发必会管理工具maven】',
 '2026-03-03', NULL, 3, 0, NOW(), NOW()),
(1, 'Spring AI', 'B站', '黑马', NULL, NULL, NULL, NULL, 0, 0, NOW(), NOW()),
(1, 'Docker', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NOW(), NOW()),
(1, 'cicd', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NOW(), NOW()),
(1, 'nginx', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NOW(), NOW()),
(1, 'mq', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NOW(), NOW()),
(1, '数据结构与算法', '网站', '代码随想录',
 'https://www.programmercarl.com/', NULL,
 '2025-08-08', NULL, 3, 0, NOW(), NOW()),
(1, 'python零基础', 'B站', '黑马',
 'https://www.bilibili.com/video/BV1qW4y1a7fU?p=40&vd_source=1e7d760d0540036329f08257a878b656',
 '【黑马程序员python零基础全套教程，8天python从入门到精通，学python看这套就够了】',
 '2026-04-21', '2026-06-14', 2, 0, NOW(), NOW()),
(1, '入门教程', 'B站', '尚硅谷',
 'https://www.bilibili.com/video/BV1RPET6tEp2?vd_source=1e7d760d0540036329f08257a878b656',
 '【零基础Vibe Coding教程，vibecoding实战，Claude Code+Codex+Cursor】',
 '2026-06-14', '2026-07-09', 2, 0, NOW(), NOW());

-- Bind tags
INSERT INTO tag_rel (tag_id, entity_type, entity_id, created_at)
SELECT t.id, 'study_plan', p.id, NOW()
FROM study_plan p
JOIN tag t ON t.user_id=1 AND t.name='Java'
WHERE p.user_id=1 AND p.is_deleted=0 AND p.name IN ('苍穹外卖','Maven','Spring AI');

INSERT INTO tag_rel (tag_id, entity_type, entity_id, created_at)
SELECT t.id, 'study_plan', p.id, NOW()
FROM study_plan p
JOIN tag t ON t.user_id=1 AND t.name='服务器'
WHERE p.user_id=1 AND p.is_deleted=0 AND p.name IN ('Docker','cicd');

INSERT INTO tag_rel (tag_id, entity_type, entity_id, created_at)
SELECT t.id, 'study_plan', p.id, NOW()
FROM study_plan p
JOIN tag t ON t.user_id=1 AND t.name='中间件'
WHERE p.user_id=1 AND p.is_deleted=0 AND p.name IN ('nginx','mq');

INSERT INTO tag_rel (tag_id, entity_type, entity_id, created_at)
SELECT t.id, 'study_plan', p.id, NOW()
FROM study_plan p
JOIN tag t ON t.user_id=1 AND t.name='算法'
WHERE p.user_id=1 AND p.is_deleted=0 AND p.name='数据结构与算法';

INSERT INTO tag_rel (tag_id, entity_type, entity_id, created_at)
SELECT t.id, 'study_plan', p.id, NOW()
FROM study_plan p
JOIN tag t ON t.user_id=1 AND t.name='Python'
WHERE p.user_id=1 AND p.is_deleted=0 AND p.name='python零基础';

INSERT INTO tag_rel (tag_id, entity_type, entity_id, created_at)
SELECT t.id, 'study_plan', p.id, NOW()
FROM study_plan p
JOIN tag t ON t.user_id=1 AND t.name='Vibe Coding'
WHERE p.user_id=1 AND p.is_deleted=0 AND p.name='入门教程';
