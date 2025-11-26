DROP PROCEDURE IF EXISTS downgrade_ecds_olap;

DELIMITER $$
CREATE PROCEDURE downgrade_ecds_olap()
BEGIN
	DECLARE old_version int(11) DEFAULT 12;
	DECLARE new_version int(11) DEFAULT 13;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version_string varchar(128) DEFAULT NULL;
	DECLARE revert_records int(11) DEFAULT 0;

	SELECT CONCAT("Attempting to downgrade OLAP schema version ", new_version, " to ", old_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ap_schema_data") THEN
		SET current_version_string = 0;
	ELSE
		SELECT value INTO current_version_string FROM `ap_schema_data` WHERE `vkey` = "VERSION";
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLAP schema version data is ", current_version_string) AS "INFO";

	IF current_version_string != old_version THEN
		IF current_version_string != new_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", new_version, " Database");
			SIGNAL SQLSTATE "45000" SET MESSAGE_TEXT = tmp_message_text;
		END IF;

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;

			SET FOREIGN_KEY_CHECKS=0;

			/* MEAT: START */
			SELECT count(*) INTO revert_records FROM ap_transact a, ap_transact rel where a.type in ('PA', 'FR') and a.related_id = rel.id and rel.type in ('SL','SB','ST') and a.amount > 0;
			IF(revert_records > 0)
			THEN
				UPDATE ap_transact u JOIN ap_transact rel ON u.related_id = rel.id
				SET u.amount = (-1 * u.amount) 
				WHERE u.type in ('PA', 'FR') AND u.amount > 0 
				AND rel.type IN ('SL','SB','ST');
			END IF;
			/* MEAT: END */
			SELECT CONCAT("Changing OLAP schema version data from ", new_version, " to ", old_version) AS "INFO";
			UPDATE `ap_schema_data` SET `value` = old_version WHERE `vkey` = "VERSION" AND `value` = new_version;

		COMMIT;
		SELECT CONCAT("Downgraded OLAP schema version ", new_version, " to ", old_version) AS "INFO";
	ELSE
		SELECT CONCAT("Not downgrading OLAP schema version, already on ", old_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL downgrade_ecds_olap();
DROP PROCEDURE IF EXISTS downgrade_ecds_olap;
