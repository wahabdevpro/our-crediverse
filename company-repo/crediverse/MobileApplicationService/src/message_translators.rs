use tonic::Status;

use crate::crediverse::crediverse_rest::FormatMsisdnResponse;
use crate::crediverse::types::CrediverseDbError;

use crate::mas::masapi::Msisdn;
use crate::{
    crediverse::{
        crediverse_rest::{CrediverseError, CrediverseErrorType, GetAgentInfoResponse},
        history::{CrediverseTransaction, HistoryError},
        statistics::StatisticsError,
        types::CrediverseStockBalance,
    },
    mas::masapi::{self, AccountInfo, StockBalance, TransactionType},
    movivy_mobile_money::{MovIvyMobileMoneyError, MovIvyMobileMoneyErrorType},
};

impl From<CrediverseError> for Status {
    fn from(error: CrediverseError) -> Self {
        let error = match error.error_type {
            CrediverseErrorType::Internal => Status::internal(error.description),
            CrediverseErrorType::RequestError => Status::internal(error.description),
            CrediverseErrorType::HttpError(_http_status) => Status::internal(error.description),
            CrediverseErrorType::Unauthorized => Status::unauthenticated(error.description),
        };
        log::error!("{}", error);
        error
    }
}

impl From<MovIvyMobileMoneyError> for Status {
    fn from(error: MovIvyMobileMoneyError) -> Self {
        let error = match error.error_type {
            MovIvyMobileMoneyErrorType::Internal => Status::internal(error.description),
            MovIvyMobileMoneyErrorType::RequestError => Status::internal(error.description),
            MovIvyMobileMoneyErrorType::HttpError(_http_status) => Status::internal(error.description),
            MovIvyMobileMoneyErrorType::Unauthorized => Status::unauthenticated(error.description),
            MovIvyMobileMoneyErrorType::HeaderValue => Status::internal(error.description),
        };
        log::error!("{}", error);
        error
    }
}

impl From<StatisticsError> for Status {
    fn from(error: StatisticsError) -> Self {
        log::error!("{}", error);
        Status::internal(error.description)
    }
}

impl From<CrediverseDbError> for Status {
    fn from(error: CrediverseDbError) -> Self {
        log::error!("{}", error);
        Status::internal(error.description)
    }
}

impl From<HistoryError> for Status {
    fn from(error: crate::crediverse::history::HistoryError) -> Self {
        log::error!("{}", error);
        Status::internal(error.description)
    }
}

impl From<String> for masapi::TransactionType {
    fn from(transaction_type: String) -> Self {
        match transaction_type.as_str() {
            "RP" => TransactionType::Replenish,
            "TX" => TransactionType::Transfer,
            "SL" => TransactionType::Sell,
            "SB" => TransactionType::SellBundle,
            "PR" => TransactionType::RegisterPin,
            "CP" => TransactionType::ChangePin,
            "BE" => TransactionType::BalanceEnquiry,
            "ST" => TransactionType::SelfTopup,
            "TS" => TransactionType::TransactionStatusEnquiry,
            "LT" => TransactionType::LastTransactionEnquiry,
            "AJ" => TransactionType::Adjust,
            "SQ" => TransactionType::SalesQuery,
            "DQ" => TransactionType::DepositsQuery,
            "FR" => TransactionType::Reverse,
            "PA" => TransactionType::ReversePartially,
            "RW" => TransactionType::PromotionReward,
            "AD" => TransactionType::Adjudicate,
            "ND" => TransactionType::NonAirtimeDebit,
            "NR" => TransactionType::NonAirtimeRefund,
            _ => {
                log::warn!("{} is not a known transaction type", transaction_type);
                TransactionType::UnknownType
            }
        }
    }
}

impl From<&str> for masapi::TransactionType {
    fn from(transaction_type: &str) -> Self {
        match transaction_type {
            "RP" => TransactionType::Replenish,
            "TX" => TransactionType::Transfer,
            "SL" => TransactionType::Sell,
            "SB" => TransactionType::SellBundle,
            "PR" => TransactionType::RegisterPin,
            "CP" => TransactionType::ChangePin,
            "BE" => TransactionType::BalanceEnquiry,
            "ST" => TransactionType::SelfTopup,
            "TS" => TransactionType::TransactionStatusEnquiry,
            "LT" => TransactionType::LastTransactionEnquiry,
            "AJ" => TransactionType::Adjust,
            "SQ" => TransactionType::SalesQuery,
            "DQ" => TransactionType::DepositsQuery,
            "FR" => TransactionType::Reverse,
            "PA" => TransactionType::ReversePartially,
            "RW" => TransactionType::PromotionReward,
            "AD" => TransactionType::Adjudicate,
            "ND" => TransactionType::NonAirtimeDebit,
            "NR" => TransactionType::NonAirtimeRefund,
            _ => {
                log::warn!("{} is not a known transaction type", transaction_type);
                TransactionType::UnknownType
            }
        }
    }
}

