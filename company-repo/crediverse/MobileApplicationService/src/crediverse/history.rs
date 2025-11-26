use std::fmt;

use mysql_async::params;
use mysql_async::prelude::{FromRow, Query, WithParams};
use mysql_async::Value;

use serde::Deserialize;
use std::error::Error;
use std::str::from_utf8;

use super::hxc::Hxc;
use super::types::CrediverseDbError;

#[derive(Debug)]
pub struct HistoryError {
    pub description: String,
}

impl fmt::Display for HistoryError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "History Error: {:?}", self.description.to_string())
    }
}

impl Error for HistoryError {}

impl From<mysql_async::FromRowError> for HistoryError {
    fn from(error: mysql_async::FromRowError) -> Self {
        HistoryError {
            description: format!("History Error: {}", error),
        }
    }
}

#[derive(Default, Debug, Deserialize, Clone)]
pub struct CrediverseTransaction {
    pub transaction_number: String,
    pub amount: String,
    pub cost_of_goods_sold: Option<String>,
    pub gross_sales_amount: Option<String>,
    pub bonus: String,
    pub transaction_type: String,
    pub start_time: u32,
    pub end_time: u32,
    pub source_msisdn: String,
    pub recipient_msisdn: String,
    pub balance_before: String,
    pub balance_after: String,
    pub bonus_balance_before: String,
    pub bonus_balance_after: String,
    pub on_hold_balance_before: String,
    pub on_hold_balance_after: String,
    pub follow_up: bool,
    pub rolled_back: bool,
    pub ret_code: String,
    pub message: String,
    pub item_description: Option<String>,
}

impl FromRow for CrediverseTransaction {
    fn from_row_opt(mut row: mysql_async::Row) -> Result<Self, mysql_async::FromRowError>
    where
        Self: Sized,
    {
        log::debug!("Transalating transaction from row: {:?}", row);

        let rolled_back: bool = match row.take("rolled_back") as Option<Vec<u8>> {
            None => false,
            Some(bytes) => bytes[0] != 0,
        };

        let follow_up: bool = match row.take("follow_up") as Option<Vec<u8>> {
            None => false,
            Some(bytes) => bytes[0] != 0,
        };

        let item_desc: Option<String> = match row.take("item_description") {
            Some(Value::Bytes(bytes)) => match from_utf8(&bytes) {
                Ok(s) => Some(s.to_string()),
                Err(err) => {
                    log::error!(
                        "UTF8 error: {}, string bytes: {:?}",
                        err,
                        bytes
                            .iter()
                            .map(|b| format!("{:02X}", b))
                            .collect::<Vec<String>>()
                            .join(" ")
                    );

                    Some(String::from_utf8_lossy(&bytes).into_owned())
                }
            },
            _ => None,
        };

        Ok(CrediverseTransaction {
            transaction_number: row.take("no").unwrap_or_default(),
            amount: row.take("amount").unwrap_or_default(),
            cost_of_goods_sold: row.take("cost_of_goods_sold").unwrap_or_default(),
            gross_sales_amount: row.take("gross_sales_amount").unwrap_or_default(),
            bonus: row.take("bonus").unwrap_or_default(),
            transaction_type: row.take("type").unwrap_or_default(),
            start_time: row.take("started").unwrap_or_default(),
            end_time: row.take("ended").unwrap_or_default(),
            source_msisdn: row.take("a_msisdn").unwrap_or_default(),
            recipient_msisdn: row.take("b_msisdn").unwrap_or_default(),
            balance_before: row.take("before").unwrap_or_default(),
            balance_after: row.take("after").unwrap_or_default(),
            bonus_balance_before: row.take("bonus_before").unwrap_or_default(),
            bonus_balance_after: row.take("bonus_after").unwrap_or_default(),
            on_hold_balance_before: row.take("hold_before").unwrap_or_default(),
            on_hold_balance_after: row.take("hold_after").unwrap_or_default(),
            follow_up,
            rolled_back,
            ret_code: row.take("ret_code").unwrap_or_default(),
            item_description: item_desc,
            message: row.take("additional").unwrap_or_default(),
        })
    }
}

/*
SELECT
no, amount, bonus, type, started, ended, a_msisdn, b_msisdn, a_after, a_bonus_before, a_hold_before, a_after, a_bonus_after, a_hold_after, follow_up, rolled_back, ret_code, additional
FROM ec_transact
WHERE
-- select all transactions where requester is a or b party
(a_msisdn = '111111111' OR b_msisdn = '111111111')
AND (
    -- always show self topup and reversals
    type IN ( 'ST','FR','PA')
    -- show Sales when requester is a party
    OR (type = 'SL' AND a_msisdn = '111111111')
    -- show adjudications when they are successful
    OR (type IN ('AD','AJ') AND ret_code = 'SUCCESS')
    -- show replenish when the requester is the receiver and it successful.
    OR (type = 'RP' AND b_msisdn = '111111111' AND ret_code = 'SUCCESS')
    -- show transfer, non airtime, sell bundle, and non airtime refund  when the requester is the sender or when the requester is the receiver and it successful.
    OR (type IN ('TX','ND','RW','NR','SB') AND ((a_msisdn = '111111111') OR (b_msisdn = '111111111' AND ret_code = 'SUCCESS')))
);

*/

