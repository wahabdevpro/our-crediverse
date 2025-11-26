DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE old_version int(11) DEFAULT 75;
	DECLARE new_version int(11) DEFAULT 76;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;
	/*MEAT: VARIABLES START*/
	DECLARE fromGroupName varchar(256) DEFAULT 'AgentUsers';
	DECLARE toGroupName varchar(256) DEFAULT 'API/Agent Users';
	/*MEAT: VARIABLES END*/
	
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

			SET FOREIGN_KEY_CHECKS=0;

			/* MEAT: START */
			/* Rename Permission Group  API/Agent Users back to AgentUsers and descriptions */
			IF EXISTS (SELECT 1 FROM es_permission WHERE grp = fromGroupName) 
			THEN
				SELECT CONCAT("Updating es_permission group name to ", toGroupName) AS "INFO";
				UPDATE es_permission SET description='May Add Agent or API Users' WHERE description='May Add Agent Users' AND grp = fromGroupName;
				UPDATE es_permission SET description='May Delete Agent or API Users' WHERE description='May Delete Agent Users' AND grp = fromGroupName;
				UPDATE es_permission SET description='May Update Agent or API Users' WHERE description='May Update Agent Users' AND grp = fromGroupName;
				UPDATE es_permission SET description='May View Agent Users' WHERE description='May View Agent Users' AND grp = fromGroupName;
				UPDATE es_permission SET grp = toGroupName WHERE grp = fromGroupName;
				SELECT CONCAT("Group name and all es_permission descriptions have been updated.") AS "INFO";
			ELSE
				SELECT CONCAT("Could not find es_permission group name ", fromGroupName, ", so upgrade script did not update anything.") AS "INFO";
			END IF;
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
