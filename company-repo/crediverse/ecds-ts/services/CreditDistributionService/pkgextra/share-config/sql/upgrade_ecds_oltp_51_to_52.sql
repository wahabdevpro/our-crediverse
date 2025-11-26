DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 51;
	DECLARE new_version int(11) DEFAULT 52;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;

	SELECT CONCAT("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = tag;
	END IF;

	/* Verify Version */
	select CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	if current_version != new_version then
		if current_version != old_version then
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" set MESSAGE_TEXT = tmp_message_text;
		end if;

		select "Starting transaction ..." as "INFO";

		start transaction;

			ALTER TABLE `er_report_schedule` CHANGE `period` `period_old` int(11) NOT NULL;
			ALTER TABLE `er_report_schedule` ADD COLUMN `period` varchar(255) NOT NULL AFTER `lm_userid`;
			UPDATE `er_report_schedule` SET `period` = 'HOUR' WHERE `period_old` = 0;
			UPDATE `er_report_schedule` SET `period` = 'DAY' WHERE `period_old` = 1;
			UPDATE `er_report_schedule` SET `period` = 'WEEK' WHERE `period_old` = 2;
			UPDATE `er_report_schedule` SET `period` = 'MONTH' WHERE `period_old` = 3;
			ALTER TABLE `er_report_schedule` DROP COLUMN `period_old`;

			select concat("Changing OLTP schema version data from ", old_version, " to ", new_version) as "INFO";
			update `ec_state` set `value` = new_version where `name` = tag;

		commit;

		select CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) as "INFO";
	else
		select CONCAT("Not upgrading OLTP schema version, already on ", new_version) as "INFO";
	end if;

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
