DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 60;
	DECLARE new_version int(11) default 61;
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

        SELECT "Checking er_report.internal_name" AS "INFO";
        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'er_report' AND COLUMN_NAME = 'internal_name') THEN
            SELECT "Adding er_report.internal_name" AS "INFO";
            ALTER TABLE `er_report` ADD COLUMN `internal_name` varchar(50) DEFAULT NULL AFTER `description`;
            SELECT "Added er_report.internal_name" AS "INFO";
        END IF;
        SELECT "Checking er_report.originator" AS "INFO";
        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'er_report' AND COLUMN_NAME = 'originator') THEN
            SELECT "Adding er_report.originator" AS "INFO";
            ALTER TABLE `er_report` ADD COLUMN `originator` varchar(64) DEFAULT NULL AFTER `name`;
            SELECT "Added er_report.originator" AS "INFO";
        END IF;
        SELECT "Checking er_report er_report_internal_name" AS "INFO";
        IF NOT EXISTS (SELECT null from information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'er_report' AND CONSTRAINT_NAME = 'er_report_internal_name') THEN
			SELECT "Adding er_report er_report_internal_name" AS "INFO";
			ALTER TABLE `er_report` ADD UNIQUE KEY `er_report_internal_name` (`company_id`,`internal_name`);
			SELECT "Added er_report er_report_internal_name" AS "INFO";
        END IF;

        SELECT "Checking er_report_schedule.internal_name" AS "INFO";
        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'er_report_schedule' AND COLUMN_NAME = 'internal_name') THEN
            SELECT "Adding er_report_schedule.internal_name" AS "INFO";
            ALTER TABLE `er_report_schedule` ADD COLUMN `internal_name` varchar(50) DEFAULT NULL AFTER `end_time_of_day`;
            SELECT "Added er_report_schedule.internal_name" AS "INFO";
        END IF;
        SELECT "Checking er_report_schedule.originator" AS "INFO";
        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'er_report_schedule' AND COLUMN_NAME = 'originator') THEN
            SELECT "Adding er_report_schedule.originator" AS "INFO";
            ALTER TABLE `er_report_schedule` ADD COLUMN `originator` varchar(64) DEFAULT NULL AFTER `lm_userid`;
            SELECT "Added er_report_schedule.originator" AS "INFO";
        END IF;
        SELECT "Checking er_report_schedule er_report_schedule_internal_name" AS "INFO";
        IF NOT EXISTS (SELECT null from information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'er_report_schedule' AND CONSTRAINT_NAME = 'er_report_schedule_internal_name') THEN
			SELECT "Adding er_report_schedule er_report_schedule_internal_name" AS "INFO";
			ALTER TABLE `er_report_schedule` ADD UNIQUE KEY `er_report_schedule_internal_name` (`company_id`,`internal_name`);
			SELECT "Added er_report_schedule er_report_schedule_internal_name" AS "INFO";
        END IF;

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