impl From<mysql_async::Error> for HistoryError {
    fn from(error: mysql_async::Error) -> Self {
        HistoryError {
            description: format!("History Error: {}", error),
        }
    }
}

impl From<CrediverseDbError> for HistoryError {
    fn from(error: CrediverseDbError) -> Self {
        HistoryError {
            description: format!("History Error: {}", error),
        }
    }
}

pub struct History<'a> {
    hxc: &'a Hxc,
}

impl History<'_> {
    pub fn new(hxc: &'_ Hxc) -> History<'_> {
        History { hxc }
    }

    async fn get_agent_id(&self, msisdn: String) -> Result<u64, HistoryError> {
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
            Err(HistoryError {
                description: format!(
                    "Agent not found or multiple agents found for MSISDN = {}",
                    msisdn
                ),
            })
        } else {
            Ok(ids[0])
        }
    }

    pub async fn get_transaction_page(
        &self,
        company_id: u32,
        requester_msisdn: String,
        start_page: usize,
        transactions_per_page: usize,
    ) -> Result<Vec<CrediverseTransaction>, HistoryError> {
        let transaction_offset = start_page * transactions_per_page;
        log::trace!(
            "Getting transaction page {} with {} transactions for {}",
            start_page,
            transactions_per_page,
            requester_msisdn
        );

        let agent_id = self.get_agent_id(requester_msisdn.clone()).await?;

        let conn = self.hxc.get_connection_pool().await?.get_conn().await?;

        let query = "SELECT no,\
                COALESCE (transaction.amount,'0.0000') as amount,\
                transaction.cost_of_goods_sold, \
                transaction.gross_sales_amount, \
                COALESCE (transaction.bonus,'0.0000') as bonus, \
                type,\
                UNIX_TIMESTAMP(transaction.started) as started,\
                UNIX_TIMESTAMP(transaction.ended) as ended,\
                transaction.a_msisdn,\
                COALESCE(transaction.b_msisdn,'') as b_msisdn,\
                CASE WHEN transaction.a_agent = :requester_id  THEN COALESCE (transaction.a_before ,'0.0000') ELSE COALESCE(transaction.b_before,'0.0000') END AS 'before',\
                CASE WHEN transaction.a_agent = :requester_id  THEN COALESCE(transaction.a_after,'0.000') ELSE COALESCE(transaction.b_after,'0.0000') END AS 'after',\
                CASE WHEN transaction.a_agent = :requester_id  THEN COALESCE(transaction.a_bonus_before,'0.0000') ELSE COALESCE(transaction.b_bonus_before,'0.0000') END AS 'bonus_before' ,\
                CASE WHEN transaction.a_agent = :requester_id  THEN COALESCE(transaction.a_bonus_after,'0.0000') ELSE COALESCE(transaction.b_bonus_after,'0.0000') END AS 'bonus_after', \
                CASE WHEN transaction.a_agent = :requester_id  THEN COALESCE(transaction.a_hold_before,'0.0000') ELSE '0.0000' END AS 'transaction.on_hold_before' ,\
                CASE WHEN transaction.a_agent = :requester_id  THEN COALESCE(transaction.a_hold_after,'0.0000') ELSE '0.0000' END AS 'on_hold_after',\
                transaction.follow_up, \
                transaction.rolled_back, \
                transaction.ret_code, \
                COALESCE(transaction.additional,'') AS additional, \
                bundle_details.item_description as item_description \
                FROM ec_transact transaction \
                LEFT OUTER JOIN non_airtime_transaction_details bundle_details ON bundle_details.id = transaction.id \
                WHERE \
                comp_id = :company_id \
                AND (a_agent = :requester_id OR b_agent = :requester_id) \
                AND ( \
                type IN ( 'ST','FR','PA') \
                OR (type = 'SL' AND a_agent = :requester_id) \
                OR (type IN ('AD','AJ') AND ret_code = 'SUCCESS') \
                OR (type = 'RP' AND b_agent = :requester_id AND ret_code = 'SUCCESS') \
                OR (type IN ('TX','ND','RW','NR','SB') AND ((a_agent = :requester_id) OR (b_agent = :requester_id AND ret_code = 'SUCCESS')))) \
                ORDER BY transaction.id DESC \
                LIMIT :transaction_offset, :transactions_per_page".to_string();

        let query = query.with(params! {
            "company_id" => company_id,
            "requester_id" => agent_id,
            "transactions_per_page" => transactions_per_page,
            "transaction_offset" => transaction_offset,
        });

        log::debug!("Query: {:?}", query);

        let transactions = query.map(conn, CrediverseTransaction::from).await?;

        log::debug!("Transactions: {:?}", transactions);

        Ok(transactions)
    }
}
/*
pub async fn get_stats(
    start_time: u64,
    end_time: u64,
    msisdn: String,
    query: String,
) -> Result<(String, u32), HistoryError> {
    match crate::STATS_DB_CONNECTION_POOL.get_conn().await {
        Ok(mut conn) => {
            let start_date_string = date_from_epoch(start_time);
            let end_date_string = date_from_epoch(end_time);

            let query = query.with(params! {
                "start_date" =>  start_date_string ,
                "end_date" =>  end_date_string ,
                "msisdn" =>  msisdn.to_string(),
            });

            let (amount, count) = &query
                .map(&mut conn, |(amount, count): (String, u32)| {
                    (amount as String, count as u32)
                })
                .await?[0];

            log::info!("Amount {}, count {}", amount, count);

            Ok((amount.to_string(), *count))
        }
        Err(err) => {
            let error_description = format!("Connection Pool Connection Error: {}", err);
            error!("{}", error_description);

            return Err(HistoryError {
                description: error_description,
            });
        }
    }
}

pub async fn get_transfer_stats(
    start_time: u64,
    end_time: u64,
    msisdn: String,
) -> Result<(String, u32), HistoryError> {
    log::info!("getting transfer History from the stats db ");
    get_stats(
        start_time,
        end_time,
        msisdn,
        "SELECT COALESCE(SUM(retail_charge), 0), COUNT(*) \
                FROM ec_transact \
                WHERE type = 'TX' \
                AND ended >= :start_date \
                AND ended < :end_date \
                AND a_msisdn = :msisdn  \
                AND ret_code = 'SUCCESS'"
            .to_string(),
    )
    .await
}

pub async fn get_bundle_sales_stats(
    start_time: u64,
    end_time: u64,
    msisdn: String,
) -> Result<(String, u32), HistoryError> {
    log::info!("getting bundle sale History from the stats db ");

    get_stats(
        start_time,
        end_time,
        msisdn,
        "SELECT COALESCE(SUM(retail_charge), 0), COUNT(*) \
                FROM ec_transact \
                WHERE type = 'ND' \
                AND ended >= :start_date \
                AND ended < :end_date \
                AND a_msisdn = :msisdn  \
                AND ret_code = 'SUCCESS'"
            .to_string(),
    )
    .await
}

pub async fn get_airtime_sales_stats(
    start_time: u64,
    end_time: u64,
    msisdn: String,
) -> Result<(String, u32), HistoryError> {
    log::info!("getting airtime sales History from the stats db ");
    get_stats(
        start_time,
        end_time,
        msisdn,
        "SELECT COALESCE(SUM(retail_charge), 0), COUNT(*) \
            FROM ec_transact \
            WHERE type IN ('ST','SL') \
            AND ended >= :start_date \
            AND ended < :end_date \
            AND a_msisdn = :msisdn \
            AND ret_code = 'SUCCESS'"
            .to_string(),
    )
    .await
}
*/