impl From<CrediverseTransaction> for masapi::Transaction {
    fn from(ecds_transaction: CrediverseTransaction) -> Self {
        let transaction_type: masapi::TransactionType = ecds_transaction.transaction_type.into();

        let mut commission_amount_str = None;

        match ecds_transaction.amount.parse::<f64>() {
            Ok(amount) => {
                if let Some(gross_sales_amount_str) = &ecds_transaction.gross_sales_amount {
                    if let Ok(gross_sales_amount) = gross_sales_amount_str.parse::<f64>() {
                        let commission_amount = gross_sales_amount - amount;
                        commission_amount_str = Some(format!("{:.4}", commission_amount));
                    }
                }
            }
            Err(_) => {
                log::warn!(
                    "{} unable to convert transaction amount {} to f64",
                    ecds_transaction.transaction_number.clone(),
                    ecds_transaction.amount.clone()
                );
            }
        };

        masapi::Transaction {
            amount: ecds_transaction.amount,
            cost_of_goods_sold: ecds_transaction.cost_of_goods_sold,
            gross_sales_amount: ecds_transaction.gross_sales_amount,
            commission_amount: commission_amount_str,
            transaction_no: ecds_transaction.transaction_number,
            bonus: ecds_transaction.bonus,
            transaction_started: ecds_transaction.start_time as u64,
            transaction_ended: ecds_transaction.end_time as u64,
            source_msisdn: ecds_transaction.source_msisdn,
            recipient_msisdn: ecds_transaction.recipient_msisdn,
            balance_before: ecds_transaction.balance_before,
            bonus_balance_before: ecds_transaction.bonus_balance_before,
            on_hold_balance_before: ecds_transaction.on_hold_balance_before,
            balance_after: ecds_transaction.balance_after,
            bonus_balance_after: ecds_transaction.bonus_balance_after,
            on_hold_balance_after: ecds_transaction.on_hold_balance_after,
            status: ecds_transaction.ret_code,
            follow_up_required: ecds_transaction.follow_up,
            rolled_back: ecds_transaction.rolled_back,
            messages: [ecds_transaction.message].to_vec(),
            item_description: ecds_transaction.item_description.map(|description| description.clone()),
            transaction_type: transaction_type as i32,
        }
    }
}

impl From<Vec<CrediverseTransaction>> for masapi::Transactions {
    fn from(crediverse_transactions: Vec<CrediverseTransaction>) -> Self {
        masapi::Transactions {
            transactions: crediverse_transactions
                .iter()
                .map(|crediverse_transaction| -> masapi::Transaction {
                    //*crediverse_transaction.into()
                    <CrediverseTransaction as Into<masapi::Transaction>>::into(
                        crediverse_transaction.clone(),
                    )
                })
                .collect(),
        }
    }
}

impl From<FormatMsisdnResponse> for Msisdn {
    fn from(response: FormatMsisdnResponse) -> Self {
        Msisdn {
            msisdn: response.msisdn.as_str().unwrap_or_default().to_string(),
        }
    }
}

impl From<GetAgentInfoResponse> for AccountInfo {
    fn from(agent_info: GetAgentInfoResponse) -> Self {
        AccountInfo {
            account_number: agent_info
                .account_number
                .as_str()
                .unwrap_or_default()
                .to_string(),
            msisdn: agent_info
                .mobile_number
                .as_str()
                .unwrap_or_default()
                .to_string(),
            title: agent_info.title.as_str().unwrap_or_default().to_string(),
            first_name: agent_info
                .first_name
                .as_str()
                .unwrap_or_default()
                .to_string(),
            initials: agent_info.initials.as_str().unwrap_or_default().to_string(),
            surname: agent_info.surname.as_str().unwrap_or_default().to_string(),
            language: agent_info.language.as_str().unwrap_or_default().to_string(),
            alt_phone_number: agent_info
                .alt_phone_number
                .as_str()
                .unwrap_or_default()
                .to_string(),
            email: agent_info.email.as_str().unwrap_or_default().to_string(),
            state: agent_info.state.as_str().unwrap_or_default().to_string(),
            activation_date: agent_info.activation_date.as_u64().unwrap_or_default() as u32,
            country_code: "unknown".to_string(),
            tier: "".to_string(),
        }
    }
}

impl From<CrediverseStockBalance> for StockBalance {
    fn from(crediverse_balance: CrediverseStockBalance) -> Self {
        StockBalance {
            balance: crediverse_balance.balance.to_string(),
            bonus_balance: crediverse_balance.bonus_balance.to_string(),
            on_hold_balance: crediverse_balance.on_hold_balance,
        }
    }
}
