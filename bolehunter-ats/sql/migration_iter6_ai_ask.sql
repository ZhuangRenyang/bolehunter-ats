-- 迭代20：AI 找人（RAG 候选人问答）
-- 1) 候选人简历全文列：导入时保存提取文本，作为 AI 检索语料
SET NAMES utf8mb4;

ALTER TABLE candidate ADD COLUMN resume_text TEXT NULL COMMENT '简历全文文本（AI 检索语料）' AFTER resume_file;
-- 2) 候选人文档向量（OpenAI 兼容 embeddings 预计算，JSON 数组；未生成时检索降级关键词）
ALTER TABLE candidate ADD COLUMN ai_embedding JSON NULL COMMENT '候选人文档向量（AI 检索用）' AFTER resume_text;
-- 3) AI 问答功能开关（默认关）
INSERT INTO feature_flag (flag_key, flag_name, description, enabled)
VALUES ('ai_ask', 'AI 找人问答', '自然语言搜索候选人：检索知识库并由大模型归纳回答', 0)
ON DUPLICATE KEY UPDATE flag_name = VALUES(flag_name);
