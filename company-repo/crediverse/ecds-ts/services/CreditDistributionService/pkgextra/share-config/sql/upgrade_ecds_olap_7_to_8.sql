DROP PROCEDURE IF EXISTS upgrade_ecds_olap;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_olap()
BEGIN
	DECLARE old_version int(11) DEFAULT 7;
	DECLARE new_version int(11) DEFAULT 8;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version_string varchar(128) DEFAULT NULL;

	SELECT CONCAT("Attempting to upgrade OLAP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ap_schema_data") THEN
		SET current_version_string = 0;
	ELSE
		SELECT value INTO current_version_string FROM `ap_schema_data` WHERE `vkey` = "VERSION";
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLAP schema version data is ", current_version_string) AS "INFO";

	IF current_version_string != new_version THEN
		IF current_version_string != old_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" SET MESSAGE_TEXT = tmp_message_text;
		END IF;

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;
			/* MEAT: START */
			/*Add the follow_up table if it doesn't exist (as a dummy)*/

			-- IF EXISTS (SELECT "1" FROM statistics WHERE TABLE_SCHEMA = 'ecdsap' AND TABLE_NAME = 'ap_transact' AND INDEX_NAME = 'ap_transact_ended' AND COLUMN_NAME = 'ended')

			IF NOT EXISTS (SELECT "1" FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ap_transact' AND COLUMN_NAME = 'ended_date') THEN
				ALTER TABLE `ap_transact`
					ADD COLUMN `ended_date` date DEFAULT NULL AFTER `ended`,
					ADD COLUMN `ended_time` time DEFAULT NULL AFTER `ended_date`,
					DROP KEY `ap_transact_ended`,
					ADD KEY `ap_transact_aggregate000` (`ended_date`,`a_msisdn`,`type`,`success`,`b_msisdn`),
					ADD KEY `ap_transact_ended` (`ended_date`,`ended_time`)
				;
			END IF;
			
			IF EXISTS (SELECT "1" FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ap_transact' AND COLUMN_NAME = 'ended') THEN
				UPDATE `ap_transact` SET ended_date = DATE(ended), ended_time = TIME(ended) WHERE ended_date IS NULL or ended_time IS NULL;
				ALTER TABLE `ap_transact` DROP COLUMN `ended`;
			END IF;

			/* MEAT: END */
			SELECT CONCAT("Changing OLAP schema version data from ", old_version, " to ", new_version) AS "INFO";
			UPDATE `ap_schema_data` SET `value` = new_version WHERE `vkey` = "VERSION" AND `value` = old_version;

		COMMIT;
		SELECT CONCAT("Upgraded OLAP schema version ", old_version, " to ", new_version) AS "INFO";
	ELSE
		SELECT CONCAT("Not upgrading OLAP schema version, already on ", new_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL upgrade_ecds_olap();
DROP PROCEDURE IF EXISTS upgrade_ecds_olap;
