use std::fmt;

use mysql_async::params;
use mysql_async::prelude::{Query, WithParams};
use std::error::Error;

use crate::local_utils::date_from_epoch;

use super::hxc::Hxc;
use super::types::{CrediverseDbError, Subject};

#[derive(Debug)]
pub struct StatisticsError {
    pub description: String,
}

pub struct SalesStatistics {
    pub sales_value: String,
    pub sales_count: u64,
    pub cost_of_goods_sold: String,
}

impl fmt::Display for StatisticsError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Statistics Error: {:?}", self.description.to_string())
    }
}

impl Error for StatisticsError {}

impl From<mysql_async::Error> for StatisticsError {
    fn from(error: mysql_async::Error) -> Self {
        StatisticsError {
            description: format!("Statistics Error: {}", error),
        }
    }
}

impl From<CrediverseDbError> for StatisticsError {
    fn from(error: CrediverseDbError) -> Self {
        StatisticsError {
            description: format!("Statistics Error: {}", error),
        }
    }
}

#[derive(Debug, Clone)]
pub struct HourlySalesSummaryEntry {
    pub date: u64,
    pub hour: u32,
    pub sales_value: String,
    pub sales_count: u64,
}

pub struct TransferStats {
    pub inbound_transfers_value: String,
    pub trade_bonus_value: String,
    pub inbound_transfers_count: u64,
}

pub struct Statistics<'a> {
    hxc: &'a Hxc,
}

