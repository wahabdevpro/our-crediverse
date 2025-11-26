DROP PROCEDURE IF EXISTS upgrade_ecds_olap;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_olap()
BEGIN
	DECLARE old_version int(11) DEFAULT 6;
	DECLARE new_version int(11) DEFAULT 7;
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
			IF NOT EXISTS (SELECT "1" FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ap_transact' AND COLUMN_NAME = 'follow_up') THEN
				ALTER TABLE `ap_transact` 
					ADD `follow_up` VARCHAR(255) NOT NULL DEFAULT 'none';
			END IF;
			
			/*Delete the temp column in the unlikely event that it exists (maybe failed upgrade)*/
			IF EXISTS (SELECT "1" FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ap_transact' AND COLUMN_NAME = 'follow_up_str') THEN
				ALTER TABLE `ap_transact` 
					DROP `follow_up_str`;
			END IF;

			ALTER TABLE  `ap_transact` 
				ADD  `follow_up_str` VARCHAR(255) NOT NULL DEFAULT  'none';
			
			UPDATE `ap_transact` 
				SET follow_up_str = 'pending' WHERE follow_up = '1' OR follow_up = 'pending';
				
			UPDATE `ap_transact` T1
			INNER JOIN `ap_transact` T2 ON (T2.type = 'AD' AND T2.related_id = T1.id)
			SET T1.follow_up_str = 'adjudicated' WHERE  T2.id IS NOT NULL;
			
			ALTER TABLE `ap_transact` 
				DROP `follow_up`;
			
			ALTER TABLE `ap_transact` 
				CHANGE `follow_up_str` `follow_up` VARCHAR(255) NOT NULL DEFAULT 'none';
			
			ALTER TABLE  `ap_transact` ADD INDEX (  `follow_up` ) ;

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
