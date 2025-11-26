DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 41;
	DECLARE new_version int(11) DEFAULT 42;
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
     
        /* Add es_client table */
        if not exists (select null 
                     from information_schema.TABLES 
                     where TABLE_SCHEMA = DATABASE()
                     and TABLE_NAME = 'es_client') then
                    
           create table `es_client` (
          `user_type` varchar(1) not null,
          `user_id` int(11) not null,
          `value_key` varchar(20) not null,
          `company_id` int(11) not null,
          `lm_time` datetime not null,
          `last_date` datetime not null,
          `lm_userid` int(11) not null,
          `value_text` text,
          `version` int(11) not null,
          primary key (`company_id`, `user_type`,`user_id`,`value_key`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8;      
                    
   
            select "Added es_client table" as "INFO";
            
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
