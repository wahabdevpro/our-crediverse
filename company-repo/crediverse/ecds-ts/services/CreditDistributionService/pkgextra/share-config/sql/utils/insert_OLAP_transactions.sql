DELIMITER //

/* DROP PROCEDURE IF EXISTS insert_olap_transactions; */

CREATE PROCEDURE insert_olap_transactions(start_number INT, end_number INT)
  BEGIN
    
    DECLARE a_agent_id int(11);
    DECLARE a_agent_balance int;
    DECLARE a_agent_group_name varchar(50);
    DECLARE a_agent_msisdn varchar(30);
    DECLARE a_agent_name varchar(93);
    DECLARE a_agent_tier_name varchar(50);
    
    DECLARE max_agent_id int;
    DECLARE last_used_agent_id int;
    
    DECLARE channel varchar(1);
    DECLARE started_ended datetime;
    DECLARE success int;
    DECLARE tr_type varchar(50);
    DECLARE version int;
    DECLARE follow_up int;
    DECLARE amount decimal(20,4);
    DECLARE ended_date date;
    DECLARE ended_time time;
    DECLARE related_id bigint(20);
    
	DECLARE CONTINUE HANDLER FOR SQLSTATE '23000' SET @x = @x;
    
    SET @max_agent_id = (SELECT MAX(id) FROM ap_agent_account);
    SET @last_used_agent_id = 1;
    
    SET @x = start_number;
    
    REPEAT
		BEGIN
	-- Agent
			SELECT id, group_name, msisdn, agent_name, tier_name
				INTO @a_agent_id, @a_agent_group_name, @a_agent_msisdn, @a_agent_name, @a_agent_tier_name
				FROM ecdsap.ap_agent_account WHERE id > @last_used_agent_id LIMIT 1;
			SET @last_used_agent_id = @a_agent_id;
            
	-- Channel
            IF @x % 17 = 0 THEN
				SET @channel = 'W';
			ELSEIF @x % 5 = 0 THEN
				SET @channel = 'A';
			ELSE
				SET @channel = 'U';
			END IF;
            
	-- Date
			SET @started_ended = DATE_ADD(CURDATE(), INTERVAL -(@x % 30) DAY);
            SET @ended_date = DATE_ADD(CURDATE(), INTERVAL -(@x % 30) DAY);
            SET @ended_time = DATE_ADD(CURDATE(), INTERVAL -(CEIL(RAND() * 12)) HOUR);
            
	-- Success
			IF @x % 31 = 0 THEN
				SET @success = 0;
			ELSE
				SET @success = 1;
			END IF;
			
	-- Type and related_id
			IF @x % 37 = 0 THEN
				SET @tr_type = 'NR';	-- Non-airtime refund
				SELECT id INTO @related_id FROM `ecdsap`.`ap_transact` WHERE id < @x AND type = 'ND' ORDER BY id DESC LIMIT 1;
			ELSEIF @x % 31 = 0 THEN
				SET @tr_type = 'FR';	-- Reverse
				SELECT id INTO @related_id FROM `ecdsap`.`ap_transact` WHERE id < @x AND type = 'SL' ORDER BY id DESC LIMIT 1;
			ELSEIF @x % 7 = 0 THEN
				SET @tr_type = 'ND';	-- Non-airtime debit
                SET @related_id = NULL;
			ELSE
				SET @tr_type = 'SL';	-- Sell
                SET @related_id = NULL;
			END IF;
    
    -- Version
			SET @version = @x % 4;
    
    -- Follow up
			IF @x % 41 = 0 THEN
				SET @follow_up = 1;
			ELSE
				SET @follow_up = 0;
			END IF;
            
	-- Amount
			SET @amount = CEIL(RAND() * 1000);
             
            INSERT INTO `ecdsap`.`ap_transact`
                (`id`, `channel`, `comp_id`, `no`, `mode`, `started`, `success`, `type`, `version`, `follow_up`,	-- <- Not null
	            `amount`, `ended_date`, `ended_time`, `related_id`, `a_group_name`, `a_agent_id`, `a_msisdn`, `a_agent_name`, `a_tier_name`)
			VALUES
                (@x, @channel, 2, @x, 'N', @started_ended, @success, @tr_type, @version, @follow_up,	-- <- Not null
	            @amount, @ended_date, @ended_time, @related_id, @a_agent_group_name, @a_agent_id, @a_agent_msisdn, @a_agent_name, @a_agent_tier_name);

			IF @x % 1000 = 0 THEN
				select concat(@x) as ''; 
			END IF;
		SET @x = @x + 1;
        END;
	UNTIL @x > end_number END REPEAT;
  END
//

SET @@local.net_read_timeout=36000;
CALL insert_olap_transactions(1, 800000)//