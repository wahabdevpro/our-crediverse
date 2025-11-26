DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 68;
	DECLARE new_version int(11) default 69;
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

			SELECT CONCAT(NOW(), " Changing email_to_agent to 0 for non agent reports") as `INFO`;
			UPDATE er_report_schedule SET email_to_agent = 0 WHERE report_specification_id IN (SELECT id FROM er_report WHERE (agent_id IS NULL) OR (agent_id = 0));
			SELECT CONCAT(NOW(), " Changed email_to_agent to 0 for non agent reports") as `INFO`;

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
