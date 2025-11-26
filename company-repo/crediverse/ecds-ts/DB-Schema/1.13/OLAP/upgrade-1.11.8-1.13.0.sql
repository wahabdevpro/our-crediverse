-- Version 16
ALTER TABLE `ap_transact` CHANGE bonus_pct buyer_trade_bonus_pct DECIMAL (20,8) ; 
ALTER TABLE `ap_transact` CHANGE bonus buyer_trade_bonus DECIMAL (20,4) ; 
ALTER TABLE `ap_transact` CHANGE bonus_prov buyer_trade_bonus_prov DECIMAL (20,4) ; 

ALTER TABLE `ap_transact` ADD COLUMN seller_trade_bonus_pct DECIMAL (20,8) ;
ALTER TABLE `ap_transact` ADD COLUMN seller_trade_bonus DECIMAL (20,4) ;
ALTER TABLE `ap_transact` ADD COLUMN seller_trade_bonus_prov DECIMAL (20,4) ;

-- Version 17
ALTER TABLE `ap_transact` ADD COLUMN channel_type VARCHAR(2) ;

