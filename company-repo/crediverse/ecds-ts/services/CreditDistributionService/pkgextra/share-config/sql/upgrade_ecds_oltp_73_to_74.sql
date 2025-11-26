/* Upgrade script removes the NOT NULL constraint on the ea_user.title colum which is not applicable in */
/* the case of API users */
drop procedure if exists upgrade_ecds_oltp;

DELIMITER $$
create procedure upgrade_ecds_oltp()
begin
	declare tag varchar(256) default 'DB Version';
	declare old_version int(11) default 73;
	declare new_version int(11) default 74;
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

		/* Remove not null constraint on title field which is not applicable to API users. */
		ALTER TABLE ea_user MODIFY title varchar(20);
        
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
