-- MySQL dump 10.13  Distrib 8.0.45, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: yuanlive_user
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
-- Current Database: `yuanlive_user`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `yuanlive_user` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `yuanlive_user`;

--
-- Table structure for table `search_history`
--

DROP TABLE IF EXISTS `search_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_history` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `keyword` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category_id` int DEFAULT NULL COMMENT '记录搜索时的关联分类，方便AI分析',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户搜索记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_history`
--

LOCK TABLES `search_history` WRITE;
/*!40000 ALTER TABLE `search_history` DISABLE KEYS */;
INSERT INTO `search_history` VALUES (800716886554881,6,'jjw',7,'2026-03-05 14:05:21'),(800716897630466,6,'jjw',7,'2026-03-05 14:05:23'),(802220028930817,7,'原神',7,'2026-03-09 20:01:39'),(802220089862914,7,'jjw',7,'2026-03-09 20:01:54'),(802220144683779,7,'三角洲',NULL,'2026-03-09 20:02:07'),(802220368628484,7,'原神',7,'2026-03-09 20:03:02'),(802221044574981,7,'jjw',7,'2026-03-09 20:05:47');
/*!40000 ALTER TABLE `search_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '菜单/权限名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `sort` int DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '前端组件路径',
  `menu_type` char(1) DEFAULT '' COMMENT '类型:M-目录,C-菜单,F-按钮',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识(如 user:list)',
  `icon` varchar(100) DEFAULT '#' COMMENT '图标',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `title` varchar(50) DEFAULT NULL COMMENT '菜单名称/标题',
  `is_visible` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否显示启用(0 -> 隐藏，1 -> 显示)',
  `is_cache` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否缓存 0否 1是',
  PRIMARY KEY (`menu_id`),
  KEY `parent_id_index` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (4,'',0,4,'/system','','M','','ri:settings-3-line','2026-01-18 14:46:30','2026-02-16 01:06:16','menus.sysManagement',1,0),(5,'SystemUser',4,1,'/system/user/index','/system/user/index','C','','ri:admin-line','2026-01-18 14:49:17','2026-02-16 01:03:39','menus.user',1,0),(7,'SystemRole',4,2,'/system/role/index','/system/role/index','C','','ri:admin-fill','2026-01-18 14:51:16','2026-02-16 00:56:22','menus.role',1,0),(8,'SystemMenu',4,3,'/system/menu/index','/system/menu/index','C','','ep:menu','2026-01-18 14:52:22','2026-02-16 00:56:22','menus.systemMenu',1,0),(9,'',0,5,'/monitor','','M','','ep:monitor','2026-01-18 14:53:45','2026-01-18 14:53:45','menus.sysMonitor',1,0),(10,'OnlineUser',9,1,'/monitor/online-user','monitor/online/index','C','','ri:user-voice-line','2026-01-18 14:55:21','2026-02-16 00:49:39','menus.onlineUser',1,1),(11,'LoginLog',9,2,'/monitor/login-logs','monitor/logs/login/index','C','','ri:window-line','2026-01-18 14:57:17','2026-01-18 14:57:17','menus.loginLog',1,0),(12,'OperationLog',9,3,'/monitor/operation-logs','monitor/logs/operation/index','C','','ri:history-fill','2026-01-18 14:58:51','2026-01-18 14:58:51','menus.operationLog',1,0);
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(30) NOT NULL COMMENT '角色名称(汉字)',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串(如 admin)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态:1-正常,0-停用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_key` (`role_key`),
  UNIQUE KEY `sys_role_pk` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'超级管理员','super-admin',1,'2026-01-17 10:47:01','2026-02-14 20:46:22'),(4,'测试','test',1,'2026-02-14 19:55:50','2026-02-15 18:27:25'),(5,'管理员','ADMIN',1,'2026-02-14 20:48:25','2026-02-14 20:48:25');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_menu`
--

DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色和菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu`
--

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (1,4),(1,5),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12);
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `uid` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码(BCrypt加密)',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态:1-正常,0-停用',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志:0-存在,1-删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `role` tinyint DEFAULT '0' COMMENT '0-用户, 1-主播, 2-管理员',
  `gender` tinyint(1) DEFAULT '0' COMMENT '用户性别 0 -> 未知 1 -> 男性 2->女性',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=793734660666370 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (6,'fordepu','$2a$10$kQ1xij2851wi9DnWu4Q28.3hO04lW1Og8ITLMA7vblHTRJV3pgBrC',NULL,NULL,'j1425127495@127.com',1,0,'2025-12-30 08:04:20','2026-02-13 18:21:18',2,0),(7,'jjw','$2a$10$FyjRY/jXESfyebRnCuHXU.NM41QHRM4NrK1zma4GBZcGtyZjh0mim','','','2121789489@qq.com',1,0,'2026-01-19 09:04:04','2026-02-26 19:47:08',1,0),(790541515841537,'test1','$2a$10$Qscn3azyfQRAmVnxqlgs9uMq4u2RQ94IlVv8mQZhJJ6LgbAKoDakS',NULL,'13795950429','1425127495@qq.com',1,0,'2026-02-04 20:01:39','2026-02-13 18:21:18',0,1),(791474412748801,'test2','$2a$10$0VyDa2LFkSnMHaYC9pqih.i176Ue50QpQxILQD62.6po/4euxz6A2',NULL,'15328235882','j1425127495@126.com',1,0,'2026-02-07 11:17:37','2026-02-14 20:48:13',0,2),(793723659146241,'test3','$2a$10$2ULdjPhIK497g4lcx9o6IOnuhZwK4UCwjcV/3l8.aZvVKCijdYGjK',NULL,'','1425127495@gmail.com',1,0,'2026-02-13 19:49:50','2026-02-13 20:34:19',0,2),(793724497699841,'test4','$2a$10$j2VskRrWzQL3c4rgQMfagetusSp51Y5kdoOvCFqoTxephSs9/Nm8i',NULL,'','123456@126.com',1,0,'2026-02-13 19:53:15','2026-02-13 20:34:13',0,1),(793734660666369,'test5','$2a$10$77qlcD/h1Yz6E0dflGwPROABQ9iPH9pyJgx8s1fKAOAJpZHKwlVWS',NULL,'','j1425127495@129.com',1,0,'2026-02-13 20:34:36','2026-02-13 20:36:27',0,1);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户和角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (6,1);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
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
-- Table structure for table `user_follow`
--

DROP TABLE IF EXISTS `user_follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '粉丝ID (发起关注的人)',
  `follow_user_id` bigint NOT NULL COMMENT '主播ID (被关注的人)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '关注状态: 1-已关注, 0-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_read_video_id` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`,`follow_user_id`),
  KEY `idx_follow_user` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户关注关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_follow`
--

LOCK TABLES `user_follow` WRITE;
/*!40000 ALTER TABLE `user_follow` DISABLE KEYS */;
INSERT INTO `user_follow` VALUES (626001,6,7,1,'2026-02-11 18:32:28','2026-02-12 00:46:15',296001),(626002,7,6,1,'2026-02-11 18:32:28','2026-02-11 18:32:27',0);
/*!40000 ALTER TABLE `user_follow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_stats`
--

DROP TABLE IF EXISTS `user_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_stats` (
  `user_id` bigint NOT NULL,
  `following_count` int DEFAULT '0' COMMENT '关注数',
  `follower_count` int DEFAULT '0' COMMENT '粉丝数',
  `total_likes_received` int DEFAULT '0' COMMENT '获赞总数',
  `video_count` int DEFAULT '0' COMMENT '视频总数',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_stats`
--

LOCK TABLES `user_stats` WRITE;
/*!40000 ALTER TABLE `user_stats` DISABLE KEYS */;
INSERT INTO `user_stats` VALUES (6,2,1,0,0,'2026-02-11 18:32:27'),(7,1,2,0,102,'2026-03-08 16:20:33'),(790541515841537,0,0,0,0,'2026-02-07 18:16:52'),(791474412748801,0,0,0,0,'2026-02-07 18:16:52');
/*!40000 ALTER TABLE `user_stats` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-09 20:37:24
