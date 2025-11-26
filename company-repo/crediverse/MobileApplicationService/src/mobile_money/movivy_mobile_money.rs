use std::{env, error::Error, fs::File, io::Read};
use reqwest::Client;
use core::fmt;
use base64::{engine::general_purpose, Engine as _};
use serde_derive::{Deserialize, Serialize};
use serde_json::Value;
use uuid::Uuid;
use serde_json::json;
use std::str::FromStr;

pub struct MovIvyMobileMoney{
    client: Client,
    //url: String,
    login_url: String,
    transaction_url: String,
}

#[derive(Eq, PartialEq, Debug)]
pub enum MMAuthStatus {
    #[allow(dead_code)]
    AuthenticationFailed(String),
    #[allow(dead_code)]
    Authenticated(String),
}

#[derive(Eq, PartialEq, Debug)]
pub enum MMTransactionStatus {
    #[allow(dead_code)]
    Success(String),
    #[allow(dead_code)]
    Failed(String, String),
    #[allow(dead_code)]
    Unknown(String, String),
}

#[derive(Debug, Clone)]
pub enum MovIvyMobileMoneyErrorType {
    Internal,
    RequestError,
    HttpError(String),
    Unauthorized,
    HeaderValue,
}
#[derive(Debug)]
pub struct MovIvyMobileMoneyError {
    pub description: String,
    pub error_type: MovIvyMobileMoneyErrorType,
}

impl fmt::Display for MovIvyMobileMoneyError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "MovIvyMobileMoney Error: {:?}", self.description.to_string())
    }
}

impl From<reqwest::Error> for MovIvyMobileMoneyError {
    fn from(error: reqwest::Error) -> Self {
        MovIvyMobileMoneyError {
            description: error.to_string(),
            error_type: MovIvyMobileMoneyErrorType::RequestError,
        }
    }
}

impl From<base64::DecodeError> for MovIvyMobileMoneyError {
    fn from(error: base64::DecodeError) -> Self {
        MovIvyMobileMoneyError {
            description: error.to_string(),
            error_type: MovIvyMobileMoneyErrorType::HeaderValue,
        }
    }
}

impl Error for MovIvyMobileMoneyError {}

