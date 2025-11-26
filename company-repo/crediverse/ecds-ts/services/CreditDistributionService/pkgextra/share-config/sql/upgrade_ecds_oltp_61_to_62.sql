
drop procedure if exists upgrade_ecds_oltp;

DELIMITER $$
create procedure upgrade_ecds_oltp()
begin
	declare tag varchar(256) default 'DB Version';
	declare old_version int(11) default 61;
	declare new_version int(11) default 62;
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

        -- Add index ix_ec_transact_started to table ec_transact
        if not exists (select null 
                     from information_schema.statistics 
                     where TABLE_SCHEMA = database()
                     and INDEX_NAME = 'ix_ec_transact_started' 
                     and TABLE_NAME = 'ec_transact') then
			
            alter table `ec_transact`                  
				add index `ix_ec_transact_started` (`comp_id`, `started` asc);

            select "Added index ix_ec_transact_started to table ec_transact" as `INFO`;

        end if;
        
        -- Add index ix_ec_transact_rev to table ec_transact
        if not exists (select null 
                     from information_schema.statistics 
                     where TABLE_SCHEMA = database()
                     and INDEX_NAME = 'ix_ec_transact_rev' 
                     and TABLE_NAME = 'ec_transact') then
			
            alter table `ec_transact`                  
				add index `ix_ec_transact_rev` (`reversed_id` asc);

            select "Added index ix_ec_transact_rev to table ec_transact" as `INFO`;

        end if;
        
        -- Add index ix_ec_transact_amsisdn to table ec_transact
        if not exists (select null 
                     from information_schema.statistics 
                     where TABLE_SCHEMA = database()
                     and INDEX_NAME = 'ix_ec_transact_amsisdn' 
                     and TABLE_NAME = 'ec_transact') then
			
            alter table `ec_transact`                  
				add index `ix_ec_transact_amsisdn` (`comp_id`, `a_msisdn` asc);

            select "Added index ix_ec_transact_amsisdn to table ec_transact" as `INFO`;

        end if;
        
        -- Add index ix_ec_transact_bmsisdn to table ec_transact
        if not exists (select null 
                     from information_schema.statistics 
                     where TABLE_SCHEMA = database()
                     and INDEX_NAME = 'ix_ec_transact_bmsisdn' 
                     and TABLE_NAME = 'ec_transact') then
			
            alter table `ec_transact`                  
				add index `ix_ec_transact_bmsisdn` (`comp_id`, `b_msisdn` asc);

            select "Added index ix_ec_transact_bmsisdn to table ec_transact" as `INFO`;

        end if;
        
        -- Add index ix_ec_transact_aagent to table ec_transact
        if not exists (select null 
                     from information_schema.statistics 
                     where TABLE_SCHEMA = database()
                     and INDEX_NAME = 'ix_ec_transact_aagent' 
                     and TABLE_NAME = 'ec_transact') then
			
            alter table `ec_transact`                  
				add index `ix_ec_transact_aagent` (`a_agent` asc);

            select "Added index ix_ec_transact_aagent to table ec_transact" as `INFO`;

        end if;
        
        -- Add index ix_ec_transact_bagent to table ec_transact
        if not exists (select null 
                     from information_schema.statistics 
                     where TABLE_SCHEMA = database()
                     and INDEX_NAME = 'ix_ec_transact_bagent' 
                     and TABLE_NAME = 'ec_transact') then
			
            alter table `ec_transact`                  
				add index `ix_ec_transact_bagent` (`b_agent` asc);

            select "Added index ix_ec_transact_bagent to table ec_transact" as `INFO`;

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
