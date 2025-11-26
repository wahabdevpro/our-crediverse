DROP PROCEDURE IF EXISTS upgrade_ecds_olap;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_olap()
BEGIN
	DECLARE old_version int(11) DEFAULT 3;
	DECLARE new_version int(11) DEFAULT 4;
	DECLARE tmp_message_text varchar(256) DEFAULT NULL;
	DECLARE current_version_string varchar(128) DEFAULT NULL;

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

		SELECT "Starting transaction ..." AS "INFO";
		START TRANSACTION;
			/* MEAT: START */

			IF NOT EXISTS (SELECT null FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ap_transact' AND COLUMN_NAME = '') then
				ALTER TABLE `ap_transact`
					ADD COLUMN `a_tier_type` varchar(1) DEFAULT NULL AFTER `a_tier_name`,
					ADD COLUMN `b_tier_type` varchar(1) DEFAULT NULL AFTER `b_tier_name`,

					ADD COLUMN `bundle_name` varchar(30) DEFAULT NULL AFTER `bonus_prov`,
					ADD COLUMN `promotion_name` varchar(50) DEFAULT NULL AFTER `no`,

					ADD COLUMN `a_agent_id` int(11) DEFAULT NULL AFTER `a_agent_acc_no`,
					ADD COLUMN `a_agent_name` varchar(82) DEFAULT NULL AFTER `a_agent_id`,
					ADD COLUMN `a_owner_imsi` varchar(15) DEFAULT NULL AFTER `a_msisdn`,
					ADD COLUMN `a_owner_name` varchar(82) DEFAULT NULL AFTER `a_owner_msisdn`,

					ADD COLUMN `b_agent_id` int(11) DEFAULT NULL AFTER `b_agent_acc_no`,
					ADD COLUMN `b_agent_name` varchar(82) DEFAULT NULL AFTER `b_agent_id`,
					ADD COLUMN `b_owner_imsi` varchar(15) DEFAULT NULL AFTER `b_msisdn`,
					ADD COLUMN `b_owner_name` varchar(82) DEFAULT NULL AFTER `b_owner_msisdn`,

					ADD KEY `ap_transact_type` (`type`),
					ADD KEY `ap_transact_a_tier_type` (`a_tier_type`),

					ADD KEY `ap_transact_a_msisdn` (`a_msisdn`),
					ADD KEY `ap_transact_a_owner_msisdn` (`a_owner_msisdn`),
					ADD KEY `ap_transact_a_tier_name` (`a_tier_name`),
					ADD KEY `ap_transact_a_group_name` (`a_group_name`),
					ADD KEY `ap_transact_a_sc_name` (`a_sc_name`),

					ADD KEY `ap_transact_b_msisdn` (`b_msisdn`),
					ADD KEY `ap_transact_b_owner_msisdn` (`b_owner_msisdn`),
					ADD KEY `ap_transact_b_tier_name` (`b_tier_name`),
					ADD KEY `ap_transact_b_group_name` (`b_group_name`),
					ADD KEY `ap_transact_b_sc_name` (`b_sc_name`)
				;
			END IF;

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
