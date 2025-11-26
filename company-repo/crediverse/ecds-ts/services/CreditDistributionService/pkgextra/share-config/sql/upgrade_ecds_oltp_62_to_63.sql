DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 62;
	DECLARE new_version int(11) default 63;
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

        SELECT "Checking er_report_schedule.delivery_channel" AS "INFO";
        IF NOT EXISTS (SELECT null from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'er_report_schedule' AND COLUMN_NAME = 'delivery_channel') THEN
            SELECT "Adding er_report_schedule.delivery_channel" AS "INFO";
			ALTER TABLE  `er_report_schedule` ADD  `delivery_channels` VARCHAR( 64 ) NULL DEFAULT NULL AFTER  `time_of_day` ;
            SELECT "Added er_report_schedule.delivery_channel" AS "INFO";
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
