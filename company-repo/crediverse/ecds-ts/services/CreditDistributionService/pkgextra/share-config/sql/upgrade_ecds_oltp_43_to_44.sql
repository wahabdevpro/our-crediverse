DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 43;
	DECLARE new_version int(11) DEFAULT 44;
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
     
        /* Add columns sms_keyword ec_bundle */
        if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'sms_keyword' 
                     and TABLE_NAME = 'ec_bundle') then
                    
            alter table `ec_bundle` 
                add column `sms_keyword` varchar(20) default null;

     
            select "Added column sms_keyword to table ec_bundle" as "INFO";
            
        end if; 
        
        /* Add column sms_keyword ec_bundle_lang */
        if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'sms_keyword' 
                     and TABLE_NAME = 'ec_bundle_lang') then
                    
            alter table `ec_bundle_lang` 
                add column `sms_keyword` varchar(20) default null;
     
            select "Added column sms_keyword to table ec_bundle_lang" as "INFO";
            
        end if; 
        
        /* Add column ussd_code ec_bundle */
        if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'ussd_code' 
                     and TABLE_NAME = 'ec_bundle') then
                    
            alter table `ec_bundle` 
                add column `ussd_code` varchar(6) default null;
     
            select "Added column ussd_code to table ec_bundle" as "INFO";
            
        end if;         

            
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
