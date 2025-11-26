DROP PROCEDURE IF EXISTS downgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE downgrade_ecds_oltp()
BEGIN
	DECLARE old_version int(11) DEFAULT 90;
	DECLARE new_version int(11) DEFAULT 91;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;

	SELECT CONCAT("Attempting to downgrade OLTP schema version ", new_version, " to ", old_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = "DB Version";
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	IF current_version != old_version THEN
		IF current_version != new_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", new_version, " Database");
			SIGNAL SQLSTATE "45000" SET MESSAGE_TEXT = tmp_message_text;
		END IF;

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;

		/* disabling this as the data type in the code has been 'long' for very long time */
		/* only use if there is some sort of problem relating to the current BIGINT data type */
		/* ALTER TABLE ep_qualify MODIFY COLUMN id INT(20); */
    
		/* MEAT: END */
		SELECT CONCAT("Changing OLTP schema version data from ", new_version, " to ", old_version) AS "INFO";
		UPDATE `ec_state` SET `value` = old_version WHERE `name` = "DB Version" AND `value` = new_version;

		COMMIT;
		SELECT CONCAT("Downgraded OLTP schema version ", new_version, " to ", old_version) AS "INFO";
	ELSE
		SELECT CONCAT("Not downgrading OLTP schema version, already on ", old_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL downgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS downgrade_ecds_oltp;


