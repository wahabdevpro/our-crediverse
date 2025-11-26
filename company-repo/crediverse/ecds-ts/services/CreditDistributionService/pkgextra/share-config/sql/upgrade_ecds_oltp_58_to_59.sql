DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) default 'DB Version';
	DECLARE old_version int(11) default 58;
	DECLARE new_version int(11) default 59;
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

        -- Add Columns last_cell_id and last_cell_expires to ea_agent
        if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'last_cell_id' 
                     and TABLE_NAME = 'ea_agent') then
			
            alter table `ea_agent` 
                add column `last_cell_expires` datetime DEFAULT NULL,
                add column `last_cell_id` int(11) DEFAULT NULL,
                add constraint `FK_Agent_Cell` foreign key (last_cell_id) references `el_cell` (`id`); 

            select "Added Columns last_cell_id and last_cell_expires to ea_agent" as "INFO";

        end if;
        
        -- Add Columns last_cell_id and last_cell_expires to ea_user
        if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'last_cell_id' 
                     and TABLE_NAME = 'ea_user') then
			
            alter table `ea_user` 
                add column `last_cell_expires` datetime DEFAULT NULL,
                add column `last_cell_id` int(11) DEFAULT NULL,
                add constraint `FK_AgentUser_Cell` foreign key (last_cell_id) references `el_cell` (`id`); 

            select "Added Columns last_cell_id and last_cell_expires to ea_user" as "INFO";

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
