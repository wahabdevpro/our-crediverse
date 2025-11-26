DROP PROCEDURE IF EXISTS upgrade_ecds_olap;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_olap()
BEGIN
	DECLARE old_version int(11) DEFAULT 10;
	DECLARE new_version int(11) DEFAULT 11;
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
			SELECT CONCAT("Not upgrading OLAP schema version, expected version ", old_version) AS "INFO";
		ELSE
		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;
			/* MEAT: START */

			SELECT "Checking if ap_transact.ap_transact_aggregate001 index exists ..." as `INFO`;
			IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name = 'ap_transact' AND index_name = 'ap_transact_aggregate001') = 0) THEN
				SELECT "Index ap_transact.ap_transact_aggregate001 does not exists, creating ..." as `INFO`;
				ALTER TABLE `ap_transact`
				    ADD KEY `ap_transact_aggregate001` (`a_owner_id`,`ended_date`,`a_msisdn`,`type`,`success`,`b_msisdn`);
				SELECT "Index ap_transact.ap_transact_aggregate001 created." as `INFO`;
			END IF;
			
			SELECT "Checking if ap_transact.ap_transact_aggregate002 index exists ..." as `INFO`;
			IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name = 'ap_transact' AND index_name = 'ap_transact_aggregate002') = 0) THEN
				SELECT "Index ap_transact.ap_transact_aggregate002 does not exists, creating ..." as `INFO`;
				ALTER TABLE `ap_transact`
				    ADD KEY `ap_transact_aggregate002` (`b_owner_id`,`ended_date`,`a_msisdn`,`type`,`success`,`b_msisdn`);
				SELECT "Index ap_transact.ap_transact_aggregate002 created." as `INFO`;
			END IF;

			/* MEAT: END */
			SELECT CONCAT("Changing OLAP schema version data from ", old_version, " to ", new_version) AS "INFO";
			UPDATE `ap_schema_data` SET `value` = new_version WHERE `vkey` = "VERSION" AND `value` = old_version;

		COMMIT;
		SELECT CONCAT("Upgraded OLAP schema version ", old_version, " to ", new_version) AS "INFO";
		END IF;
	ELSE
		SELECT CONCAT("Not upgrading OLAP schema version, already on ", new_version) AS "INFO";
	END IF;

	/* Emergency Patch */
	SELECT CONCAT("Checking if emergency patch needs to be applied to ap_transact id and related_id columns... ( current_version_string >= ", new_version, " )") AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ap_schema_data") THEN
		SET current_version_string = 0;
	ELSE
		SELECT value INTO current_version_string FROM `ap_schema_data` WHERE `vkey` = "VERSION";
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLAP schema version data is ", current_version_string) AS "INFO";
	IF (current_version_string >= new_version) THEN
		/*IF EXISTS (SELECT 'exists' FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'ap_transact' AND `COLUMN_NAME` = 'id_bigint') 
		THEN
			SELECT CONCAT("Removing temporary column from ap_transact.id_bigint") AS "INFO";
			ALTER TABLE `ap_transact` DROP `id_bigint`;
		END IF;
		
		IF EXISTS (SELECT 'exists' FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'ap_transact' AND `COLUMN_NAME` = 'related_id_bigint') 
		THEN
			SELECT CONCAT("Removing temporary column from ap_transact.related_id_bigint") AS "INFO";
			ALTER TABLE `ap_transact` DROP COLUMN `related_id_bigint`;
		END IF;
		*/

		SET @condition_ap_transact_id_match = 0;
		SET @condition_ap_transact_related_id_match = 0;
		IF EXISTS (SELECT 'exists' FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND `TABLE_NAME` = 'ap_transact' AND `COLUMN_NAME` = 'id' AND LOWER(DATA_TYPE) != 'bigint' AND LOWER(SUBSTRING(COLUMN_TYPE, 1, 6)) != 'bigint') THEN
			SET @condition_ap_transact_id_match = 1;
		END IF;
		IF EXISTS (SELECT 'exists' from information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ap_transact' AND COLUMN_NAME = 'related_id' AND LOWER(DATA_TYPE) != 'bigint' AND LOWER(SUBSTRING(COLUMN_TYPE, 1, 6)) != 'bigint') THEN
			SET @condition_ap_transact_related_id_match = 1;
		END IF;

		SELECT @condition_ap_transact_id_match, @condition_ap_transact_related_id_match, (@condition_ap_transact_id_match != 0 AND @condition_ap_transact_related_id_match != 0);

		IF (@condition_ap_transact_id_match != 0 AND @condition_ap_transact_related_id_match != 0)
		THEN
			SELECT CONCAT("Changing ap_transact.id and ap_transact.related_id to BIGINT") AS "INFO";
			ALTER TABLE ap_transact
				MODIFY COLUMN id BIGINT NOT NULL,
				MODIFY COLUMN related_id BIGINT DEFAULT NULL;
			SELECT CONCAT("Completed changing ap_transact.id and ap_transact.related_id to BIGINT") AS "INFO";
		ELSE
			SELECT CONCAT("Skipping ap_transact.id and ap_transact.related_id BIGINT column change.") AS "INFO";
		END IF;
	ELSE
		SELECT CONCAT("Not applying emergency patch, ( ", current_version_string, " < ", new_version, " )") AS "INFO";
	END IF;
	/* End: Emergency Patch */
END $$
DELIMITER ;

CALL upgrade_ecds_olap();
DROP PROCEDURE IF EXISTS upgrade_ecds_olap;
