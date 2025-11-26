use core::fmt;

use rsa::{pkcs8::DecodePublicKey, PaddingScheme, PublicKey, RsaPublicKey};
use std::{collections::HashMap, env, error::Error};

use log::{debug, info};

use reqwest::Client;

use base64::{engine::general_purpose, Engine as _};
use serde_derive::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Debug, Clone)]
pub enum CrediverseErrorType {
    Internal,
    RequestError,
    HttpError(String),
    Unauthorized,
}
#[derive(Debug)]
pub struct CrediverseError {
    pub description: String,
    pub error_type: CrediverseErrorType,
}

impl fmt::Display for CrediverseError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Crediverse Error: {:?}", self.description.to_string())
    }
}

impl Error for CrediverseError {}

#[derive(Debug, PartialEq, Eq)]
pub enum GetKeyStatus {
    PinEncryptionKey(String),
    #[allow(dead_code)]
    GetKeyFailed(String),
}

pub enum SubmitSecretStatus {
    Authenticated,
    RequireOtp,
}

#[derive(Eq, PartialEq, Debug)]
pub enum AuthStatus {
    #[allow(dead_code)]
    AuthenticationFailed(String),
    #[allow(dead_code)]
    Authenticated(String, String),
    #[allow(dead_code)]
    CredentialsRequired(String),
    #[allow(dead_code)]
    PinRequired(String),
    #[allow(dead_code)]
    OtpRequired(String),
}

fn encrypt_pin(pin: String, rsa_public_key_b64: String) -> Result<Vec<u8>, CrediverseError> {
    let padding = PaddingScheme::new_pkcs1v15_encrypt();
    let mut rng = rand::thread_rng();

    let decoded_pem: Vec<u8> = general_purpose::STANDARD
        .decode(rsa_public_key_b64)
        .map_err(|err| CrediverseError {
            description: format!("could not decode pem: {}", err),
            error_type: CrediverseErrorType::Internal,
        })?; // .expect("could not decode the key from base 64");

    let public_key =
        RsaPublicKey::from_public_key_der(&decoded_pem).map_err(|err| CrediverseError {
            description: format!("could not decode the key from base 64: {}", err),
            error_type: CrediverseErrorType::Internal,
        })?; // .expect("");

    let pin_bytes = pin.as_bytes();

    public_key
        .encrypt(&mut rng, padding, pin_bytes)
        .map_err(|err| CrediverseError {
            description: format!("could encrypt with the key: {}", err),
            error_type: CrediverseErrorType::Internal,
        })
}

async fn check_http_response<T: for<'de> serde::Deserialize<'de>>(
    response: reqwest::Response,
) -> Result<T, CrediverseError> {
    match response.status() {
        reqwest::StatusCode::OK => response.json::<T>().await.map_err(|err| CrediverseError {
            description: format!("could not parse response: {}", err),
            error_type: CrediverseErrorType::Internal,
        }),
        reqwest::StatusCode::UNAUTHORIZED => Err(CrediverseError {
            description: format!(
                "UNAUTHORIZED. HTTP status {} returned from Crediverse",
                response.status()
            ),
            error_type: CrediverseErrorType::Unauthorized,
        }),

        unexpected_http_status => Err(CrediverseError {
            description: format!(
                "HTTP status {} returned from Crediverse unexpectedly",
                unexpected_http_status
            ),
            error_type: CrediverseErrorType::HttpError(unexpected_http_status.to_string()),
        }),
    }
}
impl From<reqwest::Error> for CrediverseError {
    fn from(error: reqwest::Error) -> Self {
        CrediverseError {
            description: error.to_string(),
            error_type: CrediverseErrorType::RequestError,
        }
    }
}

pub struct Crediverse {
    crediverse_client: Client,
    url: String,
}

impl Crediverse {
    pub fn new() -> Crediverse {
        let crediverse_url = match env::var("MAS_CREDIVERSE_URL") {
            Ok(crediverse_url) => {
                log::info!(
                    "The MAS_CREDIVERSE_URL environment variable is set to: {}",
                    crediverse_url
                );
                crediverse_url
            }
            Err(_e) => {
                log::warn!("The MAS_CREDIVERSE_URL environment variable was not set, defaulting to 'http://0.0.0.0:14400'");
                "http://0.0.0.0:14400".to_string()
            }
        };

        log::info!("MAS uses Crediverse at: {}", crediverse_url);

        Crediverse {
            url: crediverse_url,
            crediverse_client: reqwest::Client::new(),
        }
    }

