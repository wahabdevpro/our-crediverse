DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 35;
	DECLARE new_version int(11) DEFAULT 36;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;

	SELECT CONCAT("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = tag;
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	IF current_version != new_version THEN
		IF current_version != old_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" SET MESSAGE_TEXT = tmp_message_text;
		END IF;

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;

			/* Create table ea_user */
      if not exists (select table_name 
                    from information_schema.tables 
                    where table_schema = DATABASE() 
                    and table_name = 'ea_user') then
                   
            create table `ea_user` (
              `id` int(11) not null AUTO_INCREMENT,
              `acc_no` varchar(20) default null,
              `a_date` datetime default null,
              `agent_id` int(11) not null,
              `channels` int(11) not null,
              `comp_id` int(11) not null,
              `attempts` int(11) default null,
              `d_date` datetime default null,
              `dept` varchar(20) default null,
              `domain_account` varchar(40) default null,
              `email` varchar(50) default null,
              `e_date` datetime default null,
              `first_name` varchar(30) not null,
              `imei` varchar(16) default null,
              `imsi` varchar(15) default null,
              `intitials` varchar(10) default null,
              `key1` tinyblob,
              `key2` tinyblob,
              `key3` tinyblob,
              `key4` tinyblob,
              `language` varchar(2) not null,
              `last_imsi` datetime default null,
              `lm_time` datetime not null,
              `lm_userid` int(11) not NULL,
              `msisdn` varchar(30) not null,
              `role_id` int(11) not null,
              `state` varchar(1) not null,
              `surname` varchar(30) not null,
              `temp_pin` bit(1) not null,
              `title` varchar(20) not null,
              `version` int(11) not null,
              primary key (`id`),
              unique KEY `ea_user_msisdn` (`msisdn`),
              unique KEY `ea_user_acc_no` (`comp_id`,`acc_no`),
              unique KEY `ea_user_domain_account` (`comp_id`,`domain_account`),
              key `FK_AgentUser_Agent` (`agent_id`),
              key `FK_AgentUser_Role` (`role_id`),
              constraint `FK_AgentUser_Role` foreign key (`role_id`) references `es_role` (`id`),
              constraint `FK_AgentUser_Agent` foreign key (`agent_id`) references `ea_agent` (`id`)
            ) ENGINE=InnoDB AUTO_INCREMENT=2 default CHARSET=utf8;
            
            select "Created table ea_user" as "INFO";
            
      end if;   
      
           
			SELECT CONCAT("Changing OLTP schema version data from ", old_version, " to ", new_version) AS "INFO";
			UPDATE `ec_state` set `value` = new_version WHERE `name` = tag;

		COMMIT;
		SELECT CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) AS "INFO";
	ELSE
		SELECT CONCAT("Not upgrading OLTP schema version, already on ", new_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
