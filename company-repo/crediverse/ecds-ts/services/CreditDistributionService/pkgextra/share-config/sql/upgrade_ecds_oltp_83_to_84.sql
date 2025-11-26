DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$                
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 83;
	DECLARE new_version int(11) DEFAULT 84;
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

	IF current_version != new_version THEN
		IF current_version != old_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" SET MESSAGE_TEXT = tmp_message_text;
		END IF;

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;


                if not exists (select table_name
                        from information_schema.tables
                        where table_schema = DATABASE()
                        and table_name = 'mobile_numbers_format_mapping') then
                        
                            CREATE TABLE `hxc`.`mobile_numbers_format_mapping` (
                              `new_prefix` varchar(2) NOT NULL,
                              `old_code` varchar(2) NOT NULL,
                              PRIMARY KEY (`old_code`),
                              UNIQUE KEY `old_code_UNIQUE` (`old_code`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

                    select "Created table mobile_numbers_format_mapping" as "INFO";
                end if;

                if not exists (select table_name
                        from information_schema.tables
                        where table_schema = DATABASE()
                        and table_name = 'mobile_numbers_format_config') then
                        
                        CREATE TABLE `hxc`.`mobile_numbers_format_config` (
                          `id` int(11) NOT NULL,
                          `old_number_length` int(11) NOT NULL DEFAULT '8',
                          `phase` int(11) NOT NULL DEFAULT '0',
                          `wrong_b_number_message_en` varchar(500),
                          `wrong_b_number_message_fr` varchar(500),
                          PRIMARY KEY (`id`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
    
                    select "Created table mobile_numbers_format_config" as "INFO";
                end if;


			SELECT CONCAT("Changing OLTP schema version data from ", old_version, " to ", new_version) AS "INFO";
			UPDATE `ec_state` set `value` = new_version WHERE `name` = tag;

		COMMIT;
		SELECT CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) AS "INFO";
	ELSE
		SELECT CONCAT("Not upgrading OLTP schema version, already on ", new_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
