DROP PROCEDURE IF EXISTS downgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE downgrade_ecds_oltp()
BEGIN
	DECLARE old_version int(11) DEFAULT 75;	/* FIXME replace 75 with the version this script is downgrading to, e.g. 12 */
	DECLARE new_version int(11) DEFAULT 76; /* FIXME replace 76 with the version this script is downgrading from, e.g. 13  */
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;
	
	/*MEAT: VARIABLES START*/
	DECLARE fromGroupName varchar(256) DEFAULT 'API/Agent Users';
	DECLARE toGroupName varchar(256) DEFAULT 'AgentUsers';
	/*MEAT: VARIABLES END*/
	
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
			/* Rename Permission Group  API/Agent Users back to AgentUsers and descriptions */
			IF EXISTS (SELECT 1 FROM es_permission WHERE grp = fromGroupName) 
			THEN
				SELECT CONCAT("Reverting es_permission group name back to AgentUsers") AS "INFO";
				UPDATE es_permission SET description='May Add Agent Users' WHERE description='May Add Agent or API Users' AND grp = fromGroupName;
				UPDATE es_permission SET description='May Delete Agent Users' WHERE description='May Delete Agent or API Users' AND grp = fromGroupName;
				UPDATE es_permission SET description='May Update Agent Users' WHERE description='May Update Agent or API Users' AND grp = fromGroupName;
				UPDATE es_permission SET description='May View Agent Users' WHERE description='May View Agent Users' AND grp = fromGroupName; 
				UPDATE es_permission SET grp = toGroupName WHERE grp = fromGroupName;
				SELECT CONCAT("Group name and all es_permission descriptions have been reverted.") AS "INFO";
			ELSE
				SELECT CONCAT("Could not find es_permission group name ", fromGroupName, ", so rollback script did not revert anything.") AS "INFO";
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
