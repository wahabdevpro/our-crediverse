DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE old_version int(11) DEFAULT 33;
	DECLARE new_version int(11) DEFAULT 34;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;

	SELECT CONCAT("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = "DB Version";
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	IF current_version != new_version THEN
		IF current_version != old_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" SET MESSAGE_TEXT = tmp_message_text;
		END IF;

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;

			SET FOREIGN_KEY_CHECKS=0;

			/* MEAT: START */

			/*
				Since database upgrades were just disregarded this upgrade has to work even if columns already exists. So do IF ( NOT EXISTS( ... ) )
			*/

			IF ( NOT EXISTS( SELECT * FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "eb_batch" AND `COLUMN_NAME` = "total_value2") )
			THEN
				ALTER TABLE `eb_batch` ADD COLUMN `total_value2` decimal(19,2) DEFAULT NULL AFTER `total_value`;
			END IF;

			IF ( NOT EXISTS( SELECT * FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "eb_stage" AND `COLUMN_NAME` = "i5") )
			THEN
				ALTER TABLE `eb_stage` ADD COLUMN `i5` int(11) DEFAULT NULL AFTER `i4`;
			END IF;

			IF ( NOT EXISTS( SELECT * FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "eb_stage" AND `COLUMN_NAME` = "r1") )
			THEN
				ALTER TABLE `eb_stage` ADD COLUMN `r1` double DEFAULT NULL AFTER `postal_zip`;
			END IF;

			IF ( NOT EXISTS( SELECT * FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "eb_stage" AND `COLUMN_NAME` = "r2") )
			THEN
				ALTER TABLE `eb_stage` ADD COLUMN `r2` double DEFAULT NULL AFTER `r1`;
			END IF;

			CREATE TABLE IF NOT EXISTS `ec_bundle` (
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
				PRIMARY KEY (`id`),
				UNIQUE KEY `ec_bundle_name` (`company_id`,`name`),
				UNIQUE KEY `ec_bundle_tag` (`company_id`,`tag`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

			CREATE TABLE IF NOT EXISTS `ec_bundle_lang` (
				`language` varchar(2) NOT NULL,
				`bundle_id` int(11) NOT NULL,
				`description` varchar(100) NOT NULL,
				`name` varchar(30) NOT NULL,
				`type` varchar(30) NOT NULL,
				`version` int(11) NOT NULL,
				PRIMARY KEY (`language`,`bundle_id`),
				KEY `FK_Bundle_Lang` (`bundle_id`),
				CONSTRAINT `FK_Bundle_Lang` FOREIGN KEY (`bundle_id`) REFERENCES `ec_bundle` (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

			CREATE TABLE IF NOT EXISTS `el_area` (
				`id` int(11) NOT NULL AUTO_INCREMENT,
				`company_id` int(11) NOT NULL,
				`lm_time` datetime NOT NULL,
				`lm_userid` int(11) NOT NULL,
				`name` varchar(30) NOT NULL,
				`parent_id` int(11) DEFAULT NULL,
				`type` varchar(30) NOT NULL,
				`version` int(11) NOT NULL,
				PRIMARY KEY (`id`),
				UNIQUE KEY `el_area_name` (`company_id`,`name`),
				KEY `FK_Area_Parent` (`parent_id`),
				CONSTRAINT `FK_Area_Parent` FOREIGN KEY (`parent_id`) REFERENCES `el_area` (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

			CREATE TABLE IF NOT EXISTS `el_area_area` (
				`id` int(11) NOT NULL,
				`sub_id` int(11) NOT NULL,
				KEY `FK8nrpxh16nsfr7k3v87b8tbp15` (`sub_id`),
				KEY `FKfgnro2b070i0ue51em6a2u69q` (`id`),
				CONSTRAINT `FK8nrpxh16nsfr7k3v87b8tbp15` FOREIGN KEY (`sub_id`) REFERENCES `el_area` (`id`),
				CONSTRAINT `FKfgnro2b070i0ue51em6a2u69q` FOREIGN KEY (`id`) REFERENCES `el_area` (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

			CREATE TABLE IF NOT EXISTS `el_area_cell` (
				`cell_id` int(11) NOT NULL,
				`area_id` int(11) NOT NULL,
				KEY `FK2djs0n5sotjaeyn0x3ax5vqr8` (`area_id`),
				KEY `FKf5rfuxdkcly9fs0txcfi1njdi` (`cell_id`),
				CONSTRAINT `FK2djs0n5sotjaeyn0x3ax5vqr8` FOREIGN KEY (`area_id`) REFERENCES `el_area` (`id`),
				CONSTRAINT `FKf5rfuxdkcly9fs0txcfi1njdi` FOREIGN KEY (`cell_id`) REFERENCES `el_cell` (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

			CREATE TABLE IF NOT EXISTS `el_cell` (
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
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

			CREATE TABLE IF NOT EXISTS `ep_promo` (
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
				PRIMARY KEY (`id`),
				UNIQUE KEY `ep_promo_name` (`company_id`,`name`),
				KEY `FK_Promo_Area` (`area_id`),
				KEY `FK_Promo_SC` (`sc_id`),
				KEY `FK_Promo_Rule` (`rule_id`),
				CONSTRAINT `FK_Promo_Area` FOREIGN KEY (`area_id`) REFERENCES `el_area` (`id`),
				CONSTRAINT `FK_Promo_Rule` FOREIGN KEY (`rule_id`) REFERENCES `et_rule` (`id`),
				CONSTRAINT `FK_Promo_SC` FOREIGN KEY (`sc_id`) REFERENCES `et_sclass` (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

			IF ( NOT EXISTS( SELECT * FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ew_item" AND `COLUMN_NAME` = "reason") )
			THEN
				ALTER TABLE `ew_item` ADD COLUMN `reason` varchar(150) DEFAULT NULL AFTER `lm_userid`;
			END IF;

			/* MEAT: END */
			SELECT CONCAT("Changing OLTP schema version data from ", old_version, " to ", new_version) AS "INFO";
			UPDATE `ec_state` set `value` = new_version WHERE `name` = "DB Version";

		COMMIT;
		SELECT CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) AS "INFO";
	ELSE
		SELECT CONCAT("Not upgrading OLTP schema version, already on ", new_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
