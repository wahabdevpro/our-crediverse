-- MariaDB dump 10.19  Distrib 10.7.3-MariaDB, for Linux (x86_64)
--
-- Host: 0.0.0.0    Database: hxc
-- ------------------------------------------------------
-- Server version	10.6.8-MariaDB-1:10.6.8+maria~focal

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ActivityReportData`
--

DROP TABLE IF EXISTS `ActivityReportData`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ActivityReportData` (
  `serviceID` varchar(50) DEFAULT NULL,
  `variantID` varchar(50) DEFAULT NULL,
  `processID` varchar(50) DEFAULT NULL,
  `channel` varchar(50) DEFAULT NULL,
  `succeeded` bigint(20) DEFAULT NULL,
  `failed` bigint(20) DEFAULT NULL,
  `chargeLevied` decimal(20,4) DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CF_Config`
--

DROP TABLE IF EXISTS `CF_Config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CF_Config` (
  `SerialVersionUID` bigint(20) NOT NULL,
  `name` varchar(80) NOT NULL,
  `sequence` int(11) NOT NULL,
  `value` varbinary(512) DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`SerialVersionUID`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SubscriptionReportData`
--

DROP TABLE IF EXISTS `SubscriptionReportData`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SubscriptionReportData` (
  `serviceID` varchar(50) DEFAULT NULL,
  `variantID` varchar(50) DEFAULT NULL,
  `serviceClass` int(11) NOT NULL,
  `subscriptions` bigint(20) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_notification`
--

DROP TABLE IF EXISTS `cf_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cf_notification` (
  `notificationsId` bigint(20) NOT NULL,
  `notificationId` int(11) NOT NULL,
  `parentId` int(11) DEFAULT NULL,
  `sequenceNo` int(11) DEFAULT NULL,
  `description` varchar(512) DEFAULT NULL,
  `language1Text` varchar(512) DEFAULT NULL,
  `language2Text` varchar(512) DEFAULT NULL,
  `language3Text` varchar(512) DEFAULT NULL,
  `language4Text` varchar(512) DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`notificationsId`,`notificationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ct_role`
--

DROP TABLE IF EXISTS `ct_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ct_role` (
  `serverRoleName` varchar(32) NOT NULL,
  `exclusive` bit(1) NOT NULL,
  `attachCommand` varchar(255) DEFAULT NULL,
  `detachCommand` varchar(255) DEFAULT NULL,
  `owner` varchar(64) DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`serverRoleName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ct_server`
--

DROP TABLE IF EXISTS `ct_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ct_server` (
  `serverHost` varchar(64) NOT NULL,
  `peerHost` varchar(64) DEFAULT NULL,
  `transactionNumberPrefix` varchar(50) DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`serverHost`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dw_transactions`
--

DROP TABLE IF EXISTS `dw_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dw_transactions` (
  `a_MSISDN` varchar(24) NOT NULL,
  `b_MSISDN` varchar(24) NOT NULL,
  `startTime` datetime NOT NULL,
  `channel` varchar(64) NOT NULL,
  `requestMode` varchar(16) NOT NULL,
  `serviceID` varchar(64) NOT NULL,
  `variantID` varchar(64) NOT NULL,
  `processID` varchar(64) NOT NULL,
  `chargeLevied` int(11) NOT NULL,
  `returnCode` varchar(64) NOT NULL,
  `rolledBack` bit(1) NOT NULL,
  `followUp` bit(1) NOT NULL,
  `param1` varchar(64) NOT NULL,
  `param2` varchar(64) NOT NULL,
  `additionalInformation` varchar(64) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ea_account`
--

DROP TABLE IF EXISTS `ea_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ea_account` (
  `agent_id` int(11) NOT NULL,
  `balance` decimal(20,4) NOT NULL,
  `bonus` decimal(20,4) NOT NULL,
  `day` datetime NOT NULL,
  `day_count` int(11) NOT NULL,
  `day_total` decimal(20,4) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `month_count` int(11) NOT NULL,
  `month_total` decimal(20,4) NOT NULL,
  `signature` bigint(20) NOT NULL,
  `version` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `on_hold` decimal(20,4) NOT NULL DEFAULT 0.0000,
  PRIMARY KEY (`id`),
  KEY `FKq8qu33yxex9vl2jyjopjebogm` (`agent_id`),
  CONSTRAINT `FKq8qu33yxex9vl2jyjopjebogm` FOREIGN KEY (`agent_id`) REFERENCES `ea_agent` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ea_agent`
--

DROP TABLE IF EXISTS `ea_agent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ea_agent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `acc_no` varchar(20) DEFAULT NULL,
  `a_date` datetime DEFAULT NULL,
  `channels` int(11) NOT NULL,
  `alt_phone` varchar(30) DEFAULT NULL,
  `area_id` int(11) DEFAULT NULL,
  `comp_id` int(11) NOT NULL,
  `dob` datetime DEFAULT NULL,
  `d_date` datetime DEFAULT NULL,
  `domain_account` varchar(40) DEFAULT NULL,
  `e_date` datetime DEFAULT NULL,
  `first_name` varchar(30) NOT NULL,
  `gender` varchar(1) DEFAULT NULL,
  `group_id` int(11) DEFAULT NULL,
  `imei` varchar(16) DEFAULT NULL,
  `imsi` varchar(15) DEFAULT NULL,
  `intitials` varchar(10) DEFAULT NULL,
  `key1` tinyblob DEFAULT NULL,
  `key2` tinyblob DEFAULT NULL,
  `key3` tinyblob DEFAULT NULL,
  `key4` tinyblob DEFAULT NULL,
  `language` varchar(2) NOT NULL,
  `last_imsi` datetime DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `max_daily_amount` decimal(20,4) DEFAULT NULL,
  `max_daily_count` int(11) DEFAULT NULL,
  `max_monthly_amount` decimal(20,4) DEFAULT NULL,
  `max_monthly_count` int(11) DEFAULT NULL,
  `max_amount` decimal(20,4) DEFAULT NULL,
  `max_report_count` int(11) DEFAULT NULL,
  `max_report_daily_schedule_count` int(11) DEFAULT NULL,
  `msisdn` varchar(30) NOT NULL,
  `postal_city` varchar(30) DEFAULT NULL,
  `postal1` varchar(50) DEFAULT NULL,
  `postal2` varchar(50) DEFAULT NULL,
  `postal_suburb` varchar(30) DEFAULT NULL,
  `postal_zip` varchar(10) DEFAULT NULL,
  `sc_id` int(11) DEFAULT NULL,
  `signature` bigint(20) NOT NULL,
  `state` varchar(1) NOT NULL,
  `street_city` varchar(30) DEFAULT NULL,
  `street1` varchar(50) DEFAULT NULL,
  `street2` varchar(50) DEFAULT NULL,
  `street_suburb` varchar(30) DEFAULT NULL,
  `street_zip` varchar(10) DEFAULT NULL,
  `supplier_id` int(11) DEFAULT NULL,
  `surname` varchar(30) NOT NULL,
  `temp_pin` bit(1) NOT NULL,
  `tier_id` int(11) NOT NULL,
  `title` varchar(20) NOT NULL,
  `version` int(11) NOT NULL,
  `warn_level` decimal(20,4) DEFAULT NULL,
  `attempts` int(11) DEFAULT NULL,
  `owner_id` int(11) DEFAULT NULL,
  `role_id` int(11) NOT NULL,
  `email` varchar(50) DEFAULT NULL,
  `pin_version` int(11) NOT NULL DEFAULT 1,
  `last_imei_update` datetime DEFAULT NULL,
  `confirm_ussd` bit(1) DEFAULT b'1',
  `last_cell_expires` datetime DEFAULT NULL,
  `last_cell_id` int(11) DEFAULT NULL,
  `auth_method` varchar(1) NOT NULL DEFAULT 'P',
  PRIMARY KEY (`id`),
  UNIQUE KEY `es_agent_acc_no` (`comp_id`,`acc_no`),
  UNIQUE KEY `es_agent_domain_account` (`comp_id`,`domain_account`),
  KEY `FK_Agent_Group` (`group_id`),
  KEY `FK_Agent_SClass` (`sc_id`),
  KEY `FK_Agent_Supplier` (`supplier_id`),
  KEY `FK_Agent_Tier` (`tier_id`),
  KEY `FK_Agent_Owner` (`owner_id`),
  KEY `FK_Agent_Area` (`area_id`),
  KEY `FK_Agent_Role` (`role_id`),
  KEY `FK_Agent_Cell` (`last_cell_id`),
  KEY `es_agent_msisdn` (`msisdn`),
  CONSTRAINT `FK_Agent_Area` FOREIGN KEY (`area_id`) REFERENCES `el_area` (`id`),
  CONSTRAINT `FK_Agent_Cell` FOREIGN KEY (`last_cell_id`) REFERENCES `el_cell` (`id`),
  CONSTRAINT `FK_Agent_Group` FOREIGN KEY (`group_id`) REFERENCES `et_group` (`id`),
  CONSTRAINT `FK_Agent_Owner` FOREIGN KEY (`owner_id`) REFERENCES `ea_agent` (`id`),
  CONSTRAINT `FK_Agent_Role` FOREIGN KEY (`role_id`) REFERENCES `es_role` (`id`),
  CONSTRAINT `FK_Agent_SClass` FOREIGN KEY (`sc_id`) REFERENCES `et_sclass` (`id`),
  CONSTRAINT `FK_Agent_Supplier` FOREIGN KEY (`supplier_id`) REFERENCES `ea_agent` (`id`),
  CONSTRAINT `FK_Agent_Tier` FOREIGN KEY (`tier_id`) REFERENCES `et_tier` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ea_user`
--

DROP TABLE IF EXISTS `ea_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ea_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `acc_no` varchar(20) DEFAULT NULL,
  `a_date` datetime DEFAULT NULL,
  `agent_id` int(11) NOT NULL,
  `channels` int(11) NOT NULL,
  `comp_id` int(11) NOT NULL,
  `attempts` int(11) DEFAULT NULL,
  `d_date` datetime DEFAULT NULL,
  `dept` varchar(20) DEFAULT NULL,
  `domain_account` varchar(40) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `e_date` datetime DEFAULT NULL,
  `first_name` varchar(30) NOT NULL,
  `imei` varchar(16) DEFAULT NULL,
  `imsi` varchar(15) DEFAULT NULL,
  `intitials` varchar(10) DEFAULT NULL,
  `key1` tinyblob DEFAULT NULL,
  `key2` tinyblob DEFAULT NULL,
  `key3` tinyblob DEFAULT NULL,
  `key4` tinyblob DEFAULT NULL,
  `language` varchar(2) NOT NULL,
  `last_imsi` datetime DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `msisdn` varchar(30) NOT NULL,
  `role_id` int(11) NOT NULL,
  `state` varchar(1) NOT NULL,
  `surname` varchar(30) NOT NULL,
  `temp_pin` bit(1) NOT NULL,
  `title` varchar(20) DEFAULT NULL,
  `version` int(11) NOT NULL,
  `pin_version` int(11) NOT NULL DEFAULT 1,
  `last_imei_update` datetime DEFAULT NULL,
  `last_cell_expires` datetime DEFAULT NULL,
  `last_cell_id` int(11) DEFAULT NULL,
  `auth_method` varchar(1) NOT NULL DEFAULT 'P',
  `channel_type` varchar(2) DEFAULT NULL, 
  PRIMARY KEY (`id`),
  UNIQUE KEY `ea_user_msisdn` (`msisdn`),
  UNIQUE KEY `ea_user_acc_no` (`comp_id`,`acc_no`),
  UNIQUE KEY `ea_user_domain_account` (`comp_id`,`domain_account`),
  KEY `FK_AgentUser_Agent` (`agent_id`),
  KEY `FK_AgentUser_Role` (`role_id`),
  KEY `FK_AgentUser_Cell` (`last_cell_id`),
  CONSTRAINT `FK_AgentUser_Agent` FOREIGN KEY (`agent_id`) REFERENCES `ea_agent` (`id`),
  CONSTRAINT `FK_AgentUser_Cell` FOREIGN KEY (`last_cell_id`) REFERENCES `el_cell` (`id`),
  CONSTRAINT `FK_AgentUser_Role` FOREIGN KEY (`role_id`) REFERENCES `es_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `eb_batch`
--

DROP TABLE IF EXISTS `eb_batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `eb_batch` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `coauth_id` int(11) DEFAULT NULL,
  `company_id` int(11) NOT NULL,
  `completed` bit(1) NOT NULL,
  `delete_count` int(11) NOT NULL,
  `domain_name` varchar(40) NOT NULL,
  `failure_count` int(11) NOT NULL,
  `filesize` bigint(20) DEFAULT NULL,
  `filename` varchar(40) NOT NULL,
  `headings` varchar(2048) DEFAULT NULL,
  `insert_count` int(11) NOT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `line_count` int(11) NOT NULL,
  `mac_address` varchar(17) DEFAULT NULL,
  `machine_name` varchar(16) DEFAULT NULL,
  `next_offset` bigint(20) NOT NULL,
  `residual_crypto` blob DEFAULT NULL,
  `residual_text` varchar(1025) DEFAULT NULL,
  `signature` bigint(20) NOT NULL,
  `time_stamp` datetime NOT NULL,
  `total_value` decimal(19,2) DEFAULT NULL,
  `type` varchar(10) NOT NULL,
  `update_count` int(11) NOT NULL,
  `version` int(11) NOT NULL,
  `webuser_id` int(11) NOT NULL,
  `state` varchar(1) NOT NULL DEFAULT 'U',
  `total_value2` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `eb_batch_filename` (`company_id`,`filename`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `eb_stage`
--

DROP TABLE IF EXISTS `eb_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `eb_stage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `acc_no` varchar(20) DEFAULT NULL,
  `action` int(11) NOT NULL,
  `agent_id` int(11) DEFAULT NULL,
  `alt_phone` varchar(30) DEFAULT NULL,
  `area_id` int(11) DEFAULT NULL,
  `b1` bit(1) DEFAULT NULL,
  `b2` bit(1) DEFAULT NULL,
  `batch_id` int(11) NOT NULL,
  `bd1` decimal(22,5) DEFAULT NULL,
  `bd2` decimal(22,5) DEFAULT NULL,
  `bd3` decimal(22,5) DEFAULT NULL,
  `bd4` decimal(22,5) DEFAULT NULL,
  `company_id` int(11) NOT NULL,
  `d1` datetime DEFAULT NULL,
  `d2` datetime DEFAULT NULL,
  `d3` datetime DEFAULT NULL,
  `d4` datetime DEFAULT NULL,
  `d5` datetime DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  `domain_account` varchar(40) DEFAULT NULL,
  `entity_id` int(11) DEFAULT NULL,
  `entity_version` int(11) DEFAULT NULL,
  `group_id` int(11) DEFAULT NULL,
  `i1` int(11) DEFAULT NULL,
  `i2` int(11) DEFAULT NULL,
  `i3` int(11) DEFAULT NULL,
  `imei` varchar(16) DEFAULT NULL,
  `imsi` varchar(15) DEFAULT NULL,
  `intitials` varchar(10) DEFAULT NULL,
  `l1` bigint(20) DEFAULT NULL,
  `language` varchar(2) DEFAULT NULL,
  `lm_time` datetime DEFAULT NULL,
  `lm_userid` int(11) NOT NULL,
  `line_no` int(11) NOT NULL,
  `msisdn` varchar(30) DEFAULT NULL,
  `name` varchar(80) DEFAULT NULL,
  `postal_city` varchar(30) DEFAULT NULL,
  `postal1` varchar(50) DEFAULT NULL,
  `postal2` varchar(50) DEFAULT NULL,
  `postal_suburb` varchar(30) DEFAULT NULL,
  `postal_zip` varchar(10) DEFAULT NULL,
  `sc_id` int(11) DEFAULT NULL,
  `signature` bigint(20) DEFAULT NULL,
  `state` varchar(1) DEFAULT NULL,
  `street_city` varchar(30) DEFAULT NULL,
  `street1` varchar(50) DEFAULT NULL,
  `street2` varchar(50) DEFAULT NULL,
  `street_suburb` varchar(30) DEFAULT NULL,
  `street_zip` varchar(10) DEFAULT NULL,
  `table_id` int(11) NOT NULL,
  `tier_id1` int(11) DEFAULT NULL,
  `tier_id2` int(11) DEFAULT NULL,
  `title` varchar(20) DEFAULT NULL,
  `type` varchar(1) DEFAULT NULL,
  `version` int(11) NOT NULL,
  `key1` tinyblob DEFAULT NULL,
  `zip` varchar(10) DEFAULT NULL,
  `i4` int(11) DEFAULT NULL,
  `i5` int(11) DEFAULT NULL,
  `r1` double DEFAULT NULL,
  `r2` double DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `audit_action` varchar(1) DEFAULT NULL,
  `audit_signature` bigint(20) DEFAULT NULL,
  `seq_no` varchar(17) DEFAULT NULL,
  `new_value` text DEFAULT NULL,
  `old_value` text DEFAULT NULL,
  `s1` varchar(10) DEFAULT NULL,
  `max_report_count` int(11) DEFAULT NULL,
  `max_report_daily_schedule_count` int(11) DEFAULT NULL,
  `auth_method` varchar(1) DEFAULT NULL,
  `area_ids` text DEFAULT NULL,
  `cell_group_ids` text DEFAULT NULL,
  `code` varchar(80) DEFAULT NULL,
  `bd5` decimal(22,5) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ec_bundle`
--

DROP TABLE IF EXISTS `ec_bundle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ec_bundle` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `description` varchar(100) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `ordinal` int(11) NOT NULL,
  `name` varchar(30) NOT NULL,
  `price` decimal(20,4) NOT NULL,
  `state` varchar(1) NOT NULL,
  `tag` varchar(15) NOT NULL,
  `disc_pct` decimal(20,8) NOT NULL,
  `type` varchar(30) NOT NULL,
  `version` int(11) NOT NULL,
  `sms_keyword` varchar(20) DEFAULT NULL,
  `ussd_code` varchar(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ec_bundle_name` (`company_id`,`name`),
  UNIQUE KEY `ec_bundle_tag` (`company_id`,`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ec_bundle_lang`
--

DROP TABLE IF EXISTS `ec_bundle_lang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ec_bundle_lang` (
  `language` varchar(2) NOT NULL,
  `bundle_id` int(11) NOT NULL,
  `description` varchar(100) NOT NULL,
  `name` varchar(30) NOT NULL,
  `type` varchar(30) NOT NULL,
  `version` int(11) NOT NULL,
  `sms_keyword` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`language`,`bundle_id`),
  KEY `FK_Bundle_Lang` (`bundle_id`),
  CONSTRAINT `FK_Bundle_Lang` FOREIGN KEY (`bundle_id`) REFERENCES `ec_bundle` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ec_config`
--

DROP TABLE IF EXISTS `ec_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ec_config` (
  `id` bigint(20) NOT NULL,
  `comp_id` int(11) NOT NULL,
  `content` blob NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`,`comp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ec_number`
--

DROP TABLE IF EXISTS `ec_number`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ec_number` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `next_value` bigint(20) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ec_number_company` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ec_smsq`
--

DROP TABLE IF EXISTS `ec_smsq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ec_smsq` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `attempts_left` int(11) NOT NULL,
  `start_second` int(11) NOT NULL,
  `company_id` int(11) NOT NULL,
  `end_second` int(11) NOT NULL,
  `expiry_time` datetime DEFAULT NULL,
  `lang3` varchar(3) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `msisdn` varchar(30) NOT NULL,
  `notification` text NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ec_smsq_ix1` (`company_id`,`start_second`,`end_second`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ec_state`
--

DROP TABLE IF EXISTS `ec_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ec_state` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `value` bigint(20) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ec_state_name` (`company_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ec_transact`
--

DROP TABLE IF EXISTS `ec_transact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ec_transact` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `a_agent` int(11) DEFAULT NULL,
  `a_after` decimal(20,4) DEFAULT NULL,
  `a_before` decimal(20,4) DEFAULT NULL,
  `a_bonus_after` decimal(20,4) DEFAULT NULL,
  `a_bonus_before` decimal(20,4) DEFAULT NULL,
  `a_cell` int(11) DEFAULT NULL,
  `a_cell_group_id` int(11) DEFAULT NULL,
  `a_group` int(11) DEFAULT NULL,
  `a_imei` varchar(16) DEFAULT NULL,
  `a_imsi` varchar(15) DEFAULT NULL,
  `a_msisdn` varchar(30) DEFAULT NULL,
  `a_sc` int(11) DEFAULT NULL,
  `a_tier` int(11) DEFAULT NULL,
  `additional` varchar(100) DEFAULT NULL,
  `amount` decimal(20,4) DEFAULT NULL,
  `gross_sales_amount` decimal(20,4) DEFAULT NULL,
  `cost_of_goods_sold` decimal(20,4) DEFAULT NULL,
  `b_agent` int(11) DEFAULT NULL,
  `b_after` decimal(20,4) DEFAULT NULL,
  `b_before` decimal(20,4) DEFAULT NULL,
  `b_bonus_after` decimal(20,4) DEFAULT NULL,
  `b_bonus_before` decimal(20,4) DEFAULT NULL,
  `b_cell` int(11) DEFAULT NULL,
  `b_cell_group_id` int(11) DEFAULT NULL,
  `b_group` int(11) DEFAULT NULL,
  `b_imei` varchar(16) DEFAULT NULL,
  `b_imsi` varchar(15) DEFAULT NULL,
  `b_msisdn` varchar(30) DEFAULT NULL,
  `b_sc` int(11) DEFAULT NULL,
  `b_tier` int(11) DEFAULT NULL,
  `bonus` decimal(20,4) DEFAULT NULL,
  `bonus_pct` decimal(20,8) DEFAULT NULL,
  `bonus_prov` decimal(20,4) DEFAULT NULL,
  `caller` varchar(30) NOT NULL,
  `channel` varchar(1) NOT NULL,
  `charge` decimal(20,4) DEFAULT NULL,
  `comp_id` int(11) NOT NULL,
  `ended` datetime NOT NULL,
  `follow_up` bit(1) NOT NULL,
  `host` varchar(16) NOT NULL,
  `in_session` varchar(32) DEFAULT NULL,
  `in_transact` varchar(32) DEFAULT NULL,
  `ext_code` varchar(15) DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `no` varchar(11) NOT NULL,
  `mode` varchar(1) NOT NULL,
  `ret_code` varchar(20) NOT NULL,
  `reversed_id` bigint(20) DEFAULT NULL,
  `rolled_back` bit(1) NOT NULL,
  `started` datetime NOT NULL,
  `rule_id` int(11) DEFAULT NULL,
  `type` varchar(2) NOT NULL,
  `version` int(11) NOT NULL,
  `a_area` int(11) DEFAULT NULL,
  `b_area` int(11) DEFAULT NULL,
  `a_owner` int(11) DEFAULT NULL,
  `b_owner` int(11) DEFAULT NULL,
  `bundle_id` int(11) DEFAULT NULL,
  `prom_id` int(11) DEFAULT NULL,
  `req_msisdn` varchar(30) NOT NULL,
  `req_type` varchar(1) NOT NULL,
  `a_hold_before` decimal(20,4) DEFAULT NULL,
  `a_hold_after` decimal(20,4) DEFAULT NULL,
  `version_81` bit(1) DEFAULT NULL,
  `b_transfer_bonus_amount` decimal(20,4) DEFAULT NULL,
  `b_transfer_bonus_profile` varchar(10) DEFAULT NULL,
  `channel_type` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ec_transact_no` (`comp_id`,`no`),
  KEY `ix_ec_transact_started` (`comp_id`,`started`),
  KEY `ix_ec_transact_rev` (`reversed_id`),
  KEY `ix_ec_transact_amsisdn` (`comp_id`,`a_msisdn`),
  KEY `ix_ec_transact_bmsisdn` (`comp_id`,`b_msisdn`),
  KEY `ix_ec_transact_aagent` (`a_agent`),
  KEY `ix_ec_transact_bagent` (`b_agent`),
  KEY `ix_ec_transact_aowner` (`a_owner`),
  KEY `ix_ec_transact_bowner` (`b_owner`),
  KEY `ix_ec_transact_ended` (`comp_id`,`ended`),
  KEY `ix_ec_transact_version_81` (`version_81`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ec_transact_ex`
--

DROP TABLE IF EXISTS `ec_transact_ex`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ec_transact_ex` (
  `transaction_id` bigint(20) NOT NULL,
  `key_type` varchar(50) NOT NULL,
  `value` longtext NOT NULL,
  UNIQUE KEY `ec_transact_ex_id` (`transaction_id`,`key_type`),
  KEY `FK_Transcation` (`transaction_id`),
  CONSTRAINT `FK_Transcation` FOREIGN KEY (`transaction_id`) REFERENCES `ec_transact` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `el_area`
--

DROP TABLE IF EXISTS `el_area`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `el_area` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `name` varchar(30) NOT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `type` varchar(30) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `el_area_name_type` (`company_id`,`name`,`type`),
  KEY `FK_Area_Parent` (`parent_id`),
  CONSTRAINT `FK_Area_Parent` FOREIGN KEY (`parent_id`) REFERENCES `el_area` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `el_area_area`
--

DROP TABLE IF EXISTS `el_area_area`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `el_area_area` (
  `id` int(11) NOT NULL,
  `sub_id` int(11) NOT NULL,
  KEY `FK8nrpxh16nsfr7k3v87b8tbp15` (`sub_id`),
  KEY `FKfgnro2b070i0ue51em6a2u69q` (`id`),
  CONSTRAINT `FK8nrpxh16nsfr7k3v87b8tbp15` FOREIGN KEY (`sub_id`) REFERENCES `el_area` (`id`),
  CONSTRAINT `FKfgnro2b070i0ue51em6a2u69q` FOREIGN KEY (`id`) REFERENCES `el_area` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `el_area_cell`
--

DROP TABLE IF EXISTS `el_area_cell`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `el_area_cell` (
  `area_id` int(11) NOT NULL,
  `cell_id` int(11) NOT NULL,
  KEY `FKf5rfuxdkcly9fs0txcfi1njdi` (`cell_id`),
  KEY `FK2djs0n5sotjaeyn0x3ax5vqr8` (`area_id`),
  CONSTRAINT `FK2djs0n5sotjaeyn0x3ax5vqr8` FOREIGN KEY (`area_id`) REFERENCES `el_area` (`id`),
  CONSTRAINT `FKf5rfuxdkcly9fs0txcfi1njdi` FOREIGN KEY (`cell_id`) REFERENCES `el_cell` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `el_cell`
--

DROP TABLE IF EXISTS `el_cell`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `el_cell` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cell_id` int(11) NOT NULL,
  `company_id` int(11) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `lat` double DEFAULT NULL,
  `lac` int(11) NOT NULL,
  `lng` double DEFAULT NULL,
  `mcc` int(11) NOT NULL,
  `mnc` int(11) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `el_cell_cid` (`company_id`,`cell_id`,`lac`,`mnc`,`mcc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `el_cell_cell_group`
--

DROP TABLE IF EXISTS `el_cell_cell_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `el_cell_cell_group` (
  `cell_id` int(11) NOT NULL,
  `cell_group_id` int(11) NOT NULL,
  KEY `FKb41f4e1149a64c209750fe32bb7b9f14` (`cell_group_id`),
  KEY `FKb2f8137cf4d54a1bb864230c1af0d85e` (`cell_id`),
  CONSTRAINT `FK1tejstrw84dfpl7gcuhraibli` FOREIGN KEY (`cell_group_id`) REFERENCES `el_cell_group` (`id`),
  CONSTRAINT `FKb2f8137cf4d54a1bb864230c1af0d85e` FOREIGN KEY (`cell_id`) REFERENCES `el_cell` (`id`),
  CONSTRAINT `FKb41f4e1149a64c209750fe32bb7b9f14` FOREIGN KEY (`cell_group_id`) REFERENCES `el_cell_group` (`id`),
  CONSTRAINT `FKjalu9hgqai06fy6cnyswdjbh8` FOREIGN KEY (`cell_id`) REFERENCES `el_cell` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `el_cell_group`
--

DROP TABLE IF EXISTS `el_cell_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `el_cell_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(5) NOT NULL,
  `company_id` int(11) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `name` varchar(20) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `el_cell_group_name` (`company_id`,`name`),
  UNIQUE KEY `el_cell_group_code` (`company_id`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ep_promo`
--

DROP TABLE IF EXISTS `ep_promo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ep_promo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `area_id` int(11) DEFAULT NULL,
  `bundle_id` int(11) DEFAULT NULL,
  `company_id` int(11) NOT NULL,
  `end_time` datetime NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `reward_amt` decimal(20,4) NOT NULL,
  `reward_pct` decimal(20,8) NOT NULL,
  `sc_id` int(11) DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `state` varchar(1) NOT NULL,
  `tgt_amount` decimal(20,4) NOT NULL,
  `tgt_period` int(11) NOT NULL,
  `rule_id` int(11) DEFAULT NULL,
  `version` int(11) NOT NULL,
  `retriggers` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ep_promo_name` (`company_id`,`name`),
  KEY `FK_Promo_Area` (`area_id`),
  KEY `FK_Promo_SC` (`sc_id`),
  KEY `FK_Promo_Rule` (`rule_id`),
  KEY `FK_Promo_Bundle` (`bundle_id`),
  CONSTRAINT `FK_Promo_Area` FOREIGN KEY (`area_id`) REFERENCES `el_area` (`id`),
  CONSTRAINT `FK_Promo_Bundle` FOREIGN KEY (`bundle_id`) REFERENCES `ec_bundle` (`id`),
  CONSTRAINT `FK_Promo_Rule` FOREIGN KEY (`rule_id`) REFERENCES `et_rule` (`id`),
  CONSTRAINT `FK_Promo_SC` FOREIGN KEY (`sc_id`) REFERENCES `et_sclass` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ep_qualify`
--

DROP TABLE IF EXISTS `ep_qualify`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ep_qualify` (
  `id` bigint(20) NOT NULL,
  `agent_id` int(11) NOT NULL,
  `amount_left` decimal(20,4) NOT NULL,
  `bundle_id` int(11) DEFAULT NULL,
  `cell_id` int(11) DEFAULT NULL,
  `company_id` int(11) NOT NULL,
  `evaluated` bit(1) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `sc_id` int(11) DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `rule_id` int(11) DEFAULT NULL,
  `version` int(11) NOT NULL,
  `blocked` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `ep_qualify_ix1` (`company_id`,`evaluated`,`agent_id`,`start_time`),
  KEY `ep_qualify_ix2` (`company_id`,`start_time`),
  KEY `ep_qualify_ix3` (`company_id`,`amount_left`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `er_report`
--

DROP TABLE IF EXISTS `er_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `er_report` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `agent_id` int(11) NOT NULL DEFAULT 0,
  `description` varchar(80) NOT NULL,
  `internal_name` varchar(50) DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `originator` varchar(64) DEFAULT NULL,
  `parameters` text NOT NULL,
  `type` varchar(50) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `er_report_name` (`company_id`,`agent_id`,`name`),
  UNIQUE KEY `er_report_internal_name` (`company_id`,`agent_id`,`internal_name`),
  KEY `er_report_agent_id` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `er_report_schedule`
--

DROP TABLE IF EXISTS `er_report_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `er_report_schedule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `description` varchar(80) NOT NULL,
  `enabled` tinyint(4) NOT NULL,
  `end_time_of_day` int(11) DEFAULT NULL,
  `internal_name` varchar(50) DEFAULT NULL,
  `last_executed` datetime DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `originator` varchar(64) DEFAULT NULL,
  `period` varchar(255) NOT NULL,
  `report_specification_id` int(11) NOT NULL,
  `start_time_of_day` int(11) DEFAULT NULL,
  `time_of_day` int(11) DEFAULT NULL,
  `delivery_channels` varchar(64) DEFAULT NULL,
  `email_to_agent` tinyint(4) NOT NULL DEFAULT 0,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `er_report_schedule_internal_name` (`company_id`,`internal_name`),
  KEY `FK_ReportSchedule_ReportSpecification` (`report_specification_id`),
  CONSTRAINT `FK_ReportSchedule_ReportSpecification` FOREIGN KEY (`report_specification_id`) REFERENCES `er_report` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `er_report_schedule_agent_user`
--

DROP TABLE IF EXISTS `er_report_schedule_agent_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `er_report_schedule_agent_user` (
  `report_schedule_id` int(11) NOT NULL,
  `agent_user_id` int(11) NOT NULL,
  KEY `er_report_schedule_agent_user_agent_user_id` (`agent_user_id`),
  KEY `er_report_schedule_agent_user_report_schedule_id` (`report_schedule_id`),
  CONSTRAINT `FKckc999ego6fqtcstpjvr6hjx8` FOREIGN KEY (`report_schedule_id`) REFERENCES `er_report_schedule` (`id`),
  CONSTRAINT `FKk5sa8nb4lecgdhyj93tmrlqmv` FOREIGN KEY (`agent_user_id`) REFERENCES `ea_user` (`id`),
  CONSTRAINT `fk_er_report_schedule_agent_user_agent_user_id` FOREIGN KEY (`agent_user_id`) REFERENCES `ea_user` (`id`),
  CONSTRAINT `fk_er_report_schedule_agent_user_report_schedule_id` FOREIGN KEY (`report_schedule_id`) REFERENCES `er_report_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `er_report_schedule_recipient_email`
--

DROP TABLE IF EXISTS `er_report_schedule_recipient_email`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `er_report_schedule_recipient_email` (
  `report_schedule_id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  PRIMARY KEY (`report_schedule_id`,`email`),
  CONSTRAINT `FKcjvlaegpn4128svql3frxg9st` FOREIGN KEY (`report_schedule_id`) REFERENCES `er_report_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `er_report_schedule_webuser`
--

DROP TABLE IF EXISTS `er_report_schedule_webuser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `er_report_schedule_webuser` (
  `report_schedule_id` int(11) NOT NULL,
  `webuser_id` int(11) NOT NULL,
  KEY `FKc6vop0e63kme5scs7s8matq2y` (`webuser_id`),
  KEY `FKfdowoe55laix6uoehv88f7j56` (`report_schedule_id`),
  CONSTRAINT `FKc6vop0e63kme5scs7s8matq2y` FOREIGN KEY (`webuser_id`) REFERENCES `es_webuser` (`id`),
  CONSTRAINT `FKfdowoe55laix6uoehv88f7j56` FOREIGN KEY (`report_schedule_id`) REFERENCES `er_report_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `es_audit`
--

DROP TABLE IF EXISTS `es_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `es_audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action` varchar(1) NOT NULL,
  `company_id` int(11) NOT NULL,
  `data_type` varchar(15) NOT NULL,
  `domain_name` varchar(40) NOT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `mac_address` varchar(17) DEFAULT NULL,
  `machine_name` varchar(16) DEFAULT NULL,
  `new_value` longtext DEFAULT NULL,
  `old_value` longtext DEFAULT NULL,
  `seq_no` varchar(17) NOT NULL,
  `signature` bigint(20) NOT NULL,
  `time_stamp` datetime NOT NULL,
  `version` int(11) NOT NULL,
  `webuser_id` int(11) NOT NULL,
  `agentuser_id` int(11) DEFAULT NULL,
  `reason_code` varchar(64) DEFAULT NULL,
  `reason_attributes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `time_stamp` (`time_stamp`),
  KEY `lm_time_index` (`lm_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `es_client`
--

DROP TABLE IF EXISTS `es_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `es_client` (
  `user_type` varchar(1) NOT NULL,
  `user_id` int(11) NOT NULL,
  `value_key` varchar(20) NOT NULL,
  `company_id` int(11) NOT NULL,
  `lm_time` datetime NOT NULL,
  `last_date` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `value_text` text DEFAULT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`company_id`,`user_type`,`user_id`,`value_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `es_company`
--

DROP TABLE IF EXISTS `es_company`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `es_company` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `country` varchar(2) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `prefix` varchar(2) NOT NULL,
  `state` varchar(1) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_qwjkbmikvb605exes435jur27` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `es_dept`
--

DROP TABLE IF EXISTS `es_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `es_dept` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `name` varchar(20) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `es_dept_name` (`company_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `es_permission`
--

DROP TABLE IF EXISTS `es_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `es_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(80) NOT NULL,
  `grp` varchar(50) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `supplier_only` bit(1) NOT NULL,
  `version` int(11) NOT NULL,
  `agent_allowed` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `es_permission_group_name` (`grp`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `es_role`
--

DROP TABLE IF EXISTS `es_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `es_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `description` varchar(80) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `permanent` bit(1) NOT NULL,
  `version` int(11) NOT NULL,
  `type` varchar(1) NOT NULL DEFAULT 'W',
  PRIMARY KEY (`id`),
  UNIQUE KEY `es_role_name` (`company_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `es_role_permission`
--

DROP TABLE IF EXISTS `es_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `es_role_permission` (
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  KEY `FKroj89ohr62axxlibjf8bb0ryl` (`permission_id`),
  KEY `FK105gkhl24ldxex9u1fag8rl64` (`role_id`),
  CONSTRAINT `FK105gkhl24ldxex9u1fag8rl64` FOREIGN KEY (`role_id`) REFERENCES `es_role` (`id`),
  CONSTRAINT `FKroj89ohr62axxlibjf8bb0ryl` FOREIGN KEY (`permission_id`) REFERENCES `es_permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `es_webuser`
--

DROP TABLE IF EXISTS `es_webuser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `es_webuser` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `acc_no` varchar(20) DEFAULT NULL,
  `a_date` date DEFAULT NULL,
  `comp_id` int(11) NOT NULL,
  `d_date` date DEFAULT NULL,
  `domain_name` varchar(40) NOT NULL,
  `email` varchar(50) DEFAULT NULL,
  `e_date` date DEFAULT NULL,
  `first_name` varchar(30) NOT NULL,
  `initials` varchar(10) NOT NULL,
  `key1` tinyblob DEFAULT NULL,
  `key2` tinyblob DEFAULT NULL,
  `lang` varchar(2) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `msisdn` varchar(30) NOT NULL,
  `state` varchar(1) NOT NULL,
  `last_name` varchar(30) NOT NULL,
  `title` varchar(20) NOT NULL,
  `version` int(11) NOT NULL,
  `dept_id` int(11) NOT NULL,
  `auth_method` varchar(1) NOT NULL DEFAULT 'X',
  `attempts` int(11) DEFAULT 0,
  `key3` tinyblob DEFAULT NULL,
  `key4` tinyblob DEFAULT NULL,
  `pin_version` int(11) NOT NULL DEFAULT 0,
  `temp_pin` bit(1) NOT NULL DEFAULT b'0',
  `service_user` BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `es_webuser_domain_name` (`comp_id`,`domain_name`),
  UNIQUE KEY `es_webuser_acc_no` (`comp_id`,`acc_no`),
  KEY `FK_User_Dept` (`dept_id`),
  CONSTRAINT `FK_User_Dept` FOREIGN KEY (`dept_id`) REFERENCES `es_dept` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `es_webuser_role`
--

DROP TABLE IF EXISTS `es_webuser_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `es_webuser_role` (
  `webuser_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  KEY `FKpwoiu0yasx9wwdn5kvx8yqu3l` (`role_id`),
  KEY `FK1wyrvmdr2mge50sl3vutxatfu` (`webuser_id`),
  CONSTRAINT `FK1wyrvmdr2mge50sl3vutxatfu` FOREIGN KEY (`webuser_id`) REFERENCES `es_webuser` (`id`),
  CONSTRAINT `FKpwoiu0yasx9wwdn5kvx8yqu3l` FOREIGN KEY (`role_id`) REFERENCES `es_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `et_group`
--

DROP TABLE IF EXISTS `et_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `et_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `description` varchar(80) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `max_daily_amount` decimal(20,4) DEFAULT NULL,
  `max_daily_count` int(11) DEFAULT NULL,
  `max_monthly_amount` decimal(20,4) DEFAULT NULL,
  `max_monthly_count` int(11) DEFAULT NULL,
  `max_amount` decimal(20,4) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `state` varchar(1) NOT NULL,
  `tier_id` int(11) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `et_group_name` (`company_id`,`name`),
  KEY `FK_Group_Tier` (`tier_id`),
  CONSTRAINT `FK_Group_Tier` FOREIGN KEY (`tier_id`) REFERENCES `et_tier` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `et_rule`
--

DROP TABLE IF EXISTS `et_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `et_rule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `area_id` int(11) DEFAULT NULL,
  `company_id` int(11) NOT NULL,
  `dow` int(11) DEFAULT NULL,
  `end_tod` datetime DEFAULT NULL,
  `group_id` int(11) DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `max_amount` decimal(20,4) DEFAULT NULL,
  `min_amount` decimal(20,4) DEFAULT NULL,
  `name` varchar(80) NOT NULL,
  `sc_id` int(11) DEFAULT NULL,
  `s_tier_id` int(11) NOT NULL,
  `start_tod` datetime DEFAULT NULL,
  `state` varchar(1) NOT NULL,
  `strict_area` bit(1) NOT NULL,
  `strict_supplier` bit(1) NOT NULL,
  `t_tier_id` int(11) NOT NULL,
  `bonus_pct` decimal(20,8) DEFAULT NULL,
  `version` int(11) NOT NULL,
  `t_group_id` int(11) DEFAULT NULL,
  `t_sc_id` int(11) DEFAULT NULL,
  `t_bonus_pct` decimal(20,8) DEFAULT NULL,
  `t_bonus_profile` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `et_rule_name` (`company_id`,`name`),
  KEY `FK_Rule_Group` (`group_id`),
  KEY `FK_Rule_SClass` (`sc_id`),
  KEY `FK_Rule_Source` (`s_tier_id`),
  KEY `FK_Rule_Target` (`t_tier_id`),
  KEY `FK_Rule_TGroup` (`t_group_id`),
  KEY `FK_Rule_TSClass` (`t_sc_id`),
  KEY `FK_Rule_Area` (`area_id`),
  CONSTRAINT `FK_Rule_Area` FOREIGN KEY (`area_id`) REFERENCES `el_area` (`id`),
  CONSTRAINT `FK_Rule_Group` FOREIGN KEY (`group_id`) REFERENCES `et_group` (`id`),
  CONSTRAINT `FK_Rule_SClass` FOREIGN KEY (`sc_id`) REFERENCES `et_sclass` (`id`),
  CONSTRAINT `FK_Rule_Source` FOREIGN KEY (`s_tier_id`) REFERENCES `et_tier` (`id`),
  CONSTRAINT `FK_Rule_TGroup` FOREIGN KEY (`t_group_id`) REFERENCES `et_group` (`id`),
  CONSTRAINT `FK_Rule_TSClass` FOREIGN KEY (`t_sc_id`) REFERENCES `et_sclass` (`id`),
  CONSTRAINT `FK_Rule_Target` FOREIGN KEY (`t_tier_id`) REFERENCES `et_tier` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `et_sclass`
--

DROP TABLE IF EXISTS `et_sclass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `et_sclass` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `description` varchar(80) NOT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `max_daily_amount` decimal(20,4) DEFAULT NULL,
  `max_daily_count` int(11) DEFAULT NULL,
  `max_monthly_amount` decimal(20,4) DEFAULT NULL,
  `max_monthly_count` int(11) DEFAULT NULL,
  `max_amount` decimal(20,4) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `state` varchar(1) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `et_sclass_name` (`company_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `et_tier`
--

DROP TABLE IF EXISTS `et_tier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `et_tier` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `description` varchar(80) NOT NULL,
  `down_pct` decimal(20,8) DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `max_daily_amount` decimal(20,4) DEFAULT NULL,
  `max_daily_count` int(11) DEFAULT NULL,
  `max_monthly_amount` decimal(20,4) DEFAULT NULL,
  `max_monthly_count` int(11) DEFAULT NULL,
  `max_amount` decimal(20,4) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `permanent` bit(1) NOT NULL,
  `state` varchar(1) NOT NULL,
  `type` varchar(1) NOT NULL,
  `version` int(11) NOT NULL,
  `allow_intratier_transfer` bit(1) NOT NULL DEFAULT b'0',
  `default_bonus_pct` DECIMAL(20,8) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `et_tier_name` (`company_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ew_item`
--

DROP TABLE IF EXISTS `ew_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ew_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_id` int(11) NOT NULL,
  `completed` datetime DEFAULT NULL,
  `by_agent_id` int(11) DEFAULT NULL,
  `by_user_id` int(11) DEFAULT NULL,
  `for_perm_id` int(11) DEFAULT NULL,
  `for_user_id` int(11) DEFAULT NULL,
  `created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `lm_time` datetime NOT NULL,
  `lm_userid` int(11) NOT NULL,
  `request` text DEFAULT NULL,
  `response` text DEFAULT NULL,
  `send_sms` bit(1) NOT NULL,
  `state` varchar(1) NOT NULL,
  `type` varchar(1) NOT NULL,
  `uri` varchar(200) NOT NULL,
  `uuid` binary(16) NOT NULL,
  `version` int(11) NOT NULL,
  `reason` varchar(150) DEFAULT NULL,
  `owner_session` varchar(36) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `work_type` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ew_item_uuid` (`uuid`),
  KEY `FK_Work_Agent` (`by_agent_id`),
  KEY `FK_Work_By` (`by_user_id`),
  KEY `FK_Work_Perm` (`for_perm_id`),
  KEY `FK_Work_For` (`for_user_id`),
  CONSTRAINT `FK_Work_Agent` FOREIGN KEY (`by_agent_id`) REFERENCES `ea_agent` (`id`),
  CONSTRAINT `FK_Work_By` FOREIGN KEY (`by_user_id`) REFERENCES `es_webuser` (`id`),
  CONSTRAINT `FK_Work_For` FOREIGN KEY (`for_user_id`) REFERENCES `es_webuser` (`id`),
  CONSTRAINT `FK_Work_Perm` FOREIGN KEY (`for_perm_id`) REFERENCES `es_permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lc_subscription`
--

DROP TABLE IF EXISTS `lc_subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lc_subscription` (
  `msisdn` varchar(28) NOT NULL,
  `serviceID` varchar(16) NOT NULL,
  `variantID` varchar(16) NOT NULL,
  `serviceClass` int(11) NOT NULL,
  `nextDateTime` datetime NOT NULL,
  `beingProcessed` bit(1) NOT NULL,
  `state` int(11) NOT NULL,
  `dateTime1` datetime DEFAULT NULL,
  `dateTime2` datetime DEFAULT NULL,
  `dateTime3` datetime DEFAULT NULL,
  `dateTime4` datetime DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`msisdn`,`serviceID`,`variantID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lc_timetrigger`
--

DROP TABLE IF EXISTS `lc_timetrigger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lc_timetrigger` (
  `serviceID` varchar(16) NOT NULL,
  `variantID` varchar(16) NOT NULL,
  `msisdnA` varchar(28) NOT NULL,
  `msisdnB` varchar(28) NOT NULL,
  `keyValue` varchar(28) NOT NULL,
  `nextDateTime` datetime NOT NULL,
  `beingProcessed` bit(1) NOT NULL,
  `state` int(11) NOT NULL,
  `dateTime1` datetime DEFAULT NULL,
  `dateTime2` datetime DEFAULT NULL,
  `dateTime3` datetime DEFAULT NULL,
  `dateTime4` datetime DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`serviceID`,`variantID`,`msisdnA`,`msisdnB`,`keyValue`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mobile_numbers_format_config`
--

DROP TABLE IF EXISTS `mobile_numbers_format_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mobile_numbers_format_config` (
  `id` int(11) NOT NULL,
  `old_number_length` int(11) NOT NULL DEFAULT 8,
  `phase` int(11) NOT NULL DEFAULT 0,
  `wrong_b_number_message_en` varchar(500) DEFAULT NULL,
  `wrong_b_number_message_fr` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mobile_numbers_format_mapping`
--

DROP TABLE IF EXISTS `mobile_numbers_format_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mobile_numbers_format_mapping` (
  `new_prefix` varchar(2) NOT NULL,
  `old_code` varchar(2) NOT NULL,
  PRIMARY KEY (`old_code`),
  UNIQUE KEY `old_code_UNIQUE` (`old_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `non_airtime_transaction_details`
--

DROP TABLE IF EXISTS `non_airtime_transaction_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `non_airtime_transaction_details` (
  `id` bigint(20) NOT NULL,
  `client_transaction_id` varchar(128) NOT NULL,
  `item_description` varchar(512) DEFAULT NULL,
  `service_user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_client_trn_id_per_user` (`client_transaction_id`,`service_user_id`),
  KEY `client_transaction_id_index` (`client_transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `se_perm`
--

DROP TABLE IF EXISTS `se_perm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `se_perm` (
  `permissionId` varchar(50) NOT NULL,
  `category` varchar(50) NOT NULL,
  `path` varchar(250) NOT NULL,
  `description` varchar(80) NOT NULL,
  `implies` varchar(50) NOT NULL,
  `roles` bigint(20) NOT NULL,
  `control` int(11) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`permissionId`,`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `se_role`
--

DROP TABLE IF EXISTS `se_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `se_role` (
  `roleId` int(11) NOT NULL,
  `name` varchar(60) NOT NULL,
  `description` varchar(100) NOT NULL,
  `builtIn` bit(1) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`roleId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `se_user`
--

DROP TABLE IF EXISTS `se_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `se_user` (
  `userId` varchar(15) NOT NULL,
  `name` varchar(60) NOT NULL,
  `mobileNumber` varchar(15) DEFAULT NULL,
  `password` varbinary(50) NOT NULL,
  `publicKey` varbinary(50) NOT NULL,
  `builtIn` bit(1) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `roles` bigint(20) NOT NULL DEFAULT 3,
  `control` int(11) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ec_transact_location`
--

DROP TABLE IF EXISTS `ec_transact_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ec_transact_location` (
  `transaction_id` bigint(20) NOT NULL,
  `latitude` double(11,8) NOT NULL,
  `longitude` double(11,8) NOT NULL,
  KEY `FK_Location_Transaction` (`transaction_id`),
  CONSTRAINT `FK_Location_Transaction` FOREIGN KEY (`transaction_id`) REFERENCES `ec_transact` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-06-01 12:04:10
