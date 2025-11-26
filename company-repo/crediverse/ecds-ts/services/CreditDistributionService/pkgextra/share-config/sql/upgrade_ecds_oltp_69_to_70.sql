DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 69;
	DECLARE new_version int(11) default 70;
	DECLARE tmp_message_text varchar(256) default null;
	DECLARE current_version int(11) default null;

	SELECT CONCAT("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) AS "INFO";

	IF not EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = tag;
	END IF;

	/* Verify Version */
	select CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	IF current_version != new_version THEN
		IF current_version != old_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" set MESSAGE_TEXT = tmp_message_text;
		END IF;

		SELECT "Starting transaction ..." as "INFO";

		START TRANSACTION;
			/* MEAT: START */

	        SELECT "Checking ea_agent.max_report_count" AS "INFO";
	        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
	            AND TABLE_NAME = 'ea_agent' AND COLUMN_NAME = 'max_report_count') THEN
	            SELECT "Adding ea_agent.max_report_count" AS "INFO";
				ALTER TABLE  `ea_agent` ADD  `max_report_count` INT NULL DEFAULT NULL AFTER  `max_amount` ;
	            SELECT "Added ea_agent.max_report_count" AS "INFO";
	        END IF;
	        
			SELECT "Checking ea_agent.max_report_daily_schedule_count" AS "INFO";
	        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
	            AND TABLE_NAME = 'ea_agent' AND COLUMN_NAME = 'max_report_daily_schedule_count') THEN
	            SELECT "Adding ea_agent.max_report_daily_schedule_count" AS "INFO";
				ALTER TABLE  `ea_agent` ADD  `max_report_daily_schedule_count` INT NULL DEFAULT NULL AFTER  `max_report_count` ;
	            SELECT "Added ea_agent.max_report_daily_schedule_count" AS "INFO";
	        END IF;

	        SELECT "Checking eb_stage.max_report_count" AS "INFO";
	        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
	            AND TABLE_NAME = 'eb_stage' AND COLUMN_NAME = 'max_report_count') THEN
	            SELECT "Adding eb_stage.max_report_count" AS "INFO";
				ALTER TABLE  `eb_stage` ADD  `max_report_count` INT NULL DEFAULT NULL ;
	            SELECT "Added eb_stage.max_report_count" AS "INFO";
	        END IF;
	        
			SELECT "Checking eb_stage.max_report_daily_schedule_count" AS "INFO";
	        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
	            AND TABLE_NAME = 'eb_stage' AND COLUMN_NAME = 'max_report_daily_schedule_count') THEN
	            SELECT "Adding eb_stage.max_report_daily_schedule_count" AS "INFO";
				ALTER TABLE  `eb_stage` ADD  `max_report_daily_schedule_count` INT NULL DEFAULT NULL ;
	            SELECT "Added eb_stage.max_report_daily_schedule_count" AS "INFO";
	        END IF;

			/* MEAT: END */
	        SELECT concat("Changing OLTP schema version data from ", old_version, " to ", new_version) as "INFO";
    	    UPDATE `ec_state` SET `value` = new_version WHERE `name` = tag;

		COMMIT;

		SELECT CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) AS "INFO";
	ELSE
		SELECT CONCAT("not upgrading OLTP schema version, already on ", new_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