    pub async fn format_msisdn(
        &self,
        session_id: String,
        msisdn: String,
    ) -> Result<FormatMsisdnResponse, CrediverseError> {
        let response = self
            .crediverse_client
            .get(format!("{}/ecds/msisdn/format/{}", &self.url, msisdn))
            .header("CS_SID", session_id.to_string())
            .send()
            .await?;

        check_http_response::<FormatMsisdnResponse>(response).await
    }

    pub async fn change_pin(
        &self,
        session_id: String,
        pin: String,
    ) -> Result<TransactResponse, CrediverseError> {
        let request = ChangePinRequest {
            session_id: session_id.clone().into(),
            inbound_session_id: session_id.clone().into(),
            inbound_transaction_id: "".into(),
            mode: "N".into(),
            version: "1".into(),
            new_pin: pin.clone().into(),
        };

        let response = self
            .crediverse_client
            .post(format!("{}/ecds/transactions/change_pin", &self.url))
            .header("CS_SID", session_id.to_string())
            .json::<ChangePinRequest>(&request)
            .send()
            .await?;

        let response = check_http_response::<TransactResponse>(response).await?;

        log::debug!("Crediverse response {:?}", response);

        let return_code = response.return_code.as_str().unwrap_or("ERROR");

        if let "SUCCESS" = return_code {
            log::info!(
                "crediverse returned response code {}",
                response.return_code.to_string().as_str()
            );
            Ok(response)
        } else if let "UNAUTHORIZED" = return_code {
            //
            // Crediverse returns code 200 despite UNATHORIZED when performing change_pin
            //
            log::error!(
                "crediverse returned response code {}",
                response.return_code.to_string().as_str()
            );
            Err(CrediverseError {
                description: "UNAUTHORIZED return_code for change_pin from Crediverse".to_string(),
                error_type: CrediverseErrorType::Unauthorized,
            })
        } else {
            log::error!(
                "crediverse returned response code {}",
                response.return_code.to_string().as_str()
            );
            Err(CrediverseError {
                description: "A technical CrediverseError occured".to_string(),
                error_type: CrediverseErrorType::Internal,
            })
        }

        //match response.return_code
        //log::debug!("credverse change pin response = {:?}", response);
    }
    #[allow(clippy::too_many_arguments)]
    pub async fn update_profile(
        &self,
        session_id: String,
        title: String,
        language: String,
        first_name: String,
        surname: String,
        email: String,
        agent_id: String,
        company_id: u32,
    ) -> Result<TransactResponse, CrediverseError> {
        let request = UpdateProfileRequest {
            title: title.clone().into(),
            language: language.clone().into(),
            first_name: first_name.clone().into(),
            surname: surname.clone().into(),
            email: email.clone().into(),
            agent_id: agent_id.clone().into(),
            company_id: company_id.into(),
        };

        let response = self
            .crediverse_client
            .put(format!("{}/ecds/agents/profile/partial", &self.url))
            .header("CS_SID", session_id.to_string())
            .json::<UpdateProfileRequest>(&request)
            .send()
            .await?;

        if response.status() == reqwest::StatusCode::NO_CONTENT {
            return Ok(TransactResponse::default());
        }

        let response = check_http_response::<TransactResponse>(response).await?;
        Ok(response)
    }

    pub async fn transfer(
        &self,
        session_id: String,
        target_msisdn: String,
        amount: String,
        latitude: Option<f64>,
        longitude: Option<f64>,
    ) -> Result<TransactResponse, CrediverseError> {
        let request = TransferRequest {
            session_id: session_id.clone().into(),
            target_msisdn: target_msisdn.into(),
            amount: amount.into(),
            latitude: latitude.into(),
            longitude: longitude.into(),
            mode: "N".into(),
            co_signatory_session_id: Value::Null,
            co_signatory_transaction_id: Value::Null,
            co_signatory_otp: Value::Null,
        };

        let response = self
            .crediverse_client
            .post(format!("{}/ecds/transactions/transfer", &self.url))
            .header("CS_SID", session_id.to_string())
            .json::<TransferRequest>(&request)
            .send()
            .await?;

        check_http_response::<TransactResponse>(response).await
    }

    pub async fn sell_airtime(
        &self,
        session_id: String,
        target_msisdn: String,
        amount: String,
        latitude: Option<f64>,
        longitude: Option<f64>,
    ) -> Result<TransactResponse, CrediverseError> {
        let request = SellAirtimeRequest {
            target_msisdn: target_msisdn.into(),
            amount: amount.into(),
            latitude: latitude.into(),
            longitude: longitude.into(),
            session_id: session_id.clone().into(),
            inbound_session_id: session_id.clone().into(),
            inbound_transaction_id: "".into(),
        };

        let response = self
            .crediverse_client
            .post(format!("{}/ecds/transactions/sell", &self.url))
            .header("CS_SID", session_id.to_string())
            .json::<SellAirtimeRequest>(&request)
            .send()
            .await?;

        check_http_response::<TransactResponse>(response).await
    }

