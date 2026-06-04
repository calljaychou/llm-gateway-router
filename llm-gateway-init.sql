/*
 Navicat Premium Data Transfer

 Source Server         : localhost_mysql8
 Source Server Type    : MySQL
 Source Server Version : 80031
 Source Host           : localhost:3306
 Source Schema         : llm-gateway

 Target Server Type    : MySQL
 Target Server Version : 80031
 File Encoding         : 65001

 Date: 04/06/2026 10:36:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for api_keys
-- ----------------------------
DROP TABLE IF EXISTS `api_keys`;
CREATE TABLE `api_keys` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `api_key_hash` varchar(128) NOT NULL,
  `api_key_prefix` varchar(16) NOT NULL COMMENT '展示用前缀',
  `status` int DEFAULT '1' COMMENT '1-正常，2-失效，3-过期',
  `expires_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `api_key_hash` (`api_key_hash`)
) ENGINE=InnoDB AUTO_INCREMENT=243 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='虚拟 API Key 表';

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门id',
  `dept_name` varchar(50) NOT NULL COMMENT '部门名称',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `leader_name` varchar(255) DEFAULT NULL COMMENT '负责人',
  `tel` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` int DEFAULT '1' COMMENT '部门状态（1正常 2停用）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=207 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- ----------------------------
-- Table structure for department_model_permissions
-- ----------------------------
DROP TABLE IF EXISTS `department_model_permissions`;
CREATE TABLE `department_model_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `model_id` bigint NOT NULL COMMENT '模型ID',
  `scope` enum('SELF','SUBTREE') NOT NULL DEFAULT 'SUBTREE' COMMENT '权限作用域：仅本部门/含子树',
  `status` int NOT NULL DEFAULT '1' COMMENT '1-生效，0-禁用',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_model_scope` (`dept_id`,`model_id`,`scope`),
  KEY `idx_dept_status` (`dept_id`,`status`),
  KEY `idx_model_status` (`model_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门模型访问权限表';

-- ----------------------------
-- Table structure for llm_usage_billing_detail
-- ----------------------------
DROP TABLE IF EXISTS `llm_usage_billing_detail`;
CREATE TABLE `llm_usage_billing_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `usage_log_id` bigint NOT NULL COMMENT '用量日志ID',
  `request_id` varchar(64) NOT NULL COMMENT '请求ID',
  `charge_item` varchar(64) NOT NULL COMMENT '计费项',
  `token_direction` varchar(16) NOT NULL COMMENT 'INPUT/OUTPUT',
  `token_type` varchar(64) NOT NULL COMMENT 'TEXT/AUDIO/IMAGE/FILE/REASONING/TOOL/PREDICTION/OTHER',
  `cache_type` varchar(32) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/CACHE_HIT/CACHE_MISS/CACHE_WRITE',
  `tokens` int NOT NULL DEFAULT '0' COMMENT 'Token数量',
  `price_cny_per_million` decimal(18,8) NOT NULL DEFAULT '0.00000000' COMMENT '百万Token单价快照',
  `amount_cny` decimal(18,8) NOT NULL DEFAULT '0.00000000' COMMENT '本计费项金额',
  `pricing_rule` varchar(128) DEFAULT NULL COMMENT '命中的价格规则',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_usage_log_id` (`usage_log_id`),
  KEY `idx_request_id` (`request_id`),
  KEY `idx_charge_item` (`charge_item`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='LLM Token计费明细';

-- ----------------------------
-- Table structure for llm_usage_log
-- ----------------------------
DROP TABLE IF EXISTS `llm_usage_log`;
CREATE TABLE `llm_usage_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `request_id` varchar(64) NOT NULL COMMENT '请求ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `api_key_id` bigint DEFAULT NULL COMMENT '虚拟Key ID',
  `vendor_id` bigint NOT NULL COMMENT '供应商ID',
  `model_id` bigint DEFAULT NULL COMMENT '模型ID',
  `endpoint` varchar(64) NOT NULL COMMENT '接口类型: chat.completions/responses/messages',
  `token_protocol` varchar(64) NOT NULL COMMENT 'Token协议',
  `use_stream` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否流式',
  `request_model` varchar(128) DEFAULT NULL COMMENT '请求模型名或模型别名',
  `upstream_model` varchar(128) DEFAULT NULL COMMENT '上游真实模型名',
  `resolved_model` varchar(128) DEFAULT NULL COMMENT 'Token计算解析模型名',
  `model_encoding` varchar(64) DEFAULT NULL COMMENT 'Token计算encoding',
  `token_calc_source` varchar(32) NOT NULL COMMENT 'REPORTED_USAGE/LOCAL_ESTIMATE/MERGED/UNSUPPORTED',
  `token_calc_supported` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否支持Token计算',
  `token_calc_note` text COMMENT 'Token计算说明',
  `input_tokens` int NOT NULL DEFAULT '0' COMMENT '输入Token总数',
  `output_tokens` int NOT NULL DEFAULT '0' COMMENT '输出Token总数',
  `total_tokens` int NOT NULL DEFAULT '0' COMMENT '总Token数',
  `billable_input_tokens` int NOT NULL DEFAULT '0' COMMENT '参与计费的输入Token数',
  `billable_output_tokens` int NOT NULL DEFAULT '0' COMMENT '参与计费的输出Token数',
  `reserved_amount_cny` decimal(18,8) NOT NULL DEFAULT '0.00000000' COMMENT '预占金额',
  `amount_cny` decimal(18,8) NOT NULL DEFAULT '0.00000000' COMMENT '最终结算金额',
  `billing_strategy` varchar(64) NOT NULL COMMENT '计费策略',
  `billing_currency` varchar(16) NOT NULL DEFAULT 'CNY' COMMENT '计费币种',
  `latency_ms` int DEFAULT NULL COMMENT '耗时毫秒',
  `status_code` int NOT NULL COMMENT 'HTTP状态码',
  `error_code` varchar(64) DEFAULT NULL COMMENT '错误码',
  `accounting_status` varchar(32) NOT NULL COMMENT '结算状态',
  `request_started_at` datetime DEFAULT NULL COMMENT '请求开始时间',
  `settled_at` datetime DEFAULT NULL COMMENT '结算时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `token_calc_detail` json DEFAULT NULL COMMENT 'Token计算过程快照',
  `billing_detail` json DEFAULT NULL COMMENT '金额计算过程快照',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request_id` (`request_id`),
  KEY `idx_user_created` (`user_id`,`created_at`),
  KEY `idx_vendor_model_created` (`vendor_id`,`model_id`,`created_at`),
  KEY `idx_accounting_status` (`accounting_status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='LLM调用用量主日志';

-- ----------------------------
-- Table structure for llm_usage_token_detail
-- ----------------------------
DROP TABLE IF EXISTS `llm_usage_token_detail`;
CREATE TABLE `llm_usage_token_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `usage_log_id` bigint NOT NULL COMMENT '用量日志ID',
  `request_id` varchar(64) NOT NULL COMMENT '请求ID',
  `token_direction` varchar(16) NOT NULL COMMENT 'INPUT/OUTPUT',
  `token_type` varchar(64) NOT NULL COMMENT 'TEXT/AUDIO/IMAGE/FILE/REASONING/TOOL/PREDICTION/OTHER',
  `cache_type` varchar(32) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/CACHE_HIT/CACHE_MISS/CACHE_WRITE',
  `tokens` int NOT NULL DEFAULT '0' COMMENT 'Token数量',
  `billable_tokens` int NOT NULL DEFAULT '0' COMMENT '参与计费的Token数量',
  `source` varchar(32) NOT NULL COMMENT 'REPORTED/LOCAL/MERGED/DERIVED',
  `provider_field` varchar(128) DEFAULT NULL COMMENT '上游原始字段名',
  `note` varchar(512) DEFAULT NULL COMMENT '说明',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_usage_log_id` (`usage_log_id`),
  KEY `idx_request_id` (`request_id`),
  KEY `idx_direction_type_cache` (`token_direction`,`token_type`,`cache_type`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='LLM Token用量明细';

-- ----------------------------
-- Table structure for master_keys
-- ----------------------------
DROP TABLE IF EXISTS `master_keys`;
CREATE TABLE `master_keys` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vendor_id` bigint NOT NULL COMMENT '供应商ID',
  `api_key_encrypted` text NOT NULL COMMENT 'AES 加密存储',
  `weight` int DEFAULT '10' COMMENT '负载均衡权重',
  `status` int DEFAULT '1' COMMENT '1-正常,2-异常',
  `error_count` int DEFAULT '0' COMMENT '连续失败次数',
  `last_checked_at` timestamp NULL DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `index_vendor_id` (`vendor_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='主密钥池表';

-- ----------------------------
-- Table structure for model_price_rule
-- ----------------------------
DROP TABLE IF EXISTS `model_price_rule`;
CREATE TABLE `model_price_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_id` bigint NOT NULL COMMENT '模型ID',
  `vendor_id` bigint NOT NULL COMMENT '供应商ID',
  `charge_item` varchar(64) NOT NULL COMMENT '计费项',
  `price_cny_per_million` decimal(18,8) NOT NULL COMMENT '百万Token单价',
  `currency` varchar(16) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model_charge_item` (`model_id`,`charge_item`),
  KEY `idx_vendor_model` (`vendor_id`,`model_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模型Token价格规则';

-- ----------------------------
-- Table structure for models
-- ----------------------------
DROP TABLE IF EXISTS `models`;
CREATE TABLE `models` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `model_alias` varchar(64) NOT NULL COMMENT '别名，如 gpt-4-enterprise',
  `real_model_name` varchar(64) NOT NULL COMMENT '供应商模型名，如 gpt-4o',
  `vendor_id` bigint NOT NULL COMMENT '供应商ID',
  `billing_type` enum('FREE','PAID') DEFAULT 'PAID',
  `input_price_cny_per_million` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '输入Token每百万人民币单价',
  `output_price_cny_per_million` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '输出Token每百万人民币单价',
  `active` tinyint(1) DEFAULT '1' COMMENT '是否活跃',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `model_alias` (`model_alias`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模型定义表';

-- ----------------------------
-- Table structure for position
-- ----------------------------
DROP TABLE IF EXISTS `position`;
CREATE TABLE `position` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态（1正常 2停用）',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位信息表';

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `created_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色信息表';

-- ----------------------------
-- Table structure for usage_stats_daily_department
-- ----------------------------
DROP TABLE IF EXISTS `usage_stats_daily_department`;
CREATE TABLE `usage_stats_daily_department` (
  `stat_date` date NOT NULL,
  `dept_id` bigint NOT NULL,
  `total_tokens` bigint NOT NULL DEFAULT '0',
  `request_cnt` bigint NOT NULL DEFAULT '0',
  `error_cnt` bigint NOT NULL DEFAULT '0',
  `active_users` int NOT NULL DEFAULT '0',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`stat_date`,`dept_id`),
  KEY `idx_dept_date` (`dept_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门维度日聚合';

-- ----------------------------
-- Table structure for usage_stats_daily_user
-- ----------------------------
DROP TABLE IF EXISTS `usage_stats_daily_user`;
CREATE TABLE `usage_stats_daily_user` (
  `stat_date` date NOT NULL,
  `user_id` bigint NOT NULL,
  `dept_id` bigint NOT NULL,
  `total_tokens` bigint NOT NULL DEFAULT '0',
  `request_cnt` bigint NOT NULL DEFAULT '0',
  `error_cnt` bigint NOT NULL DEFAULT '0',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`stat_date`,`user_id`),
  KEY `idx_dept_user_date` (`dept_id`,`user_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户维度日聚合';

-- ----------------------------
-- Table structure for user_post_rel
-- ----------------------------
DROP TABLE IF EXISTS `user_post_rel`;
CREATE TABLE `user_post_rel` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与岗位关联表';

-- ----------------------------
-- Table structure for user_quota_accounts
-- ----------------------------
DROP TABLE IF EXISTS `user_quota_accounts`;
CREATE TABLE `user_quota_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `current_quota_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '当前仍在有效期内的总配额金额，人民币',
  `used_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '当前已消耗金额，人民币',
  `expired_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '当前已过期金额累计，人民币',
  `transferred_in_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '累计转入金额，人民币',
  `transferred_out_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '累计转出金额，人民币',
  `available_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '当前可消费、可转配的剩余金额，人民币',
  `allow_transfer_out` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否允许向外转配',
  `earliest_expire_at` datetime DEFAULT NULL COMMENT '当前有效配额中的最早过期时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_earliest_expire_at` (`earliest_expire_at`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户配额账户表';

-- ----------------------------
-- Table structure for user_quota_grants
-- ----------------------------
DROP TABLE IF EXISTS `user_quota_grants`;
CREATE TABLE `user_quota_grants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `source_type` varchar(32) NOT NULL COMMENT '配额来源：ADMIN_GRANT,TRANSFER_I,COMPENSATE',
  `source_user_id` bigint DEFAULT NULL COMMENT '来源用户ID，管理员发放时为空',
  `source_grant_id` bigint DEFAULT NULL COMMENT '来源配额批次ID，转配时用于追踪原始配额',
  `granted_amount` decimal(18,6) NOT NULL COMMENT '发放金额，人民币',
  `remaining_amount` decimal(18,6) NOT NULL COMMENT '当前剩余金额，人民币',
  `consumed_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '已消费金额，人民币',
  `expired_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '过期金额，人民币',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '批次状态:ACTIVE,DEPLETED,EXPIRED',
  `granted_by` bigint DEFAULT NULL COMMENT '操作人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_status_expire` (`user_id`,`status`,`expires_at`),
  KEY `idx_expired_query` (`user_id`,`expires_at`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户配额批次表';

-- ----------------------------
-- Table structure for user_quota_transactions
-- ----------------------------
DROP TABLE IF EXISTS `user_quota_transactions`;
CREATE TABLE `user_quota_transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `biz_no` varchar(64) NOT NULL COMMENT '业务流水号',
  `user_id` bigint NOT NULL COMMENT '额度归属用户',
  `grant_id` bigint DEFAULT NULL COMMENT '关联配额批次ID',
  `change_type` varchar(32) NOT NULL COMMENT '变更类型:ADMIN_GRANT, ADMIN_RECLAIM, TRANSFER_OUT, TRANSFER_IN, USAGE_RESERVE, USAGE_SETTLE, USAGE_REFUND, QUOTA_EXPIRE',
  `delta_amount` decimal(18,6) NOT NULL COMMENT '变更金额，正负号表示增减，人民币',
  `quota_before_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '变更前当前有效总配额金额，人民币',
  `quota_after_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '变更后当前有效总配额金额，人民币',
  `available_before_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '变更前剩余金额，人民币',
  `available_after_amount` decimal(18,6) NOT NULL DEFAULT '0.000000' COMMENT '变更后剩余金额，人民币',
  `counterparty_user_id` bigint DEFAULT NULL COMMENT '转配对手方用户ID',
  `request_id` varchar(64) DEFAULT NULL COMMENT '关联请求ID',
  `operator_user_id` bigint DEFAULT NULL COMMENT '操作人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`,`created_time`),
  KEY `idx_biz_no` (`biz_no`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户配额流水表';

-- ----------------------------
-- Table structure for user_role_rel
-- ----------------------------
DROP TABLE IF EXISTS `user_role_rel`;
CREATE TABLE `user_role_rel` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户和角色关联表';

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `username` varchar(64) NOT NULL COMMENT '用户账号',
  `email` varchar(128) NOT NULL COMMENT '用户邮箱',
  `mobile` varchar(11) DEFAULT NULL COMMENT '手机号码',
  `gender` int DEFAULT '3' COMMENT '用户性别（1男 2女 3未知）',
  `avatar_url` varchar(200) DEFAULT NULL COMMENT '头像地址',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `password_changed` tinyint(1) DEFAULT '0' COMMENT '强制首登改密',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` int DEFAULT '1' COMMENT '状态1-正常，2-离职',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '状态0-正常，1-已删除',
  `use_time` timestamp NULL DEFAULT NULL COMMENT '系统使用时间',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=176 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';

-- ----------------------------
-- Table structure for vendors
-- ----------------------------
DROP TABLE IF EXISTS `vendors`;
CREATE TABLE `vendors` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '供应商名称',
  `base_url` varchar(255) NOT NULL COMMENT '供应商URL',
  `status` int DEFAULT '1' COMMENT '1-正常，2-停用',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商表';

INSERT INTO `llm-gateway`.`department` (`id`, `parent_id`, `dept_name`, `order_num`, `leader_name`, `tel`, `status`, `del_flag`, `created_time`, `updated_time`) VALUES (1, 0, '中国轻研科技', 0, NULL, '17799999999', 1, 0, NULL, NULL);
INSERT INTO `llm-gateway`.`users` (`id`, `name`, `dept_id`, `username`, `email`, `mobile`, `gender`, `avatar_url`, `password`, `password_changed`, `remark`, `status`, `del_flag`, `use_time`, `created_time`, `updated_time`) VALUES (100, 'admin', 1, 'admin', 'admin@qy.com', '17799999999', 3, NULL, '$2a$10$c0oiCRigE6py.94Bqj14We76fP29Wh2GD4l507ikniNuitKv/OaSq', 0, NULL, 1, 0, '2026-05-30 01:01:26', '2026-04-21 16:12:18', '2026-04-21 16:12:18');
INSERT INTO `llm-gateway`.`roles` (`id`, `role_name`, `role_key`, `role_sort`, `created_by`, `created_time`, `updated_by`, `updated_time`) VALUES (100, '超级管理员', 'admin', 1, '', '2026-04-21 16:27:27', '', '2026-05-28 21:52:14');
INSERT INTO `llm-gateway`.`roles` (`id`, `role_name`, `role_key`, `role_sort`, `created_by`, `created_time`, `updated_by`, `updated_time`) VALUES (101, '普通角色', 'common', 3, '', '2026-04-21 16:27:30', '', '2026-05-28 21:52:14');
INSERT INTO `llm-gateway`.`roles` (`id`, `role_name`, `role_key`, `role_sort`, `created_by`, `created_time`, `updated_by`, `updated_time`) VALUES (103, 'QRoute负责人', 'llm-lead', 2, '', '2026-05-28 21:52:07', '', '2026-05-28 21:52:12');
INSERT INTO `llm-gateway`.`user_role_rel` (`user_id`, `role_id`) VALUES (100, 100);



SET FOREIGN_KEY_CHECKS = 1;
