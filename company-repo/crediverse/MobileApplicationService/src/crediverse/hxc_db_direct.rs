use mysql_async::params;
use mysql_async::prelude::{Query, WithParams};

use super::hxc::Hxc;
use super::types::{CrediverseAgent, CrediverseDbError, CrediverseStockBalance, Subject};

#[derive(Debug, Clone, Copy, PartialEq)]
pub enum SaleType {
    Wholesale,
    Retail,
}
pub struct HxcDb<'a> {
    hxc: &'a Hxc,
}

impl HxcDb<'_> {
    pub fn new(hxc: &'_ Hxc) -> HxcDb<'_> {
        HxcDb { hxc }
    }

    pub async fn get_agent_id(&self, msisdn: String) -> Result<u64, CrediverseDbError> {
        let conn = self.hxc.get_connection_pool().await?.get_conn().await?;

        let query = "SELECT id \
        FROM ea_agent \
        WHERE \
        msisdn = :msisdn \
        AND state = 'A'";

        let query = query.with(params! {
            "msisdn" => msisdn.clone(),
        });
        let ids = query.map(conn, |id: u64| id).await?;

        if ids.len() != 1 {
            Err(CrediverseDbError {
                description: format!(
                    "Agent not found or multiple agents found for MSISDN = {}",
                    msisdn
                ),
            })
        } else {
            Ok(ids[0])
        }
    }

    pub async fn get_agent(
        &self,
        agent_id: u64,
        company_id: u32,
    ) -> Result<CrediverseAgent, CrediverseDbError> {
        let query = "SELECT ea_agent.first_name,ea_agent.surname, ea_agent.msisdn ,ea_account.balance, ea_account.bonus, ea_account.on_hold \
            FROM ea_agent \
            JOIN ea_account ON ea_agent.id = ea_account.agent_id \
            WHERE agent_id = :agent_id AND comp_id= :company_id AND state = 'A';"
            .with(params! {
                "agent_id" => agent_id,
                "company_id" => company_id,
            });

        let mut conn = self.hxc.get_connection_pool().await?.get_conn().await?;

        let found_agents = &query
            .map(
                &mut conn,
                |(first_name, surname, msisdn, balance, bonus_balance, on_hold_balance): (
                    String,
                    String,
                    String,
                    String,
                    String,
                    String,
                )| {
                    CrediverseAgent {
                        first_name,
                        surname,
                        msisdn,
                        stock_balance: CrediverseStockBalance {
                            balance,
                            bonus_balance,
                            on_hold_balance,
                        },
                    }
                },
            )
            .await?;

        let agent = match found_agents.clone().len() {
            0 => {
                return Err(CrediverseDbError {
                    description: format!("No Agent for agent_id {}", agent_id),
                })
            }
            1 => found_agents[0].clone(),
            _ => {
                return Err(CrediverseDbError {
                    description: format!(
                        "Multiple Crediverse Agents found for agent_id {}",
                        agent_id
                    ),
                })
            }
        };

        Ok(agent)
    }

    pub async fn get_balance(
        &self,
        subject: Subject,
        company_id: u32,
    ) -> Result<CrediverseStockBalance, CrediverseDbError> {
        let query = "SELECT CAST(SUM(account.balance) AS DECIMAL(20,4)),CAST(SUM(account.bonus) AS DECIMAL(20,4)), CAST(SUM(account.on_hold) AS DECIMAL(20,4)) \
        FROM ea_agent agent \
        JOIN ea_account account ON account.agent_id = agent.id \
        WHERE comp_id = :company_id AND agent.state = 'A' AND "
        .to_string();

        let query = match subject.clone() {
            Subject::Team(team_agent_ids) => {
                let team_agent_ids_string: String = team_agent_ids
                    .iter()
                    .map(|num| num.to_string())
                    .collect::<Vec<String>>()
                    .join(",");

                format!("{} agent.id in ({}) ; ", query, team_agent_ids_string)
                    .with(params! {"company_id"=>company_id})
            }
            Subject::Agent(agent_id) => format!("{} agent.id = :agent_id", query).with(params! {
                "company_id"=>company_id,
                "agent_id"=> agent_id
            }),
        };

        let mut conn = self.hxc.get_connection_pool().await?.get_conn().await?;
        let found_stock_balance = query
            .map(
                &mut conn,
                |(balance, bonus_balance, on_hold_balance): (String, String, String)| {
                    CrediverseStockBalance {
                        balance,
                        bonus_balance,
                        on_hold_balance,
                    }
                },
            )
            .await?;

        let stock_balance = match found_stock_balance.len() {
            0 => {
                return Err(CrediverseDbError {
                    description: format!("No Agent for agent_id {}", subject),
                })
            }
            1 => found_stock_balance[0].clone(),
            _ => {
                return Err(CrediverseDbError {
                    description: format!(
                        "Multiple Crediverse Agents found for agent_ids {}",
                        subject
                    ),
                })
            }
        };

        Ok(stock_balance)
    }

    pub async fn get_sale_type(
        &self,
        seller_msisdn: String,
        buyer_msisdn: String,
    ) -> Result<SaleType, CrediverseDbError> {
        if seller_msisdn == buyer_msisdn {
            log::debug!("seller is buyer!");
            Ok(SaleType::Retail)
        } else {
            let mut conn = self.hxc.get_connection_pool().await?.get_conn().await?;
            let query = "SELECT state FROM ea_agent WHERE msisdn = :msisdn";

            let query = query.with(params! {
                "msisdn" => buyer_msisdn.clone(),
            });

            log::debug!("Query: {:?}", query);

            // Check if there are agents with the same MSISDN
            let agents_with_buyer_msisdn = query.map(&mut conn, |state: String| state).await?;

            let mut sale_type = SaleType::Retail;

            /*  Check for all agents with the same MSISDN.
            There may be multiple deactivated agents and at most one
            active agent using the same MSISDN */
            for agent_state in agents_with_buyer_msisdn {
                // For agents that haven't been deactivated, perform a transfer
                if agent_state != "D" {
                    sale_type = SaleType::Wholesale;
                    // Buyer is an agent. Perform an airtime transfer
                    log::debug!("{} is an agent, selling wholesale", buyer_msisdn);
                    break;
                }
            }

            if sale_type == SaleType::Retail {
                // Buyer is not an Agent. Perform an airtime sale
                log::debug!("{} is not an agent, selling retail", buyer_msisdn);
            }

            Ok(sale_type)
        }
    }
}
