DROP PROCEDURE IF EXISTS upgrade_ecds_oltp;

DELIMITER $$
CREATE PROCEDURE upgrade_ecds_oltp()
BEGIN
	DECLARE tag varchar(256) DEFAULT 'DB Version';
	DECLARE old_version int(11) DEFAULT 34;
	DECLARE new_version int(11) DEFAULT 35;
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

			/* Create Foreign Key FK_Agent_Area */
      if not exists (select null from information_schema.TABLE_CONSTRAINTS where
                   CONSTRAINT_SCHEMA = DATABASE() AND
                   CONSTRAINT_NAME   = 'FK_Agent_Area' AND
                   CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
            alter table `ea_agent` add constraint `FK_Agent_Area` foreign key (`area_id`) references `el_area` (`id`);
            select "Created Foreign Key FK_Agent_Area" as "INFO";
      end if;      
      
      
			/* Create Foreign Key FK_Promo_Area */
      if not exists (select null from information_schema.TABLE_CONSTRAINTS where
                   CONSTRAINT_SCHEMA = DATABASE() AND
                   CONSTRAINT_NAME   = 'FK_Promo_Area' AND
                   CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
            alter table `ep_promo` add constraint `FK_Promo_Area` foreign key (`area_id`) references `el_area` (`id`);
            select "Created Foreign Key FK_Promo_Area" as "INFO";
      end if;  
      
      
			/* Create Foreign Key FK_Rule_Area */
      if not exists (select null from information_schema.TABLE_CONSTRAINTS where
                   CONSTRAINT_SCHEMA = DATABASE() AND
                   CONSTRAINT_NAME   = 'FK_Rule_Area' AND
                   CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
            alter table `et_rule` add constraint `FK_Rule_Area` foreign key (`area_id`) references `el_area` (`id`);
            select "Created Foreign Key FK_Rule_Area" as "INFO";
      end if;  
      
      
			/* Create Foreign Key FK_Promo_Bundle */
      if not exists (select null from information_schema.TABLE_CONSTRAINTS where
                   CONSTRAINT_SCHEMA = DATABASE() AND
                   CONSTRAINT_NAME   = 'FK_Promo_Bundle' AND
                   CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
            alter table `ep_promo` add constraint `FK_Promo_Bundle` foreign key (`bundle_id`) references `ec_bundle` (`id`);
            select "Created Foreign Key FK_Promo_Bundle" as "INFO";
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
