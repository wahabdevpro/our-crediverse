DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 56;
	DECLARE new_version int(11) default 57;
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

	if current_version != new_version then
		if current_version != old_version then
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" set MESSAGE_TEXT = tmp_message_text;
		end if;

		select "Starting transaction ..." as "INFO";
        
		start transaction;
            
      -- Add column a_hold_before to table ec_transact      
			if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'a_hold_before' 
                     and TABLE_NAME = 'ec_transact') then

				alter table `ec_transact` 
                add column `a_hold_before` decimal(20,4) not null default 0,
                add column `a_hold_after` decimal(20,4) not null default 0;
        select "Added columns a_hold_before and a_hold_after to table ec_transact" as "INFO";        
                
			end if;
                         			
      -- Add column on_hold to table ea_account      
			if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'on_hold' 
                     and TABLE_NAME = 'ea_account') then

				alter table ea_account add column `on_hold` decimal(20,4) not null default 0;
        select "Added column on_hold to table ea_account" as "INFO";        
                
			end if;

			select concat("Changing OLTP schema version data from ", old_version, " to ", new_version) as "INFO";
			update `ec_state` set `value` = new_version where `name` = tag;
            
		commit;

		select CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) as "INFO";
	else
		select CONCAT("not upgrading OLTP schema version, already on ", new_version) as "INFO";
	end if;

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
