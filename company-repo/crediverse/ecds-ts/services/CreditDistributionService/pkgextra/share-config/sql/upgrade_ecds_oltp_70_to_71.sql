DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 70;
	DECLARE new_version int(11) default 71;
	DECLARE tmp_message_text varchar(256) default null;
	DECLARE current_version int(11) default null;

	SELECT CONCAT("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = "DB Version";
	END IF;
	
	/* Verify Version */
	select CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	IF current_version != new_version THEN
		IF current_version != old_version THEN
			SELECT CONCAT("Not upgrading OLAP schema version, expected version ", old_version) AS "INFO";
		ELSE
			SELECT "Starting transaction ..." as "INFO";
	
			START TRANSACTION;
				/* MEAT: START */
	
		        SELECT "Checking eb_stage.auth_method" AS "INFO";
		        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
		            AND TABLE_NAME = 'eb_stage' AND COLUMN_NAME = 'auth_method') THEN
		            SELECT "Adding eb_stage.auth_method" AS "INFO";
					ALTER TABLE  `eb_stage` ADD  `auth_method` varchar(1) DEFAULT NULL ;
		            SELECT "Added eb_stage.auth_method" AS "INFO";
		        END IF;
		        
				/* MEAT: END */
		        SELECT concat("Changing OLTP schema version data from ", old_version, " to ", new_version) as "INFO";
	    	    UPDATE `ec_state` SET `value` = new_version WHERE `name` = tag;
	
			COMMIT;
	
			SELECT CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) AS "INFO";
		END IF;
	ELSE
		SELECT CONCAT("not upgrading OLTP schema version, already on ", new_version) AS "INFO";
	END IF;
	
	
	/* Emergency Patch */
	SELECT CONCAT("Checking if emergency patch needs to be applied to ec_transact id and reversed_id columns... ") AS "INFO";
	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = "DB Version";
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLAP schema version data is ", current_version) AS "INFO";
	IF (current_version >= new_version) THEN
		IF EXISTS (SELECT 'exists' FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND `TABLE_NAME` = 'ec_transact' AND `COLUMN_NAME` = 'id' AND LOWER(DATA_TYPE) != 'bigint' AND LOWER(SUBSTRING(COLUMN_TYPE, 1, 6)) != 'bigint')
			AND EXISTS (SELECT 'exists' from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ec_transact' AND COLUMN_NAME = 'reversed_id' AND LOWER(DATA_TYPE) != 'bigint' AND LOWER(SUBSTRING(COLUMN_TYPE, 1, 6)) != 'bigint')
		THEN
			SELECT CONCAT("Changing ec_transact.id and ec_transact.reversed_id to BIGINT") AS "INFO";
			ALTER TABLE ec_transact 
				MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT,
				MODIFY COLUMN reversed_id BIGINT DEFAULT NULL;
			SELECT CONCAT("Completed changing ec_transact.id and ec_transact.reversed_id to BIGINT") AS "INFO";
		ELSE
			SELECT CONCAT("Skipping ec_transact.id BIGINT and ec_transact.reversed_id column change.") AS "INFO";
		END IF;
	ELSE
		SELECT CONCAT("Current version ", current_version , " not greater than ", new_version, " version. Doing nothing.") AS "INFO";
	END IF;
	/* End: Emergency Patch */

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
