/* vim: set ft=sql sts=4 ts=4 sw=4 expandtab fo-=t: */
DELIMITER $$

drop procedure if exists upgrade_ecds_oltp $$

create procedure upgrade_ecds_oltp()
begin

    /* Verify Version */
    select value into @version from `hxc`.`ec_state` where name = 'DB Version';
    if @version != 30 then
        signal sqlstate '45000' set message_text = 'Expected Version 30 Database';
    end if;

    start transaction;

    CREATE TABLE `ew_item` (
        `id` int(11) NOT NULL AUTO_INCREMENT,
        `company_id` int(11) NOT NULL,
        `completed` datetime DEFAULT NULL,
        `by_agent_id` int(11) DEFAULT NULL,
        `by_user_id` int(11) DEFAULT NULL,
        `for_perm_id` int(11) DEFAULT NULL,
        `for_user_id` int(11) DEFAULT NULL,
        `created` datetime NOT NULL,
        `description` varchar(80) NOT NULL,
        `lm_time` datetime NOT NULL,
        `lm_userid` int(11) NOT NULL,
        `request` text,
        `response` text,
        `send_sms` bit(1) NOT NULL,
        `state` varchar(1) NOT NULL,
        `type` varchar(1) NOT NULL,
        `uri` varchar(200) NOT NULL,
        `uuid` binary(16) NOT NULL,
        `version` int(11) NOT NULL,
        PRIMARY KEY (`id`),
        UNIQUE KEY `ew_item_uuid` (`uuid`),
        KEY `FK_Work_Agent` (`by_agent_id`),
        KEY `FK_Work_By` (`by_user_id`),
        KEY `FK_Work_Perm` (`for_perm_id`),
        KEY `FK_Work_For` (`for_user_id`),
        CONSTRAINT `FK_Work_Agent` FOREIGN KEY (`by_agent_id`) REFERENCES `ea_agent` (`id`),
        CONSTRAINT `FK_Work_By` FOREIGN KEY (`by_user_id`) REFERENCES `es_webuser` (`id`),
        CONSTRAINT `FK_Work_For` FOREIGN KEY (`for_user_id`) REFERENCES `es_webuser` (`id`),
        CONSTRAINT `FK_Work_Perm` FOREIGN KEY (`for_perm_id`) REFERENCES `es_permission` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    /* Create Department table */
    create table `es_dept` (
      `id` int(11) not null AUTO_INCREMENT,
      `company_id` int(11) not null,
      `lm_time` datetime not null,
      `lm_userid` int(11) not null,
      `name` varchar(20) not null,
      `version` int(11) not null,
      primary key (`id`),
      unique key `es_dept_name` (`company_id`,`name`)
    ) engine=InnoDB default CHARSET=utf8;

    /* Add a DepartmentID column in WebUser which is nullable at first */
    alter table `es_webuser` add column `dept_id` int(11) null;

    /* Make empty Departments in WebUser table '-' */
    update `es_webuser` set `dept` = '-' where `dept` is null or `dept` = '';

    /* Insert distinct departments from WebUser table to Department table*/
    insert into `es_dept` (company_id,lm_time,lm_userid,name,version)
    select distinct `comp_id`, curdate(), 0, `dept`, 0 from `es_webuser` order by `dept`;

    /* Update Department IDs in WebUser table */
    update  `es_webuser` as w
    join    `es_dept` as d on w.dept = d.name and w.`comp_id` = d.`company_id`
    set      w.`dept_id` = d.`id`;

    /* Make the DepartmentID in the WebUser table not null */
    alter table `es_webuser` change column `dept_id` `dept_id` int(11) not null;

    /* Add foreign key between WebUser and Department tables */
    alter table `es_webuser` add constraint `FK_User_Dept` foreign key (`dept_id`) references `es_dept` (`id`);

    /* Drop the old Department column in Web User */ 
    alter table `es_webuser` drop column `dept`;

    /* Update Version */
    update `hxc`.`ec_state` set value = 31 where name = 'DB Version';

    commit;

end $$

call upgrade_ecds_oltp() $$
drop procedure if exists upgrade_ecds_oltp $$

DELIMITER ;






