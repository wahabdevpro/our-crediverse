DROP PROCEDURE IF EXISTS upgrade_ecds_olap;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_olap()
BEGIN
	DECLARE old_version int(11) DEFAULT 0;
	DECLARE new_version int(11) DEFAULT 1;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version_string varchar(128) DEFAULT NULL;
	DECLARE effective_current_version_string varchar(128) DEFAULT NULL;

	SELECT CONCAT("Attempting to upgrade OLAP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ap_schema_data") THEN
		SET current_version_string = old_version;
		SET effective_current_version_string = current_version_string;
		SELECT CONCAT("Table ap_schema_data does not exist ... creating it with version ", current_version_string) AS "WARNING";
		CREATE TABLE `ap_schema_data`
		(
		  `vkey` varchar(64) NOT NULL,
		  `created_at` datetime NOT NULL,
		  `updated_at` datetime NOT NULL,
		  `value` varchar(128) NOT NULL,
		  PRIMARY KEY (`vkey`)
		) ENGINE=InnoDB;
		INSERT INTO `ap_schema_data` (`vkey`, `value`, `created_at`, `updated_at`) VALUES
		( "VERSION", current_version_string, NOW(), NOW() );
	ELSE
		SELECT value INTO current_version_string FROM `ap_schema_data` WHERE `vkey` = "VERSION";
		IF ( current_version_string = new_version )
			AND ( NOT EXISTS( SELECT * FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ap_transact" AND `COLUMN_NAME` = "bonus_pct") )
			AND ( NOT EXISTS( SELECT * FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ap_transact" AND `COLUMN_NAME` = "bonus_prov") )
			THEN

			SELECT CONCAT("OLAP schema version data is ", new_version, " but bonus_pct and bonus_prov columnds do not exist in ap_transact. Setting version to ", old_version) AS "WARNING";
			SET effective_current_version_string = old_version;
		ELSE
			SET effective_current_version_string = current_version_string;
		END IF;
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current effective OLAP schema version data is ", effective_current_version_string) AS "INFO";

	IF effective_current_version_string != new_version THEN
		IF effective_current_version_string != old_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" SET MESSAGE_TEXT = tmp_message_text;
		END IF;

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;

			ALTER TABLE `ap_transact`
			ADD COLUMN `bonus_pct` decimal(20,8) DEFAULT NULL,
			ADD COLUMN `bonus_prov` decimal(20,4) DEFAULT NULL;

			/* Update Version */
			IF current_version_string != new_version THEN
				SELECT CONCAT("Changing OLAP schema version data from ", old_version, " to ", new_version) AS "INFO";
				UPDATE `ap_schema_data` SET `value` = new_version WHERE `vkey` = "VERSION" AND `value` = old_version;
			ELSE
				SELECT CONCAT("OLAP schema version data is already ", new_version, " not updating ...") AS "WARNING";
			END IF;

		COMMIT;
		SELECT CONCAT("Upgraded OLAP schema version ", old_version, " to ", new_version) AS "INFO";
	ELSE
		SELECT CONCAT("Not upgrading OLAP schema version, already on ", new_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL upgrade_ecds_olap();
DROP PROCEDURE IF EXISTS upgrade_ecds_olap;
