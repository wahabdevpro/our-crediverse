DELIMITER $$

drop procedure if exists upgrade_ecds_oltp $$

create procedure upgrade_ecds_oltp()
begin

    /* Verify Version */
    select value into @version from `hxc`.`ec_state` where name = 'DB Version';
    if @version != 28 then
        signal sqlstate '45000' set message_text = 'Expected Version 28 Database';
    end if;
    
    start transaction;

    /* Add 2 more nullable columns to the transaction table */
    alter table `hxc`.`ec_transact` add column `bonus_pct` decimal(20,8) null default 0, add column `bonus_prov` decimal(20,4) default null;

    /* Update Version */
    update `hxc`.`ec_state` set value = 29 where name = 'DB Version';

    commit;

end $$

call upgrade_ecds_oltp() $$
drop procedure if exists upgrade_ecds_oltp $$

DELIMITER ;






