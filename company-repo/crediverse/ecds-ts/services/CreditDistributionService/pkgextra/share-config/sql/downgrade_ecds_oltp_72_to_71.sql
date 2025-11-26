drop procedure if exists downgrade_ecds_oltp;

DELIMITER $$
create procedure downgrade_ecds_oltp()
begin
	declare tag varchar(256) default 'DB Version';
	declare old_version int(11) default 72;
	declare new_version int(11) default 71;
	declare tmp_message_text varchar(256) default null;
	declare current_version int(11) default null;

	select concat("Attempting to downgrade OLTP schema version ", old_version, " to ", new_version) as `INFO`;

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

        -- Drop index ix_ec_transact_aowner from table ec_transact
        if exists (select null 
                     from information_schema.statistics 
                     where TABLE_SCHEMA = database()
                     and INDEX_NAME = 'ix_ec_transact_aowner' 
                     and TABLE_NAME = 'ec_transact') then
			
            -- alter table `ec_transact`                  
			--	drop index `ix_ec_transact_aowner`;

            -- select "Dropped index ix_ec_transact_aowner from table ec_transact" as `INFO`;
            
			select "NOT dropping index ix_ec_transact_aowner from table ec_transact, should not impact operations" as `INFO`;

        end if;
        
        -- Drop index ix_ec_transact_bowner from table ec_transact
        if exists (select null 
                     from information_schema.statistics 
                     where TABLE_SCHEMA = database()
                     and INDEX_NAME = 'ix_ec_transact_bowner' 
                     and TABLE_NAME = 'ec_transact') then
			
            -- alter table `ec_transact`                  
			--	drop index `ix_ec_transact_bowner`;

            -- select "Dropped index ix_ec_transact_bowner from table ec_transact" as `INFO`;
            
			select "NOT dropping index ix_ec_transact_bowner from table ec_transact, should not impact operations" as `INFO`;

        end if;
        

        select concat("Changing OLTP schema version data from ", old_version, " to ", new_version) as `INFO`;
        update `ec_state` set `value` = new_version where `name` = tag;
            
		commit;

		select concat("Downgraded OLTP schema version ", old_version, " to ", new_version) as `INFO`;
	else
		select concat("Not downgrading OLTP schema version, already on ", new_version) as `INFO`;
	end if;

end $$
DELIMITER ;

call downgrade_ecds_oltp();
drop procedure if exists downgrade_ecds_oltp;
