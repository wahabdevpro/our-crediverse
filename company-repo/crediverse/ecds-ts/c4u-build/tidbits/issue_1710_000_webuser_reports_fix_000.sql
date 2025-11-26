DROP PROCEDURE IF EXISTS issue_1710_000_webuser_reports_fix_000;

DELIMITER $$
CREATE PROCEDURE issue_1710_000_webuser_reports_fix_000()
BEGIN
	DECLARE issue varchar(256) default 'issue_1710_000_webuser_reports_fix_000';
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE broken_version int(11) DEFAULT 68;
	DECLARE current_version int(11) DEFAULT NULL;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE agent_id_is_nullable varchar(128) DEFAULT NULL;

	SELECT CONCAT(NOW(), " Attempting to fix OLTP schema version ", broken_version) AS "INFO";
	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = tag;
	END IF;

	IF current_version = broken_version THEN

		SELECT CONCAT(NOW(), " Starting transaction ...") as "INFO";

		START TRANSACTION;

			SELECT IS_NULLABLE INTO agent_id_is_nullable  FROM `information_schema`.`COLUMNS` WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'er_report' AND COLUMN_NAME = 'agent_id';
			SELECT CONCAT(NOW(), " Checking if er_report.agent_id is nullable : ", agent_id_is_nullable) as `INFO`;

			IF (agent_id_is_nullable IS NULL) THEN
				SET tmp_message_text = CONCAT(NOW(), " could not determine nullable state of er_report.agent_id");
				SIGNAL SQLSTATE "45000" set MESSAGE_TEXT = tmp_message_text;
			END IF;

			IF ( agent_id_is_nullable != 'YES' ) THEN
				SELECT CONCAT(NOW(), " Making er_report.agent_id nullable") as `INFO`;
				ALTER TABLE `er_report` CHANGE  `agent_id` `agent_id` INT( 11 );
			ELSE
				SELECT CONCAT(NOW(), " er_report.agent_id is already nullable") as `INFO`;
			END IF;

			SELECT CONCAT(NOW(), " Changing er_report.agent_id = 0 to NULL") as `INFO`;
			UPDATE er_report SET agent_id = NULL WHERE agent_id = 0;
			SELECT CONCAT(NOW(), " Changing email_to_agent to 0 for non agent reports") as `INFO`;
			UPDATE er_report_schedule SET email_to_agent = 0 WHERE report_specification_id IN (SELECT id FROM er_report WHERE agent_id IS NULL);

		COMMIT;
	ELSE
		SELECT CONCAT(NOW(), "not applying ", issue, " AS ", current_version, " !=  ", broken_version) AS "INFO";
	END IF;
END $$
DELIMITER ;

CALL issue_1710_000_webuser_reports_fix_000();
DROP PROCEDURE IF EXISTS issue_1710_000_webuser_reports_fix_000;
