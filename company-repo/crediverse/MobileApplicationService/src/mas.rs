use crate::crediverse::crediverse_rest::{
    AuthStatus, Crediverse, CrediverseError, GetKeyStatus, SubmitSecretStatus,
};
use crate::crediverse::history::{CrediverseTransaction, History};
use crate::crediverse::hxc_db_direct::{HxcDb, SaleType};
use crate::crediverse::statistics::Statistics;
use crate::mobile_money::movivy_mobile_money::MovIvyMobileMoneyErrorType;
use crate::mobile_money::movivy_mobile_money::{
    MMAuthStatus, MMTransactionStatus, MovIvyMobileMoney,
};
use crate::wallet_owner::WalletOwner;
use crate::{crediverse::types::Subject, mas::mas_server::Mas};

use tonic::transport::{Certificate, Channel, ClientTlsConfig, Identity};
use tonic::{Code, Request, Response, Status};

use log::{error, info, trace, warn};

use jsonwebtoken::{encode, EncodingKey, Header};

use serde::{Deserialize, Serialize};

use std::env;

use std::sync::{Arc, Mutex};
use tonic::transport::Uri;
use tonic::Extensions;

use masapi::*;

use std::time::UNIX_EPOCH;

use chrono::prelude::*;
use csv::WriterBuilder;
use std::collections::HashMap;
use std::fs;
use std::fs::OpenOptions;
use std::io::Write;
use std::path::Path;
use std::io;
use std::time::SystemTime;

fn directory_exists(dir: &str) -> bool {
    if let Ok(metadata) = fs::metadata(dir) {
        metadata.is_dir()
    } else {
        false
    }
}

impl AnalyticsEventType {
    fn from_int_to_string(r#type: i32) -> &'static str {
        match r#type {
            0 => "CRASH",
            1 => "EXCEPTION",
            2 => "ACTION",
            _ => panic!("Invalid AnalyticsEventType value"),
        }
    }
}

fn convert_to_cents(s: &str) -> Result<u64, Status> {
    let parts: Vec<&str> = s.split('.').collect();
    let units: u64 = match parts[0].parse::<u64>() {
        Ok(units) => units,
        Err(e) => {
            return Err(Status::invalid_argument(format!(
                "Could not parse the mobile money amount: {}",
                e
            )))
        }
    } * 100; // the vault holds cents

    let cents: u64 =
        if parts.len() > 1 {
            let c = match parts[1].parse::<u64>() {
                Ok(cents) => cents,
                Err(e) => {
                    return Err(Status::invalid_argument(format!(
                        "Could not parse the Mobile Money amount: {}",
                        e
                    )))
                }
            };

            match parts[1].len() {
                1 => c * 10, // if there is only one digit, multiply by 10
                2 => c,
                3 => c / 10,
                4 => c / 100,
                _ => return Err(Status::invalid_argument(
                    "Could not parse the Mobile Money amount: More than 4 digits after the period",
                )),
            }
        } else {
            0
        };

    Ok(units + cents)
}

pub mod team_service_api {
    tonic::include_proto!("team_service_api");
}

use team_service_api::team_service_api_client::TeamServiceApiClient;

pub mod credivault_api {
    tonic::include_proto!("credivault_api");
}

use credivault_api::credi_vault_client::CrediVaultClient;
// use credivault_api::*;

pub mod masapi {
    // The string specified here must match the proto package name
    tonic::include_proto!("masapi");
}

fn get_mobile_money_agent_username() -> String {
    let default_value = "ABEL_API_USER".to_string();
    let var_name = "MOBILE_MONEY_AGENT_USERNAME".to_string();
    crate::local_utils::get_env_config(var_name, default_value)
}

fn get_mobile_money_agent_password() -> String {
    let default_value = "fl@gWSp4".to_string();
    let var_name = "MOBILE_MONEY_AGENT_PASSWORD".to_string();
    crate::local_utils::get_env_config(var_name, default_value)
}

pub enum AuthorizationStatus {
    Authorized(String, String, String),
    #[allow(dead_code)]
    TokenExpired,
}

pub struct MasImplError {
    description: String,
}

impl From<MasImplError> for Status {
    fn from(error: MasImplError) -> Self {
        let error = Status::internal(error.description);
        error!("MasImpError: {}", error);
        error
    }
}

impl From<jwt::Error> for MasImplError {
    fn from(error: jwt::Error) -> Self {
        error!("jwt::Error: {}", error);
        MasImplError {
            description: error.to_string(),
        }
    }
}

pub struct MasIpml<'a> {
    crediverse: &'a Crediverse,
    statistics: &'a Statistics<'a>,
    history: &'a History<'a>,
    hxc_db: &'a HxcDb<'a>,
    team_service_api: TeamServiceApiClient<Channel>,
    credivault_api: CrediVaultClient<Channel>,
    credivault_wallet_owner: &'a WalletOwner,
    movivy_mobile_money: &'a MovIvyMobileMoney,
    session_timeout: usize,
    feedback_path: String,
    analytics_path: String,
    app_version_config: Arc<Mutex<VersionConfig>>,
}

fn get_configured_timeout() -> usize {
    match env::var("MAS_SESSION_TIMEOUT") {
        Ok(timeout) => match timeout.parse::<usize>() {
            Ok(timeout) => {
                info!(
                    "MAS_SESSION_TIMEOUT environment variable set to: {}.",
                    timeout.to_string()
                );
                timeout
            }
            Err(err) => {
                warn!("Could not parse the MAS_SESSION_TIMEOUT environment variable: {} Defaulting to 90 seconds.",err);
                90
            }
        },
        Err(_e) => {
            warn!("The MAS_SESSION_TIMEOUT environment variable was not set, defaulting to 90 seconds");
            90
        }
    }
}

fn create_team_channel() -> Channel {
    let team_service_api_url = match env::var("TEAM_SERVICE_API_URL") {
        Ok(team_service_url) => match team_service_url.parse::<String>() {
            Ok(team_service_url) => {
                info!(
                    "TEAM_SERVICE_API_URL environment variable set to: {}.",
                    team_service_url
                );
                team_service_url
            }
            Err(err) => {
                warn!("Could not parse the TEAM_SERVICE_API_URL environment variable: {} ,Defaulting to http://0.0.0.0:5100",err);
                "http://0.0.0.0:5100".to_string()
            }
        },
        Err(_e) => {
            warn!("The TEAM_SERVICE_API_URL environment variable was not set, defaulting to http://0.0.0.0:5100");
            "http://0.0.0.0:5100".to_string()
        }
    };

    let uri = team_service_api_url
        .parse::<Uri>()
        .expect("Could not parse the TEAM_SERVICE_API_URL environment variable ");

    Channel::builder(uri).connect_lazy()
    /*
     */
}

fn create_credivault_channel(tls_config: ClientTlsConfig) -> Channel {
    let credivault_url = match env::var("CREDIVAULT_URL") {
        Ok(credivault_url) => match credivault_url.parse::<String>() {
            Ok(credivault_url) => {
                info!(
                    "CREDIVAULT_URL environment variable set to: {}.",
                    credivault_url
                );
                credivault_url
            }
            Err(err) => {
                warn!("Could not parse the CREDIVAULT_URL environment variable: {} ,Defaulting to https://credivault:5020",err);
                "https://credivault:5020".to_string()
            }
        },
        Err(_e) => {
            warn!("The CREDIVAULT_URL environment variable was not set, defaulting to https://credivault:5020");
            "https://credivault:5020".to_string()
        }
    };

    let uri = credivault_url
        .parse::<Uri>()
        .expect("Could not parse the CREDIVAULT_URL environment variable ");

    Channel::builder(uri)
        .tls_config(tls_config)
        .expect("could not create the credivault_client")
        .connect_lazy()
}

fn deprecation_date_deserialize<'de, D>(deserializer: D) -> Result<u64, D::Error>
where
    D: serde::Deserializer<'de>,
{
    let date_str = String::deserialize(deserializer)?;
    let parsed_date = NaiveDate::parse_from_str(&date_str, "%Y-%m-%d")
        .map_err(serde::de::Error::custom)?;
    Ok(parsed_date.and_hms_opt(0, 0, 0).unwrap().timestamp() as u64)
}

#[derive(Deserialize, Clone)]
pub struct AppVersion {
    pub version_code: u32,
    pub version_name: String,
    pub supported: bool,
    #[serde(deserialize_with = "deprecation_date_deserialize")]
    pub deprecation_date: u64,
    pub priority: u32,
    pub download_url: String,
}

#[derive(Deserialize)]
pub struct VersionConfig {
    pub schema_version: u32,
    pub configuration_version: u32,
    pub app_versions: Vec<AppVersion>,
    #[serde(skip)]
    pub app_versions_map: HashMap<u32, AppVersion>,
    #[serde(skip)]
    pub loaded_at: Option<SystemTime>,
    #[serde(skip)]
    pub filename: String,
}

impl VersionConfig {
    pub fn load_from_file(filename: &str) -> Result<VersionConfig, Status> {
        let file_contents = match std::fs::read_to_string(filename) {
            Ok(contents) => contents,
            Err(error) => {
                error!("VersionConfig::load_from_file: failed to read config file '{}' with error: {}", filename, error);
                return Err(Status::internal("Internal service error"));
            }
        };
        let version_config: VersionConfig = match serde_json::from_str(&file_contents) {
            Ok(config) => config,
            Err(error) => {
                error!("VersionConfig::load_from_file: JSON parsing error: {}", error);
                return Err(Status::internal("Internal service error"));
            }
        };

        let app_versions_map: HashMap<u32, AppVersion> = version_config.app_versions
            .iter()
            .cloned()
            .map(|app_version| (app_version.version_code, app_version.clone()))
            .collect();

        let loaded_at_ts = SystemTime::now();
        let loaded_at: Option<SystemTime> = Some(loaded_at_ts);

        Ok(VersionConfig {
            app_versions_map,
			loaded_at,
            filename: filename.to_string(),
            ..version_config
        })
    }

