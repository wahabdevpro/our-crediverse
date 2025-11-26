DROP PROCEDURE IF EXISTS downgrade_ecds_olap;

DELIMITER $$
CREATE PROCEDURE downgrade_ecds_olap()
BEGIN
	DECLARE new_version int(11) DEFAULT 11;
	DECLARE current_version_string varchar(128) DEFAULT NULL;

	/*booleans ...*/
	-- DECLARE ap_transact_id_match int(11) DEFAULT 0;
	-- DECLARE ap_transact_related_id_match int(11) DEFAULT 0;
	-- DECLARE all_match int(11) DEFAULT 0;

	SELECT CONCAT("Checking if emergency patch downgrade is applicable and needs to be applied to ap_transact id and related_id columns... ( current_version_string >= ", new_version," )") AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ap_schema_data") THEN
		SET current_version_string = 0;
	ELSE
		SELECT value INTO current_version_string FROM `ap_schema_data` WHERE `vkey` = "VERSION";
	END IF;

	SELECT CONCAT("Current OLAP schema version data is ", current_version_string) AS "INFO";

	IF (current_version_string >= new_version) THEN
		SET @condition_ap_transact_id_match = 0;
		SET @condition_ap_transact_related_id_match = 0;

		IF EXISTS (SELECT 'exists' from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ap_transact' AND COLUMN_NAME = 'id' AND LOWER(DATA_TYPE) != 'int' AND LOWER(SUBSTRING(COLUMN_TYPE, 1, 3)) != 'int') THEN
			SET @condition_ap_transact_id_match = 1;
		END IF;
		IF EXISTS (SELECT 'exists' from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ap_transact' AND COLUMN_NAME = 'related_id' AND LOWER(DATA_TYPE) != 'int' AND LOWER(SUBSTRING(COLUMN_TYPE, 1, 3)) != 'int') THEN
			SET @condition_ap_transact_related_id_match = 1;
		END IF;
		
		SELECT @condition_ap_transact_id_match, @condition_ap_transact_related_id_match, (@condition_ap_transact_related_id_match != 0 AND @condition_ap_transact_id_match != 0);

		IF (@condition_ap_transact_related_id_match != 0 AND @condition_ap_transact_id_match != 0) THEN
			SELECT CONCAT("Changing ap_transact.id and ap_transact.related_id to INT") AS "INFO";

			SELECT CONCAT("Adding id_int and related_id_int column...") AS "INFO";
			ALTER TABLE ap_transact
				ADD COLUMN id_int INT NOT NULL,
				ADD COLUMN related_id_int INT DEFAULT NULL;

			SELECT CONCAT("Updating id_int column to id and related_id_int to related_id column...") AS "INFO";
			UPDATE ap_transact SET id_int = id, related_id_int = related_id;
			
			ALTER TABLE ap_transact
				DROP COLUMN id,
				DROP COLUMN related_id,
				CHANGE COLUMN id_int id INT NOT NULL PRIMARY KEY,
				CHANGE COLUMN related_id_int related_id INT DEFAULT NULL;
		END IF;
	ELSE
		SELECT CONCAT("Not applying emergency patch downgrade, ( ", current_version_string, " < ", new_version, " )") AS "INFO";
	END IF;	
END $$
DELIMITER ;

CALL downgrade_ecds_olap();
DROP PROCEDURE IF EXISTS downgrade_ecds_olap;