    pub async fn get_tier_name(
        &self,
        session_id: String,
        tier_id: String,
    ) -> Result<String, CrediverseError> {
        let response = self
            .crediverse_client
            .get(format!("{}/ecds/tiers/{}", &self.url, tier_id))
            .header("CS_SID", session_id.to_string())
            .send()
            .await?;

        let tier_response = check_http_response::<GetTierResponse>(response).await?;
        Ok(tier_response.name.as_str().unwrap_or("Unknown").to_string())
    }

    pub async fn _get_account_info(
        &self,
        session_id: String,
        agent_id: String,
    ) -> Result<GetAccountInfoResponse, CrediverseError> {
        let response = self
            .crediverse_client
            .get(format!("{}/ecds/accounts/{}", &self.url, agent_id))
            .header("CS_SID", session_id.to_string())
            .send()
            .await?;

        info!("AccountInfoResponse:{:?}", response);

        check_http_response::<GetAccountInfoResponse>(response).await
    }

    pub async fn get_agent_info(
        &self,
        session_id: String,
    ) -> Result<GetAgentInfoResponse, CrediverseError> {
        let session = self.get_session(session_id).await?;

        let response = self
            .crediverse_client
            .get(format!("{}/ecds/agents/{}", &self.url, session.agent_id))
            .header("CS_SID", session.session_id.to_string())
            .send()
            .await?;

        check_http_response::<GetAgentInfoResponse>(response).await
    }

    pub async fn get_session(
        &self,
        session_id: String,
    ) -> Result<GetSessionResponse, CrediverseError> {
        let response = self
            .crediverse_client
            .get(format!("{}/ecds/sessions/{}", &self.url, session_id))
            .send()
            .await?;

        check_http_response::<GetSessionResponse>(response).await
    }

    pub async fn login_as_api_user(
        &self,
        username: String,
        password: String,
    ) -> Result<AuthStatus, CrediverseError> {
        let session_id = self.get_mobile_money_agent_session_id().await?;

        log::trace!("logging in as api user with {}", username);

        self.submit_user_name_and_password(session_id.to_string(), username, password)
            .await
    }

    pub async fn _login(&self, msisdn: String, pin: String) -> Result<AuthStatus, CrediverseError> {
        let session_id = self.get_session_id().await?;

        match self.get_key(session_id.to_string(), msisdn).await? {
            GetKeyStatus::PinEncryptionKey(key) => {
                match self
                    .submit_pin(session_id.to_string(), key.clone(), pin)
                    .await?
                {
                    SubmitSecretStatus::Authenticated => Ok(AuthStatus::Authenticated(
                        session_id.to_string(),
                        key.to_string(),
                    )),
                    SubmitSecretStatus::RequireOtp => {
                        Ok(AuthStatus::OtpRequired(session_id.to_string()))
                    }
                }
            }
            GetKeyStatus::GetKeyFailed(e) => Err(CrediverseError {
                description: e,
                error_type: CrediverseErrorType::Internal,
            }),
        }
    }

    fn create_submit_otp_request(&self, session_id: String, otp: String) -> AuthenticateRequest {
        let mut request = self.create_authenicate_request_template();
        request.session_id = session_id.into();
        request.data = general_purpose::STANDARD.encode(otp).into();
        request.channel = "P".into();
        request
    }

    fn create_submit_pin_request(
        &self,
        session_id: String,
        rsa_public_key_b64: String,
        pin: String,
    ) -> Result<AuthenticateRequest, CrediverseError> {
        let mut request = self.create_authenicate_request_template();
        request.channel = "P".into();

        request.session_id = session_id.into();
        let encrypted_pin: Vec<u8> = encrypt_pin(pin, rsa_public_key_b64)?;
        let encripted_pin_b64 = general_purpose::STANDARD.encode(encrypted_pin).into();
        request.data = encripted_pin_b64;
        Ok(request)
    }

    fn create_get_session_request(&self) -> AuthenticateRequest {
        self.create_authenicate_request_template()
    }

    fn create_get_key_request(&self, session_id: String, msisdn: String) -> AuthenticateRequest {
        let mut request = self.create_authenicate_request_template();
        request.channel = "P".into();
        request.data = general_purpose::STANDARD.encode(msisdn).into();
        request.session_id = session_id.into();
        request
    }

    fn create_agent_get_key_request(
        &self,
        session_id: String,
        msisdn: String,
    ) -> AuthenticateRequest {
        let mut request = self.create_get_key_request(session_id, msisdn);
        request.channel = "P".into();
        request
    }