    pub fn get_version(&self, version_code: u32) -> Option<&AppVersion> {
        self.app_versions_map.get(&version_code)
    }

    pub fn get_latest_version(&self) -> Option<&AppVersion> {
        self.app_versions_map.values().max_by_key(|v| v.version_code)
    }

    pub fn get_max_priority(&self, since_version: u32) -> Option<u32> {
        let mut max_priority = None;

        for (version_code, app_version) in &self.app_versions_map {
            if *version_code > since_version {
                if let Some(existing_max) = max_priority {
                    max_priority = Some(app_version.priority.max(existing_max));
                } else {
                    max_priority = Some(app_version.priority);
                }
            }
        }

        max_priority
    }

    pub fn has_changed(&self) -> io::Result<bool> {
        if let None = self.loaded_at {
            return Ok(true);
        }
        let metadata = fs::metadata(&self.filename)?;
        let modification_time = metadata.modified()?;

        Ok(modification_time > self.loaded_at.unwrap())
    }
}

pub struct MasConfig<'a> {
    pub crediverse: &'a Crediverse,
    pub statistics: &'a Statistics<'a>,
    pub history: &'a History<'a>,
    pub hxc_db: &'a HxcDb<'a>,
    pub wallet_owner: &'a WalletOwner,
    pub credivault_client_cert: Vec<u8>,
    pub credivault_client_key: Vec<u8>,
    pub movivy_mobile_money: &'a MovIvyMobileMoney,
    pub pem: Vec<u8>,
    pub feedback_path: String,
    pub analytics_path: String,
    pub app_version_config: Arc<Mutex<VersionConfig>>,
}

impl MasIpml<'_> {
    pub fn new(config: MasConfig) -> MasIpml {
        let team_service_api_channel = create_team_channel();
        let team_service_api_client = TeamServiceApiClient::new(team_service_api_channel);

        let ca = Certificate::from_pem(config.pem);

        let client_identity =
            Identity::from_pem(config.credivault_client_cert, config.credivault_client_key);

        let tls_config = ClientTlsConfig::new()
            .domain_name("credivault")
            .ca_certificate(ca)
            .identity(client_identity);

        let credivault_channel = create_credivault_channel(tls_config);
        let credivault_client = CrediVaultClient::new(credivault_channel);

        MasIpml {
            crediverse: config.crediverse,
            statistics: config.statistics,
            history: config.history,
            hxc_db: config.hxc_db,
            team_service_api: team_service_api_client,
            credivault_api: credivault_client,
            credivault_wallet_owner: config.wallet_owner,
            movivy_mobile_money: config.movivy_mobile_money,
            session_timeout: get_configured_timeout(),
            feedback_path: config.feedback_path,
            analytics_path: config.analytics_path,
            app_version_config: config.app_version_config,
        }
    }
}

#[derive(Debug, Default, PartialEq, Eq, Serialize, Deserialize)]
pub struct TokenData {
    pub session_id: String,
    pub mobile_money_token: String,
    pub msisdn: String,
    pub exp: usize,
}

fn get_claim_data(extensions: &Extensions) -> Result<TokenData, Status> {
    match extensions.get::<AuthorizationStatus>() {
        None => {
            let error_message = "AuthorizationStatus not found on request extentions".to_string();
            error!("get_claim_data error: {}", error_message);
            Err(Status::permission_denied(error_message))
        }
        Some(authorization_status) => match authorization_status {
            AuthorizationStatus::TokenExpired => {
                let error_message = "Token Expired".to_string();
                error!("get_claim_data error: {}", error_message);
                Err(Status::permission_denied(error_message))
            }
            AuthorizationStatus::Authorized(token_msisdn, token_session_id, mobile_money_token) => {
                Ok(TokenData {
                    msisdn: token_msisdn.to_string(),
                    session_id: token_session_id.to_string(),
                    exp: 0,
                    mobile_money_token: mobile_money_token.to_string(),
                })
            }
        },
    }
}

fn create_jwt_token(claims: TokenData) -> Result<String, Box<dyn std::error::Error>> {
    Ok(encode(
        &Header::default(),
        &claims,
        &EncodingKey::from_secret(crate::MAS_JWT_SECRET.as_bytes()),
    )?)
}

fn create_login_response(
    submit_pin_status: Result<SubmitSecretStatus, CrediverseError>,
    msisdn: String,
    session_id: String,
    session_timeout: usize,
) -> Result<Response<LoginResponse>, Status> {
    match submit_pin_status {
        Err(err) => {
            let error = Status::unauthenticated(err.to_string());
            error!("Submit Pin Error: {}", error);
            Err(error)
        }
        Ok(submit_pin_status) => {
            let time_now = match SystemTime::now().duration_since(SystemTime::UNIX_EPOCH) {
                Ok(time_now) => time_now.as_secs() as usize,
                Err(_) => 0,
            };

            let session_expiry_time = session_timeout + time_now;

            let token = create_jwt_token(TokenData {
                session_id,
                msisdn: msisdn.to_string(),
                exp: session_expiry_time,
                mobile_money_token: "".to_string(),
            })
            .unwrap_or("".to_string());
            let response = match submit_pin_status {
                SubmitSecretStatus::Authenticated => LoginResponse {
                    agent_id: "".to_string(),
                    agent_msisdn: msisdn,
                    login_token: token,
                    refresh_token: "".to_string(),
                    authentication_status: AuthenticationStatus::Authenticated as i32,
                    message: "".to_string(),
                },
                SubmitSecretStatus::RequireOtp => LoginResponse {
                    agent_id: "".to_string(),
                    agent_msisdn: msisdn,
                    login_token: token,
                    refresh_token: "".to_string(),
                    authentication_status: AuthenticationStatus::RequireOtp as i32,
                    message: "".to_string(),
                },
            };
            Ok(Response::new(response))
        }
    }
}

fn _transaction_should_be_shown(transaction: &masapi::Transaction, sender_is_owner: bool) -> bool {
    match transaction.transaction_type() {
        TransactionType::UnknownType => false,
        TransactionType::SalesQuery => false,
        TransactionType::DepositsQuery => false,
        TransactionType::LastTransactionEnquiry => false,
        TransactionType::RegisterPin => false,
        TransactionType::ChangePin => false,
        TransactionType::TransactionStatusEnquiry => false,
        TransactionType::BalanceEnquiry => false,

        TransactionType::SelfTopup => true,
        TransactionType::Adjudicate => true,
        TransactionType::Reverse => true,
        TransactionType::ReversePartially => true,

        TransactionType::Sell => sender_is_owner,

        TransactionType::Adjust => transaction.status == "SUCCESS",

        TransactionType::Replenish => match sender_is_owner {
            true => false,
            false => transaction.status == "SUCCESS",
        },

        TransactionType::Transfer => match sender_is_owner {
            true => true,
            false => transaction.status == "SUCCESS",
        },
        TransactionType::SellBundle => match sender_is_owner {
            true => true,
            false => transaction.status == "SUCCESS",
        },
        TransactionType::PromotionReward => match sender_is_owner {
            true => true,
            false => transaction.status == "SUCCESS",
        },
        TransactionType::NonAirtimeDebit => match sender_is_owner {
            true => true,
            false => transaction.status == "SUCCESS",
        },
        TransactionType::NonAirtimeRefund => match sender_is_owner {
            true => true,
            false => transaction.status == "SUCCESS",
        },
    }
}

fn _filter_transactions(transactions: Vec<Transaction>, owner_msisdn: String) -> Vec<Transaction> {
    transactions
        .into_iter()
        .filter(move |transaction| -> bool {
            _transaction_should_be_shown(transaction, owner_msisdn == transaction.source_msisdn)
        })
        .collect()
}

