-- 迭代23：WebSocket 实时协作（路线图中期计划「WebSocket 聊天模块决策」→ 选择重建）
-- 1) 清除老系统残留：chat 表为 2022 年旧求职项目的聊天表（utf8 字符集、timestamp 用 varchar、
--    缺 read 状态字段），随一起 remove 的还有半成品 WebSocket 代码，全库已无引用。
DROP TABLE IF EXISTS `chat`;

-- 2) 内部即时消息表（顾问之间的单聊）
CREATE TABLE IF NOT EXISTS `im_message`  (
  `id`            int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `sender_id`     int(0) NOT NULL COMMENT '发送者用户id',
  `receiver_id`   int(0) NOT NULL COMMENT '接收者用户id',
  `content`       varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息正文',
  `is_read`       tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `read_time`     datetime(3) NULL DEFAULT NULL COMMENT '已读时间',
  `create_time`   datetime(3) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time`   datetime(3) NULL DEFAULT NULL COMMENT '更新时间',
  `delete_time`   datetime(3) NULL DEFAULT NULL COMMENT '逻辑删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_im_conv` (`sender_id`, `receiver_id`, `create_time`) USING BTREE,
  KEY `idx_im_recv_read` (`receiver_id`, `is_read`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '内部即时消息（单聊）' ROW_FORMAT = Dynamic;

-- 3) WebSocket 实时协作功能开关（默认关闭，不影响既有行为）
INSERT INTO feature_flag (flag_key, flag_name, description, enabled)
VALUES ('im_realtime', '实时消息', 'WebSocket 实时推送：站内通知即时到达 + 内部顾问单聊', 0)
ON DUPLICATE KEY UPDATE flag_name = VALUES(flag_name);
