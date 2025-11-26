DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 39;
	DECLARE new_version int(11) DEFAULT 40;
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

        /* Add column type to table es_role */
        if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'type' 
                     and TABLE_NAME = 'es_role') then
                    
            alter table `es_role` add column `type` varchar(1) not null default 'W';

            select "Added column type to table es_role" as "INFO";
            
        end if;     
      
        /* Create AgentAll role */
        insert `es_role` (company_id, description, lm_time, lm_userid, name, permanent, version, type)
        select  
            C.id as company_id, 
            "All Agent Permissions" as description, 
            CURRENT_TIMESTAMP() as lm_time, 
            0 as lm_userid, 
            'AgentAll' as name, 
            1 as permanent, 
            C.version, 
            'A' as type
        from `es_company` as C
        left join `es_role` as R on R.company_id = C.id and R.name = 'AgentAll' and R.type = 'A'
        where R.id is null;
        select "Added AgentAll role" as "INFO";
        
        /* Add column role_id to table ea_agent; */
        if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'role_id' 
                     and TABLE_NAME = 'ea_agent;') then
                    
            alter table `ea_agent` add column `role_id` int(11) null;

            update  `ea_agent` as A
            join    `es_role` as R on R.name = 'AgentAll' and R.company_id = A.comp_id
            set     A.`role_id` = R.`id`;

            alter table `ea_agent` change column `role_id` `role_id` int(11) not null,
            add constraint `FK_Agent_Role` foreign key (`role_id` ) references `es_role` (`id` );

            select "Added column role_id to table ea_agent;" as "INFO";
            
        end if;
             
        /* Add column agent_allowed to table es_permission */
        if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'agent_allowed' 
                     and TABLE_NAME = 'es_permission') then
                    
            alter table `es_permission` add column `agent_allowed` bit(1) not null default 0;

            select "Added column agent_allowed to table es_permission" as "INFO";
            
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