    pub async fn submit_otp(
        &self,
        session_id: String,
        otp: String,
    ) -> Result<String, CrediverseError> {
        let request = self.create_submit_otp_request(session_id, otp);

        let response = self
            .crediverse_client
            .post(format!("{}/ecds/authentication/authenticate", &self.url))
            .json::<AuthenticateRequest>(&request)
            .send()
            .await?;

        let response = response.json::<AuthenticateResponse>().await?;

        debug!("submit_otp response = {:?}", response);

        Ok(response
            .key1
            .as_str()
            .ok_or(CrediverseError {
                description: "submit_otp: The crediverse response did not contain a 'key1' value"
                    .to_string(),
                error_type: CrediverseErrorType::Internal,
            })?
            .to_string())
    }

    pub async fn submit_pin(
        &self,
        session_id: String,
        rsa_public_key_b64: String,
        pin: String,
    ) -> Result<SubmitSecretStatus, CrediverseError> {
        let response = self
            .crediverse_client
            .post(format!("{}/ecds/authentication/authenticate", &self.url))
            .json::<AuthenticateRequest>(&self.create_submit_pin_request(
                session_id,
                rsa_public_key_b64,
                pin,
            )?)
            .send()
            .await?
            .json::<AuthenticateResponse>()
            .await?
            .return_code;

        let return_code = response.as_str().ok_or(CrediverseError {
            description:
                "submit_pin: The crediverse response did not contain a 'return_code' value"
                    .to_string(),
            error_type: CrediverseErrorType::Internal,
        })?;

        match return_code {
            "AUTHENTICATED" => Ok(SubmitSecretStatus::Authenticated),
            "REQUIRE_UTF8_OTP" => Ok(SubmitSecretStatus::RequireOtp),
            _ => Err(CrediverseError {
                description: return_code.to_string(),
                error_type: CrediverseErrorType::Unauthorized,
            }),
        }
    }

    pub async fn submit_user_name_and_password(
        &self,
        session_id: String,
        username: String,
        password: String,
    ) -> Result<AuthStatus, CrediverseError> {
        log::trace!("Submitting username and password for mombile money user ");

        let mut request = self.create_authenicate_request_template();

        request.username = username.as_str().into();
        request.password = password.as_str().into();

        request.channel = "A".into();
        request.session_id = session_id.clone().into();

        log::trace!(
            "submit_user_name_and_password Request for mombile money user {:?}",
            request
        );

        let raw_authentitcate_response = self
            .crediverse_client
            .post(format!("{}/ecds/authentication/auth", &self.url))
            .json::<AuthenticateRequest>(&request)
            .send()
            .await?;

        log::trace!(
            "Raw submit_user_name_and_password response for mombile money user {:?}",
            raw_authentitcate_response
        );

        let authentitcate_response = raw_authentitcate_response
            .json::<AuthenticateResponse>()
            .await?;

        log::trace!(
            "submit_user_name_and_password response for mombile money user {:?}",
            authentitcate_response
        );

        let return_code = authentitcate_response
            .return_code
            .as_str()
            .ok_or(CrediverseError {
                description:
                    "submit_user_name_and_password: The crediverse response did not contain a 'return_code' value"
                        .to_string(),
                error_type: CrediverseErrorType::Internal,
            })?;

        let additional_information = authentitcate_response
            .additional_information
            .as_str()
            .unwrap_or("No additional_information available");

        match return_code {
            "AUTHENTICATED" => Ok(AuthStatus::Authenticated(session_id, "".into())),
            unexpected_code => Err(CrediverseError {
                description: format!(
                    "submit_user_name_and_password: Crediverse returned 'return_code' ({} , {} ) instead of 'AUTHENTICATED'",
                    unexpected_code,
                    additional_information
                ),
                error_type: CrediverseErrorType::Internal,
            }),
        }
    }

    pub async fn get_key(
        &self,
        session_id: String,
        msisdn: String,
    ) -> Result<GetKeyStatus, CrediverseError> {
        let key_request = self.create_agent_get_key_request(session_id, msisdn);

        let authentitcate_response = self
            .crediverse_client
            .post(format!("{}/ecds/authentication/authenticate", &self.url))
            .json::<AuthenticateRequest>(&key_request)
            .send()
            .await?
            .json::<AuthenticateResponse>()
            .await?;

        let key = authentitcate_response
            .key1
            .as_str()
            .ok_or(CrediverseError {
                description: "get_key:  The crediverse response did not contain a 'key1' value"
                    .to_string(),
                error_type: CrediverseErrorType::Internal,
            })?;

        let return_code = authentitcate_response
            .return_code
            .as_str()
            .ok_or(CrediverseError {
                description:
                    "get_key: The crediverse response did not contain a 'return_code' value"
                        .to_string(),
                error_type: CrediverseErrorType::Internal,
            })?;

        match return_code {
            "REQUIRE_RSA_PIN" => Ok(GetKeyStatus::PinEncryptionKey(key.to_string())),
            unexpected_code => Err(CrediverseError {
                description: format!(
                    "get_key: Crediverse returned 'return_code' ({}) instead of 'REQUIRE_RSA_PIN' or 'REQUIRE_RSA_PASSWORD'",
                    unexpected_code
                ),
                error_type: CrediverseErrorType::Internal,
            }),
        }
    }