pub fn json_null() -> Value {
    Value::Null
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct MMCashTransferRequest {
    #[serde(rename = "request-id", default = "json_null")]
    pub request_id: Value,
    #[serde(rename = "destination", default = "json_null")]
    pub destination: Value,
    #[serde(rename = "amount", default = "json_null")]
    pub amount: Value,
    #[serde(rename = "remarks", default = "json_null")]
    pub remarks: Value,
    #[serde(rename = "extended-data", default = "json_null")]
    pub extended_data: Value,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct MMCashTransferResponse {
    #[serde(rename = "request-id", default = "json_null")]
    pub request_id: Value,
    #[serde(rename = "status", default = "json_null")]
    pub status: Value,
    #[serde(rename = "statusdescription", default = "json_null")]
    pub status_description: Value,
    #[serde(rename = "trans-id", default = "json_null")]
    pub transaction_id: Value,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct MMAuthenticateResponse {
    #[serde(rename = "token", default = "json_null")]
    pub token: Value,
    #[serde(rename = "status", default = "json_null")]
    pub status: Value,
    #[serde(rename = "statusdescription", default = "json_null")]
    pub status_description: Value,
}


impl MovIvyMobileMoney {
    pub fn new() -> MovIvyMobileMoney {
        let url = match env::var("MOVIVY_MOBILE_MONEY_URL") {
            Ok(url) => {
                log::info!(
                    "The MOVIVY_MOBILE_MONEY_URL environment variable is set to: {}",
                    url
                );
                url
            }
            Err(_e) => {
                log::warn!("The MOVIVY_MOBILE_MONEY_URL environment variable was not set, defaulting to 'https://0.0.0.0'");
                "https://0.0.0.0".to_string()
            }
        };

        let login_url: String = format!("{}/api/gateway/3pp/request/token", url);
        let transaction_url: String = format!("{}/api/gateway/3pp/transaction/process", url);

        log::info!("MAS uses MovIvy Mobile Money at: {}", url);

        let use_ssl_cert = match env::var("MOVIVY_MOBILE_MONEY_USE_SSL_CERTIFICATE") {
            Err(_e) => false,
            Ok(movivy_use_ssl_certificate) => {
                movivy_use_ssl_certificate == "TRUE"
            }
        };

        if use_ssl_cert == true {
            let ssl_cert_path = env::var("MOVIVY_MOBILE_MONEY_SSL_CERT_PATH")
                .unwrap_or_else(|err| { 
                    log::error!("{}. SSL certificate path not provided (set the MOVIVY_MOBILE_MONEY_SSL_CERT_PATH env variable)", err); 
                    std::process::exit(1); 
                });
            let ssl_password = env::var("MOVIVY_MOBILE_MONEY_SSL_PASSWORD")
                .unwrap_or_else(|err| { 
                    log::error!("{}. SSL certificate password not provided (set the MOVIVY_MOBILE_MONEY_SSL_PASSWORD env variable)", err); 
                    std::process::exit(1); 
                });

            let mut buf = Vec::new();
            File::open(ssl_cert_path)
                .unwrap()
                .read_to_end(&mut buf)
                .unwrap();

            // create an Identity from the PKCS#12 archive
            let pkcs12 = reqwest::Identity::from_pkcs12_der(&buf, ssl_password.as_str())
                .unwrap();

            // get a client builder
            let client = reqwest::Client::builder()
                .identity(pkcs12)
                .danger_accept_invalid_certs(true)
                .danger_accept_invalid_hostnames(true)
                .build().unwrap();

            MovIvyMobileMoney {
                //url,
                client,
                login_url,
                transaction_url,
            }
        } else {
            // get a client builder
            let client = reqwest::Client::builder()
                .danger_accept_invalid_certs(true)
                .danger_accept_invalid_hostnames(true)
                .build().unwrap();

            MovIvyMobileMoney {
                //url,
                client,
                login_url,
                transaction_url,
            }
        }
    }

    pub async fn login
        (&self, username: String, password: String) 
        -> Result<MMAuthStatus, MovIvyMobileMoneyError>{

        let auth_string = format!("{}:{}", username.clone(), password.clone());
        let encoded_auth_string = general_purpose::STANDARD.encode(&auth_string);
        let auth_header_value = format!("Basic {}", encoded_auth_string);
        
        let response = self.client
            .get(format!("{}", &self.login_url))
            .header(reqwest::header::AUTHORIZATION, auth_header_value)
            .header("command-id", String::from("gettoken"))
            .send()
            .await?
            .json::<MMAuthenticateResponse>()
            .await?;

        
        let token = response
            .token
            .as_str()
            .ok_or(MovIvyMobileMoneyError {
                description: format!("Mobile Money login failed. MovIvyMobileMoney returned ({}:{})'", 
                response.status.as_u64().unwrap_or_default(),
                response.status_description),
                error_type: MovIvyMobileMoneyErrorType::Unauthorized,
            })?;

        Ok(MMAuthStatus::Authenticated(token.to_string()))

    }

    pub async fn cash_transfer(
        &self,
        amount: String, 
        destination_msisdn: String,
        login_token: String)
        -> Result<MMTransactionStatus, MovIvyMobileMoneyError>{
        let auth_header_value = format!("Bearer {}", login_token); 
        let request_id = format!("{}", Uuid::new_v4());

        let amount_json = match serde_json::Number::from_str(&amount){
            Ok(num) => Value::Number(num),
            Err(_err)=> return Err(MovIvyMobileMoneyError {
                description: format!("Cash Transfer failed: Invalid amount value'"),
                error_type: MovIvyMobileMoneyErrorType::Internal,
            })
        };

        let response = self.client
            .post(format!("{}", &self.transaction_url))
            .header(reqwest::header::AUTHORIZATION, auth_header_value)
            .header("command-id", String::from("transfer-api-transaction"))
            .json::<MMCashTransferRequest>(&MMCashTransferRequest{
                request_id: request_id.clone().into(),
                destination: destination_msisdn.clone().into(),
                amount: amount_json,
                remarks: format!("Transferring").into(),    // The MM system doesn't accept spaces in this parameter's value
                extended_data: json!({
                    "ext2":"Test".to_string(),
                    "custommessage":format!("Transferred {} CFA to {}",
                    amount.clone(), destination_msisdn.clone()),
                }),
            })
            .send()
            .await?
            .json::<MMCashTransferResponse>()
            .await?;

        let status = response
            .status
            .as_str()
            .ok_or(MovIvyMobileMoneyError {
                description: format!("Cash Transfer failed. MovIvyMobileMoney returned ({}:{})'", 
                response.status.as_u64().unwrap_or_default(),
                response.status_description),
                error_type: MovIvyMobileMoneyErrorType::Unauthorized,
            })?;

        //Ok(MMTransactionStatus::Success(status.to_string()))
        match status{
            "0" => Ok(MMTransactionStatus::Success(status.to_string())),
            "15" => Ok(MMTransactionStatus::Unknown(status.to_string(), response.status_description.to_string())),
            _ => Err(MovIvyMobileMoneyError {
                description: format!("Cash Transfer failed. MovIvyMobileMoney returned ({}:{})'", 
                status.clone(),
                response.status_description),
                error_type: MovIvyMobileMoneyErrorType::RequestError,
            })
        }


        
    }
}