#[tonic::async_trait]
impl Mas for MasIpml<'static> {
    async fn format_msisdn(
        &self,
        request: Request<masapi::Msisdn>,
    ) -> Result<Response<masapi::Msisdn>, Status> {
        let extentions = request.extensions();
        let request = request.get_ref();
        let session_id = get_claim_data(extentions)?.session_id;

        trace!("Format MSISDN Request: {}", request);

        let formatted_msisdn = self
            .crediverse
            .format_msisdn(session_id.to_string(), request.msisdn.to_string())
            .await?;

        let response: masapi::Msisdn = formatted_msisdn.into();

        trace!("Format MSISDN Response: {}", response);

        Ok(Response::new(response))
    }

    async fn is_team_lead(
        &self,
        request: Request<masapi::NoParam>,
    ) -> Result<Response<masapi::IsTeamLead>, Status> {
        trace!("Is Team Lead Request: {}", request.get_ref());
        let extentions = request.extensions();
        let token_claim = get_claim_data(extentions)?;
        let msisdn = token_claim.msisdn;

        let team_lead_agent_id = self.hxc_db.get_agent_id(msisdn).await?;

        let team_lead = team_service_api::TeamLead {
            agent_id: team_lead_agent_id,
        };

        match self.team_service_api.clone().is_team_lead(team_lead).await {
            Ok(is_team_lead) => {
                let response = Response::new(masapi::IsTeamLead {
                    is_team_lead: is_team_lead.get_ref().is_team_lead,
                });
                trace!("Is Team Lead Response: {}", response.get_ref());
                Ok(response)
            }
            Err(e) => return Err(Status::internal(format!("IS_TEAM_LEAD_FAILED:  {}", e))),
        }
    }

    async fn buy_airtime_with_mobile_money(
        &self,
        request: Request<BuyWithMobileMoneyRequest>,
    ) -> Result<Response<masapi::Ok>, Status> {
        trace!(
            "Buy Airtime With Mobile Money Request: {}",
            request.get_ref()
        );
        let extentions = request.extensions();

        let token_claim = get_claim_data(extentions)?;
        let msisdn = token_claim.msisdn;

        let request = request.get_ref();

        let raw_amount = request.mobile_money_amount.clone();

        let amount = match convert_to_cents(raw_amount.as_str()) {
            Ok(amount) => amount,
            Err(error) => {
                let message = format!("Could not parse mombile money amount: {}", error);

                error!("{}", message.as_str());

                return Err(Status::failed_precondition(message));
            }
        };

        trace!("Buy Airtime With Mobile Money for: {} ", amount);

        let at_time = match SystemTime::now().duration_since(SystemTime::UNIX_EPOCH) {
            Ok(time_now) => time_now.as_secs() as u32,
            Err(_) => 0,
        };

        let mut transfer_mobile_money_request = credivault_api::TransferRequest {
            amount,
            at_time,
            from_wallet: msisdn.clone(),
            to_wallet: "operator_mobile_money_wallet".to_string(),
            ownership_key: self.credivault_wallet_owner.pubilc_key_base64(),
            signature: "".to_string(),
        };

        trace!(
            "Buy Airtime With Mobile Money; VaultRequest {:?} ",
            transfer_mobile_money_request
        );

        let signature = self
            .credivault_wallet_owner
            .sign_transfer(transfer_mobile_money_request.clone());

        trace!("Buy Airtime With Mobile Money; signature {:?} ", signature);

        transfer_mobile_money_request.signature = signature;

        let vault_transfer_response = self
            .credivault_api
            .clone()
            .transfer(transfer_mobile_money_request)
            .await?;

        trace!(
            "Buy Airtime With Mobile Money; vault transfer response {:?} ",
            vault_transfer_response
        );

        // transfer the crediverse credit
        //
        //

        trace!("Buy Airtime With Mobile Money; mobile money transfered");

        let mm_agent_username = get_mobile_money_agent_username();
        let mm_agent_password = get_mobile_money_agent_password();

        trace!(
            "calling crediverse.login_as_api_user({})",
            mm_agent_username
        );

        let login_result = self
            .crediverse
            .login_as_api_user(mm_agent_username, mm_agent_password)
            .await?;

        trace!("login_result = {:?}", login_result);

        let mm_agent_session_id = match login_result {
            AuthStatus::Authenticated(session_id, _) => session_id,
            AuthStatus::AuthenticationFailed(error) => return Err(Status::unauthenticated(error)),
            AuthStatus::CredentialsRequired(error) => return Err(Status::unauthenticated(error)),
            AuthStatus::PinRequired(error) => return Err(Status::unauthenticated(error)),
            AuthStatus::OtpRequired(session_id) => return Err(Status::unauthenticated(session_id)),
        };

        trace!("mm_agent_session_id : {:?}", mm_agent_session_id);

        let transfer_response = self
            .crediverse
            .transfer(
                mm_agent_session_id.clone(),
                msisdn,
                raw_amount.to_string(),
                None,
                None,
            )
            .await?;

        trace!("TransferResponse : {:?}", transfer_response);

        let _transaction_number = transfer_response.transaction_number.as_str().unwrap_or("");

        let ok = masapi::Ok {};
        trace!("Buy With Mobile Money Response: {}", ok);
        Ok(Response::new(ok))
    }

    async fn get_mobile_money_balance(
        &self,
        request: Request<NoParam>,
    ) -> Result<Response<MobileMoneyBalance>, Status> {
        trace!("Get Mobile Money Balance Request: {}", request.get_ref());
        let extentions = request.extensions();

        let token_claim = get_claim_data(extentions)?;
        let msisdn = token_claim.msisdn;

        let balance = self
            .credivault_api
            .clone()
            .get_balance(credivault_api::GetBalanceRequest { wallet: msisdn })
            .await?
            .get_ref()
            .balance;

        let response = MobileMoneyBalance {
            mobile_money_balance: format!("{:.2}", balance / 100),
        };
        trace!("Get Mobile Money Balance Response: {}", response); // the vault returns cents
        Ok(Response::new(response))
    }

    async fn get_team_stock_balance(
        &self,
        request: Request<NoParam>,
    ) -> Result<Response<StockBalance>, Status> {
        trace!("Get Team Stock Balance Request: {}", request.get_ref());
        let extentions = request.extensions();

        let token_claim = get_claim_data(extentions)?;
        let msisdn = token_claim.msisdn;

        let agent_id = self.hxc_db.get_agent_id(msisdn).await?;

        let company_id: u32 = self
            .crediverse
            .get_session(token_claim.session_id.to_string())
            .await?
            .company_id
            .as_u64()
            .unwrap_or_default() as u32;

        let team_lead = team_service_api::TeamLead { agent_id: agent_id };

        let team_agent_ids = match self
            .team_service_api
            .clone()
            .get_team_agent_ids(team_lead.clone())
            .await
        {
            Ok(team_agent_ids) => team_agent_ids
                .into_inner()
                .agent_ids
                .into_iter()
                .map(|agent_id| agent_id)
                .collect(),
            Err(e) => match e.code() {
                Code::NotFound => {
                    let status = Status::not_found(format!("TEAM_NOT_FOUND : {}", e));
                    log::error!("{}", status);
                    return Err(status);
                }

                _ => {
                    let status = Status::internal(format!("GET_TEAM_FAILED: {}", e));
                    log::error!("{}", status);
                    return Err(status);
                }
            },
        };

        let response = self
            .hxc_db
            .get_balance(Subject::Team(team_agent_ids), company_id)
            .await?
            .into();

        trace!("Get Team Stock Balance Response: {}", response);

        Ok(Response::new(response))
    }

    async fn login(
        &self,
        request: Request<LoginRequest>,
    ) -> Result<Response<LoginResponse>, Status> {
        let request_ref = request.get_ref();

        trace!("Login Request");

        let session_id = self.crediverse.get_session_id().await?;

        let msisdn = request_ref.msisdn.to_string();

        let pin_encryption_key = self
            .crediverse
            .get_key(session_id.to_string(), msisdn.to_string())
            .await?;

        match pin_encryption_key {
            GetKeyStatus::PinEncryptionKey(key) => {
                let submit_pin_status = self
                    .crediverse
                    .submit_pin(session_id.to_string(), key, request_ref.pin.to_string())
                    .await;
                create_login_response(submit_pin_status, msisdn, session_id, self.session_timeout)
            }
            GetKeyStatus::GetKeyFailed(reason) => {
                let error = Status::unauthenticated(reason);
                error!("Login Error GetKeyFailed: {}", error);
                Err(error)
            }
        }
    }

    async fn submit_otp(
        &self,
        login_request: Request<LoginRequest>,
    ) -> Result<Response<LoginResponse>, Status> {
        let extentions = login_request.extensions();
        let login_request = login_request.get_ref();

        trace!("Submit Pin Request");

        let otp = login_request.one_time_pin.to_string();
        let msisdn = login_request.msisdn.to_string();

        if otp.is_empty() || msisdn.is_empty() {
            let error = Status::new(Code::InvalidArgument, "otp and msisdn are mandatory");
            error!("Submit Otp Error: {}", error);
            return Err(error);
        } else {
            let token_claim = get_claim_data(extentions)?;

            let session = self
                .crediverse
                .get_session(token_claim.session_id.to_string())
                .await?;

            let agent_id = session.agent_id.to_string();

            let _ = self
                .crediverse
                .submit_otp(session.session_id.to_string(), otp)
                .await?;

            let login_response = LoginResponse {
                agent_id,
                agent_msisdn: msisdn,
                login_token: "".into(),
                refresh_token: "".into(),
                authentication_status: 0,
                message: "".into(),
            };

            Ok(Response::new(login_response))
        }
    }

    async fn update_login_token(
        &self,
        refresh_request: Request<masapi::NoParam>,
    ) -> Result<Response<UpdateTokenResponse>, Status> {
        let extensions = refresh_request.extensions();

        let existing_token_data = get_claim_data(extensions)?;

        trace!("Update Token Request");
        /*
         * The session is automatically updated with this GET request.
         *  If the update fails, then the token renewal will also fail
         */
        self.crediverse
            .get_session(existing_token_data.session_id.to_string())
            .await?;

        let time_now = match SystemTime::now().duration_since(SystemTime::UNIX_EPOCH) {
            Ok(time_now) => time_now.as_secs() as usize,
            Err(_) => 0,
        };

        let session_expiry_time = self.session_timeout + time_now;
        let token = create_jwt_token(TokenData {
            session_id: existing_token_data.session_id.to_string(),
            msisdn: existing_token_data.msisdn.to_string(),
            exp: session_expiry_time,
            mobile_money_token: existing_token_data.mobile_money_token.to_string(),
        })
        .unwrap_or_default();

        let response = UpdateTokenResponse { login_token: token };

        Ok(Response::new(response))
    }

    async fn get_version_status(
        &self,
        request: Request<VersionStatusRequest>,
    ) -> Result<Response<VersionStatusResponse>, Status> {

        let request = request.get_ref();

        let mut response = VersionStatusResponse {
            is_app_version_ok: false,
            app_version_deprecation_date: None,
            app_version_code_latest: 0,
            app_version_name_latest: String::new(),
            app_update_priority: None,
            app_update_download_url: None,
        };

        let version_config_safe = match self.app_version_config.lock() {
            Ok(config) => config,
            Err(_) => return Err(Status::internal("Internal service error")),
        };

        if let Some(app_version) = version_config_safe.get_version(request.app_version_code) {
			response.is_app_version_ok = app_version.supported;
            response.app_version_deprecation_date = Some(app_version.deprecation_date);
        } else {
            warn!(
                "get_version_status: request from app version {}, not a known version, cannot support.",
                request.app_version_code
            );
        }

        if let Some(latest_version) = version_config_safe.get_latest_version() {
            response.app_version_code_latest = latest_version.version_code;
            response.app_version_name_latest = latest_version.version_name.clone();
            if request.app_version_code < latest_version.version_code {
                response.app_update_priority = version_config_safe.get_max_priority(request.app_version_code);
                response.app_update_download_url = Some(latest_version.download_url.clone());
            }
        } else {
            // this should never happen
            error!("get_version_status: unable to determine the latest app version, check service configuration..");
            return Err(Status::internal("Unable to determine latest version"));
        }

        info!(
            "get_version_status ok: request from app version: {}, version ok: {}, latest: {} ({}), priority {}.",
            request.app_version_code,
            response.is_app_version_ok,
            response.app_version_code_latest,
            response.app_version_name_latest,
            response.app_update_priority.unwrap_or(0)
        );

        Ok(Response::new(response))
    }

    async fn get_account_info(
        &self,
        request: Request<AgentId>,
    ) -> Result<Response<AccountInfo>, Status> {
        let extentions = request.extensions();
        let session_id = get_claim_data(extentions)?.session_id;

        trace!("Get Account Info Request: {}", request.get_ref());
        let account_details = self
            .crediverse
            .get_agent_info(session_id.to_string())
            .await?;

        let tier_name = match account_details.tier_id.as_u64() {
            Some(tier_id) => {
                match self
                    .crediverse
                    .get_tier_name(session_id.clone(), tier_id.to_string())
                    .await
                {
                    Ok(tier_name) => tier_name,
                    Err(_) => tier_id.to_string(),
                }
            }
            None => "Unknown Tier".to_string(),
        };

        let session = self.crediverse.get_session(session_id.to_string()).await?;

        let mut account_info: AccountInfo = account_details.into();

        account_info.country_code = session.country_id;
        account_info.tier = tier_name.to_string();

        trace!("Get Account Info Response: {}", account_info);

        Ok(Response::new(account_info))
    }

    async fn get_stock_balance(
        &self,
        request: Request<GetStockBalanceRequest>,
    ) -> Result<Response<StockBalance>, Status> {
        trace!("Get Stock Balance Request: {}", request.get_ref());

        let extentions = request.extensions();
        let request = request.get_ref();

        let token_claim = get_claim_data(extentions)?;

        let company_id: u32 = self
            .crediverse
            .get_session(token_claim.session_id.to_string())
            .await?
            .company_id
            .as_u64()
            .unwrap_or_default() as u32;

        let msisdn = match request.msisdn.clone() {
            Some(msisdn) => msisdn,
            None => token_claim.msisdn,
        };

        let agent_id = self.hxc_db.get_agent_id(msisdn).await?;

        let balance = self
            .hxc_db
            .get_balance(Subject::Agent(agent_id), company_id)
            .await?
            .into();

        trace!("Get Stock Balance Response: {}", balance);

        Ok(Response::new(balance))
    }

    async fn get_transactions(
        &self,
        request: Request<GetTransactionsRequest>,
    ) -> Result<Response<Transactions>, Status> {
        let extentions = request.extensions();

        let token_claim = get_claim_data(extentions)?;
        let owner_msisdn = token_claim.msisdn;

        trace!("Get Transactions Request: {}", request.get_ref());

        let company_id: u32 = self
            .crediverse
            .get_session(token_claim.session_id.to_string())
            .await?
            .company_id
            .as_u64()
            .unwrap_or_default() as u32;

        let request = request.get_ref();

        let transactions: Vec<CrediverseTransaction> = self
            .history
            .get_transaction_page(
                company_id,
                owner_msisdn,
                request.start_page as usize,
                request.transactions_per_page as usize,
            )
            .await?;

        let response = Response::new(transactions.into());
        trace!("Get Transactions Response: {}", response.get_ref());

        Ok(response)
    }

    async fn sell_airtime_wholesale(
        &self,
        request: Request<masapi::SellAirtimeRequest>,
    ) -> Result<Response<masapi::SellAirtimeResponse>, Status> {
        let extentions = request.extensions();
        let token_claim = get_claim_data(extentions)?;
        let session_id = token_claim.session_id;
        let request = request.get_ref();

        trace!("Sell Airtime Request: {}", request);

        let recipient_msisdn: String = match self
            .crediverse
            .format_msisdn(session_id.to_string(), request.msisdn.clone())
            .await?
            .msisdn
            .as_str()
        {
            Some(str) => str.to_string(),
            None => return Err(Status::invalid_argument("Could not format MSISDN")),
        };

        let (return_code, follow_up) = {
            let response = self
                .crediverse
                .transfer(
                    session_id.clone(),
                    recipient_msisdn,
                    request.amount.to_string(),
                    request.latitude,
                    request.longitude,
                )
                .await?;

            (response.return_code, response.follow_up)
        };

        let sell_airtime_response = masapi::SellAirtimeResponse {
            follow_up_required: follow_up.as_bool().unwrap_or(false),
        };

        let response = Response::<masapi::SellAirtimeResponse>::new(sell_airtime_response);

        let status_str = return_code.to_string();

        match status_str.as_str() {
            "\"SUCCESS\"" => {
                trace!("Sell Airtime Response: {}", response.get_ref());
                Ok(response)
            }
            "\"INSUFFICIENT_FUNDS\"" => {
                error!("Sell Airtime Error: {}", status_str);
                Err(Status::failed_precondition(status_str))
            }
            &_ => {
                error!("Sell Airtime Error: {}", status_str);
                Err(Status::internal(status_str))
            }
        }
    }

    async fn sell_airtime(
        &self,
        request: Request<masapi::SellAirtimeRequest>,
    ) -> Result<Response<masapi::SellAirtimeResponse>, Status> {
        let extentions = request.extensions();
        let token_claim = get_claim_data(extentions)?;
        let session_id = token_claim.session_id;
        let seller_msisdn = token_claim.msisdn;
        let request = request.get_ref();

        trace!("Sell Airtime Request: {}", request);

        let recipient_msisdn: String = match self
            .crediverse
            .format_msisdn(session_id.to_string(), request.msisdn.clone())
            .await?
            .msisdn
            .as_str()
        {
            Some(str) => str.to_string(),
            None => return Err(Status::invalid_argument("Could not format MSISDN")),
        };

        let disambiguate_sale_type = match env::var("AUTOMATIC_WHOLESALE_RETAIL_DISAMBIGUATION") {
            Err(_e) => false,
            Ok(automatic_wholesale_retail_disambiguation) => {
                automatic_wholesale_retail_disambiguation == "TRUE"
            }
        };

        let sale_type = if disambiguate_sale_type {
            self.hxc_db
                .get_sale_type(seller_msisdn, recipient_msisdn.clone())
                .await?
        } else {
            SaleType::Retail
        };

        let (return_code, follow_up) = match sale_type {
            SaleType::Retail => {
                let sell_airtime_response = self
                    .crediverse
                    .sell_airtime(
                        session_id.clone(),
                        recipient_msisdn,
                        request.amount.to_string(),
                        request.latitude,
                        request.longitude,
                    )
                    .await?;

                (
                    sell_airtime_response.return_code,
                    sell_airtime_response.follow_up,
                )
            }
            SaleType::Wholesale => {
                let transfer_response = self
                    .crediverse
                    .transfer(
                        session_id.clone(),
                        request.msisdn.to_string(),
                        request.amount.to_string(),
                        request.latitude,
                        request.longitude,
                    )
                    .await?;

                (transfer_response.return_code, transfer_response.follow_up)
            }
        };

        let sell_airtime_response = masapi::SellAirtimeResponse {
            follow_up_required: follow_up.as_bool().unwrap_or(false),
        };

        let response = Response::<masapi::SellAirtimeResponse>::new(sell_airtime_response);

        let status_str = return_code.to_string();

        match status_str.as_str() {
            "\"SUCCESS\"" => {
                trace!("Sell Airtime Response: {}", response.get_ref());
                Ok(response)
            }
            "\"INSUFFICIENT_FUNDS\"" => {
                error!("Sell Airtime Error: {}", status_str);
                Err(Status::failed_precondition(status_str))
            }
            &_ => {
                error!("Sell Airtime Error: {}", status_str);
                Err(Status::internal(status_str))
            }
        }
    }

    async fn get_team_sales_summary(
        &self,
        request: Request<SalesSummaryRequest>,
    ) -> Result<Response<SalesSummaryResponse>, Status> {
        let extentions = request.extensions();
        let request = request.get_ref();

        trace!("Get Team Sales Summary Request: {}", request);
        let token_claim = get_claim_data(extentions)?;
        let msisdn = token_claim.msisdn;

        let team_lead_agent_id = self.hxc_db.get_agent_id(msisdn).await?;

        let team_lead = team_service_api::TeamLead {
            agent_id: team_lead_agent_id,
        };

        let team_agent_ids = match self
            .team_service_api
            .clone()
            .get_team_agent_ids(team_lead.clone())
            .await
        {
            Ok(team_agent_ids) => team_agent_ids,
            Err(e) => match e.code() {
                Code::NotFound => return Err(Status::not_found(format!("TEAM_NOT_FOUND : {}", e))),
                _ => return Err(Status::internal(format!("GET_TEAM_FAILED: {}", e))),
            },
        }
        .into_inner();

        let company_id: u32 = self
            .crediverse
            .get_session(token_claim.session_id.to_string())
            .await?
            .company_id
            .as_u64()
            .unwrap_or_default() as u32;

        let team_agent_ids = team_agent_ids.agent_ids;

        let airtime_sales = self
            .statistics
            .get_airtime_sales_stats(
                company_id,
                request.start_time,
                request.end_time,
                Subject::Team(team_agent_ids.clone()),
            )
            .await?;

        let airtime_unknown_cost_count = self
            .statistics
            .get_number_of_airtime_transactions_without_cost(
                company_id,
                request.start_time,
                request.end_time,
                Subject::Team(team_agent_ids.clone()),
            )
            .await?;

        let bundle_sales = self
            .statistics
            .get_bundle_sales_stats(
                company_id,
                request.start_time,
                request.end_time,
                Subject::Team(team_agent_ids.clone()),
            )
            .await?;

        let bundle_unknown_cost_count = self
            .statistics
            .get_number_of_bundle_transactions_without_cost(
                company_id,
                request.start_time,
                request.end_time,
                Subject::Team(team_agent_ids.clone()),
            )
            .await?;

        let transfer_stats = self
            .statistics
            .get_inbound_transfer_stats(
                company_id,
                request.start_time,
                request.end_time,
                Subject::Team(team_agent_ids.clone()),
            )
            .await?;

        let now = SystemTime::now();
        let now = now.duration_since(UNIX_EPOCH).unwrap_or_default().as_secs();

        let response = SalesSummaryResponse {
            response_time: now,
            start_time: request.start_time,
            end_time: request.end_time,
            sales_summary: Some(SalesSummaryValue {
                airtime_sales_value: airtime_sales.sales_value,
                airtime_sales_count: airtime_sales.sales_count,
                airtime_cost_of_goods_sold: airtime_sales.cost_of_goods_sold,
                airtime_unknown_cost_count,
                bundle_sales_value: bundle_sales.sales_value,
                bundle_sales_count: bundle_sales.sales_count,
                bundle_cost_of_goods_sold: bundle_sales.cost_of_goods_sold,
                bundle_unknown_cost_count,
                inbound_transfers_value: transfer_stats.inbound_transfers_value,
                trade_bonus_value: transfer_stats.trade_bonus_value,
                inbound_transfers_count: transfer_stats.inbound_transfers_count,
            }),
        };
        trace!("Get Team Sales Summary Response: {}", response);

        Ok(Response::<masapi::SalesSummaryResponse>::new(response))
    }

    async fn get_team(&self, request: Request<NoParam>) -> Result<Response<Team>, Status> {
        let extentions = request.extensions();
        let request = request.get_ref();

        trace!("Get Team Request: {}", request);
        let token_claim = get_claim_data(extentions)?;
        let msisdn = token_claim.msisdn;

        let team_lead_agent_id = self.hxc_db.get_agent_id(msisdn).await?;

        let team_lead = team_service_api::TeamLead {
            agent_id: team_lead_agent_id,
        };

        let team_members = match self
            .team_service_api
            .clone()
            .get_team_members(team_lead.clone())
            .await
        {
            Ok(team_members) => team_members,
            Err(e) => match e.code() {
                Code::NotFound => return Err(Status::not_found(format!("TEAM_NOT_FOUND : {}", e))),
                _ => return Err(Status::internal(format!("GET_TEAM_FAILED: {}", e))),
            },
        }
        .into_inner()
        .members;

        let company_id: u32 = self
            .crediverse
            .get_session(token_claim.session_id.to_string())
            .await?
            .company_id
            .as_u64()
            .unwrap_or_default() as u32;

        let mut members: Vec<TeamMember> = Vec::new();

        for member in team_members {
            match self.hxc_db.get_agent(member.agent_id, company_id).await {
                Ok(crediverse_agent) => members.push(TeamMember {
                    first_name: crediverse_agent.first_name,
                    surname: crediverse_agent.surname,
                    msisdn: crediverse_agent.msisdn,
                    stock_balance: Some(StockBalance {
                        balance: crediverse_agent.stock_balance.balance,
                        bonus_balance: crediverse_agent.stock_balance.bonus_balance,
                        on_hold_balance: crediverse_agent.stock_balance.on_hold_balance,
                    }),
                    sales_targets: member
                        .sales_targets
                        .map(|member_sales_targets| SalesTargets {
                            daily_amount: member_sales_targets.daily_amount.clone(),
                            weekly_amount: member_sales_targets.weekly_amount.clone(),
                            monthly_amount: member_sales_targets.monthly_amount,
                        }),
                }),
                Err(error) => {
                    warn!("Agent not found: {}", error);
                }
            };
        }

        Ok(Response::new(Team { members }))
    }

    async fn get_team_membership(
        &self,
        request: tonic::Request<NoParam>,
    ) -> Result<tonic::Response<TeamMembership>, tonic::Status> {
        let extentions = request.extensions();
        let request = request.get_ref();

        trace!("Get Team Membership Request: {}", request);
        let token_claim = get_claim_data(extentions)?;
        let msisdn = token_claim.msisdn;

        let agent_id = self.hxc_db.get_agent_id(msisdn).await?;

        let team_member = team_service_api::Member { agent_id };

        let membership = match self
            .team_service_api
            .clone()
            .get_membership(team_member.clone())
            .await
        {
            Ok(team_membership) => team_membership,
            Err(e) => match e.code() {
                Code::NotFound => {
                    return Err(Status::not_found(format!(
                        "GET_MEMBERSHIP_NOT_FOUND : {}",
                        e
                    )))
                }
                _ => return Err(Status::internal(format!("GET_MEMBERSHIP_FAILED: {}", e))),
            },
        }
        .into_inner();

        let company_id: u32 = self
            .crediverse
            .get_session(token_claim.session_id.to_string())
            .await?
            .company_id
            .as_u64()
            .unwrap_or_default() as u32;

        let member = self.hxc_db.get_agent(agent_id, company_id).await?;

        let team_lead_agent_id = membership
            .team_lead
            .ok_or(Status::not_found("Team Lead Agent not found"))?
            .agent_id;

        let team_lead = self
            .hxc_db
            .get_agent(team_lead_agent_id, company_id)
            .await?;

        Ok(Response::new(TeamMembership {
            member_msisdn: member.msisdn,
            team_lead_msisdn: team_lead.msisdn,
            sales_targets: membership.sales_targets.map(|sales_targets| SalesTargets {
                daily_amount: sales_targets.daily_amount.clone(),
                weekly_amount: sales_targets.weekly_amount.clone(),
                monthly_amount: sales_targets.monthly_amount,
            }),
        }))
    }

    async fn set_team_member_sales_target(
        &self,
        request: tonic::Request<SetTeamMemberSalesTargetRequest>,
    ) -> Result<tonic::Response<Ok>, tonic::Status> {
        let extentions = request.extensions();
        let request = request.get_ref();

        trace!("Set Team Member Sales Target: {:?}", request);
        let token_claim = get_claim_data(extentions)?;
        let msisdn = token_claim.msisdn;

        let team_lead_agent_id = self.hxc_db.get_agent_id(msisdn).await?;
        let member_agent_id = self.hxc_db.get_agent_id(request.msisdn.clone()).await?;

        let team_lead = team_service_api::TeamLead {
            agent_id: team_lead_agent_id,
        };
        let team_member = team_service_api::Member {
            agent_id: member_agent_id,
        };
        let period: team_service_api::Period = match request.period {
            0 => team_service_api::Period::Day,
            1 => team_service_api::Period::Week,
            2 => team_service_api::Period::Month,
            _ => {
                return Err(Status::internal(format!(
                    "SET_TEAM_MEMBER_SALES_TARGET FAILED: invalid Period value {}",
                    request.period
                )))
            }
        };
        let sales_request = team_service_api::MemberSetSalesTargetRequest {
            member: Some(team_member),
            team_lead: Some(team_lead),
            period: period as i32,
            target_amount: request.target_amount.clone(),
        };

        match self
            .team_service_api
            .clone()
            .set_team_member_sales_target(sales_request.clone())
            .await
        {
            Ok(_) => {}
            Err(e) => match e.code() {
                Code::NotFound => return Err(Status::not_found(format!("NOT_FOUND : {}", e))),
                _ => {
                    return Err(Status::internal(format!(
                        "SET_TEAM_MEMBER_SALES_TARGET FAILED: {}",
                        e
                    )))
                }
            },
        };

        let ok = masapi::Ok {};
        trace!("Set Team Member Sales Target Response: {}", ok);
        Ok(Response::new(ok))
    }

    async fn get_sales_summary(
        &self,
        request: Request<SalesSummaryRequest>,
    ) -> Result<Response<SalesSummaryResponse>, Status> {
        let extentions = request.extensions();
        let request = request.get_ref();
        trace!("Get Sales Summary Request: {}", request);

        let token_claim = get_claim_data(extentions)?;

        let msisdn = match request.msisdn.clone() {
            Some(msisdn) => msisdn,
            None => token_claim.msisdn,
        };

        let company_id: u32 = self
            .crediverse
            .get_session(token_claim.session_id.to_string())
            .await?
            .company_id
            .as_u64()
            .unwrap_or_default() as u32;

        let agent_id = self.hxc_db.get_agent_id(msisdn).await?;

        let airtime_sales = self
            .statistics
            .get_airtime_sales_stats(
                company_id,
                request.start_time,
                request.end_time,
                Subject::Agent(agent_id),
            )
            .await?;

        let airtime_unknown_cost_count = self
            .statistics
            .get_number_of_airtime_transactions_without_cost(
                company_id,
                request.start_time,
                request.end_time,
                Subject::Agent(agent_id),
            )
            .await?;

        let bundle_sales = self
            .statistics
            .get_bundle_sales_stats(
                company_id,
                request.start_time,
                request.end_time,
                Subject::Agent(agent_id),
            )
            .await?;

        let bundle_unknown_cost_count = self
            .statistics
            .get_number_of_bundle_transactions_without_cost(
                company_id,
                request.start_time,
                request.end_time,
                Subject::Agent(agent_id),
            )
            .await?;

        let transfer_stats = self
            .statistics
            .get_inbound_transfer_stats(
                company_id,
                request.start_time,
                request.end_time,
                Subject::Agent(agent_id),
            )
            .await?;

        let now = SystemTime::now();
        let now = now.duration_since(UNIX_EPOCH).unwrap_or_default().as_secs();

        let response = SalesSummaryResponse {
            response_time: now,
            start_time: request.start_time,
            end_time: request.end_time,
            sales_summary: Some(SalesSummaryValue {
                airtime_sales_value: airtime_sales.sales_value,
                airtime_sales_count: airtime_sales.sales_count,
                airtime_cost_of_goods_sold: airtime_sales.cost_of_goods_sold,
                airtime_unknown_cost_count,
                bundle_sales_value: bundle_sales.sales_value,
                bundle_sales_count: bundle_sales.sales_count,
                bundle_cost_of_goods_sold: bundle_sales.cost_of_goods_sold,
                bundle_unknown_cost_count,
                inbound_transfers_value: transfer_stats.inbound_transfers_value,
                inbound_transfers_count: transfer_stats.inbound_transfers_count,
                trade_bonus_value: transfer_stats.trade_bonus_value,
            }),
        };

        trace!("Get Sales Summary Response: {}", request);

        Ok(Response::<masapi::SalesSummaryResponse>::new(response))
    }

    async fn get_global_sales_summary(
        &self,
        request: Request<SalesSummaryRequest>,
    ) -> Result<Response<SalesSummaryResponse>, Status> {
        let extentions = request.extensions();
        let request = request.get_ref();
        log::trace!("Get Global Sales Summary Request: {}", request);

        let token_claim = get_claim_data(extentions)?;

        let company_id: u32 = self
            .crediverse
            .get_session(token_claim.session_id.to_string())
            .await?
            .company_id
            .as_u64()
            .unwrap_or_default() as u32;

        let global_sales = self
            .statistics
            .get_global_sales_stats(company_id, request.start_time, request.end_time)
            .await?;

        let now = SystemTime::now();
        let now = now.duration_since(UNIX_EPOCH).unwrap_or_default().as_secs();

        let response = SalesSummaryResponse {
            response_time: now,
            start_time: request.start_time,
            end_time: request.end_time,
            sales_summary: Some(SalesSummaryValue {
                airtime_sales_value: global_sales.0,
                airtime_sales_count: global_sales.1,
                airtime_cost_of_goods_sold: "0.0000".to_string(),
                airtime_unknown_cost_count: 0,
                bundle_sales_value: "0".to_string(),
                bundle_sales_count: 0,
                bundle_cost_of_goods_sold: "0.0000".to_string(),
                bundle_unknown_cost_count: 0,
                inbound_transfers_value: "0.0000".to_string(),
                trade_bonus_value: "0.0000".to_string(),
                inbound_transfers_count: 0,
            }),
        };

        log::trace!("Get Global Sales Summary Response: {}", response);

        Ok(Response::<masapi::SalesSummaryResponse>::new(response))
    }

    async fn get_global_hourly_sales_summary(
        &self,
        request: Request<SalesSummaryRequest>,
    ) -> Result<Response<HourlySalesSummaryResponse>, Status> {
        let extentions = request.extensions();
        let request = request.get_ref();
        log::trace!("Get Global Hourly Sales Summary Request: {}", request);

        let token_claim = get_claim_data(extentions)?;

        let company_id: u32 = self
            .crediverse
            .get_session(token_claim.session_id.to_string())
            .await?
            .company_id
            .as_u64()
            .unwrap_or_default() as u32;

        let global_sales = self
            .statistics
            .get_global_hourly_sales_stats(company_id, request.start_time, request.end_time)
            .await?;

        log::trace!(
            "Get Global Hourly Sales Summary Request: sales: {:?}",
            global_sales
        );

        // convert HourlySalesSummaryEntry to HourlySalesSummaryValue

        let mut sales_summary: Vec<HourlySalesSummaryValue> = Vec::new();
        let start_time: u64 = request.start_time;
        let end_time: u64 = request.end_time;

        for ts in (start_time..end_time).step_by(3600) {
            let dt = match Local.timestamp_opt(ts as i64, 0) {
                chrono::LocalResult::None => {
                    return Err(Status::invalid_argument(
                        "Start and/or end time is invalid!",
                    ))
                }
                chrono::LocalResult::Single(dt) => dt,
                chrono::LocalResult::Ambiguous(first, _) => {
                    log::warn!(
                        "Multiple timestamps returned from timestamp_otp, using the first: {}",
                        first
                    );
                    first
                }
            };

            let date = dt.date_naive().and_hms_opt(0, 0, 0).unwrap();
            let hour = dt.hour();

            let mut airtime_sales_value = "0.00".to_string();
            let mut airtime_sales_count = 0;

            for entry in &global_sales {
                if entry.date == date.timestamp() as u64 && entry.hour == hour {
                    airtime_sales_value = entry.sales_value.clone();
                    airtime_sales_count = entry.sales_count;
                    break;
                }
            }

            sales_summary.push(HourlySalesSummaryValue {
                date: date.timestamp() as u64,
                hour: hour as u64,
                airtime_sales_value,
                airtime_sales_count,
                bundle_sales_value: "0.00".to_string(),
                bundle_sales_count: 0,
            });
        }

        for summary in &sales_summary {
            log::trace!(
                "Get Global Hourly Sales Summary Request: sales: entry: date: {}, hour: {}, sales amount: {}, sales count: {}",
                summary.date,
                summary.hour,
                summary.airtime_sales_value,
                summary.airtime_sales_count,
            );
        }

        // end convert

        let now = SystemTime::now();
        let now = now.duration_since(UNIX_EPOCH).unwrap_or_default().as_secs();

        let response = HourlySalesSummaryResponse {
            response_time: now,
            start_time: request.start_time,
            end_time: request.end_time,
            sales_summary,
        };

        //log::trace!("Get Global Hourly Sales Summary Response: {}", response);

        Ok(Response::<masapi::HourlySalesSummaryResponse>::new(
            response,
        ))
    }

    async fn change_pin(
        &self,
        request: Request<ChangePinRequest>,
    ) -> Result<Response<masapi::Ok>, Status> {
        let extensions = request.extensions();

        let existing_token_data = get_claim_data(extensions)?;
        let session_id = existing_token_data.session_id;
        let request = request.get_ref();

        trace!("Change Pin Request: {}", request);

        match self
            .crediverse
            .change_pin(session_id.clone(), request.new_pin.clone())
            .await
        {
            Ok(_change_pin_response) => {
                let response = masapi::Ok {};
                trace!("Change Pin Response: {}", response);
                Ok(Response::new(response))
            }
            Err(err) => {
                error!("Change Pin Error: {}", err);
                return Err(Status::internal("CHANGE_PIN_FAILED".to_string()));
            }
        }
    }

    async fn update_profile(
        &self,
        request: Request<masapi::UpdateProfileRequest>,
    ) -> Result<Response<masapi::Ok>, Status> {
        let extensions = request.extensions();

        let existing_token_data = get_claim_data(extensions)?;
        let session_id = existing_token_data.session_id;
        let request = request.get_ref();
        trace!("Update Profile Request: {}", request);
        let company_id: u32 = self
            .crediverse
            .get_session(session_id.to_string())
            .await?
            .company_id
            .as_u64()
            .unwrap_or_default() as u32;

        self.crediverse
            .update_profile(
                session_id.clone(),
                request.title.clone(),
                request.language.clone(),
                request.first_name.clone(),
                request.surname.clone(),
                request.email.clone(),
                request.agent_id.clone(),
                company_id,
            )
            .await?;

        trace!("Update Profile response : {}", masapi::Ok {});

        Ok(Response::new(masapi::Ok {}))
    }

    async fn get_agent_feedback(
        &self,
        request: Request<masapi::FeedBackRequest>,
    ) -> Result<Response<masapi::FeedBackResponse>, Status> {
        let extentions = request.extensions();

        let token_claim = get_claim_data(extentions)?;
        let transaction_owner_msisdn = token_claim.msisdn;

        let request = request.get_ref();
        trace!("Agent Feedback Request: {}", request);

        let dt = Local::now();
        let time_str = format!("{:02}:{:02}:{:02}", dt.hour(), dt.minute(), dt.second());
        let date_str = format!("{}{:02}{:02}", dt.year(), dt.month(), dt.day());
        let filename = format!("agent_feedback_{}.csv", date_str);
        let file_path = format!("{}/{}", self.feedback_path, filename);

        // Create the feedback string to be added to the CSV file
        let string_to_append = format!(
            "{},{},{},{},{},\"{}\"",
            time_str,
            request.agent_id,
            transaction_owner_msisdn,
            request.tier,
            request.name,
            request.feed_back_request_msg
        );

        // Check if file exists or not
        let file_exists = Path::new(&file_path).exists();
        let mut file = match OpenOptions::new()
            .write(true)
            .create(true)
            .append(true)
            .open(&file_path)
        {
            Ok(f) => f,
            Err(e) => {
                error!(
                    "Agent Feedback Error: Failed to open file {:?}: {}",
                    file_path, e
                );
                return Err(Status::internal("Internal Error".to_string()));
            }
        };

        // If the file doesn't exist, create one and add the column titles
        if !file_exists {
            if let Err(e) = writeln!(
                file,
                "Time,Agent ID,Agent MSISDN,Agent Tier,Agent Name,Feedback"
            ) {
                error!(
                    "Agent Feedback Error:  Failed to write to file {:?}: {}",
                    file_path, e
                );
                return Err(Status::internal("Internal Error".to_string()));
            }
        }

        // Append the received feedback to the file
        if let Err(e) = writeln!(file, "{}", string_to_append) {
            error!(
                "Agent Feedback Error: Failed to write to file {:?}: {}",
                file_path, e
            );
            Err(Status::internal("Internal Error".to_string()))
        } else {
            let response = FeedBackResponse {};
            trace!("Agent Feedback Response: {}", response);
            Ok(Response::new(response))
        }
    }

    async fn submit_analytics(
        &self,
        request: Request<masapi::AnalyticsRequest>,
    ) -> Result<Response<masapi::Ok>, Status> {
        let extentions = request.extensions();

        let token_claim = get_claim_data(extentions)?;
        let transaction_owner_msisdn = token_claim.msisdn;

        let request = request.get_ref();
        trace!(
            "Agent submit analytics request: {} entries",
            request.events.len()
        );

        let dt = Local::now();
        let time_str = format!("{:02}:{:02}:{:02}", dt.hour(), dt.minute(), dt.second());
        let date_str = format!("{}{:02}{:02}", dt.year(), dt.month(), dt.day());

        if directory_exists(&self.analytics_path) == false {
            match fs::create_dir_all(&self.analytics_path) {
                Ok(_) => println!(
                    "Analytics directory '{}' created successfully",
                    self.analytics_path
                ),
                Err(e) => {
                    error!(
                        "Failed to create directory '{}' with: {}",
                        self.analytics_path, e
                    );
                    return Err(Status::internal(format!("Unable to save")));
                }
            }
        }

        let mut file_map: HashMap<String, std::fs::File> = HashMap::new();
        let mut file_names_map: HashMap<String, String> = HashMap::new();

        for event in &request.events {
            let formatted_time = match chrono::NaiveDateTime::from_timestamp_opt(event.time as i64, 0) {
                Some(time) => time,
                None => {
                    warn!("Event contains invalid timestamp {}, ignoring it.", event.time);
                    continue;
                }
            };
            let event_date_str = formatted_time.format("%Y%m%d").to_string();
            let event_time_str = formatted_time.format("%Y%m%d %H:%M:%S+00").to_string();
            let receive_time_str = format!("{} {}", date_str, time_str);

            let mut wtr = WriterBuilder::new()
                .quote_style(csv::QuoteStyle::Necessary)
                .from_writer(Vec::new());

            match wtr.write_record(&[
                event_time_str,
                receive_time_str,
                AnalyticsEventType::from_int_to_string(event.r#type).to_string(),
                request.app_version.clone(),
                transaction_owner_msisdn.clone(),
                event.content.clone(),
            ]) {
                Ok(_) => {}
                Err(e) => {
                    error!("Failed to write CSV record: {}", e);
                    continue;
                }
            };

            let inner_buffer = match wtr.into_inner() {
                Ok(inner) => inner,
                Err(e) => {
                    error!("Failed to get inner buffer: {}", e);
                    continue;
                }
            };

            let record = match String::from_utf8(inner_buffer) {
                Ok(string) => string,
                Err(e) => {
                    error!("Failed to convert to String: {}", e);
                    continue;
                }
            };

            let file = match file_map.entry(event_date_str.clone()) {
                std::collections::hash_map::Entry::Occupied(entry) => entry.into_mut(),
                std::collections::hash_map::Entry::Vacant(entry) => {
                    let filename = format!("app_analytics_{}.csv", event_date_str.clone());
                    let file_path = format!("{}/{}", self.analytics_path, filename);

                    let file_exists = Path::new(&file_path).exists();
                    let mut new_file = match OpenOptions::new()
                        .write(true)
                        .create(true)
                        .append(true)
                        .open(&file_path)
                    {
                        Ok(f) => f,
                        Err(e) => {
                            error!(
                                "Agent Analytics Error: Failed to open file {:?}: {}",
                                file_path, e
                            );
                            return Err(Status::internal(format!("Unable to save")));
                        }
                    };

                    if !file_exists {
                        if let Err(e) = writeln!(
                            new_file,
                            "Event Time,Receive Time,Event Type,App Version,Agent MSISDN,Content"
                        ) {
                            error!(
                                "Agent Analytics Error:  Failed to write to file {:?}: {}",
                                file_path, e
                            );
                            return Err(Status::internal(format!("Unable to save")));
                        }
                    }

                    file_names_map.insert(event_date_str.clone(), file_path);

                    entry.insert(new_file)
                }
            };

            let file_name = file_names_map.entry(event_date_str.clone());

            // Append the received feedback to the file
            if let Err(e) = write!(file, "{}", record) {
                error!(
                    "Agent Analytics Error: Failed to write to file {:?}: {}",
                    file_name, e
                );
                continue;
            }
        }

        let ok = masapi::Ok {};
        trace!("Agent submit analytics response: {}", ok);
        Ok(Response::new(ok))
    }

    async fn mobile_money_login(
        &self,
        request: Request<masapi::MobileMoneyLoginRequest>,
    ) -> Result<Response<masapi::MobileMoneyLoginResponse>, Status> {
        let request_ref = request.get_ref();
        let extensions = request.extensions();
        let existing_token_data = get_claim_data(extensions)?;

        trace!("Mobile Money Login Request");

        let username = request_ref.username.to_string();
        let password = request_ref.password.to_string();
        let result = self.movivy_mobile_money.login(username, password).await;

        match result {
            Ok(MMAuthStatus::Authenticated(token)) => {
                trace!("Mobile Money Login successful");
                let time_now = match SystemTime::now().duration_since(SystemTime::UNIX_EPOCH) {
                    Ok(time_now) => time_now.as_secs() as usize,
                    Err(_) => 0,
                };

                let session_expiry_time = self.session_timeout + time_now;

                let login_token = create_jwt_token(TokenData {
                    session_id: existing_token_data.session_id.to_string(),
                    msisdn: existing_token_data.msisdn.to_string(),
                    exp: session_expiry_time,
                    mobile_money_token: token,
                })
                .unwrap_or("".to_string());
                let response: masapi::MobileMoneyLoginResponse = masapi::MobileMoneyLoginResponse {
                    login_token: login_token,
                    status: masapi::MobileMoneyResponseStatus::MmSuccess.into(),
                };
                Ok(Response::new(response))
            }
            Err(err) => {
                error!(
                    "Mobile Money Login failed. Status: [{:?}]. Description: [{}]",
                    err.error_type, err.description
                );
                let response: masapi::MobileMoneyLoginResponse = masapi::MobileMoneyLoginResponse {
                    login_token: "".to_string(),
                    status: masapi::MobileMoneyResponseStatus::MmFailed.into(),
                };
                Ok(Response::new(response))
            }
            _ => {
                let response: masapi::MobileMoneyLoginResponse = masapi::MobileMoneyLoginResponse {
                    login_token: "".to_string(),
                    status: masapi::MobileMoneyResponseStatus::MmFailed.into(),
                };
                Ok(Response::new(response))
            }
        }
    }

    async fn mobile_money_deposit(
        &self,
        request: Request<masapi::MobileMoneyDepositRequest>,
    ) -> Result<Response<masapi::MobileMoneyDepositResponse>, Status> {
        let request_ref = request.get_ref();
        let amount = request_ref.amount.to_string();
        let destination_msisdn = request_ref.destination_msisdn.to_string();
        let extensions = request.extensions();
        let existing_token_data = get_claim_data(extensions)?;

        let result = self
            .movivy_mobile_money
            .cash_transfer(
                amount,
                destination_msisdn,
                existing_token_data.mobile_money_token.to_string(),
            )
            .await;

        match result {
            Ok(MMTransactionStatus::Success(status)) => {
                trace!(
                    "Mobile Money deposit successful. Status: [{}]",
                    status.to_string()
                );
                let response: MobileMoneyDepositResponse = MobileMoneyDepositResponse {
                    status: MobileMoneyResponseStatus::MmSuccess.into(),
                };
                Ok(Response::new(response))
            }
            Ok(MMTransactionStatus::Failed(status, reason)) => {
                error!(
                    "Mobile Money deposit failed. Status: [{:?}]. Description:[{}]",
                    status.to_string(),
                    reason.to_string()
                );
                let response: MobileMoneyDepositResponse = MobileMoneyDepositResponse {
                    status: MobileMoneyResponseStatus::MmFailed.into(),
                };
                Ok(Response::new(response))
            }
            Ok(MMTransactionStatus::Unknown(status, reason)) => {
                error!(
                    "Mobile Money deposit status unknown. Status: [{:?}]. Description:[{}]",
                    status.to_string(),
                    reason.to_string()
                );
                let response: MobileMoneyDepositResponse = MobileMoneyDepositResponse {
                    status: MobileMoneyResponseStatus::MmFailed.into(),
                };
                Ok(Response::new(response))
            }
            Err(err) => match err.error_type {
                MovIvyMobileMoneyErrorType::Unauthorized => {
                    error!(
                        "Mobile Money deposit failed. Status: [{:?}]. Description:[{}]",
                        err.error_type,
                        err.description.to_string()
                    );
                    let response: MobileMoneyDepositResponse = MobileMoneyDepositResponse {
                        status: MobileMoneyResponseStatus::MmUnauthenticated.into(),
                    };
                    Ok(Response::new(response))
                }
                _ => {
                    error!(
                        "Mobile Money deposit failed. Status: [{:?}]. Description:[{}]",
                        err.error_type,
                        err.description.to_string()
                    );
                    let response: MobileMoneyDepositResponse = MobileMoneyDepositResponse {
                        status: MobileMoneyResponseStatus::MmFailed.into(),
                    };
                    Ok(Response::new(response))
                }
            },
        }
    }
}

