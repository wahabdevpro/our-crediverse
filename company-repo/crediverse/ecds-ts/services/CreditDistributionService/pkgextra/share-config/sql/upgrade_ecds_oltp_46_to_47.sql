DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 46;
	DECLARE new_version int(11) DEFAULT 47;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;

	SELECT CONCAT("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = tag;
	END IF;

	/* Verify Version */
	SELECT CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	if current_version != new_version then
		if current_version != old_version then
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" set MESSAGE_TEXT = tmp_message_text;
		end if;

		select "Starting transaction ..." as "INFO";
		start transaction;
			SET FOREIGN_KEY_CHECKS=0;
			/* MEAT: START */

			CREATE TABLE IF NOT EXISTS `er_report` (
			  `id` int(11) NOT NULL AUTO_INCREMENT,
			  `company_id` int(11) NOT NULL,
			  `description` varchar(80) NOT NULL,
			  `lm_time` datetime NOT NULL,
			  `lm_userid` int(11) NOT NULL,
			  `name` varchar(50) NOT NULL,
			  `parameters` text NOT NULL,
			  `type` varchar(50) NOT NULL,
			  `version` int(11) NOT NULL,
			  PRIMARY KEY (`id`),
			  UNIQUE KEY `er_report_name` (`company_id`,`name`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

			/* MEAT: END */
			select concat("Changing OLTP schema version data from ", old_version, " to ", new_version) as "INFO";
			update `ec_state` set `value` = new_version where `name` = tag;
		commit;

		select CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) as "INFO";
	else
		select CONCAT("Not upgrading OLTP schema version, already on ", new_version) as "INFO";
	end if;

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
