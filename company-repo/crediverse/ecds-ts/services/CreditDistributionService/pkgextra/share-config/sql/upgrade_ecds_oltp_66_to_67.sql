DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 66;
	DECLARE new_version int(11) default 67;
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

	        SELECT "Checking if er_report.er_report_name index exists ..." as `INFO`;
			IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name = 'er_report' AND index_name = 'er_report_name') > 0) THEN
	        	SELECT "Index er_report.er_report_name exists, dropping ..." as `INFO`;
				ALTER TABLE `er_report` DROP INDEX `er_report_name`;
	        	SELECT "Index er_report.er_report_name dropped." as `INFO`;
			END IF;
			
	        SELECT "Checking if er_report.er_report_internal_name index exists ..." as `INFO`;
			IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name = 'er_report' AND index_name = 'er_report_internal_name') > 0) THEN
	        	SELECT "Index er_report.er_report_internal_name exists, dropping ..." as `INFO`;
				ALTER TABLE `er_report` DROP INDEX `er_report_internal_name`;
	        	SELECT "Index er_report.er_report_internal_name dropped." as `INFO`;
			END IF;

	        SELECT "Convering the er_report.agent_id to non-NULL in order to enforce uniqueness in MYSQL ..." as `INFO`;
			UPDATE `er_report` SET agent_id = 0 WHERE agent_id IS NULL;
			ALTER TABLE  `er_report` CHANGE  `agent_id`  `agent_id` INT( 11 ) NOT NULL DEFAULT  '0';
	        SELECT "Converted the er_report.agent_id column to non-NULL." as `INFO`;

	        SELECT "Recreating er_report.er_report_name index ..." as `INFO`;
			ALTER TABLE `er_report` ADD UNIQUE `er_report_name` ( `company_id` , `agent_id` , `name` );
	        SELECT "Created er_report.er_report_name index." as `INFO`;

	        SELECT "Recreating er_report.er_report_internal_name index ..." as `INFO`;
			ALTER TABLE `er_report` ADD UNIQUE `er_report_internal_name` ( `company_id` , `agent_id` , `internal_name` );
	        SELECT "Created er_report.er_report_internal_name index." as `INFO`;

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
