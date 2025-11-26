DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE old_version int(11) DEFAULT 72;
	DECLARE new_version int(11) DEFAULT 73;
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

			CREATE TABLE IF NOT EXISTS `el_cell_cell_group` (
				`cell_id` int(11) NOT NULL,
				`cell_group_id` int(11) NOT NULL,
				KEY `FKb41f4e1149a64c209750fe32bb7b9f14` (`cell_group_id`),
				KEY `FKb2f8137cf4d54a1bb864230c1af0d85e` (`cell_id`),
				CONSTRAINT `FKb41f4e1149a64c209750fe32bb7b9f14` FOREIGN KEY (`cell_group_id`) REFERENCES `el_cell_group` (`id`),
				CONSTRAINT `FKb2f8137cf4d54a1bb864230c1af0d85e` FOREIGN KEY (`cell_id`) REFERENCES `el_cell` (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	        
			SELECT "Checking eb_stage.area_ids" AS "INFO";
	        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
	            AND TABLE_NAME = 'eb_stage' AND COLUMN_NAME = 'area_ids') THEN
	            SELECT "Adding eb_stage.area_ids" AS "INFO";
				ALTER TABLE  `eb_stage` ADD  `area_ids` TEXT NULL DEFAULT NULL ; 
	            SELECT "Added eb_stage.area_ids" AS "INFO";
	        END IF;
	        
			SELECT "Checking eb_stage.cell_group_ids" AS "INFO";
	        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
	            AND TABLE_NAME = 'eb_stage' AND COLUMN_NAME = 'cell_group_ids') THEN
	            SELECT "Adding eb_stage.cell_group_ids" AS "INFO";
				ALTER TABLE  `eb_stage` ADD  `cell_group_ids` TEXT NULL DEFAULT NULL ; 
	            SELECT "Added eb_stage.cell_group_ids" AS "INFO";
	        END IF;

			SELECT "Checking ec_transact.a_cell_group_id" AS "INFO";
	        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
	            AND TABLE_NAME = 'ec_transact' AND COLUMN_NAME = 'a_cell_group_id') THEN
	            SELECT "Adding ec_transact.a_cell_group_id" AS "INFO";
				ALTER TABLE  `ec_transact` ADD  `a_cell_group_id` TEXT NULL DEFAULT NULL ; 
	            SELECT "Added ec_transact.a_cell_group_id" AS "INFO";
	        END IF;
	        
			SELECT "Checking ec_transact.b_cell_group_id" AS "INFO";
	        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
	            AND TABLE_NAME = 'ec_transact' AND COLUMN_NAME = 'b_cell_group_id') THEN
	            SELECT "Adding ec_transact.b_cell_group_id" AS "INFO";
				ALTER TABLE  `ec_transact` ADD  `b_cell_group_id` TEXT NULL DEFAULT NULL ; 
	            SELECT "Added ec_transact.b_cell_group_id" AS "INFO";
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
