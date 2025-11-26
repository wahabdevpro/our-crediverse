DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 64;
	DECLARE new_version int(11) default 65;
	DECLARE tmp_message_text varchar(256) default null;
	DECLARE current_version int(11) default null;

	SELECT CONCAT("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) AS "INFO";

	IF not EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = tag;
	END IF;

	/* Verify Version */
	select CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	IF current_version != new_version THEN
		IF current_version != old_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" set MESSAGE_TEXT = tmp_message_text;
		END IF;

		SELECT "Starting transaction ..." as "INFO";

		START TRANSACTION;
			/* MEAT: START */

			IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name = 'er_report' AND index_name = 'ix_er_report_agent_id') > 0) THEN
				SET @s = CONCAT('DROP INDEX `ix_er_report_agent_id` ON `er_report`');
				PREPARE stmt FROM @s;
				EXECUTE stmt;
			END IF;
			
			IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name = 'er_report' AND index_name = 'er_report_agent_id') > 0) THEN
				SET @s = CONCAT('DROP INDEX `er_report_agent_id` ON `er_report`');
				PREPARE stmt FROM @s;
				EXECUTE stmt;
			END IF;
	
			ALTER TABLE `er_report` ADD INDEX er_report_agent_id (  `agent_id` ) ;
	
	        SELECT "Creating table er_report_schedule_agent_user" as `INFO`;
			CREATE TABLE IF NOT EXISTS `er_report_schedule_agent_user` (
			  `report_schedule_id` int(11) NOT NULL,
			  `agent_user_id` int(11) NOT NULL,
			  KEY `er_report_schedule_agent_user_agent_user_id` (`agent_user_id`),
			  KEY `er_report_schedule_agent_user_report_schedule_id` (`report_schedule_id`)
			) ENGINE=InnoDB;
	
			ALTER TABLE `er_report_schedule_agent_user`
			  ADD CONSTRAINT `fk_er_report_schedule_agent_user_agent_user_id` FOREIGN KEY (`agent_user_id`) REFERENCES `ea_user` (`id`),
			  ADD CONSTRAINT `fk_er_report_schedule_agent_user_report_schedule_id` FOREIGN KEY (`report_schedule_id`) REFERENCES `er_report_schedule` (`id`);
	        SELECT "Created table er_report_schedule_agent_user" as `INFO`;
	
	        SELECT "Creating table er_report_schedule_recipient_email" as `INFO`;
			CREATE TABLE IF NOT EXISTS `er_report_schedule_recipient_email` (
				`report_schedule_id` int(11) NOT NULL,
				`email` varchar(255) NOT NULL,
				PRIMARY KEY (`report_schedule_id`, `email`)
			) ENGINE=InnoDB;
	        SELECT "Created table er_report_schedule_recipient_email" as `INFO`;

			/* MEAT: END */
	        SELECT concat("Changing OLTP schema version data from ", old_version, " to ", new_version) as "INFO";
    	    UPDATE `ec_state` SET `value` = new_version WHERE `name` = tag;

		COMMIT;

		SELECT CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) AS "INFO";
	ELSE
		SELECT CONCAT("not upgrading OLTP schema version, already on ", new_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
