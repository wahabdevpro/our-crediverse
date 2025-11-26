DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 49;
	DECLARE new_version int(11) DEFAULT 50;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;

	SELECT CONCAT("Attempting to upgrade OLTP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = tag;
	END IF;

	/* Verify Version */
	select CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	if current_version != new_version then
		if current_version != old_version then
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" set MESSAGE_TEXT = tmp_message_text;
		end if;

		select "Starting transaction ..." as "INFO";
        
		start transaction;
 			
      -- Create table ep_qualify
        create table if not exists `ep_qualify` (
          `id` int(11) not null,
          `agent_id` int(11) not null,
          `amount_left` decimal(20,4) not null,
          `bundle_id` int(11) default null,
          `cell_id` int(11) default null,
          `company_id` int(11) not null,
          `evaluated` bit(1) not null,
          `lm_time` datetime not null,
          `lm_userid` int(11) not null,
          `sc_id` int(11) default null,
          `start_time` datetime not null,
          `rule_id` int(11) default null,
          `version` int(11) not null,
          primary key (`id`),
          key `ep_qualify_ix1` (`company_id`,`evaluated`,`agent_id`,`start_time`),
          key `ep_qualify_ix2` (`company_id`,`start_time`),
          key `ep_qualify_ix3` (`company_id`,`amount_left`)
        ) ENGINE=InnoDB default CHARSET=utf8;

			select concat("Changing OLTP schema version data from ", old_version, " to ", new_version) as "INFO";
			update `ec_state` set `value` = new_version where `name` = tag;
            
		commit;

		select CONCAT("Upgraded OLTP schema version ", old_version, " to ", new_version) as "INFO";
	else
		select CONCAT("Not upgrading OLTP schema version, already on ", new_version) as "INFO";
	end if;

END $$
DELIMITER ;

CALL upgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;
