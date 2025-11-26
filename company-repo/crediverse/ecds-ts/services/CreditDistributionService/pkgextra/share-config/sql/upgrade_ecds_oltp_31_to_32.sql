/* vim: set ft=sql sts=4 ts=4 sw=4 expandtab fo-=t: */
DELIMITER $$

drop procedure if exists upgrade_ecds_oltp $$

create procedure upgrade_ecds_oltp()
begin

    /* Verify Version */
    select value into @version from `hxc`.`ec_state` where name = 'DB Version';
    if @version != 31 then
        signal sqlstate '45000' set message_text = 'Expected Version 31 Database';
    end if;
    
    start transaction;
    
    /* Alter the ew_item table and change the description column from 80 characters to 255 characters */
    ALTER TABLE `ew_item`
  	CHANGE COLUMN `description` `description` VARCHAR(255);

    /* Update Version */
    update `hxc`.`ec_state` set value = 32 where name = 'DB Version';

    commit;

end $$

call upgrade_ecds_oltp() $$
drop procedure if exists upgrade_ecds_oltp $$

DELIMITER ;






