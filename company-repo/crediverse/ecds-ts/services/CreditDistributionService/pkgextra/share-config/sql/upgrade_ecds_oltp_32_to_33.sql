/* vim: set ft=sql sts=4 ts=4 sw=4 expandtab fo-=t: */
DELIMITER $$

drop procedure if exists upgrade_ecds_oltp $$

create procedure upgrade_ecds_oltp()
begin

    /* Verify Version */
    select value into @version from `hxc`.`ec_state` where name = 'DB Version';
    if @version != 32 then
        signal sqlstate '45000' set message_text = 'Expected Version 32 Database';
    end if;

    start transaction;

    ALTER TABLE `ea_agent`
    CHANGE COLUMN `acc_no` `acc_no` varchar(20) DEFAULT NULL;
    ALTER TABLE `es_webuser`
    CHANGE COLUMN `acc_no` `acc_no` varchar(20) DEFAULT NULL;

    /* Update Version */
    update `hxc`.`ec_state` set value = 33 where name = 'DB Version';

    commit;

end $$

call upgrade_ecds_oltp() $$
drop procedure if exists upgrade_ecds_oltp $$

DELIMITER ;