/*
*

message Transaction {
    no                       string transactionNo = 1;
    amount                   string amount = 2;
    bonus                    string bonus = 3; bonus
    type                     TransactionType transactionType = 4;
    started                  uint64 transactionStarted = 5;
    ended                    uint64 transactionEnded = 6;
    a_msisdn                 string sourceMsisdn = 7;
    b_msisdn                 string recipientMsisdn = 8;
    a_after                  string balanceBefore = 9;
    a_bonus_before           string bonusBalanceBefore = 10;
    a_hold_before            string onHoldBalanceBefore = 11;
    a_after                  string balanceAfter = 12;
    a_bonus_after            string bonusBalanceAfter = 13;
    a_hold_after             string onHoldBalanceAfter = 14;
    follow_up                bool followUpRequired = 16;
    rolled_back              bool rolledBack = 17;
    ret_code                 string status = 15;
    additional               repeated string messages = 18;
}



+--------------------------+---------------+------+-----+---------+----------------+
| Field                    | Type          | Null | Key | Default | Extra          |
+--------------------------+---------------+------+-----+---------+----------------+
| id                       | bigint(20)    | NO   | PRI | NULL    | auto_increment |
| a_after                  | decimal(20,4) | YES  |     | NULL    |                |
| a_before                 | decimal(20,4) | YES  |     | NULL    |                |
| a_bonus_after            | decimal(20,4) | YES  |     | NULL    |                |
| a_bonus_before           | decimal(20,4) | YES  |     | NULL    |                |
| a_cell                   | int(11)       | YES  |     | NULL    |                |
| a_cell_group_id          | int(11)       | YES  |     | NULL    |                |
| a_group                  | int(11)       | YES  |     | NULL    |                |
| a_imei                   | varchar(16)   | YES  |     | NULL    |                |
| a_imsi                   | varchar(15)   | YES  |     | NULL    |                |
| a_msisdn                 | varchar(30)   | YES  |     | NULL    |                |
| a_sc                     | int(11)       | YES  |     | NULL    |                |
| a_tier                   | int(11)       | YES  |     | NULL    |                |
| additional               | varchar(100)  | YES  |     | NULL    |                |
| amount                   | decimal(20,4) | YES  |     | NULL    |                |
| retail_charge            | decimal(20,4) | YES  |     | NULL    |                |
| cost_of_goods_sold       | decimal(20,4) | YES  |     | NULL    |                |
| b_agent                  | int(11)       | YES  | MUL | NULL    |                |
| b_after                  | decimal(20,4) | YES  |     | NULL    |                |
| b_before                 | decimal(20,4) | YES  |     | NULL    |                |
| b_bonus_after            | decimal(20,4) | YES  |     | NULL    |                |
| b_bonus_before           | decimal(20,4) | YES  |     | NULL    |                |
| b_cell                   | int(11)       | YES  |     | NULL    |                |
| b_cell_group_id          | int(11)       | YES  |     | NULL    |                |
| b_group                  | int(11)       | YES  |     | NULL    |                |
| b_imei                   | varchar(16)   | YES  |     | NULL    |                |
| b_imsi                   | varchar(15)   | YES  |     | NULL    |                |
| b_msisdn                 | varchar(30)   | YES  |     | NULL    |                |
| b_sc                     | int(11)       | YES  |     | NULL    |                |
| b_tier                   | int(11)       | YES  |     | NULL    |                |
| bonus                    | decimal(20,4) | YES  |     | NULL    |                |
| bonus_pct                | decimal(20,8) | YES  |     | NULL    |                |
| bonus_prov               | decimal(20,4) | YES  |     | NULL    |                |
| caller                   | varchar(30)   | NO   |     | NULL    |                |
| channel                  | varchar(1)    | NO   |     | NULL    |                |
| charge                   | decimal(20,4) | YES  |     | NULL    |                |
| comp_id                  | int(11)       | NO   | MUL | NULL    |                |
| ended                    | datetime      | NO   |     | NULL    |                |
| follow_up                | bit(1)        | NO   |     | NULL    |                |
| host                     | varchar(16)   | NO   |     | NULL    |                |
| in_session               | varchar(32)   | YES  |     | NULL    |                |
| in_transact              | varchar(32)   | YES  |     | NULL    |                |
| ext_code                 | varchar(15)   | YES  |     | NULL    |                |
| lm_time                  | datetime      | NO   |     | NULL    |                |
| lm_userid                | int(11)       | NO   |     | NULL    |                |
| no                       | varchar(11)   | NO   |     | NULL    |                |
| mode                     | varchar(1)    | NO   |     | NULL    |                |
| ret_code                 | varchar(20)   | NO   |     | NULL    |                |
| reversed_id              | bigint(20)    | YES  | MUL | NULL    |                |
| rolled_back              | bit(1)        | NO   |     | NULL    |                |
| started                  | datetime      | NO   |     | NULL    |                |
| rule_id                  | int(11)       | YES  |     | NULL    |                |
| type                     | varchar(2)    | NO   |     | NULL    |                |
| version                  | int(11)       | NO   |     | NULL    |                |
| a_area                   | int(11)       | YES  |     | NULL    |                |
| b_area                   | int(11)       | YES  |     | NULL    |                |
| a_owner                  | int(11)       | YES  | MUL | NULL    |                |
| b_owner                  | int(11)       | YES  | MUL | NULL    |                |
| bundle_id                | int(11)       | YES  |     | NULL    |                |
| prom_id                  | int(11)       | YES  |     | NULL    |                |
| req_msisdn               | varchar(30)   | NO   |     | NULL    |                |
| req_type                 | varchar(1)    | NO   |     | NULL    |                |
| a_hold_before            | decimal(20,4) | YES  |     | NULL    |                |
| a_hold_after             | decimal(20,4) | YES  |     | NULL    |                |
| version_81               | bit(1)        | YES  | MUL | NULL    |                |
| b_transfer_bonus_amount  | decimal(20,4) | YES  |     | NULL    |                |
| b_transfer_bonus_profile | varchar(10)   | YES  |     | NULL    |                |
| channel_type             | varchar(2)    | YES  |     | NULL    |                |
+--------------------------+---------------+------+-----+---------+----------------+

*
*
*/
