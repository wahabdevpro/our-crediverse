DROP PROCEDURE IF EXISTS upgrade_ecds_olap;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_olap()
BEGIN
	DECLARE old_version int(11) DEFAULT 15;
	DECLARE new_version int(11) DEFAULT 16;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version_string varchar(128) DEFAULT NULL;
	DECLARE problem_records int(11) DEFAULT 0;
	/* LARGE TABLE HANDLING: BEGIN */
	DECLARE large_table_threshold bigint DEFAULT 1000000;
	DECLARE transaction_records bigint DEFAULT NULL;
	/* LARGE TABLE HANDLING: END */

	SELECT CONCAT("Attempting to upgrade OLAP schema version ", old_version, " to ", new_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ap_schema_data") THEN
		SET current_version_string = 0;
	ELSE
		SELECT value INTO current_version_string FROM `ap_schema_data` WHERE `vkey` = "VERSION";
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLAP schema version data is ", current_version_string) AS "INFO";

	IF current_version_string != new_version THEN
		IF current_version_string != old_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", old_version, " Database");
			SIGNAL SQLSTATE "45000" SET MESSAGE_TEXT = tmp_message_text;
		END IF;
		
		/* LARGE TABLE HANDLING: BEGIN */
		SELECT COUNT(*) INTO transaction_records FROM ap_transact;
		SELECT CONCAT("Found ", transaction_records, " in ap_transact") AS "INFO";
		/* LARGE TABLE HANDLING: END */

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;
		
		/* LARGE TABLE HANDLING: BEGIN */
		IF transaction_records < large_table_threshold THEN
			ALTER TABLE `ap_transact` CHANGE bonus_pct buyer_trade_bonus_pct DECIMAL (20,8) ; 
			ALTER TABLE `ap_transact` CHANGE bonus buyer_trade_bonus DECIMAL (20,4) ; 
			ALTER TABLE `ap_transact` CHANGE bonus_prov buyer_trade_bonus_prov DECIMAL (20,4) ; 

			ALTER TABLE `ap_transact` ADD COLUMN seller_trade_bonus_pct DECIMAL (20,8) ;
			ALTER TABLE `ap_transact` ADD COLUMN seller_trade_bonus DECIMAL (20,4) ;
			ALTER TABLE `ap_transact` ADD COLUMN seller_trade_bonus_prov DECIMAL (20,4) ;
		ELSE
			SELECT "=====================================================================" AS "INFO";
			SELECT "!!! WARNING, PLEASE READ !!!" AS "INFO";
			SELECT "Due to the size of the ap_transact table, schema modifications " AS "INFO";
			SELECT "will not be performed as part of the standard upgrade process. " AS "INFO";
			SELECT "=====================================================================" AS "INFO";
		END IF;
		/* LARGE TABLE HANDLING: END */

		/* MEAT: END */
		SELECT CONCAT("Changing OLAP schema version data from ", old_version, " to ", new_version) AS "INFO";
		UPDATE `ap_schema_data` SET `value` = new_version WHERE `vkey` = "VERSION" AND `value` = old_version;


		COMMIT;
		SELECT CONCAT("Upgraded OLAP schema version ", old_version, " to ", new_version) AS "INFO";
	ELSE
		SELECT CONCAT("Not upgrading OLAP schema version, already on ", new_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL upgrade_ecds_olap();
DROP PROCEDURE IF EXISTS upgrade_ecds_olap;