    pub async fn get_mobile_money_agent_session_id(&self) -> Result<String, CrediverseError> {
        let mut request = self.create_get_session_request();
        request.channel = "A".into();

        log::trace!("get_mobile_money_agent_session_id request: {:?}", request);

        let session = self
            .crediverse_client
            .post(format!("{}/ecds/authentication/auth", &self.url))
            .json::<AuthenticateRequest>(&request)
            .send()
            .await?
            .json::<AuthenticateResponse>()
            .await?;

        Ok(session
            .session_id
            .as_str()
            .ok_or(CrediverseError{
                description: "get_session_id: The response from crediverse did not contain a 'session_id' value"
                    .to_string(),
                error_type: CrediverseErrorType::Internal,
            })?
            .to_string())
    }

    pub async fn get_session_id(&self) -> Result<String, CrediverseError> {
        let mut request = self.create_get_session_request();
        request.channel = "P".into();

        let session = self
            .crediverse_client
            .post(format!("{}/ecds/authentication/authenticate", &self.url))
            .json::<AuthenticateRequest>(&self.create_get_session_request())
            .send()
            .await?
            .json::<AuthenticateResponse>()
            .await?;

        Ok(session
            .session_id
            .as_str()
            .ok_or(CrediverseError{
                description: "get_session_id: The response from crediverse did not contain a 'session_id' value"
                    .to_string(),
                error_type: CrediverseErrorType::Internal,
            })?
            .to_string())
    }

    fn create_authenicate_request_template(&self) -> AuthenticateRequest {
        AuthenticateRequest {
            session_id: serde_json::Value::Null,
            inbound_transaction_id: serde_json::Value::Null,
            inbound_session_id: serde_json::Value::Null,
            version: "1".into(),
            mode: "N".into(),
            company_id: 2.into(),
            channel: "P".into(),
            host_name: serde_json::Value::Null,
            mac_address: serde_json::Value::Null,
            ip_address: serde_json::Value::Null,
            data: serde_json::Value::Null,
            username: serde_json::Value::Null,
            password: serde_json::Value::Null,
            one_time_pin: serde_json::Value::Null,
            user_type: "AGENT".into(),
            co_sign_for_session_id: serde_json::Value::Null,
            co_signatory_transaction_id: serde_json::Value::Null,
            custom_pin_change_message: serde_json::Value::Null,
        }
    }
}

