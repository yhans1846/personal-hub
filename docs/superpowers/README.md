# docs/superpowers — 历史设计与实现计划

> **非唯一真相源（非 SSOT）。**  
> 现行规范与契约以仓库根索引为准：`docs/PROJECT.md`、`TECH_STACK.md`、`API.md`、`DATABASE.md`、`STYLE_GUIDE.md`、`PAGE_SPEC.md`、`DEPLOYMENT.md`、`CHANGELOG.md`。

本目录保存会话中产生的 **设计规格（specs/）** 与 **实现计划（plans/）**，便于追溯决策过程。

## 使用约定

1. 实现或改行为前，先查 canonical docs + `CHANGELOG`，再视需要回看本目录。
2. 文首标有「已取代 / 已落地 / 历史」的文件仅作档案，路径与模块名可能已过时（如旧 Maven `ph-knowledge`、已删的 `LIST_PAGE_SPEC.md`）。
3. 新增功能仍可写入本目录；落地后须同步更新对应主文档与 `CHANGELOG`，并在规格文首标注状态。

## 已知取代关系（摘要）

| 档案 | 状态 |
|------|------|
| `specs/2026-07-17-login-split-slider-captcha-design.md` | 滑动拼图方案已被书架归位验证码 + 产品化登录 UI 取代 |
| `specs/2026-07-17-login-product-ui-redesign.md` | 已落地（现行登录页） |
| `plans/2026-07-17-backend-module-merge-ph-biz.md` | 已落地（Maven 4 模块 + `ph-biz`） |
| 多数 `*LIST_PAGE_SPEC*` / `PROJECT_AUDIT*` 引用 | 文档已合并/删除，以 `PAGE_SPEC.md` 为准 |
