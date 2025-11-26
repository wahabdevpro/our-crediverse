DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE old_version int(11) DEFAULT 95;
	DECLARE new_version int(11) DEFAULT 96;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;

	SELECT CONCAT("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = "DB Version";
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	IF current_version != new_version THEN
		IF current_version != old_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" SET MESSAGE_TEXT = tmp_message_text;
		END IF;

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;

		ALTER TABLE `et_tier` ADD `default_bonus_pct` DECIMAL(20,8) NULL DEFAULT NULL AFTER `allow_intratier_transfer`;
		
		/* MEAT: END */
		SELECT CONCAT("Changing OLTP schema version data from ", old_version, " to ", new_version) AS "INFO";
		UPDATE `ec_state` SET `value` = new_version WHERE `name` = "DB Version";
		COMMIT;

		SELECT CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) AS "INFO";
	ELSE
		SELECT CONCAT("Not upgrading OLTP schema version, already on ", new_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
