DROP PROCEDURE IF EXISTS downgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE downgrade_ecds_oltp()
BEGIN
	DECLARE old_version int(11) DEFAULT 78;
	DECLARE new_version int(11) DEFAULT 79;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;
	DECLARE max_audit_key bigint DEFAULT NULL;
	DECLARE counter int DEFAULT 0;
	DECLARE chunk_size int DEFAULT 100000;

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

			SET FOREIGN_KEY_CHECKS=0;

			/* MEAT: START */
			IF EXISTS (SELECT 'exists' from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'es_audit' AND COLUMN_NAME = 'id' AND LOWER(DATA_TYPE) != 'int' AND LOWER(SUBSTRING(COLUMN_TYPE, 1, 3)) != 'int')
			THEN
				SELECT CONCAT("Changing es_audit.id to INT") AS "INFO";
				SELECT CONCAT("Adding temporary column: id_int column...") AS "INFO";
				ALTER TABLE es_audit 
					ADD COLUMN id_int INT NOT NULL;
				COMMIT;
				
				SELECT max(id) INTO max_audit_key FROM es_audit;
				
				SELECT CONCAT("Updating id_int column to id...") AS "INFO";
				updateKey: LOOP
					SET counter = counter + chunk_size;
					UPDATE es_audit SET id_int = id WHERE id <= counter AND id >= (counter - chunk_size);
					COMMIT;
					IF counter < max_audit_key THEN
						ITERATE updateKey;
					END IF;
					LEAVE updateKey;
				END LOOP updateKey;
	
				SELECT CONCAT("Dropping old column and renaming new column to take their place...") AS "INFO";
				ALTER TABLE es_audit
					DROP COLUMN id,
					CHANGE COLUMN id_int id INT NOT NULL PRIMARY KEY AUTO_INCREMENT;
			ELSE
				SELECT CONCAT("Skipping es_audit.id INT column change.") AS "INFO";
			END IF;

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
