-- 迭代 31 修复：种子数据乱码修复（双重编码数据重写）
-- 执行方式：mysql --default-character-set=utf8mb4 bolehunter < fix_seed.sql
SET NAMES utf8mb4;

-- 1. tenant
UPDATE `tenant` SET `name`='默认团队', `contact`='管理员', `remark`='开源版默认租户' WHERE id=1;

-- 2. group（逐行 UPDATE，保留 group_permission 关联）
UPDATE `group` SET `info`='超级用户组' WHERE id=1;
UPDATE `group` SET `name`='JobSeeker(求职者)', `info`='求职者' WHERE id=2;
UPDATE `group` SET `name`='hr(招聘者)', `info`='招聘者' WHERE id=3;
UPDATE `group` SET `name`='admin(平台管理员)', `info`='平台管理员' WHERE id=4;
UPDATE `group` SET `name`='sysadmin(系统管理员)', `info`='系统管理员' WHERE id=5;
UPDATE `group` SET `name`='顾问(Consultant)', `info`='猎头顾问：管理本人客户/职位/候选人数据' WHERE id=6;
UPDATE `group` SET `name`='只读(Readonly)', `info`='只读角色：仅查看，不可修改' WHERE id=7;

-- 3. sys_dept（重建）
DELETE FROM `sys_dept`;
INSERT INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `sort`, `leader`) VALUES
(1, 0, '总部',   0, NULL),
(2, 1, '猎头一部', 1, NULL),
(3, 1, '猎头二部', 2, NULL),
(4, 1, '客户成功部', 3, NULL);

-- 4. sys_dict_type（重建）
DELETE FROM `sys_dict_type`;
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `remark`) VALUES
('客户等级', 'client_level', '客户公司分级'),
('所属行业', 'industry',     '客户/候选人行业'),
('推荐阶段', 'recommendation_stage', '推荐流程状态机（固定核心流程）'),
('候选人来源', 'candidate_source', '简历来源渠道'),
('职位状态', 'position_status', '职位生命周期状态'),
('沟通方式', 'communication_type', '沟通记录渠道');

-- 5. sys_dict_data（重建，全部 39 项业务字典）
DELETE FROM `sys_dict_data`;
INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_sort`) VALUES
('client_level', 'A级-重点客户', 'A', 1),
('client_level', 'B级-普通客户', 'B', 2),
('client_level', 'C级-潜在客户', 'C', 3),
('industry', 'IT/互联网/游戏', 'it',        1),
('industry', '医疗健康',       'medical',   2),
('industry', '智能制造',       'manufacture', 3),
('industry', '金融',           'finance',   4),
('industry', '教育',           'education', 5),
('industry', '出海/跨境电商',   'export',    6),
('industry', '其他',           'other',     99),
('recommendation_stage', '待推荐',   'PENDING',      1),
('recommendation_stage', '已推荐',   'RECOMMENDED',  2),
('recommendation_stage', '客户已读', 'CLIENT_READ',  3),
('recommendation_stage', '待面试',   'INTERVIEW_PENDING', 4),
('recommendation_stage', '面试中',   'INTERVIEWING', 5),
('recommendation_stage', '待Offer',  'OFFER_PENDING', 6),
('recommendation_stage', 'Offer',    'OFFERED',      7),
('recommendation_stage', '入职',     'ONBOARDED',    8),
('recommendation_stage', '回款',     'PAID',         9),
('recommendation_stage', '归档',     'ARCHIVED',     10),
('candidate_source', '主动投递',   'apply',    1),
('candidate_source', '猎头寻访',   'sourcing', 2),
('candidate_source', '内部推荐',   'referral', 3),
('candidate_source', '简历库激活', 'reactive', 4),
('position_status', '招聘中', 'OPEN',    1),
('position_status', '暂停',   'PAUSED',  2),
('position_status', '完成',   'DONE',    3),
('position_status', '关闭',   'CLOSED',  4),
('communication_type', '电话', 'phone', 1),
('communication_type', '微信', 'wechat', 2),
('communication_type', '邮件', 'email', 3),
('communication_type', '面谈', 'meeting', 4),
('communication_type', '备注', 'note', 5),
('client_status', '潜在客户', 'potential', 1),
('client_status', '签约合作', 'signing', 2),
('client_status', '暂停合作', 'suspended', 3),
('client_status', '流失',     'lost', 4),
('education', '大专', 'associate', 1),
('education', '本科', 'bachelor', 2);

-- 6. feature_flag（重建，含 iter6/iter7 追加的开关）
DELETE FROM `feature_flag`;
INSERT INTO `feature_flag` (`flag_key`, `flag_name`, `description`, `enabled`) VALUES
('resume_parse',    '简历解析', '导入简历后自动解析姓名/电话/邮箱等字段（迭代2）', 0),
('client_portal',   '客户门户', '客户HR只读门户入口（简易版）', 0),
('ai_assist',       'AI提效',   'AI简历摘要/人岗匹配/话术生成（迭代7）', 0),
('notify_mail',     '邮件通知', '任务到期与推荐反馈邮件提醒', 0),
('notify_wecom',    '企微通知', '企业微信机器人通知', 0),
('data_export',     '数据导出', '候选人/推荐记录导出 Excel', 1),
('candidate_dedup', '候选人查重', '手机号+邮箱组合查重提示', 1),
('ai_ask',          'AI 找人问答', '自然语言搜索候选人：检索知识库并由大模型归纳回答', 0),
('im_realtime',     '实时消息', 'WebSocket 实时推送：站内通知即时到达 + 内部顾问单聊', 0);
