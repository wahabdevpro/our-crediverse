DELIMITER $$

drop procedure if exists upgrade_ecds_oltp $$

create procedure upgrade_ecds_oltp()
begin

    /* Verify Version */
    select value into @version from `hxc`.`ec_state` where name = 'DB Version';
    if @version != 29 then
        signal sqlstate '45000' set message_text = 'Expected Version 29 Database';
    end if;
    
    start transaction;

    /* Add 2 more nullable columns to the transaction table */
    ALTER TABLE `hxc`.`ea_agent` ADD UNIQUE KEY `es_agent_domain_account` (`comp_id`,`domain_account`);

    /* Update Version */
    update `hxc`.`ec_state` set value = 30 where name = 'DB Version';

    commit;

end $$

call upgrade_ecds_oltp() $$
drop procedure if exists upgrade_ecds_oltp $$

DELIMITER ;






