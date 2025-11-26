DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 55;
	DECLARE new_version int(11) default 56;
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
 			
      -- Add column retriggers to table ep_promo      
			if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'retriggers' 
                     and TABLE_NAME = 'ep_promo') then

				alter table ep_promo add column `retriggers` bit(1) not null default 0;
        select "Added column retriggers to table ea_agent" as "INFO";        
                
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
