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
-- Dumping data for table `live_category`
--

LOCK TABLES `live_category` WRITE;
/*!40000 ALTER TABLE `live_category` DISABLE KEYS */;
INSERT INTO `live_category` VALUES (1,0,'游戏','https://api.iconify.design/mdi:controller.svg',0,'game'),(2,0,'户外','https://api.iconify.design/ic:baseline-terrain.svg',0,'out'),(3,1,'CS','https://api.iconify.design/simple-icons:counterstrike.svg?color=%23ff9a00',0,'csgo'),(5,2,'探险','https://api.iconify.design/ic:baseline-explore.svg',0,'explore'),(7,1,'原神','https://api.iconify.design/material-symbols:star-outline.svg?color=%234eb0ff',0,'genshi'),(8,2,'三角洲','https://api.iconify.design/mdi:triangle-outline.svg',0,'delta');
/*!40000 ALTER TABLE `live_category` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `live_room`
--

LOCK TABLES `live_room` WRITE;
/*!40000 ALTER TABLE `live_room` DISABLE KEYS */;
INSERT INTO `live_room` VALUES (790546539380737,7,'原神启动!!!',NULL,0,0,7,'2026-02-12 00:49:00','2026-02-04 20:22:06','2026-02-12 00:49:00');
/*!40000 ALTER TABLE `live_room` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `undo_log`
--

LOCK TABLES `undo_log` WRITE;
/*!40000 ALTER TABLE `undo_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `undo_log` ENABLE KEYS */;
UNLOCK TABLES;

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
  `start_time` datetime DEFAULT (now()) COMMENT '开播时间',
  `end_time` datetime DEFAULT NULL COMMENT '关播时间(直播记录)',
  `peak_viewers` int DEFAULT '0' COMMENT '本场最高在线人数(直播记录)',
  `watch_count` int DEFAULT '0' COMMENT '本场累计观看人次(直播记录)',
  `video_url` varchar(512) DEFAULT NULL COMMENT '回放视频地址',
  `cover_url` varchar(512) DEFAULT NULL COMMENT '视频封面',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞总数(视频)',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论总数(视频)',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '播放次数(视频)',
  `share_count` int NOT NULL DEFAULT '0' COMMENT '分享数(视频)',
  `collect_count` int NOT NULL DEFAULT '0' COMMENT '收藏数(视频)',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '视频类型 0 -> 录播 1-> 上传视频',
  `create_time` datetime DEFAULT NULL COMMENT '视频上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_anchor_time` (`user_id`,`start_time`),
  KEY `video_resource_id_user_id_index` (`id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=621002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='直播或视频记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `video_resource`
--

LOCK TABLES `video_resource` WRITE;
/*!40000 ALTER TABLE `video_resource` DISABLE KEYS */;
INSERT INTO `video_resource` VALUES (121001,7,790546539380737,6,'jjw-原神启动!!!-2026年02月07日21:55-直播回放','2026-02-07 21:55:34','2026-02-07 21:55:40',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-07/790546539380737-1770472540346.mp4',NULL,0,0,0,0,0,0,NULL),(136001,7,790546539380737,7,'jjw-原神启动!!!-2026年02月07日21:59-直播回放','2026-02-07 21:59:00','2026-02-07 21:59:08',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-07/790546539380737-1770472747640.mp4',NULL,0,0,0,0,0,0,NULL),(136002,7,790546539380737,26,'jjw-原神启动!!!-2026年02月07日22:36-直播回放','2026-02-07 22:36:38','2026-02-07 22:37:04',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-07/790546539380737-1770475024124.mp4',NULL,0,0,0,0,0,0,NULL),(136003,7,790546539380737,60,'jjw-原神启动!!!-2026年02月07日22:38-直播回放','2026-02-07 22:38:18','2026-02-07 22:39:18',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-07/790546539380737-1770475158164.mp4',NULL,0,0,0,0,0,0,NULL),(136004,7,790546539380737,24,'jjw-原神启动!!!-2026年02月07日22:46-直播回放','2026-02-07 22:46:51','2026-02-07 22:47:15',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-07/790546539380737-1770475635091.mp4',NULL,0,0,0,0,0,0,NULL),(136005,7,790546539380737,4,'jjw-原神启动!!!-2026年02月07日22:48-直播回放','2026-02-07 22:48:13','2026-02-07 22:48:18',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-07/790546539380737-1770475697792.mp4',NULL,0,0,0,0,0,0,NULL),(161001,7,790546539380737,2,'jjw-原神启动!!!-2026年02月08日11:36-直播回放','2026-02-08 11:36:37','2026-02-08 11:36:42',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-08/790546539380737-1770521802039.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-08/790546539380737-1770521802039.jpg',0,0,0,0,0,0,NULL),(161002,7,790546539380737,8,'jjw-原神启动!!!-2026年02月08日11:39-直播回放','2026-02-08 11:39:12','2026-02-08 11:39:22',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-08/790546539380737-1770521962102.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-08/790546539380737-1770521962102.jpg',0,0,0,0,0,0,NULL),(176001,7,790546539380737,28,'jjw-原神启动!!!-2026年02月08日15:39-直播回放','2026-02-08 15:39:37','2026-02-08 15:40:07',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-08/790546539380737-1770536407992.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-08/790546539380737-1770536407992.jpg',0,0,0,0,0,0,NULL),(191001,7,790546539380737,22,'jjw-原神启动!!!-2026年02月08日16:06-直播回放','2026-02-08 16:06:35','2026-02-08 16:06:59',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-08/790546539380737-1770538020125.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-08/790546539380737-1770538020125.jpg',0,0,0,0,0,0,NULL),(206001,7,790546539380737,76,'jjw-原神启动!!!-2026年02月08日16:13-直播回放','2026-02-08 16:13:55','2026-02-08 16:15:12',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-08/790546539380737-1770538512674.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-08/790546539380737-1770538512674.jpg',0,0,0,0,0,0,NULL),(281001,7,790546539380737,152,'jjw-原神启动!!!-2026年02月09日17:28-直播回放','2026-02-09 17:28:12','2026-02-09 17:30:47',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-09/790546539380737-1770629448086.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-09/790546539380737-1770629448086.jpg',0,0,0,0,0,0,NULL),(296001,7,790546539380737,58,'jjw-原神启动!!!-2026年02月09日18:25-直播回放','2026-02-09 18:25:06','2026-02-09 18:26:05',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-09/790546539380737-1770632766206.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-09/790546539380737-1770632766206.jpg',0,0,0,0,0,0,NULL),(311001,7,790546539380737,68,'jjw-原神启动!!!-2026年02月09日19:29-直播回放','2026-02-09 19:29:36','2026-02-09 19:30:46',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-09/790546539380737-1770636646444.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-09/790546539380737-1770636646444.jpg',0,0,0,0,0,0,NULL),(326001,7,790546539380737,21,'jjw-原神启动!!!-2026年02月09日19:55-直播回放','2026-02-09 19:55:26','2026-02-09 19:55:49',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-09/790546539380737-1770638149861.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-09/790546539380737-1770638149861.jpg',0,0,0,0,0,0,NULL),(326002,7,790546539380737,13,'jjw-原神启动!!!-2026年02月09日19:56-直播回放','2026-02-09 19:56:13','2026-02-09 19:56:29',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-09/790546539380737-1770638188884.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-09/790546539380737-1770638188884.jpg',0,0,0,0,0,0,NULL),(341001,7,790546539380737,151,'jjw-原神启动!!!-2026年02月09日20:10-直播回放','2026-02-09 20:10:31','2026-02-09 20:13:04',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-09/790546539380737-1770639184078.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-09/790546539380737-1770639184078.jpg',0,0,0,0,0,0,NULL),(386001,7,790546539380737,32,'jjw-原神启动!!!-2026年02月10日10:56-直播回放','2026-02-10 10:56:07','2026-02-10 10:56:42',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770692202590.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770692202590.jpg',0,0,0,0,0,0,NULL),(386002,7,790546539380737,9,'jjw-原神启动!!!-2026年02月10日10:58-直播回放','2026-02-10 10:58:39','2026-02-10 10:58:50',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770692329982.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770692329982.jpg',0,0,0,0,0,0,NULL),(401001,7,790546539380737,40,'jjw-原神启动!!!-2026年02月10日11:27-直播回放','2026-02-10 11:27:33','2026-02-10 11:28:16',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770694096143.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770694096143.jpg',0,0,0,0,0,0,NULL),(416001,7,790546539380737,76,'jjw-原神启动!!!-2026年02月10日11:32-直播回放','2026-02-10 11:32:33','2026-02-10 11:33:51',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770694432250.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770694432250.jpg',0,0,0,0,0,0,NULL),(441001,7,790546539380737,179,'jjw-原神启动!!!-2026年02月10日11:45-直播回放','2026-02-10 11:45:40','2026-02-10 11:48:41',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770695322043.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770695322043.jpg',0,0,0,0,0,0,NULL),(476001,7,790546539380737,323,'jjw-原神启动!!!-2026年02月10日16:00-直播回放','2026-02-10 16:00:20','2026-02-10 16:05:45',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770710745470.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770710745470.jpg',0,0,0,0,0,0,NULL),(476002,7,790546539380737,129,'jjw-原神启动!!!-2026年02月10日16:11-直播回放','2026-02-10 16:11:16','2026-02-10 16:13:27',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770711207346.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770711207346.jpg',0,0,0,0,0,0,NULL),(491001,7,790546539380737,97,'jjw-原神启动!!!-2026年02月10日16:14-直播回放','2026-02-10 16:14:26','2026-02-10 16:16:05',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770711365505.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770711365505.jpg',0,0,0,0,0,0,NULL),(506001,7,790546539380737,74,'jjw-原神启动!!!-2026年02月10日16:47-直播回放','2026-02-10 16:47:12','2026-02-10 16:48:28',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770713308086.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770713308086.jpg',0,0,0,0,0,0,NULL),(521001,7,790546539380737,60,'jjw-原神启动!!!-2026年02月10日19:02-直播回放','2026-02-10 19:02:20','2026-02-10 19:03:22',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770721402678.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770721402678.jpg',0,0,0,0,0,0,NULL),(536001,7,790546539380737,51,'jjw-原神启动!!!-2026年02月10日19:11-直播回放','2026-02-10 19:11:47','2026-02-10 19:12:40',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770721959822.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770721959822.jpg',0,0,0,0,0,0,NULL),(536002,7,790546539380737,33,'jjw-原神启动!!!-2026年02月10日19:14-直播回放','2026-02-10 19:14:59','2026-02-10 19:15:34',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-10/790546539380737-1770722133801.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-10/790546539380737-1770722133801.jpg',0,0,0,0,0,0,NULL),(551001,7,790546539380737,57,'jjw-原神启动!!!-2026年02月11日17:28-直播回放','2026-02-11 17:28:43','2026-02-11 17:29:43',1,1,'http://127.0.0.1:9000/yuanlive/records/2026-02-11/790546539380737-1770802184242.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-11/790546539380737-1770802184242.jpg',0,0,0,0,0,0,NULL),(551002,7,790546539380737,170,'jjw-原神启动!!!-2026年02月11日17:50-直播回放','2026-02-11 17:50:38','2026-02-11 17:53:29',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-11/790546539380737-1770803610221.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-11/790546539380737-1770803610221.jpg',0,0,0,0,0,0,NULL),(551003,7,790546539380737,15,'jjw-原神启动!!!-2026年02月11日18:11-直播回放','2026-02-11 18:11:18','2026-02-11 18:11:35',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-11/790546539380737-1770804695722.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-11/790546539380737-1770804695722.jpg',0,0,0,0,0,0,NULL),(551004,7,790546539380737,46,'jjw-原神启动!!!-2026年02月11日19:20-直播回放','2026-02-11 19:20:10','2026-02-11 19:21:01',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-11/790546539380737-1770808861474.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-11/790546539380737-1770808861474.jpg',0,0,0,0,0,0,NULL),(551005,7,790546539380737,6,'jjw-原神启动!!!-2026年02月11日19:26-直播回放','2026-02-11 19:26:15','2026-02-11 19:26:24',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-11/790546539380737-1770809184099.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-11/790546539380737-1770809184099.jpg',0,0,0,0,0,0,NULL),(576001,7,790546539380737,83,'jjw-原神启动!!!-2026年02月11日20:17-直播回放','2026-02-11 20:17:17','2026-02-11 20:18:43',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-11/790546539380737-1770812323952.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-11/790546539380737-1770812323952.jpg',0,0,0,0,0,0,NULL),(576002,7,790546539380737,62,'jjw-原神启动!!!-2026年02月11日20:19-直播回放','2026-02-11 20:19:48','2026-02-11 20:20:52',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-11/790546539380737-1770812451954.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-11/790546539380737-1770812451954.jpg',0,0,0,0,0,0,NULL),(576003,7,790546539380737,10,'jjw-原神启动!!!-2026年02月11日20:36-直播回放','2026-02-11 20:36:50','2026-02-11 20:37:02',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-11/790546539380737-1770813422112.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-11/790546539380737-1770813422112.jpg',0,0,0,0,0,0,NULL),(621001,7,790546539380737,2,'jjw-原神启动!!!-2026年02月12日00:49-直播回放','2026-02-12 00:49:01','2026-02-12 00:49:04',0,0,'http://127.0.0.1:9000/yuanlive/records/2026-02-12/790546539380737-1770828545202.mp4','http://127.0.0.1:9000/yuanlive/records/cover/2026-02-12/790546539380737-1770828545202.jpg',0,0,0,0,0,0,'2026-02-12 00:49:06');
/*!40000 ALTER TABLE `video_resource` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-12  1:09:13