#[cfg(test)]
mod test {

    use crate::mas::*;

    fn create_test_transaction(
        transaction_type: TransactionType,
        source_msisdn: impl Into<String>,
        recipient_msisdn: impl Into<String>,
        status: impl Into<String>,
    ) -> masapi::Transaction {
        masapi::Transaction {
            commission_amount: None,
            gross_sales_amount: None,
            transaction_no: "".to_string(),
            amount: "".to_string(),
            cost_of_goods_sold: None,
            bonus: "".to_string(),
            transaction_type: transaction_type as i32,
            transaction_started: 0,
            transaction_ended: 0,
            source_msisdn: source_msisdn.into(),
            recipient_msisdn: recipient_msisdn.into(),
            balance_before: "".to_string(),
            bonus_balance_before: "".to_string(),
            on_hold_balance_before: "".to_string(),
            balance_after: "".to_string(),
            bonus_balance_after: "".to_string(),
            on_hold_balance_after: "".to_string(),
            status: status.into(),
            follow_up_required: false,
            rolled_back: false,
            messages: Vec::<String>::new(),
            item_description: None,
        }
    }

    #[tokio::test]
    async fn transaction_should_be_shown_test() {
        let tests = [
            (TransactionType::Adjudicate, "100", "200", "FAIL", true),
            (TransactionType::Adjudicate, "200", "100", "FAIL", true),
            (TransactionType::Adjudicate, "100", "200", "SUCCESS", true),
            (TransactionType::Adjudicate, "200", "100", "SUCCESS", true),
            (TransactionType::Adjust, "100", "200", "FAIL", false),
            (TransactionType::Adjust, "100", "200", "SUCCESS", true),
            (TransactionType::Adjust, "200", "100", "FAIL", false),
            (TransactionType::Adjust, "200", "100", "SUCCESS", true),
            (TransactionType::BalanceEnquiry, "100", "200", "FAIL", false),
            (
                TransactionType::BalanceEnquiry,
                "100",
                "200",
                "SUCCESS",
                false,
            ),
            (TransactionType::BalanceEnquiry, "200", "100", "FAIL", false),
            (
                TransactionType::BalanceEnquiry,
                "200",
                "100",
                "SUCCESS",
                false,
            ),
            (TransactionType::ChangePin, "100", "200", "FAIL", false),
            (TransactionType::ChangePin, "100", "200", "SUCCESS", false),
            (TransactionType::ChangePin, "200", "100", "FAIL", false),
            (TransactionType::ChangePin, "200", "100", "SUCCESS", false),
            (TransactionType::DepositsQuery, "100", "200", "FAIL", false),
            (
                TransactionType::DepositsQuery,
                "100",
                "200",
                "SUCCESS",
                false,
            ),
            (TransactionType::DepositsQuery, "200", "100", "FAIL", false),
            (
                TransactionType::DepositsQuery,
                "200",
                "100",
                "SUCCESS",
                false,
            ),
            (
                TransactionType::LastTransactionEnquiry,
                "100",
                "200",
                "FAIL",
                false,
            ),
            (
                TransactionType::LastTransactionEnquiry,
                "100",
                "200",
                "SUCCESS",
                false,
            ),
            (
                TransactionType::LastTransactionEnquiry,
                "200",
                "100",
                "FAIL",
                false,
            ),
            (
                TransactionType::LastTransactionEnquiry,
                "200",
                "100",
                "SUCCESS",
                false,
            ),
            (TransactionType::NonAirtimeDebit, "100", "200", "FAIL", true),
            (
                TransactionType::NonAirtimeDebit,
                "100",
                "200",
                "SUCCESS",
                true,
            ),
            (
                TransactionType::NonAirtimeDebit,
                "200",
                "100",
                "FAIL",
                false,
            ),
            (
                TransactionType::NonAirtimeDebit,
                "200",
                "100",
                "SUCCESS",
                true,
            ),
            (
                TransactionType::NonAirtimeRefund,
                "100",
                "200",
                "FAIL",
                true,
            ),
            (
                TransactionType::NonAirtimeRefund,
                "100",
                "200",
                "SUCCESS",
                true,
            ),
            (
                TransactionType::NonAirtimeRefund,
                "200",
                "100",
                "FAIL",
                false,
            ),
            (
                TransactionType::NonAirtimeRefund,
                "200",
                "100",
                "SUCCESS",
                true,
            ),
            (TransactionType::PromotionReward, "100", "200", "FAIL", true),
            (
                TransactionType::PromotionReward,
                "100",
                "200",
                "SUCCESS",
                true,
            ),
            (
                TransactionType::PromotionReward,
                "200",
                "100",
                "FAIL",
                false,
            ),
            (
                TransactionType::PromotionReward,
                "200",
                "100",
                "SUCCESS",
                true,
            ),
            (TransactionType::RegisterPin, "100", "200", "FAIL", false),
            (TransactionType::RegisterPin, "100", "200", "SUCCESS", false),
            (TransactionType::RegisterPin, "200", "100", "FAIL", false),
            (TransactionType::RegisterPin, "200", "100", "SUCCESS", false),
            (TransactionType::Replenish, "100", "200", "FAIL", false),
            (TransactionType::Replenish, "100", "200", "SUCCESS", false),
            (TransactionType::Replenish, "200", "100", "FAIL", false),
            (TransactionType::Replenish, "200", "100", "SUCCESS", true),
            (TransactionType::Reverse, "100", "200", "FAIL", true),
            (TransactionType::Reverse, "100", "200", "SUCCESS", true),
            (TransactionType::Reverse, "200", "100", "FAIL", true),
            (TransactionType::Reverse, "200", "100", "SUCCESS", true),
            (
                TransactionType::ReversePartially,
                "100",
                "200",
                "FAIL",
                true,
            ),
            (
                TransactionType::ReversePartially,
                "100",
                "200",
                "SUCCESS",
                true,
            ),
            (
                TransactionType::ReversePartially,
                "200",
                "100",
                "FAIL",
                true,
            ),
            (
                TransactionType::ReversePartially,
                "200",
                "100",
                "SUCCESS",
                true,
            ),
            (TransactionType::SalesQuery, "100", "200", "FAIL", false),
            (TransactionType::SalesQuery, "100", "200", "SUCCESS", false),
            (TransactionType::SalesQuery, "200", "100", "FAIL", false),
            (TransactionType::SalesQuery, "200", "100", "SUCCESS", false),
            (TransactionType::SelfTopup, "100", "200", "FAIL", true),
            (TransactionType::SelfTopup, "100", "200", "SUCCESS", true),
            (TransactionType::SelfTopup, "200", "100", "FAIL", true),
            (TransactionType::SelfTopup, "200", "100", "SUCCESS", true),
            (TransactionType::Sell, "100", "200", "FAIL", true),
            (TransactionType::Sell, "100", "200", "SUCCESS", true),
            (TransactionType::Sell, "200", "100", "FAIL", false),
            (TransactionType::Sell, "200", "100", "SUCCESS", false),
            (TransactionType::SellBundle, "100", "200", "FAIL", true),
            (TransactionType::SellBundle, "100", "200", "SUCCESS", true),
            (TransactionType::SellBundle, "200", "100", "FAIL", false),
            (TransactionType::SellBundle, "200", "100", "SUCCESS", true),
            (
                TransactionType::TransactionStatusEnquiry,
                "100",
                "200",
                "FAIL",
                false,
            ),
            (
                TransactionType::TransactionStatusEnquiry,
                "100",
                "200",
                "SUCCESS",
                false,
            ),
            (
                TransactionType::TransactionStatusEnquiry,
                "200",
                "100",
                "FAIL",
                false,
            ),
            (
                TransactionType::TransactionStatusEnquiry,
                "200",
                "100",
                "SUCCESS",
                false,
            ),
            (TransactionType::Transfer, "100", "200", "FAIL", true),
            (TransactionType::Transfer, "100", "200", "SUCCESS", true),
            (TransactionType::Transfer, "200", "100", "FAIL", false),
            (TransactionType::Transfer, "200", "100", "SUCCESS", true),
        ];

        tests.map(|test| {
            let (transaction_type, sender, receiver, transaction_status, expected_outcome) = test;

            let transaction =
                create_test_transaction(transaction_type, sender, receiver, transaction_status);

            let owner = "100";
            let should_transaction_be_shown =
                _transaction_should_be_shown(&transaction, sender == owner);

            if should_transaction_be_shown != expected_outcome {
                println!(
                    "Testing {:?}, it should be {} but is {}",
                    transaction, expected_outcome, should_transaction_be_shown
                );
            }
            assert_eq!(should_transaction_be_shown, expected_outcome);
        });
    }
}