#[derive(Serialize, Deserialize, Debug)]
struct JSONResponse {
    json: HashMap<String, Value>,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Transaction {
    pub id: Value,
    #[serde(rename = "companyID")]
    pub company_id: Value,
    pub version: Value,
    #[serde(rename = "type")]
    pub type_field: Value,
    pub number: Value,
    #[serde(rename = "reversedID")]
    pub reversed_id: Value,
    pub amount: Value,
    pub buyer_trade_bonus_amount: Value,
    pub buyer_trade_bonus_provision: Value,
    pub buyer_trade_bonus_percentage: Value,
    pub charge_levied: Value,
    pub hostname: Value,
    pub start_time: Value,
    pub end_time: Value,
    pub channel: Value,
    pub channel_type: Value,
    #[serde(rename = "callerID")]
    pub caller_id: Value,
    #[serde(rename = "inboundTransactionID")]
    pub inbound_transaction_id: Value,
    #[serde(rename = "inboundSessionID")]
    pub inbound_session_id: Value,
    pub request_mode: Value,
    #[serde(rename = "transferRuleID")]
    pub transfer_rule_id: Value,
    #[serde(rename = "a_AgentID")]
    pub a_agent_id: Value,
    #[serde(rename = "a_MSISDN")]
    pub a_msisdn: Value,
    #[serde(rename = "a_TierID")]
    pub a_tier_id: Value,
    #[serde(rename = "a_ServiceClassID")]
    pub a_service_class_id: Value,
    #[serde(rename = "a_GroupID")]
    pub a_group_id: Value,
    #[serde(rename = "a_AreaID")]
    pub a_area_id: Value,
    #[serde(rename = "a_OwnerAgentID")]
    pub a_owner_agent_id: Value,
    #[serde(rename = "a_IMSI")]
    pub a_imsi: Value,
    #[serde(rename = "a_IMEI")]
    pub a_imei: Value,
    #[serde(rename = "a_CellID")]
    pub a_cell_id: Value,
    #[serde(rename = "a_CellGroupID")]
    pub a_cell_group_id: Value,
    #[serde(rename = "a_BalanceBefore")]
    pub a_balance_before: Value,
    #[serde(rename = "a_BalanceAfter")]
    pub a_balance_after: Value,
    #[serde(rename = "a_BonusBalanceBefore")]
    pub a_bonus_balance_before: Value,
    #[serde(rename = "a_BonusBalanceAfter")]
    pub a_bonus_balance_after: Value,
    #[serde(rename = "a_OnHoldBalanceBefore")]
    pub a_on_hold_balance_before: Value,
    #[serde(rename = "a_OnHoldBalanceAfter")]
    pub a_on_hold_balance_after: Value,
    #[serde(rename = "b_AgentID")]
    pub b_agent_id: Value,
    #[serde(rename = "b_MSISDN")]
    pub b_msisdn: Value,
    #[serde(rename = "b_TierID")]
    pub b_tier_id: Value,
    #[serde(rename = "b_ServiceClassID")]
    pub b_service_class_id: Value,
    #[serde(rename = "b_GroupID")]
    pub b_group_id: Value,
    #[serde(rename = "b_AreaID")]
    pub b_area_id: Value,
    #[serde(rename = "b_OwnerAgentID")]
    pub b_owner_agent_id: Value,
    #[serde(rename = "b_IMSI")]
    pub b_imsi: Value,
    #[serde(rename = "b_IMEI")]
    pub b_imei: Value,
    #[serde(rename = "b_CellID")]
    pub b_cell_id: Value,
    #[serde(rename = "b_CellGroupID")]
    pub b_cell_group_id: Value,
    #[serde(rename = "b_TransferBonusAmount")]
    pub b_transfer_bonus_amount: Value,
    #[serde(rename = "b_TransferBonusProfile")]
    pub b_transfer_bonus_profile: Value,
    #[serde(rename = "b_BalanceBefore")]
    pub b_balance_before: Value,
    #[serde(rename = "b_BalanceAfter")]
    pub b_balance_after: Value,
    #[serde(rename = "b_BonusBalanceBefore")]
    pub b_bonus_balance_before: Value,
    #[serde(rename = "b_BonusBalanceAfter")]
    pub b_bonus_balance_after: Value,
    #[serde(rename = "requesterMSISDN")]
    pub requester_msisdn: Value,
    pub requester_type: Value,
    #[serde(rename = "bundleID")]
    pub bundle_id: Value,
    #[serde(rename = "promotionID")]
    pub promotion_id: Value,
    pub return_code: Value,
    pub last_external_result_code: Value,
    pub rolled_back: Value,
    pub follow_up: Value,
    pub additional_information: Value,
    #[serde(rename = "a_FirstName")]
    pub a_first_name: Value,
    #[serde(rename = "a_Surname")]
    pub a_surname: Value,
    #[serde(rename = "a_TierName")]
    pub a_tier_name: Value,
    #[serde(rename = "a_TierType")]
    pub a_tier_type: Value,
    #[serde(rename = "a_GroupName")]
    pub a_group_name: Value,
    #[serde(rename = "a_AreaName")]
    pub a_area_name: Value,
    #[serde(rename = "a_AreaType")]
    pub a_area_type: Value,
    #[serde(rename = "a_OwnerFirstName")]
    pub a_owner_first_name: Value,
    #[serde(rename = "a_OwnerSurname")]
    pub a_owner_surname: Value,
    #[serde(rename = "a_CellGroupCode")]
    pub a_cell_group_code: Value,
    #[serde(rename = "b_FirstName")]
    pub b_first_name: Value,
    #[serde(rename = "b_Surname")]
    pub b_surname: Value,
    #[serde(rename = "b_TierName")]
    pub b_tier_name: Value,
    #[serde(rename = "b_TierType")]
    pub b_tier_type: Value,
    #[serde(rename = "b_GroupName")]
    pub b_group_name: Value,
    #[serde(rename = "b_AreaName")]
    pub b_area_name: Value,
    #[serde(rename = "b_AreaType")]
    pub b_area_type: Value,
    #[serde(rename = "b_OwnerFirstName")]
    pub b_owner_first_name: Value,
    #[serde(rename = "b_OwnerSurname")]
    pub b_owner_surname: Value,
    #[serde(rename = "b_CellGroupCode")]
    pub b_cell_group_code: Value,
    pub dedicated_account_refill_infos: Value,
    pub dedicated_account_reverse_info: Value,
    pub dedicated_account_current_balance_infos: Value,
    pub main_account_current_balance: Value,
    pub balance_and_date_failed: Value,
    pub non_airtime_item_description: Value,
    pub acgi: Value,
    pub bcgi: Value,
    pub dabonus_reversal_enabled: Value,
    pub next_dedicated_account_refill_infos: Value,
    pub has_next_dedicated_account_refill_infos: Value,
    pub dedicated_account_reverse_info_string: Value,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct GetTransactionsResponse {
    pub found_rows: Value,
    pub instances: Vec<Transaction>,
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FormatMsisdnResponse {
    pub msisdn: Value,
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ChangePinRequest {
    #[serde(rename = "sessionID")]
    pub session_id: Value,
    #[serde(rename = "inboundTransactionID")]
    pub inbound_transaction_id: Value,
    #[serde(rename = "inboundSessionID")]
    pub inbound_session_id: Value,
    pub mode: Value,
    pub version: Value,
    #[serde(rename = "newPin")]
    pub new_pin: Value,
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UpdateProfileRequest {
    #[serde(rename = "title")]
    pub title: Value,
    #[serde(rename = "language")]
    pub language: Value,
    #[serde(rename = "firstName")]
    pub first_name: Value,
    #[serde(rename = "surname")]
    pub surname: Value,
    #[serde(rename = "email")]
    pub email: Value,
    #[serde(rename = "id")]
    pub agent_id: Value,
    #[serde(rename = "companyID")]
    pub company_id: Value,
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TransferRequest {
    #[serde(rename = "sessionID")]
    pub session_id: Value,
    #[serde(rename = "coSignatorySessionID")]
    pub co_signatory_session_id: Value,
    #[serde(rename = "coSignatoryTransactionID")]
    pub co_signatory_transaction_id: Value,
    pub mode: Value,
    #[serde(rename = "coSignatoryOTP")]
    pub co_signatory_otp: Value,
    #[serde(rename = "targetMSISDN")]
    pub target_msisdn: Value,
    #[serde(rename = "latitude")]
    pub latitude: Value,
    #[serde(rename = "longitude")]
    pub longitude: Value,
    pub amount: Value,
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SellAirtimeRequest {
    #[serde(rename = "targetMSISDN")]
    pub target_msisdn: Value,
    pub amount: Value,
    #[serde(rename = "sessionID")]
    pub session_id: Value,
    #[serde(rename = "inboundTransactionID")]
    pub inbound_transaction_id: Value,
    #[serde(rename = "latitude")]
    pub latitude: Value,
    #[serde(rename = "longitude")]
    pub longitude: Value,
    #[serde(rename = "inboundSessionID")]
    pub inbound_session_id: Value, //pub version : "VERSION_CURRENT",
                                   //pub mode = : "MODE_NORMAL",
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TransactResponse {
    pub charge: Value,
    pub response: Value,
    pub transaction_number: Value,
    #[serde(rename = "inboundTransactionID")]
    pub inbound_transaction_id: Value,
    #[serde(rename = "inboundSessionID")]
    pub inbound_session_id: Value,
    pub return_code: Value,
    pub additional_information: Value,
    pub follow_up: Value,
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct GetSessionResponse {
    #[serde(rename = "sessionID")]
    pub session_id: String,

    #[serde(rename = "webUserID")]
    pub web_user_id: Value,

    #[serde(rename = "agentID")]
    pub agent_id: i64,

    #[serde(rename = "agentUserID")]
    pub agent_user_id: Value,

    #[serde(rename = "companyID")]
    pub company_id: Value,

    pub expiry_time: Value,

    pub domain_account_name: String,

    #[serde(rename = "languageID")]
    pub language_id: String,

    #[serde(rename = "countryID")]
    pub country_id: String,

    pub company_prefix: String,

    #[serde(rename = "ownerAgentID")]
    pub owner_agent_id: Value,
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct GetTierResponse {
    pub id: Value,
    #[serde(rename = "companyID")]
    pub company_id: Value,
    pub version: Value,
    pub name: Value,
    #[serde(rename = "type")]
    pub tier_type: Value,
    pub description: Value,
    pub permanent: Value,
    pub state: Value,
    pub down_stream_percentage: Value,
    pub allow_intra_tier_transfer: Value,
    pub max_transaction_amount: Value,
    pub max_daily_count: Value,
    pub max_daily_amount: Value,
    pub max_monthly_count: Value,
    pub max_monthly_amount: Value,
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct GetAccountInfoResponse {
    pub id: Value,
    #[serde(rename = "agentID")]
    pub agent_id: Value,
    pub version: Value,
    pub balance: Value,
    pub bonus_balance: Value,
    pub on_hold_balance: Value,
    pub tampered_with: Value,
    pub day: Value,
    pub day_count: Value,
    pub day_total: Value,
    pub month_count: Value,
    pub month_total: Value,
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct GetAgentInfoResponse {
    pub id: i64,
    #[serde(rename = "companyID")]
    pub company_id: Value,
    pub version: Value,
    pub account_number: Value,
    pub mobile_number: Value,
    pub imei: Value,
    pub last_imei_update: Value,
    pub imsi: Value,
    pub last_imsi_change: Value,
    pub title: Value,
    pub first_name: Value,
    pub initials: Value,
    pub surname: Value,
    pub language: Value,
    pub domain_account_name: Value,
    pub gender: Value,
    pub date_of_birth: Value,
    pub street_address_line1: Value,
    pub street_address_line2: Value,
    pub street_address_suburb: Value,
    pub street_address_city: Value,
    pub street_address_zip: Value,
    pub postal_address_line1: Value,
    pub postal_address_line2: Value,
    pub postal_address_suburb: Value,
    pub postal_address_city: Value,
    pub postal_address_zip: Value,
    pub alt_phone_number: Value,
    pub email: Value,
    #[serde(rename = "tierID")]
    pub tier_id: Value,
    #[serde(rename = "groupID")]
    pub group_id: Value,
    #[serde(rename = "areaID")]
    pub area_id: Value,
    #[serde(rename = "serviceClassID")]
    pub service_class_id: Value,
    #[serde(rename = "roleID")]
    pub role_id: Value,
    pub state: Value,
    pub activation_date: Value,
    pub deactivation_date: Value,
    pub expiration_date: Value,
    #[serde(rename = "supplierAgentID")]
    pub supplier_agent_id: Value,
    #[serde(rename = "ownerAgentID")]
    pub owner_agent_id: Value,
    pub allowed_channels: Value,
    pub warning_threshold: Value,
    pub max_transaction_amount: Value,
    pub max_daily_count: Value,
    pub max_daily_amount: Value,
    pub max_monthly_count: Value,
    pub max_monthly_amount: Value,
    pub report_count_limit: Value,
    pub report_daily_schedule_limit: Value,
    pub temporary_pin: Value,
    pub signature: Value,
    pub tampered_with: bool,
    pub imsi_locked_out: Value,
    pub pin_locked_out: Value,
    pub pin_version: Value,
    pub confirm_ussd: Value,
    pub authentication_method: Value,
    pub consecutive_auth_failures: Value,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AuthenticateRequest {
    #[serde(rename = "sessionID")]
    pub session_id: Value,
    #[serde(rename = "inboundTransactionID")]
    pub inbound_transaction_id: Value,
    #[serde(rename = "inboundSessionID")]
    pub inbound_session_id: Value,
    pub version: Value,
    pub mode: Value,
    #[serde(rename = "companyID")]
    pub company_id: Value,
    pub channel: Value,
    pub host_name: Value,
    pub mac_address: Value,
    pub ip_address: Value,
    pub data: Value,
    pub username: Value,
    pub password: Value,
    pub one_time_pin: Value,
    pub user_type: Value,
    #[serde(rename = "coSignForSessionID")]
    pub co_sign_for_session_id: Value,
    #[serde(rename = "coSignatoryTransactionID")]
    pub co_signatory_transaction_id: Value,
    pub custom_pin_change_message: Value,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct AuthenticateResponse {
    #[serde(rename = "transactionNumber", default = "json_null")]
    pub transaction_number: Value,
    #[serde(rename = "inboundTransactionID", default = "json_null")]
    pub inbound_transaction_id: Value,
    #[serde(rename = "inboundSessionID", default = "json_null")]
    pub inbound_session_id: Value,
    #[serde(rename = "returnCode", default = "json_null")]
    pub return_code: Value,
    #[serde(rename = "additionalInformation", default = "json_null")]
    pub additional_information: Value,
    #[serde(rename = "sessionID", default = "json_null")]
    pub session_id: Value,
    #[serde(rename = "moreInformationRequired", default = "json_null")]
    pub more_information_required: Value,
    #[serde(rename = "key1", default = "json_null")]
    pub key1: Value,
    #[serde(rename = "key2", default = "json_null")]
    pub key2: Value,
    #[serde(rename = "value", default = "json_null")]
    pub value: Value,
}

pub fn json_null() -> Value {
    Value::Null
}
