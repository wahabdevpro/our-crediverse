DROP PROCEDURE IF EXISTS downgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE downgrade_ecds_oltp()
BEGIN
	DECLARE old_version int(11) DEFAULT 94;
	DECLARE new_version int(11) DEFAULT 95;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version int(11) DEFAULT NULL;
	/* LARGE TABLE HANDLING: BEGIN */
	DECLARE large_table_threshold bigint DEFAULT 1000000;
	DECLARE transaction_records bigint DEFAULT NULL;
	/* LARGE TABLE HANDLING: END */

	SELECT CONCAT("Attempting to downgrade OLTP schema version ", new_version, " to ", old_version) AS "INFO";

	IF NOT EXISTS (SELECT * FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = "ec_state") THEN
		SET current_version = 0;
	ELSE
		SELECT value INTO current_version FROM `ec_state` WHERE name = "DB Version";
	END IF;

	/* Verify Version */

	SELECT CONCAT("Current OLTP schema version data is ", current_version) AS "INFO";

	IF current_version != old_version THEN
		IF current_version != new_version THEN
			SET tmp_message_text = CONCAT("Expected Version ", new_version, " Database");
			SIGNAL SQLSTATE "45000" SET MESSAGE_TEXT = tmp_message_text;
		END IF;

		/* LARGE TABLE HANDLING: BEGIN */
		SELECT COUNT(*) INTO transaction_records FROM ec_transact;
		SELECT CONCAT("Found ", transaction_records, " in ec_transact") AS "INFO";
		/* LARGE TABLE HANDLING: END */

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;

		/* LARGE TABLE HANDLING: BEGIN */
		IF transaction_records < large_table_threshold THEN
			ALTER TABLE `ec_transact`
				DROP `gross_sales_amount`,
				DROP `cost_of_goods_sold`;
		ELSE
			SELECT "=====================================================================" AS "INFO";
			SELECT "!!! WARNING, PLEASE READ !!!" AS "INFO";
			SELECT "Due to the size of the ec_transact table, schema modifications " AS "INFO";
			SELECT "will not be performed as part of the standard upgrade process. " AS "INFO";
			SELECT "=====================================================================" AS "INFO";
		END IF;
		/* LARGE TABLE HANDLING: END */

		/* MEAT: END */
		SELECT CONCAT("Changing OLTP schema version data from ", new_version, " to ", old_version) AS "INFO";
		UPDATE `ec_state` SET `value` = old_version WHERE `name` = "DB Version" AND `value` = new_version;

		COMMIT;
		SELECT CONCAT("Downgraded OLTP schema version ", new_version, " to ", old_version) AS "INFO";
	ELSE
		SELECT CONCAT("Not downgrading OLTP schema version, already on ", old_version) AS "INFO";
	END IF;

END $$
DELIMITER ;

CALL downgrade_ecds_oltp();
DROP PROCEDURE IF EXISTS downgrade_ecds_oltp;


