DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 40;
	DECLARE new_version int(11) DEFAULT 41;
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
     
        /* Add columns bundle_id, prom_id, req_msisdn and req_type to table ec_transact */
        if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'req_msisdn' 
                     and TABLE_NAME = 'ec_transact') then
                    
            alter table `ec_transact` 
                add column `bundle_id` int(11) null,
                add column `prom_id` int(11)  null,
                add column `req_msisdn` varchar(30) null,
                add column `req_type` varchar(1) null;
                
            update `ec_transact` as T
            left join `es_webuser` as W on (T.caller = W.domain_name or T.caller = W.msisdn) and T.comp_id = W.comp_id
            left join `ea_agent` as A on (T.caller = A.domain_account or T.caller = A.msisdn) and T.comp_id = A.comp_id
            left join `ea_user` as U on (T.caller = U.domain_account or T.caller = U.msisdn) and T.comp_id = U.comp_id
            set T.req_msisdn = case 
                when W.msisdn is not null then W.msisdn 
                when A.msisdn is not null then A.msisdn 
                when U.msisdn is not null then U.msisdn 
                else '' end,
                T.req_type = case 
                when W.msisdn is not null then 'W' 
                when A.msisdn is not null then 'A' 
                when U.msisdn is not null then 'U' 
                else 'W' end;
                
            alter table `ec_transact` 
                change column `req_msisdn` `req_msisdn` varchar(30) not null,
                change column `req_type` `req_type` varchar(1) not null;    
   
            select "Added columns bundle_id, prom_id, req_msisdn and req_type to table ec_transact" as "INFO";
            
        end if; 
        

        /* Add column agentuser_id to table es_audit */
        if not exists (select null 
                     from information_schema.COLUMNS 
                     where TABLE_SCHEMA = DATABASE()
                     and COLUMN_NAME = 'agentuser_id' 
                     and TABLE_NAME = 'es_audit') then
                    
            alter table `es_audit` add column `agentuser_id` int(11) null default null;

            select "Added column agentuser_id to table es_audit" as "INFO";
            
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
