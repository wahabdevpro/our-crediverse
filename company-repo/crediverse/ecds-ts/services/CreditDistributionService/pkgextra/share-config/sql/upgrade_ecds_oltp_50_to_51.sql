DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 50;
	DECLARE new_version int(11) DEFAULT 51;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;

	SELECT CONCAT("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = tag;
	END IF;

	/* Verify Version */
	select CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	if current_version != new_version then
		if current_version != old_version then
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" set MESSAGE_TEXT = tmp_message_text;
		end if;

		select "Starting transaction ..." as "INFO";

		start transaction;

			CREATE TABLE `er_report_schedule` (
				`id` int(11) NOT NULL AUTO_INCREMENT,
				`company_id` int(11) NOT NULL,
				`description` varchar(80) NOT NULL,
				`enabled` tinyint(4) NOT NULL,
				`end_time_of_day` int(11) DEFAULT NULL,
				`last_executed` datetime DEFAULT NULL,
				`lm_time` datetime NOT NULL,
				`lm_userid` int(11) NOT NULL,
				`period` int(11) NOT NULL,
				`report_specification_id` int(11) NOT NULL,
				`start_time_of_day` int(11) DEFAULT NULL,
				`time_of_day` int(11) DEFAULT NULL,
				`version` int(11) NOT NULL,
				PRIMARY KEY (`id`),
				KEY `FK_ReportSchedule_ReportSpecification` (`report_specification_id`),
				CONSTRAINT `FK_ReportSchedule_ReportSpecification` FOREIGN KEY (`report_specification_id`) REFERENCES `er_report` (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

			CREATE TABLE `er_report_schedule_webuser` (
				`report_schedule_id` int(11) NOT NULL,
				`webuser_id` int(11) NOT NULL,
				KEY `FKc6vop0e63kme5scs7s8matq2y` (`webuser_id`),
				KEY `FKfdowoe55laix6uoehv88f7j56` (`report_schedule_id`),
				CONSTRAINT `FKc6vop0e63kme5scs7s8matq2y` FOREIGN KEY (`webuser_id`) REFERENCES `es_webuser` (`id`),
				CONSTRAINT `FKfdowoe55laix6uoehv88f7j56` FOREIGN KEY (`report_schedule_id`) REFERENCES `er_report_schedule` (`id`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