impl Statistics<'_> {
    pub fn new(hxc: &'_ Hxc) -> Statistics<'_> {
        Statistics { hxc }
    }

    pub async fn get_inbound_transfer_stats(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
        subject: Subject,
    ) -> Result<TransferStats, StatisticsError> {
        log::trace!("getting inbount transfer statistics from the stats db ");

        let mut conn = self.hxc.get_connection_pool().await?.get_conn().await?;

        let query = "SELECT \
                CAST(\
                  COALESCE( \
                    SUM( \
                         transfer.gross_sales_amount \
                    ),'0.0000' \
                  ) AS DECIMAL(20,4) \
                ) AS inbound_transfers_value, \
                CAST(\
                  COALESCE( \
                    SUM( \
                         COALESCE(transfer.bonus,'0.0000') \
                    ),'0.0000' \
                  ) AS DECIMAL(20,4) \
                ) AS transfer_bonus_value, \
                COALESCE(COUNT(*),0) as inbound_transfers_count \
        FROM ec_transact transfer \
        WHERE \
            transfer.comp_id = :company_id \
            AND transfer.type = 'TX' \
            AND transfer.b_agent {subject_select_clause} \
            AND transfer.started >= :start_date \
            AND transfer.started < :end_date \
            AND transfer.ret_code = 'SUCCESS'"
            .to_string();

        let query = create_query_with_params(subject, query, start_time, end_time, company_id);

        let (inbound_transfers_value, transfer_bonus_value, inbound_transfers_count) = &query
            .map(
                &mut conn,
                |(inbound_transfers_value, transfer_bonus_value, inbound_transfers_count): (
                    String,
                    String,
                    u32,
                )| {
                    (
                        inbound_transfers_value as String,
                        transfer_bonus_value as String,
                        inbound_transfers_count as u64,
                    )
                },
            )
            .await?[0];

        Ok(TransferStats {
            inbound_transfers_value: inbound_transfers_value.to_string(),
            trade_bonus_value: transfer_bonus_value.to_string(),
            inbound_transfers_count: *inbound_transfers_count,
        })
    }

    pub async fn get_global_sales_stats(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
    ) -> Result<(String, u64), StatisticsError> {
        log::trace!("getting global sales statistics from the stats db ");
        self.get_global_stats(
            company_id,
            start_time,
            end_time,
            "SELECT CAST(COALESCE(SUM(T1.gross_sales_amount - COALESCE(T2.gross_sales_amount ,'0.000')),'0.000') AS DECIMAL(20,4)), \
                COALESCE(SUM(IF(T2.reversed_id is NULL,1,0)),0) \
                FROM ec_transact T1 \
                LEFT JOIN ec_transact T2 ON (T2.reversed_id = T1.id AND T2.comp_id = T1.comp_id AND T2.type = 'NR' AND T2.ret_code = 'SUCCESS') \
                WHERE T1.comp_id = :comp_id \
                AND T1.started >= :start_date \
                AND T1.started < :end_date \
                AND T1.type IN ('SL', 'ST', 'ND') \
                AND T1.ret_code = 'SUCCESS'"
                .to_string(),
        )
        .await
    }
    async fn get_global_stats(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
        query: String,
    ) -> Result<(String, u64), StatisticsError> {
        let mut conn = self.hxc.get_connection_pool().await?.get_conn().await?;
        let start_date_string = date_from_epoch(start_time);
        let end_date_string = date_from_epoch(end_time);

        log::trace!(
            "get_global_stats for: company id {}, period {} <= T <= {}",
            company_id,
            start_date_string,
            end_date_string
        );

        let query = query.with(params! {
            "start_date" =>  start_date_string ,
            "end_date" =>  end_date_string ,
            "comp_id" =>  company_id,
        });

        let (amount, count) = &query
            .map(&mut conn, |(amount, count): (String, u32)| {
                (amount as String, count as u64)
            })
            .await?[0];

        log::trace!(
            "get_global_stats result: amount {}, count {}",
            amount,
            count
        );

        Ok((amount.to_string(), *count))
    }

    pub async fn get_global_hourly_sales_stats(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
    ) -> Result<Vec<HourlySalesSummaryEntry>, StatisticsError> {
        log::trace!("getting global hourly sales statistics from the stats db ");
        self.get_global_hourly_stats(
            company_id,
            start_time,
            end_time,
            "SELECT UNIX_TIMESTAMP(DATE(T1.started)) stats_date, HOUR(T1.started) stats_hour,  CAST(COALESCE(SUM(T1.gross_sales_amount - COALESCE(T2.gross_sales_amount ,'0.000')),'0.000') AS DECIMAL(20,4)), \
                COALESCE(SUM(IF(T2.reversed_id is NULL,1,0)),0) transaction_count \
                FROM ec_transact T1 \
                LEFT JOIN ec_transact T2 ON (T2.reversed_id = T1.id AND T2.comp_id = T1.comp_id AND T2.type = 'NR' AND T2.ret_code = 'SUCCESS') \
                WHERE T1.comp_id = :comp_id \
                AND T1.started >= :start_date \
                AND T1.started < :end_date \
                AND T1.type IN ('SL', 'ST', 'ND') \
                AND T1.ret_code = 'SUCCESS' \
                GROUP BY DATE(T1.started), HOUR(T1.started) "
                .to_string(),
        )
        .await
    }
    async fn get_global_hourly_stats(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
        query: String,
    ) -> Result<Vec<HourlySalesSummaryEntry>, StatisticsError> {
        let mut conn = self.hxc.get_connection_pool().await?.get_conn().await?;
        let start_date_string = date_from_epoch(start_time);
        let end_date_string = date_from_epoch(end_time);

        log::trace!(
            "get_global_hourly_stats for: company id {}, period {} <= T <= {}",
            company_id,
            start_date_string,
            end_date_string
        );

        let query = query.with(params! {
            "start_date" =>  start_date_string ,
            "end_date" =>  end_date_string ,
            "comp_id" =>  company_id,
        });

        let result = &query
            .map(
                &mut conn,
                |(for_date, for_hour, amount, count): (u64, u32, String, u64)| {
                    HourlySalesSummaryEntry {
                        date: for_date,
                        hour: for_hour,
                        sales_value: amount,
                        sales_count: count,
                    }
                },
            )
            .await?;

        log::trace!("get_global_hourly_stats result: {:?}", result);

        Ok(result.to_vec())
    }

    async fn get_stats(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
        subject: Subject,
        query: String,
    ) -> Result<SalesStatistics, StatisticsError> {
        let mut conn = self.hxc.get_connection_pool().await?.get_conn().await?;

        let start_date_string = date_from_epoch(start_time);
        let end_date_string = date_from_epoch(end_time);

        log::debug!(
            "Getting stats from {} to {}",
            start_date_string,
            end_date_string
        );

        let query = create_query_with_params(subject, query, start_time, end_time, company_id);

        let (amount, cost_of_goods_sold, count) = &query
            .map(
                &mut conn,
                |(amount, cost_of_goods_sold, count): (String, String, u32)| {
                    (amount as String, cost_of_goods_sold as String, count as u64)
                },
            )
            .await?[0];

        log::trace!("Amount {}, count {}", amount, count);

        Ok(SalesStatistics {
            sales_value: amount.to_string(),
            sales_count: *count,
            cost_of_goods_sold: cost_of_goods_sold.to_string(),
        })
    }

    pub async fn get_bundle_sales_stats(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
        subject: Subject,
    ) -> Result<SalesStatistics, StatisticsError> {
        log::trace!("getting bundle sale statistics from the stats db ");

        self.get_stats(
        company_id,
        start_time,
        end_time,
        subject,
        "SELECT CAST(\
                  COALESCE( \
                    SUM( \
                         bundle_sale.gross_sales_amount - COALESCE(reversal.gross_sales_amount ,'0.0000') \
                    ),'0.0000' \
                  ) AS DECIMAL(20,4) \
                ) as sales_value, \
                CAST(\
                  COALESCE( \
                    SUM( \
                         COALESCE(bundle_sale.cost_of_goods_sold,'0.0000') - COALESCE(reversal.cost_of_goods_sold,'0.0000') \
                    ),'0.0000' \
                  ) AS DECIMAL(20,4) \
                ) as cost_of_goods_sold, \
            COALESCE(SUM(IF(reversal.reversed_id is NULL,1,0)),0) as sales_count \
            FROM ec_transact bundle_sale  \
            LEFT JOIN ec_transact reversal \
                ON bundle_sale.id = reversal.reversed_id \
                AND bundle_sale.comp_id = reversal.comp_id \
                AND reversal.ret_code = 'SUCCESS' \
            WHERE \
            bundle_sale.comp_id = :company_id \
            AND bundle_sale.a_agent {subject_select_clause} \
            AND bundle_sale.type = 'ND' \
            AND bundle_sale.started >= :start_date \
            AND bundle_sale.started < :end_date \
            AND bundle_sale.ret_code = 'SUCCESS'".to_string(),
    )
    .await
    }

    async fn get_number_of_transactions_without_cost(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
        subject: Subject,
        query: String,
    ) -> Result<u64, StatisticsError> {
        let mut conn = self.hxc.get_connection_pool().await?.get_conn().await?;

        let start_date_string = date_from_epoch(start_time);
        let end_date_string = date_from_epoch(end_time);

        log::debug!(
            "Getting number of airtime transactions without cost from {} to {}",
            start_date_string,
            end_date_string
        );

        let query = create_query_with_params(subject, query, start_time, end_time, company_id);

        let count = &query.map(&mut conn, |count: u32| count as u64).await?[0];

        log::trace!(
            "Number of transaction without cost_of_goods_sold: {}",
            count
        );

        Ok(*count)
    }

    pub async fn get_number_of_bundle_transactions_without_cost(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
        subject: Subject,
    ) -> Result<u64, StatisticsError> {
        log::trace!("getting number of airtime transactions without cost_of_goods_sold");
        self.get_number_of_transactions_without_cost(
            company_id,
            start_time,
            end_time,
            subject,
            // NOTE: This SQL is not the same as the airtime one
            "SELECT 
            COALESCE(SUM(IF(reversal.reversed_id is NULL,1,0)),0) as transaction_count \
            FROM ec_transact bundle_sale  \
            LEFT JOIN ec_transact reversal \
                ON bundle_sale.id = reversal.reversed_id \
                AND bundle_sale.comp_id = reversal.comp_id \
                AND reversal.ret_code = 'SUCCESS' \
            WHERE \
            bundle_sale.comp_id = :company_id \
            AND bundle_sale.a_agent {subject_select_clause} \
            AND bundle_sale.type = 'ND' \
            AND bundle_sale.started >= :start_date \
            AND bundle_sale.started < :end_date \
            AND bundle_sale.ret_code = 'SUCCESS'\
            AND bundle_sale.cost_of_goods_sold IS NULL "
                .to_string(),
        )
        .await
    }

    pub async fn get_number_of_airtime_transactions_without_cost(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
        subject: Subject,
    ) -> Result<u64, StatisticsError> {
        log::trace!("getting number of airtime transactions without cost_of_goods_sold");
        self.get_number_of_transactions_without_cost(
            company_id,
            start_time,
            end_time,
            subject,
            // NOTE: This SQL is not the same as the bundle sales one
            "SELECT COALESCE(COUNT(*),0) \
            FROM ec_transact airtime_sale \
            WHERE \
                airtime_sale.comp_id = :company_id \
                AND airtime_sale.type IN ('ST','SL','TX') \
                AND airtime_sale.a_agent {subject_select_clause} \
                AND airtime_sale.started >= :start_date \
                AND airtime_sale.started < :end_date \
                AND airtime_sale.ret_code = 'SUCCESS' \
                AND airtime_sale.cost_of_goods_sold is NULL"
                .to_string(),
        )
        .await
    }

    pub async fn get_airtime_sales_stats(
        &self,
        company_id: u32,
        start_time: u64,
        end_time: u64,
        subject: Subject,
    ) -> Result<SalesStatistics, StatisticsError> {
        log::trace!("getting airtime sales statistics from the stats db ");
        self.get_stats(
            company_id,
            start_time,
            end_time,
            subject,
            // NOTE: This SQL is not the same as the bundle sales one
            "SELECT CAST( \
                    COALESCE( \
                        SUM( \
                        airtime_sale.gross_sales_amount \
                        ),'0.000' \
                    ) AS DECIMAL(20,4) \
                ) as sales_value, \
                CAST( \
                    COALESCE( \
                        SUM( \
                        airtime_sale.cost_of_goods_sold \
                        ),'0.000' \
                    ) AS DECIMAL(20,4)  \
                ) as cost_of_goods_sold, \
            COALESCE(COUNT(*),0) \
        FROM ec_transact airtime_sale \
        WHERE \
            airtime_sale.comp_id = :company_id \
            AND airtime_sale.type IN ('ST','SL','TX') \
            AND airtime_sale.a_agent {subject_select_clause} \
            AND airtime_sale.started >= :start_date \
            AND airtime_sale.started < :end_date \
            AND airtime_sale.ret_code = 'SUCCESS'"
                .to_string(),
        )
        .await
    }
}

