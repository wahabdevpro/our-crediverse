-- Version 86
ALTER TABLE `ea_agent`
DROP INDEX `es_agent_msisdn` ;

ALTER TABLE `ea_agent`
ADD INDEX `es_agent_msisdn` (`msisdn` ASC);

ALTER TABLE `es_webuser`
MODIFY  `service_user` BOOLEAN DEFAULT FALSE;


-- Version 87
ALTER TABLE eb_stage 
	MODIFY bd1 DECIMAL(22,5) NULL DEFAULT NULL,
	MODIFY bd2 DECIMAL(22,5) NULL DEFAULT NULL,
	MODIFY bd3 DECIMAL(22,5) NULL DEFAULT NULL,
	MODIFY bd4 DECIMAL(22,5) NULL DEFAULT NULL;


-- Version 88
ALTER TABLE eb_stage ADD COLUMN bd5 DECIMAL(22,5) NULL DEFAULT NULL;

ALTER TABLE ec_transact CHANGE bonus_pct buyer_trade_bonus_pct DECIMAL(20,8) NULL DEFAULT NULL;
ALTER TABLE ec_transact ADD COLUMN seller_trade_bonus_pct DECIMAL(20,8) NULL DEFAULT NULL;

ALTER TABLE ec_transact CHANGE bonus buyer_trade_bonus DECIMAL(20,4) NULL DEFAULT NULL;
ALTER TABLE ec_transact ADD COLUMN seller_trade_bonus DECIMAL(20,4) NULL DEFAULT NULL;

ALTER TABLE ec_transact CHANGE bonus_prov buyer_trade_bonus_prov DECIMAL(20,4) NULL DEFAULT NULL;
ALTER TABLE ec_transact ADD COLUMN seller_trade_bonus_prov DECIMAL(20,4) NULL DEFAULT NULL;

ALTER TABLE et_rule CHANGE bonus_pct buyer_trade_bonus_pct DECIMAL(20,8) NULL DEFAULT NULL;
ALTER TABLE et_rule ADD COLUMN seller_trade_bonus_pct DECIMAL(20,8) NULL DEFAULT NULL;

UPDATE et_rule SET seller_trade_bonus_pct = 0.0;


-- Version 89
ALTER TABLE ea_user ADD COLUMN channel_type VARCHAR(2) NULL DEFAULT NULL;

ALTER TABLE ec_transact ADD COLUMN channel_type VARCHAR(2) NULL DEFAULT NULL;


-- Version 90
ALTER TABLE ea_account ADD COLUMN on_hold_bonus_provision decimal(20,4) NOT NULL DEFAULT '0.0000';


-- Version 91
ALTER TABLE ep_qualify MODIFY COLUMN id BIGINT(20);

