CREATE DATABASE IF NOT EXISTS team_service_db;
USE team_service_db;

CREATE TABLE IF NOT EXISTS Agent (
    id INT AUTO_INCREMENT,
    agent_id  BIGINT UNSIGNED NOT NULL,
    team_lead_agent_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (agent_id, team_lead_agent_id)
);

CREATE INDEX if NOT EXISTS idx_team_lead_agent_id ON Agent(team_lead_agent_id);

-- 2023-06-16 team member sales target

ALTER TABLE `Agent`
	ADD `daily_sales_target_amount` BIGINT NULL DEFAULT NULL AFTER `team_lead_agent_id`,
	ADD `weekly_sales_target_amount` BIGINT NULL DEFAULT NULL AFTER `daily_sales_target_amount`,
	ADD `monthly_sales_target_amount` BIGINT NULL DEFAULT NULL AFTER `weekly_sales_target_amount`;

--
