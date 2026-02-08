-- MySQL dump 10.13  Distrib 8.0.45, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: yuanlive_live
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `live_category`
--

DROP TABLE IF EXISTS `live_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `live_category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `parent_id` int DEFAULT '0' COMMENT '父分类ID (0表示一级分类)',
  `name` varchar(32) NOT NULL COMMENT '分类名称',
  `icon_url` varchar(255) DEFAULT NULL COMMENT '分类图标',
  `sort_weight` int DEFAULT '0' COMMENT '排序权重 (越大越靠前)',
  `value` varchar(32) NOT NULL COMMENT '分类名对应的英文名',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `live_room`
--

DROP TABLE IF EXISTS `live_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `live_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '房间主键ID',
  `anchor_id` bigint NOT NULL COMMENT '主播ID (对应user库id)',
  `title` varchar(128) NOT NULL COMMENT '直播间标题',
  `cover_img` varchar(255) DEFAULT NULL COMMENT '直播间封面图URL',
  `room_status` tinyint(1) DEFAULT '0' COMMENT '直播状态 0:未开播 1:直播中',
  `view_count` int DEFAULT '0' COMMENT '当前在线人数',
  `category_id` int DEFAULT NULL COMMENT '分类ID',
  `last_start_time` datetime DEFAULT NULL COMMENT '最近一次开播时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_anchor_id` (`anchor_id`),
  KEY `idx_status` (`room_status`)
) ENGINE=InnoDB AUTO_INCREMENT=790546539380738 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播间表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `undo_log`
--

DROP TABLE IF EXISTS `undo_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `undo_log` (
  `branch_id` bigint NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(128) NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AT回滚表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `video_resource`
--

DROP TABLE IF EXISTS `video_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `room_id` bigint DEFAULT NULL COMMENT '直播间ID',
  `duration` bigint DEFAULT NULL COMMENT '视频时长',
  `title` varchar(255) DEFAULT NULL COMMENT '视频标题（录播可默认为“直播回放+日期”',
  `start_time` datetime NOT NULL DEFAULT (now()) COMMENT '开播时间',
  `end_time` datetime DEFAULT NULL COMMENT '关播时间(录播)',
  `peak_viewers` int DEFAULT '0' COMMENT '本场最高在线人数',
  `watch_count` int DEFAULT '0' COMMENT '本场累计观看人次',
  `video_url` varchar(512) DEFAULT NULL COMMENT '回放视频地址',
  `cover_url` varchar(512) DEFAULT NULL COMMENT '视频封面',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞总数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论总数',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '播放次数',
  `share_count` int NOT NULL DEFAULT '0' COMMENT '分享数',
  `collect_count` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '视频类型 0 -> 录播 1-> 上传视频',
  PRIMARY KEY (`id`),
  KEY `idx_anchor_time` (`user_id`,`start_time`),
  KEY `video_resource_id_user_id_index` (`id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=261002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播或视频记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-08 21:56:31
