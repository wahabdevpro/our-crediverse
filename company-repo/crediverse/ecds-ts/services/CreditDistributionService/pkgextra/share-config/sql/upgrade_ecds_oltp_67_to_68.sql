
drop procedure if exists upgrade_ecds_oltp;

DELIMITER $$
create procedure upgrade_ecds_oltp()
begin
	declare tag varchar(256) default 'DB Version';
	declare old_version int(11) default 67;
	declare new_version int(11) default 68;
	declare tmp_message_text varchar(256) default null;
	declare current_version int(11) default null;

	select concat("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) as `INFO`;

	if not exists (select * from `information_schema`.`TABLES` where `TABLE_SCHEMA` = database() and `TABLE_NAME` = "ec_state") then
		set current_version = 0;
	else
		select value into current_version from `ec_state` where name = tag;
	end if;

	/* Verify Version */
	select concat("Current OLTP schema version data is ", current_version) as `INFO`;

	if current_version != new_version then
		if current_version != old_version then
			set tmp_message_text = concat("Expected Version ", old_version, " Database");
			signal sqlstate "45000" set message_text = tmp_message_text;
		end if;

		select "Starting transaction ..." as `INFO`;
        
		start transaction;

		/* Add Column ea_agent.auth_method if it doesn't exist */
		if not exists (select null 
				 from information_schema.COLUMNS 
				 where TABLE_SCHEMA = database()
				 and COLUMN_NAME = 'auth_method' 
				 and TABLE_NAME = 'ea_agent') then

			alter table `ea_agent` add column `auth_method` varchar(1) not null default 'P';
		end if;
        
		/* Add Column ea_user.auth_method if it doesn't exist */
		if not exists (select null 
				 from information_schema.COLUMNS 
				 where TABLE_SCHEMA = database()
				 and COLUMN_NAME = 'auth_method' 
				 and TABLE_NAME = 'ea_user') then

			alter table `ea_user` add column `auth_method` varchar(1) not null default 'P';
		end if;
        
        /* Add Column es_webuser.auth_method if it doesn't exist */
		if not exists (select null 
				 from information_schema.COLUMNS 
				 where TABLE_SCHEMA = database()
				 and COLUMN_NAME = 'auth_method' 
				 and TABLE_NAME = 'es_webuser') then

			alter table `es_webuser` add column `auth_method` varchar(1) not null default 'X';
		end if;
        
        /* Add Column es_webuser.attempts if it doesn't exist */
		if not exists (select null 
				 from information_schema.COLUMNS 
				 where TABLE_SCHEMA = database()
				 and COLUMN_NAME = 'attempts' 
				 and TABLE_NAME = 'es_webuser') then

			alter table `es_webuser` add column `attempts`int(11) null default 0;
		end if;
        
        /* Add Column es_webuser.key3 if it doesn't exist */
		if not exists (select null 
				 from information_schema.COLUMNS 
				 where TABLE_SCHEMA = database()
				 and COLUMN_NAME = 'key3' 
				 and TABLE_NAME = 'es_webuser') then

			alter table `es_webuser` add column `key3` tinyblob;
		end if;
        
        /* Add Column es_webuser.key4 if it doesn't exist */
		if not exists (select null 
				 from information_schema.COLUMNS 
				 where TABLE_SCHEMA = database()
				 and COLUMN_NAME = 'key4' 
				 and TABLE_NAME = 'es_webuser') then

			alter table `es_webuser` add column `key4` tinyblob;
		end if;        

        /* Add Column es_webuser.pin_version if it doesn't exist */
		if not exists (select null 
				 from information_schema.COLUMNS 
				 where TABLE_SCHEMA = database()
				 and COLUMN_NAME = 'pin_version' 
				 and TABLE_NAME = 'es_webuser') then

			alter table `es_webuser` add column `pin_version` int(11) not null default 0;
		end if; 

        /* Add Column es_webuser.temp_pin if it doesn't exist */
		if not exists (select null 
				 from information_schema.COLUMNS 
				 where TABLE_SCHEMA = database()
				 and COLUMN_NAME = 'temp_pin' 
				 and TABLE_NAME = 'es_webuser') then

			alter table `es_webuser` add column `temp_pin` bit(1) not null default 0;
		end if;

        select concat("Changing OLTP schema version data from ", old_version, " to ", new_version) as `INFO`;
        update `ec_state` set `value` = new_version where `name` = tag;
            
		commit;

		select concat("Upgraded OLTP schema version ", old_version, " to ", new_version) as `INFO`;
	else
		select concat("Not upgrading OLTP schema version, already on ", new_version) as `INFO`;
	end if;

end $$
DELIMITER ;

call upgrade_ecds_oltp();
drop procedure if exists upgrade_ecds_oltp;
