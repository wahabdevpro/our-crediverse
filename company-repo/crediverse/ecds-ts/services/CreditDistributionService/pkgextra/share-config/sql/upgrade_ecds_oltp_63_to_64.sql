DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 63;
	DECLARE new_version int(11) default 64;
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

		START transaction;

        SELECT "Checking er_report.agent_id" AS "INFO";
        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'er_report' AND COLUMN_NAME = 'agent_id') THEN
            SELECT "Adding er_report.agent_id" AS "INFO";
			ALTER TABLE  `er_report` ADD  `agent_id` INT NULL DEFAULT NULL AFTER  `company_id` ;
            SELECT "Added er_report.agent_id" AS "INFO";
        END IF;
        
        SELECT "Chcking ix_er_report_agent_id index to table er_report" as `INFO`;
		IF NOT EXISTS (SELECT NULL 
                     FROM information_schema.statistics 
                     WHERE TABLE_SCHEMA = database()
                     AND INDEX_NAME = 'ix_er_report_agent_id' 
                     AND TABLE_NAME = 'er_report') THEN
            
			SELECT "Adding index ix_er_report_agent_id to table er_report" as `INFO`;
			
            ALTER TABLE `er_report`                  
				ADD INDEX er_report_agent_id (  `agent_id` ) ;

            SELECT "Added index ix_er_report_agent_id to table er_report" as `INFO`;

        END IF;

        SELECT "Updating built-in agent role / permissions" as `INFO`;
		UPDATE `es_permission` SET agent_allowed = 1 WHERE grp = 'Reports';
        SELECT "Updated built-in agent role / permissions" as `INFO`;
			
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
