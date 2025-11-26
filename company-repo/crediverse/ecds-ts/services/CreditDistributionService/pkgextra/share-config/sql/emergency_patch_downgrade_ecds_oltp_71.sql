DROP PROCEDURE IF EXISTS downgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE downgrade_ecds_oltp()
BEGIN
	DECLARE old_version int(11) DEFAULT 70;
	DECLARE new_version int(11) DEFAULT 71;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version varchar(128) DEFAULT NULL;

	SELECT CONCAT("Attempting to revert emergency patch on OLTP schema version ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = "DB Version";
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	IF current_version >= new_version THEN

		SELECT CONCAT("Checking if emergency patch downgrade is applicable and needs to be applied to ec_transact id and reversed_id columns... ") AS "INFO";
		
		IF EXISTS (SELECT 'exists' from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ec_transact' AND COLUMN_NAME = 'id' AND LOWER(DATA_TYPE) != 'int' AND LOWER(SUBSTRING(COLUMN_TYPE, 1, 3)) != 'int')
			AND EXISTS (SELECT 'exists' from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ec_transact' AND COLUMN_NAME = 'reversed_id' AND LOWER(DATA_TYPE) != 'int' AND LOWER(SUBSTRING(COLUMN_TYPE, 1, 3)) != 'int')
		THEN
			SELECT CONCAT("Changing ec_transact.id to INT") AS "INFO";
			SELECT CONCAT("Adding temporary columns: id_int column and reversed_id_int...") AS "INFO";
			ALTER TABLE ec_transact 
				ADD COLUMN id_int INT NOT NULL,
				ADD COLUMN reversed_id_int INT DEFAULT NULL;
				
			SELECT CONCAT("Updating id_int column to id and reversed_id_int to reversed_id column...") AS "INFO";
            UPDATE ec_transact SET id_int = id, reversed_id_int = reversed_id;

			SELECT CONCAT("Dropping old columns and renaming new columns to take their place...") AS "INFO";
			ALTER TABLE ec_transact
				DROP COLUMN id,
                DROP COLUMN reversed_id,
				CHANGE COLUMN id_int id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                CHANGE COLUMN reversed_id_int reversed_id INT DEFAULT NULL;
		ELSE
			SELECT CONCAT("Skipping ec_transact.id and ec_transact.reversed_id INT column change.") AS "INFO";
		END IF;
	ELSE
		SELECT CONCAT("Current version ", current_version , " not greater than or equal to ", new_version, " version. Doing nothing.") AS "INFO";
	END IF;
END $$
DELIMITER ;

CALL downgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS downgrade_ecds_oltp;
