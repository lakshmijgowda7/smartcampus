-- MySQL dump 10.13  Distrib 8.0.23, for Win64 (x86_64)
--
-- Host: localhost    Database: smart_campus_db
-- ------------------------------------------------------
-- Server version	8.0.23

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
-- Table structure for table `activity_logs`
--

DROP TABLE IF EXISTS `activity_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `action` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_type` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_id` bigint DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_activity_created` (`created_at`),
  KEY `idx_activity_user` (`user_id`),
  CONSTRAINT `fk_activity_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_logs`
--

LOCK TABLES `activity_logs` WRITE;
/*!40000 ALTER TABLE `activity_logs` DISABLE KEYS */;
INSERT INTO `activity_logs` VALUES (1,1,'USER_LOGIN','USER',1,'Administrator logged in from campus subnet','192.168.1.10','2026-10-29 14:14:36'),(2,4,'CREATE_BOOKING','BOOKING',1,'Student John Doe submitted room reservation','192.168.1.45','2026-10-29 14:14:36'),(3,1,'APPROVE_BOOKING','BOOKING',1,'Administrator approved booking #1','192.168.1.10','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `activity_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `booking_date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `purpose` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `rejection_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `approver_notes` text COLLATE utf8mb4_unicode_ci,
  `approved_by_user_id` bigint DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_bookings_approver` (`approved_by_user_id`),
  KEY `idx_bookings_lookup` (`resource_id`,`booking_date`,`start_time`,`end_time`),
  KEY `idx_bookings_status` (`status`),
  KEY `idx_bookings_user` (`user_id`),
  KEY `idx_bookings_date` (`booking_date`),
  CONSTRAINT `fk_bookings_approver` FOREIGN KEY (`approved_by_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_bookings_resource` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_bookings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES (1,1,2,'2026-10-26','10:00:00','12:00:00','CS301 Java OOP Special Workshop','APPROVED',NULL,'Approved for departmental seminar',1,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,3,3,'2026-10-26','14:00:00','16:00:00','CS302 Database Practical Lab Session','APPROVED',NULL,'Lab session scheduled',1,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(3,5,1,'2026-10-27','09:00:00','13:00:00','Annual Tech Symposium Orientation','APPROVED',NULL,'Approved campus-wide event',1,'2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `campus_events`
--

DROP TABLE IF EXISTS `campus_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campus_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `category` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `organizer` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `resource_id` bigint DEFAULT NULL,
  `venue_name` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SCHEDULED',
  `max_participants` int DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_events_resource` (`resource_id`),
  KEY `idx_events_start` (`start_time`),
  KEY `idx_events_category` (`category`),
  KEY `idx_events_status` (`status`),
  CONSTRAINT `fk_events_resource` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `campus_events`
--

LOCK TABLES `campus_events` WRITE;
/*!40000 ALTER TABLE `campus_events` DISABLE KEYS */;
INSERT INTO `campus_events` VALUES (1,'HackCampus 2026 Hackathon','36-hour flagship campus hackathon on AI, IoT and Sustainability','ACADEMIC','ACM Student Chapter',5,'Main Seminar Auditorium','2026-11-05 09:00:00','2026-11-06 21:00:00','SCHEDULED',200,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,'Robotics Showcase & Competition','Exhibition of autonomous rovers and drone prototypes','WORKSHOP','Robotics Club',4,'IoT & Embedded Systems Lab','2026-11-12 10:00:00','2026-11-12 16:00:00','SCHEDULED',60,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(3,'Campus Cultural Fest - Tarang 2026','Inter-collegiate music, dance, and theatrical performances','CULTURAL','Student Cultural Council',5,'Main Seminar Auditorium','2026-11-20 17:00:00','2026-11-22 22:00:00','SCHEDULED',300,'2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `campus_events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `department` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `credits` int NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `course_code` (`course_code`),
  KEY `idx_courses_dept` (`department`),
  KEY `idx_courses_code` (`course_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses` DISABLE KEYS */;
INSERT INTO `courses` VALUES (1,'CS301','Object Oriented Programming with Java','Computer Science',4,'Fundamental OOP principles, design patterns, polymorphism, multithreading, and collections framework.','2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,'CS302','Database Management Systems','Computer Science',4,'Relational algebra, SQL, normalization, concurrency control, and indexing.','2026-10-29 14:14:36','2026-10-29 14:14:36'),(3,'CS303','Operating Systems & Concurrency','Computer Science',3,'Process synchronization, thread scheduling, memory management, and deadlock prevention.','2026-10-29 14:14:36','2026-10-29 14:14:36'),(4,'EC201','Digital Signal Processing','Electronics & Comm.',4,'Discrete-time signals, Fourier analysis, and digital filters.','2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `equipment`
--

DROP TABLE IF EXISTS `equipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint DEFAULT NULL,
  `serial_number` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `specifications` text COLLATE utf8mb4_unicode_ci,
  `equipment_condition` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GOOD',
  `purchase_date` date DEFAULT NULL,
  `warranty_expiry` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `serial_number` (`serial_number`),
  KEY `fk_equipment_resource` (`resource_id`),
  KEY `idx_equipment_serial` (`serial_number`),
  CONSTRAINT `fk_equipment_resource` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipment`
--

LOCK TABLES `equipment` WRITE;
/*!40000 ALTER TABLE `equipment` DISABLE KEYS */;
INSERT INTO `equipment` VALUES (1,1,'PROJ-EPS-0981','Epson EB-L630U','6200 lumens Laser Projector, 1920x1200 WUXGA','EXCELLENT','2024-01-15','2027-01-15','2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,3,'SRV-DELL-8821','Dell PowerEdge R750','2x Xeon Gold 6330, 128GB RAM, 4TB NVMe SSD','EXCELLENT','2023-09-01','2026-09-01','2026-10-29 14:14:36','2026-10-29 14:14:36'),(3,4,'OSC-RIG-4412','Rigol DS1054Z','50MHz 4-Channel Digital Oscilloscope','GOOD','2023-11-20','2025-11-20','2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `equipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `faculty`
--

DROP TABLE IF EXISTS `faculty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `faculty` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `employee_id` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `department` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `designation` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `office_location` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(25) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `employee_id` (`employee_id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_faculty_dept` (`department`),
  KEY `idx_faculty_emp_id` (`employee_id`),
  CONSTRAINT `fk_faculty_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `faculty`
--

LOCK TABLES `faculty` WRITE;
/*!40000 ALTER TABLE `faculty` DISABLE KEYS */;
INSERT INTO `faculty` VALUES (1,2,'FAC1001','Dr. Alan Turing','faculty.alan@campus.edu','Computer Science','Professor & Head','Turing Block, Room 301','+1-555-0101','Male','1975-06-23','2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,3,'FAC1002','Dr. Grace Hopper','faculty.grace@campus.edu','Computer Science','Associate Professor','Hopper Block, Room 204','+1-555-0102','Female','1980-12-09','2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `faculty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `faculty_courses`
--

DROP TABLE IF EXISTS `faculty_courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `faculty_courses` (
  `faculty_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `assigned_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`faculty_id`,`course_id`),
  KEY `fk_fc_course` (`course_id`),
  CONSTRAINT `fk_fc_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fc_faculty` FOREIGN KEY (`faculty_id`) REFERENCES `faculty` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `faculty_courses`
--

LOCK TABLES `faculty_courses` WRITE;
/*!40000 ALTER TABLE `faculty_courses` DISABLE KEYS */;
INSERT INTO `faculty_courses` VALUES (1,1,'2026-10-29 14:14:36'),(1,2,'2026-10-29 14:14:36'),(2,3,'2026-10-29 14:14:36');
/*!40000 ALTER TABLE `faculty_courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','init schema','SQL','V1__init_schema.sql',-330194471,'root','2026-10-29 14:14:36',905,1),(2,'2','seed data','SQL','V2__seed_data.sql',-585452488,'root','2026-10-29 14:14:36',75,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item_claims`
--

DROP TABLE IF EXISTS `item_claims`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item_claims` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_id` bigint NOT NULL,
  `claimant_user_id` bigint NOT NULL,
  `proof_description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `claim_status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `reviewer_notes` text COLLATE utf8mb4_unicode_ci,
  `reviewed_by_user_id` bigint DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_claim_item` (`item_id`),
  KEY `fk_claim_claimant` (`claimant_user_id`),
  KEY `fk_claim_reviewer` (`reviewed_by_user_id`),
  KEY `idx_claims_status` (`claim_status`),
  CONSTRAINT `fk_claim_claimant` FOREIGN KEY (`claimant_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_claim_item` FOREIGN KEY (`item_id`) REFERENCES `lost_found_items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_claim_reviewer` FOREIGN KEY (`reviewed_by_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item_claims`
--

LOCK TABLES `item_claims` WRITE;
/*!40000 ALTER TABLE `item_claims` DISABLE KEYS */;
/*!40000 ALTER TABLE `item_claims` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item_matches`
--

DROP TABLE IF EXISTS `item_matches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item_matches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `lost_item_id` bigint NOT NULL,
  `found_item_id` bigint NOT NULL,
  `match_score` double NOT NULL,
  `match_confidence` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `match_explanation` text COLLATE utf8mb4_unicode_ci,
  `is_verified` tinyint(1) NOT NULL DEFAULT '0',
  `verified_by_user_id` bigint DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match_pair` (`lost_item_id`,`found_item_id`),
  KEY `fk_match_found` (`found_item_id`),
  KEY `fk_match_verifier` (`verified_by_user_id`),
  KEY `idx_match_score` (`match_score`),
  CONSTRAINT `fk_match_found` FOREIGN KEY (`found_item_id`) REFERENCES `lost_found_items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_match_lost` FOREIGN KEY (`lost_item_id`) REFERENCES `lost_found_items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_match_verifier` FOREIGN KEY (`verified_by_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item_matches`
--

LOCK TABLES `item_matches` WRITE;
/*!40000 ALTER TABLE `item_matches` DISABLE KEYS */;
INSERT INTO `item_matches` VALUES (1,1,2,88.5,'HIGH','Category match (OTHER), location match (Central Library), matching keywords: [blue, flask, bottle, nasa, sticker], dates within 24h',0,NULL,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,3,4,94,'HIGH','Category match (ELECTRONICS), location match (Science Complex), matching keywords: [macbook, air, space, gray, black, sleeve], reported same day',0,NULL,'2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `item_matches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lost_found_items`
--

DROP TABLE IF EXISTS `lost_found_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lost_found_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `location_found_lost` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `incident_date` date NOT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'REPORTED',
  `reporter_user_id` bigint NOT NULL,
  `contact_email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_lf_reporter` (`reporter_user_id`),
  KEY `idx_lf_type` (`item_type`),
  KEY `idx_lf_category` (`category`),
  KEY `idx_lf_status` (`status`),
  KEY `idx_lf_date` (`incident_date`),
  CONSTRAINT `fk_lf_reporter` FOREIGN KEY (`reporter_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lost_found_items`
--

LOCK TABLES `lost_found_items` WRITE;
/*!40000 ALTER TABLE `lost_found_items` DISABLE KEYS */;
INSERT INTO `lost_found_items` VALUES (1,'LOST','Blue Hydroflask Water Bottle','OTHER','32oz blue steel bottle with NASA sticker and minor dent on bottom rim','Central Library Reading Hall','2026-10-22','REPORTED',4,'student.john@campus.edu','+1-555-0201',NULL,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,'FOUND','Navy Blue Stainless Steel Flask','OTHER','Found 32oz insulated metal water bottle with NASA sticker on table 4','Central Library','2026-10-23','REPORTED',5,'student.alice@campus.edu','+1-555-0202',NULL,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(3,'LOST','Apple MacBook Air M2 Space Gray','ELECTRONICS','Space gray MacBook with sticker of GitHub Octocat, black protective sleeve','Science Complex 2nd Floor','2026-10-24','REPORTED',6,'student.bob@campus.edu','+1-555-0203',NULL,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(4,'FOUND','MacBook Air Space Gray in Black Case','ELECTRONICS','Found laptop in black zippered sleeve left on bench near Lab 201','Science Complex Corridor','2026-10-24','REPORTED',2,'faculty.alan@campus.edu','+1-555-0101',NULL,'2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `lost_found_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `notification_type` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `reference_id` bigint DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_notifications_user_read` (`user_id`,`is_read`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,4,'Booking Approved','Your booking request for Lecture Hall 101 on 2026-10-26 has been approved.','BOOKING',0,1,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,4,'Potential Lost Item Match Found','A found item matching your Blue Hydroflask report was submitted by another campus member.','LOST_FOUND',0,1,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(3,6,'Potential Match Detected','A found laptop matching your reported Space Gray MacBook Air was logged by faculty.','LOST_FOUND',0,3,'2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resources`
--

DROP TABLE IF EXISTS `resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resources` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `resource_type` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `building` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `floor_number` int NOT NULL DEFAULT '1',
  `room_number` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `capacity` int NOT NULL DEFAULT '0',
  `operational_status` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OPERATIONAL',
  `maintenance_notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `resource_code` (`resource_code`),
  KEY `idx_resources_type` (`resource_type`),
  KEY `idx_resources_status` (`operational_status`),
  KEY `idx_resources_building` (`building`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resources`
--

LOCK TABLES `resources` WRITE;
/*!40000 ALTER TABLE `resources` DISABLE KEYS */;
INSERT INTO `resources` VALUES (1,'CR-101','Lecture Hall 101','CLASSROOM','Academic Block A',1,'101',60,'OPERATIONAL','Equipped with 4K laser projector and surround audio','2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,'CR-102','Lecture Hall 102','CLASSROOM','Academic Block A',1,'102',60,'OPERATIONAL','Equipped with smart board and AC','2026-10-29 14:14:36','2026-10-29 14:14:36'),(3,'LAB-201','Advanced Computing Laboratory','LABORATORY','Science Complex',2,'201',40,'OPERATIONAL','40 high-performance workstations with Linux/Windows','2026-10-29 14:14:36','2026-10-29 14:14:36'),(4,'LAB-202','IoT & Embedded Systems Lab','LABORATORY','Science Complex',2,'202',30,'OPERATIONAL','Oscilloscopes, logic analyzers, and sensor kits','2026-10-29 14:14:36','2026-10-29 14:14:36'),(5,'SEM-AUD','Main Seminar Auditorium','SEMINAR_HALL','Central Convention Center',1,'AUD-1',250,'OPERATIONAL','Motorized stage, dual screens, live broadcasting setup','2026-10-29 14:14:36','2026-10-29 14:14:36'),(6,'CR-205','Seminar Room 205 (Maintenance)','CLASSROOM','Academic Block B',2,'205',45,'MAINTENANCE','HVAC repair scheduled through end of week','2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `resources` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `usn` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `department` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `semester` int NOT NULL,
  `phone` varchar(25) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `usn` (`usn`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_students_dept` (`department`),
  KEY `idx_students_semester` (`semester`),
  KEY `idx_students_usn` (`usn`),
  CONSTRAINT `fk_students_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES (1,4,'1CS21CS001','John Doe','student.john@campus.edu','Computer Science',6,'+1-555-0201','Male','2003-05-15','2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,5,'1CS21CS002','Alice Smith','student.alice@campus.edu','Computer Science',6,'+1-555-0202','Female','2003-08-22','2026-10-29 14:14:36','2026-10-29 14:14:36'),(3,6,'1EC21EC015','Bob Miller','student.bob@campus.edu','Electronics & Comm.',4,'+1-555-0203','Male','2004-02-10','2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `timetable_entries`
--

DROP TABLE IF EXISTS `timetable_entries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `timetable_entries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `faculty_id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  `academic_year` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `semester` int NOT NULL,
  `student_group` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `day_of_week` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_tt_course` (`course_id`),
  KEY `idx_tt_group` (`student_group`,`day_of_week`,`start_time`,`end_time`),
  KEY `idx_tt_faculty` (`faculty_id`,`day_of_week`,`start_time`,`end_time`),
  KEY `idx_tt_resource` (`resource_id`,`day_of_week`,`start_time`,`end_time`),
  CONSTRAINT `fk_tt_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tt_faculty` FOREIGN KEY (`faculty_id`) REFERENCES `faculty` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tt_resource` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `timetable_entries`
--

LOCK TABLES `timetable_entries` WRITE;
/*!40000 ALTER TABLE `timetable_entries` DISABLE KEYS */;
INSERT INTO `timetable_entries` VALUES (1,1,1,1,'2026-2027',6,'CS-6A','MONDAY','09:00:00','10:00:00','2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,2,1,2,'2026-2027',6,'CS-6A','MONDAY','10:00:00','11:00:00','2026-10-29 14:14:36','2026-10-29 14:14:36'),(3,3,2,3,'2026-2027',6,'CS-6A','MONDAY','11:15:00','13:15:00','2026-10-29 14:14:36','2026-10-29 14:14:36'),(4,1,1,1,'2026-2027',6,'CS-6B','TUESDAY','10:00:00','11:00:00','2026-10-29 14:14:36','2026-10-29 14:14:36'),(5,4,2,4,'2026-2027',4,'EC-4A','WEDNESDAY','14:00:00','16:00:00','2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `timetable_entries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_users_role` (`role`),
  KEY `idx_users_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin@campus.edu','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','ADMIN',1,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(2,'faculty.alan@campus.edu','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','FACULTY',1,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(3,'faculty.grace@campus.edu','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','FACULTY',1,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(4,'student.john@campus.edu','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','STUDENT',1,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(5,'student.alice@campus.edu','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','STUDENT',1,'2026-10-29 14:14:36','2026-10-29 14:14:36'),(6,'student.bob@campus.edu','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','STUDENT',1,'2026-10-29 14:14:36','2026-10-29 14:14:36');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-04 16:52:34
