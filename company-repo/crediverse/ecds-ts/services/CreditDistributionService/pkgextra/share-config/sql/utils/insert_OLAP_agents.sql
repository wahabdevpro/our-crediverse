DELIMITER //

/* DROP PROCEDURE IF EXISTS insert_olap_agents; */

CREATE PROCEDURE insert_olap_agents(start_number INT, end_number INT)
  BEGIN

	DECLARE agent_id int(11);
	DECLARE balance decimal(20,4);
	DECLARE bonus_balance decimal(20,4);
	DECLARE comp_id int(11);
	DECLARE group_name varchar(50);
	DECLARE hold_balance decimal(20,4);
	DECLARE msisdn varchar(30);
	DECLARE agent_name varchar(93);
	DECLARE owner_id int(11);
	DECLARE state varchar(255);
	DECLARE tier_name varchar(50);
    
    DECLARE max_group_id int;
    DECLARE last_used_group_id int;
    
	DECLARE CONTINUE HANDLER FOR SQLSTATE '23000' SET @x = @x;
    
    SET @last_used_group_id = 1;
    
    SET @x = start_number;
    SET @max_agent_id = (SELECT MAX(id) FROM ecdsap.ap_agent_account);
    SET @max_group_id = (SELECT MAX(id) FROM hxc.et_group);
    REPEAT
		BEGIN
        
        SET @agent_id = @x;
        SET @balance = CEIL(RAND() * 5000);
        SET @bonus_balance = CEIL(RAND() * 100);
        SET @comp_id = 2;
        
        SELECT id, `name` INTO @last_used_group_id, @group_name FROM hxc.et_group WHERE company_id = @comp_id AND id > @last_used_group_id ORDER BY id LIMIT 1;
        IF @last_used_group_id = @max_group_id THEN
			SET @last_used_group_id = 1; 
		END IF;
        
        SET @hold_balance = 0;
        SET @msisdn = 1000000000 + @x;
        SET @agent_name = concat('agent_', @x);
        SET @owner_id = NULL;
        SET @state = 'A';
        SET @tier_name = 'eCabine';
        
        INSERT INTO `ecdsap`.`ap_agent_account`
			(`id`, `balance`,`bonus_balance`, `comp_id`, `group_name`, `hold_balance`, `msisdn`, `agent_name`, `owner_id`, `state`, `tier_name`)
		VALUES
			(@agent_id, @balance, @bonus_balance, @comp_id, @group_name, @hold_balance, @msisdn, @agent_name, @owner_id, @state, @tier_name);
            
			IF @x % 1000 = 0 THEN
				select concat(@x) as ''; 
			END IF;
            
		SET @x = @x + 1;
        END;
	UNTIL @x > end_number END REPEAT;
  END
//

SET @@local.net_read_timeout=36000;
CALL insert_olap_agents(1, 60000)//