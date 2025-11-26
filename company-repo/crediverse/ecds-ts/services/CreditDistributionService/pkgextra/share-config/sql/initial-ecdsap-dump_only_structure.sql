CREATE DATABASE  IF NOT EXISTS `ecdsap` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `ecdsap`;
-- MySQL dump 10.16  Distrib 10.1.47-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: ecdsap
-- ------------------------------------------------------
-- Server version	10.1.47-MariaDB-0ubuntu0.18.04.1

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
-- Table structure for table `ap_agent_account`
--

DROP TABLE IF EXISTS `ap_agent_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ap_agent_account` (
  `id` int(11) NOT NULL,
  `balance` decimal(20,4) NOT NULL,
  `bonus_balance` decimal(20,4) NOT NULL,
  `comp_id` int(11) NOT NULL,
  `group_name` varchar(50) DEFAULT NULL,
  `hold_balance` decimal(20,4) DEFAULT NULL,
  `msisdn` varchar(30) NOT NULL,
  `agent_name` varchar(93) NOT NULL,
  `owner_id` int(11) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `tier_name` varchar(50) DEFAULT NULL,
  `modification_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `ap_agent_account_msisdn` (`msisdn` ASC),
  KEY `ap_agent_account_tier_name` (`tier_name`),
  KEY `ap_agent_account_group_name` (`group_name`),
  KEY `ap_agent_account_owner_id` (`owner_id`),
  KEY `modification_time_index` (`modification_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ap_analytics`
--

DROP TABLE IF EXISTS `ap_analytics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ap_analytics` (
  `transaction_type` varchar(2) NOT NULL,
  `dt` date NOT NULL,
  `data_type` varchar(10) NOT NULL,
  `value` bigint(20) NOT NULL,
  PRIMARY KEY (`transaction_type`,`dt`,`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ap_group`
--

DROP TABLE IF EXISTS `ap_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ap_group` (
  `id` int(11) NOT NULL,
  `comp_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ap_group_name` (`comp_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ap_schema_data`
--

DROP TABLE IF EXISTS `ap_schema_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ap_schema_data` (
  `vkey` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `value` varchar(128) NOT NULL,
  PRIMARY KEY (`vkey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ap_transact`
--

DROP TABLE IF EXISTS `ap_transact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ap_transact` (
  `id` bigint(20) NOT NULL,
  `a_agent_acc_no` varchar(20) DEFAULT NULL,
  `a_agent_id` int(11) DEFAULT NULL,
  `a_agent_name` varchar(82) DEFAULT NULL,
  `a_area_name` varchar(128) DEFAULT NULL,
  `a_after` decimal(20,4) DEFAULT NULL,
  `a_before` decimal(20,4) DEFAULT NULL,
  `a_cell` int(11) DEFAULT NULL,
  `a_group_name` varchar(50) DEFAULT NULL,
  `a_imsi` varchar(15) DEFAULT NULL,
  `a_msisdn` varchar(30) DEFAULT NULL,
  `a_owner_imsi` varchar(15) DEFAULT NULL,
  `a_sc_name` varchar(50) DEFAULT NULL,
  `a_tier_name` varchar(50) DEFAULT NULL,
  `a_tier_type` varchar(1) DEFAULT NULL,
  `amount` decimal(20,4) DEFAULT NULL,
  `gross_sales_amount` decimal(20,4) DEFAULT NULL,
  `cost_of_goods_sold` decimal(20,4) DEFAULT NULL,
  `b_agent_acc_no` varchar(20) DEFAULT NULL,
  `b_agent_id` int(11) DEFAULT NULL,
  `b_agent_name` varchar(82) DEFAULT NULL,
  `b_area_name` varchar(128) DEFAULT NULL,
  `b_after` decimal(20,4) DEFAULT NULL,
  `b_before` decimal(20,4) DEFAULT NULL,
  `b_cell` int(11) DEFAULT NULL,
  `b_group_name` varchar(50) DEFAULT NULL,
  `b_imsi` varchar(15) DEFAULT NULL,
  `b_msisdn` varchar(30) DEFAULT NULL,
  `b_owner_imsi` varchar(15) DEFAULT NULL,
  `b_sc_name` varchar(50) DEFAULT NULL,
  `b_tier_name` varchar(50) DEFAULT NULL,
  `b_tier_type` varchar(1) DEFAULT NULL,
  `bonus` decimal(20,4) DEFAULT NULL,
  `bonus_pct` decimal(20,8) DEFAULT NULL,
  `bonus_prov` decimal(20,4) DEFAULT NULL,
  `bundle_name` varchar(30) DEFAULT NULL,
  `channel` varchar(1) NOT NULL,
  `charge` decimal(20,4) DEFAULT NULL,
  `comp_id` int(11) NOT NULL,
  `ended_date` date DEFAULT NULL,
  `ended_time` time DEFAULT NULL,
  `no` varchar(11) NOT NULL,
  `promotion_name` varchar(50) DEFAULT NULL,
  `mode` varchar(1) NOT NULL,
  `started` datetime NOT NULL,
  `success` tinyint(4) NOT NULL,
  `type` varchar(2) NOT NULL,
  `version` int(11) NOT NULL,
  `a_owner_msisdn` varchar(30) DEFAULT NULL,
  `a_owner_name` varchar(82) DEFAULT NULL,
  `a_owner_id` int(11) DEFAULT NULL,
  `b_owner_msisdn` varchar(30) DEFAULT NULL,
  `b_owner_name` varchar(82) DEFAULT NULL,
  `b_owner_id` int(11) DEFAULT NULL,
  `a_imei` varchar(16) DEFAULT NULL,
  `b_imei` varchar(16) DEFAULT NULL,
  `rolled_back` tinyint(4) DEFAULT NULL,
  `related_id` bigint(20) DEFAULT NULL,
  `follow_up` varchar(255) NOT NULL DEFAULT 'none',
  `b_transfer_bonus_amount` decimal(20,4) DEFAULT NULL,
  `b_transfer_bonus_profile` varchar(10) DEFAULT NULL,
  `modification_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `channel_type` varchar(2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ap_transact_no` (`comp_id`,`no`),
  KEY `ap_transact_type` (`type`),
  KEY `ap_transact_a_tier_type` (`a_tier_type`),
  KEY `ap_transact_a_msisdn` (`a_msisdn`),
  KEY `ap_transact_a_owner_msisdn` (`a_owner_msisdn`),
  KEY `ap_transact_a_tier_name` (`a_tier_name`),
  KEY `ap_transact_a_group_name` (`a_group_name`),
  KEY `ap_transact_a_sc_name` (`a_sc_name`),
  KEY `ap_transact_b_msisdn` (`b_msisdn`),
  KEY `ap_transact_b_owner_msisdn` (`b_owner_msisdn`),
  KEY `ap_transact_b_tier_name` (`b_tier_name`),
  KEY `ap_transact_b_group_name` (`b_group_name`),
  KEY `ap_transact_b_sc_name` (`b_sc_name`),
  KEY `follow_up` (`follow_up`),
  KEY `ap_transact_follow_up` (`follow_up`),
  KEY `ap_transact_aggregate000` (`ended_date`,`a_msisdn`,`type`,`success`,`b_msisdn`),
  KEY `ap_transact_ended` (`ended_date`,`ended_time`),
  KEY `ap_transact_aggregate001` (`a_owner_id`,`ended_date`,`a_msisdn`,`type`,`success`,`b_msisdn`),
  KEY `ap_transact_aggregate002` (`b_owner_id`,`ended_date`,`a_msisdn`,`type`,`success`,`b_msisdn`),
  KEY `modification_time_index` (`modification_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'ecdsap'
--

--
-- Dumping routines for database 'ecdsap'
--
/*!50003 DROP PROCEDURE IF EXISTS `dorepeat` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `dorepeat`(start_number INT, end_number INT)
BEGIN
    
    DECLARE a_agent_id int(11);
    DECLARE a_agent_balance int;
    DECLARE a_agent_group_name varchar(50);
    DECLARE a_agent_msisdn varchar(30);
    DECLARE a_agent_name varchar(93);
    DECLARE a_agent_tier_name varchar(50);
    
    DECLARE max_agent_id int;
    DECLARE last_used_agent_id int;
    
    DECLARE channel varchar(1);
    DECLARE started_ended datetime;
    DECLARE success int;
    DECLARE tr_type varchar(50);
    DECLARE version int;
    DECLARE follow_up int;
    DECLARE amount decimal(20,4);
    DECLARE ended_date date;
    DECLARE ended_time time;
    DECLARE related_id bigint(20);
    
	DECLARE CONTINUE HANDLER FOR SQLSTATE '23000' SET @x = @x;
    
    SET @max_agent_id = (SELECT MAX(id) FROM ap_agent_account);
    SET @last_used_agent_id = 1;
    
    SET @x = start_number;
    
    REPEAT
		BEGIN
	-- Agent
			SELECT id, group_name, msisdn, agent_name, tier_name
				INTO @a_agent_id, @a_agent_group_name, @a_agent_msisdn, @a_agent_name, @a_agent_tier_name
				FROM ecdsap.ap_agent_account WHERE id > @last_used_agent_id LIMIT 1;
			SET @last_used_agent_id = @a_agent_id;
            
	-- Channel
            IF @x % 17 = 0 THEN
				SET @channel = 'W';
			ELSEIF @x % 5 = 0 THEN
				SET @channel = 'A';
			ELSE
				SET @channel = 'U';
			END IF;
            
	-- Date
			SET @started_ended = DATE_ADD(CURDATE(), INTERVAL -(@x % 30) DAY);
            SET @ended_date = DATE_ADD(CURDATE(), INTERVAL -(@x % 30) DAY);
            SET @ended_time = DATE_ADD(CURDATE(), INTERVAL -(CEIL(RAND() * 12)) HOUR);
            
	-- Success
			IF @x % 31 = 0 THEN
				SET @success = 0;
			ELSE
				SET @success = 1;
			END IF;
			
	-- Type and related_id
			IF @x % 37 = 0 THEN
				SET @tr_type = 'NR';	-- Non-airtime refund
				SELECT id INTO @related_id FROM `ecdsap`.`ap_transact` WHERE id < @x AND type = 'ND' ORDER BY id DESC LIMIT 1;
			ELSEIF @x % 31 = 0 THEN
				SET @tr_type = 'FR';	-- Reverse
				SELECT id INTO @related_id FROM `ecdsap`.`ap_transact` WHERE id < @x AND type = 'SL' ORDER BY id DESC LIMIT 1;
			ELSEIF @x % 7 = 0 THEN
				SET @tr_type = 'ND';	-- Non-airtime debit
                SET @related_id = NULL;
			ELSE
				SET @tr_type = 'SL';	-- Sell
                SET @related_id = NULL;
			END IF;
    
    -- Version
			SET @version = @x % 4;
    
    -- Follow up
			IF @x % 41 = 0 THEN
				SET @follow_up = 1;
			ELSE
				SET @follow_up = 0;
			END IF;
            
	-- Amount
			SET @amount = CEIL(RAND() * 1000);
             
            INSERT INTO `ecdsap`.`ap_transact`
(`id`, `channel`, `comp_id`, `no`, `mode`, `started`, `success`, `type`, `version`, `follow_up`,	-- <- Not null
	`amount`, `ended_date`, `ended_time`, `related_id`, `a_group_name`, `a_agent_id`, `a_msisdn`, `a_agent_name`, `a_tier_name`)
			VALUES
(@x, @channel, 2, @x, 'N', @started_ended, @success, @tr_type, @version, @follow_up,	-- <- Not null
	@amount, @ended_date, @ended_time, @related_id, @a_agent_group_name, @a_agent_id, @a_agent_msisdn, @a_agent_name, @a_agent_tier_name);

			IF @x % 1000 = 0 THEN
				select concat(@x) as ''; 
			END IF;
		SET @x = @x + 1;
        END;
	UNTIL @x > end_number END REPEAT;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `insert_agent` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_agent`(start_number INT, end_number INT)
BEGIN

	DECLARE agent_id int(11);
	DECLARE balance decimal(20,4);
	DECLARE bonus_balance decimal(20,4);
	DECLARE comp_id int(11);
	DECLARE group_name varchar(50);
	DECLARE hold_balance decimal(20,4);
	DECLARE msisdn varchar(30);
	DECLARE agent_name varchar(93);
	DECLARE owner_id int(11);
	DECLARE state varchar(255);
	DECLARE tier_name varchar(50);
    
    DECLARE max_group_id int;
    DECLARE last_used_group_id int;
    
	DECLARE CONTINUE HANDLER FOR SQLSTATE '23000' SET @x = @x;
    
    SET @last_used_group_id = 1;
    
    SET @x = start_number;
    SET @max_agent_id = (SELECT MAX(id) FROM ecdsap.ap_agent_account);
    SET @max_group_id = (SELECT MAX(id) FROM hxc.et_group);
    REPEAT
		BEGIN
        
        SET @agent_id = @x;
        SET @balance = CEIL(RAND() * 5000);
        SET @bonus_balance = CEIL(RAND() * 100);
        SET @comp_id = 2;
        
        SELECT id, `name` INTO @last_used_group_id, @group_name FROM hxc.et_group WHERE company_id = @comp_id AND id > @last_used_group_id ORDER BY id LIMIT 1;
        IF @last_used_group_id = @max_group_id THEN
			SET @last_used_group_id = 1; 
		END IF;
        
        SET @hold_balance = 0;
        SET @msisdn = 1000000000 + @x;
        SET @agent_name = concat('agent_', @x);
        SET @owner_id = NULL;
        SET @state = 'A';
        SET @tier_name = 'eCabine';
        
        INSERT INTO `ecdsap`.`ap_agent_account`
(`id`, `balance`,`bonus_balance`, `comp_id`, `group_name`, `hold_balance`, `msisdn`, `agent_name`, `owner_id`, `state`, `tier_name`)
		VALUES
(@agent_id, @balance, @bonus_balance, @comp_id, @group_name, @hold_balance, @msisdn, @agent_name, @owner_id, @state, @tier_name);
            
			IF @x % 1000 = 0 THEN
				select concat(@x) as ''; 
			END IF;
            
		SET @x = @x + 1;
        END;
	UNTIL @x > end_number END REPEAT;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `insert_olap_transactions` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_olap_transactions`(start_number INT, end_number INT)
BEGIN
    
    DECLARE a_agent_id int(11);
    DECLARE a_agent_balance int;
    DECLARE a_agent_group_name varchar(50);
    DECLARE a_agent_msisdn varchar(30);
    DECLARE a_agent_name varchar(93);
    DECLARE a_agent_tier_name varchar(50);
    
    DECLARE max_agent_id int;
    DECLARE last_used_agent_id int;
    
    DECLARE channel varchar(1);
    DECLARE started_ended datetime;
    DECLARE success int;
    DECLARE tr_type varchar(50);
    DECLARE version int;
    DECLARE follow_up int;
    DECLARE amount decimal(20,4);
    DECLARE ended_date date;
    DECLARE ended_time time;
    DECLARE related_id bigint(20);
    
	DECLARE CONTINUE HANDLER FOR SQLSTATE '23000' SET @x = @x;
    
    SET @max_agent_id = (SELECT MAX(id) FROM ap_agent_account);
    SET @last_used_agent_id = 1;
    
    SET @x = start_number;
    
    REPEAT
		BEGIN
	-- Agent
			SELECT id, group_name, msisdn, agent_name, tier_name
				INTO @a_agent_id, @a_agent_group_name, @a_agent_msisdn, @a_agent_name, @a_agent_tier_name
				FROM ecdsap.ap_agent_account WHERE id > @last_used_agent_id LIMIT 1;
			SET @last_used_agent_id = @a_agent_id;
            
	-- Channel
            IF @x % 17 = 0 THEN
				SET @channel = 'W';
			ELSEIF @x % 5 = 0 THEN
				SET @channel = 'A';
			ELSE
				SET @channel = 'U';
			END IF;
            
	-- Date
			SET @started_ended = DATE_ADD(CURDATE(), INTERVAL -(@x % 30) DAY);
            SET @ended_date = DATE_ADD(CURDATE(), INTERVAL -(@x % 30) DAY);
            SET @ended_time = DATE_ADD(CURDATE(), INTERVAL -(CEIL(RAND() * 12)) HOUR);
            
	-- Success
			IF @x % 31 = 0 THEN
				SET @success = 0;
			ELSE
				SET @success = 1;
			END IF;
			
	-- Type and related_id
			IF @x % 37 = 0 THEN
				SET @tr_type = 'NR';	-- Non-airtime refund
				SELECT id INTO @related_id FROM `ecdsap`.`ap_transact` WHERE id < @x AND type = 'ND' ORDER BY id DESC LIMIT 1;
			ELSEIF @x % 31 = 0 THEN
				SET @tr_type = 'FR';	-- Reverse
				SELECT id INTO @related_id FROM `ecdsap`.`ap_transact` WHERE id < @x AND type = 'SL' ORDER BY id DESC LIMIT 1;
			ELSEIF @x % 7 = 0 THEN
				SET @tr_type = 'ND';	-- Non-airtime debit
                SET @related_id = NULL;
			ELSE
				SET @tr_type = 'SL';	-- Sell
                SET @related_id = NULL;
			END IF;
    
    -- Version
			SET @version = @x % 4;
    
    -- Follow up
			IF @x % 41 = 0 THEN
				SET @follow_up = 1;
			ELSE
				SET @follow_up = 0;
			END IF;
            
	-- Amount
			SET @amount = CEIL(RAND() * 1000);
             
            INSERT INTO `ecdsap`.`ap_transact`
(`id`, `channel`, `comp_id`, `no`, `mode`, `started`, `success`, `type`, `version`, `follow_up`,	-- <- Not null
	`amount`, `ended_date`, `ended_time`, `related_id`, `a_group_name`, `a_agent_id`, `a_msisdn`, `a_agent_name`, `a_tier_name`)
			VALUES
(@x, @channel, 2, @x, 'N', @started_ended, @success, @tr_type, @version, @follow_up,	-- <- Not null
	@amount, @ended_date, @ended_time, @related_id, @a_agent_group_name, @a_agent_id, @a_agent_msisdn, @a_agent_name, @a_agent_tier_name);

			IF @x % 1000 = 0 THEN
				select concat(@x) as ''; 
			END IF;
		SET @x = @x + 1;
        END;
	UNTIL @x > end_number END REPEAT;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-10-13 17:48:35