fn create_query_with_params(
    subject: Subject,
    query: String,
    start_time: u64,
    end_time: u64,
    company_id: u32,
) -> mysql_async::QueryWithParams<String, mysql_async::Params> {
    let start_date_string = date_from_epoch(start_time);
    let end_date_string = date_from_epoch(end_time);

    let query = match subject {
        Subject::Agent(agent_id) => {
            let subject_parameter = " = :agent_id".to_string();
            let query = query.replace("{subject_select_clause}", subject_parameter.as_str());

            query.with(params! {
                "start_date" =>  start_date_string ,
                "end_date" =>  end_date_string ,
                "agent_id" =>  agent_id,
                "company_id" =>  company_id,
            })
        }
        Subject::Team(team_msisdns) => {
            let mut team_msisdns = team_msisdns
                .iter()
                .map(|x| format!("\"{}\",", x))
                .collect::<String>();

            team_msisdns.truncate(team_msisdns.len() - 1);

            let subject_parameter = format!("IN ({} )", team_msisdns);

            let query = query.replace("{subject_select_clause}", subject_parameter.as_str());

            log::debug!("team sales report query: {}", query);

            query.with(params! {
                "start_date" =>  start_date_string ,
                "end_date" =>  end_date_string ,
                "company_id" =>  company_id,
            })
        }
    };
    query
}
