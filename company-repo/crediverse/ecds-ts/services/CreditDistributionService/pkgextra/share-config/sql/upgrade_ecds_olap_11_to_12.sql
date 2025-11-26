DROP PROCEDURE IF EXISTS upgrade_ecds_olap;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_olap()
BEGIN
	DECLARE old_version int(11) DEFAULT 11;
	DECLARE new_version int(11) DEFAULT 12;
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

			CREATE TABLE IF NOT EXISTS `ap_analytics` (
			  `dt` date NOT NULL,
			  `transaction_type` varchar(16) NOT NULL,
			  `data_type` varchar(128) NOT NULL DEFAULT '',
			  `value` bigint(20) NOT NULL,
			  PRIMARY KEY (`dt`,`transaction_type`,`data_type`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
