#[macro_use]
extern crate lazy_static;

use jsonwebtoken::{decode, Algorithm, DecodingKey, Validation};
use once_cell::sync::Lazy;
use std::sync::{Arc, Mutex};

use crate::crediverse::history::History;
use crate::crediverse::hxc_db_direct::HxcDb;
use crate::crediverse::statistics::Statistics;
use crate::{crediverse::crediverse_rest::Crediverse, mas::MasConfig};

use crate::crediverse::hxc::Hxc;
use crate::wallet_owner::WalletOwner;
use crate::mobile_money::movivy_mobile_money::{MovIvyMobileMoney, self};
use rand::{distributions::Alphanumeric, Rng}; // 0.8
use crate::build_info::build_info::CI_DOCKER_TAG;
use crate::build_info::build_info::CI_GITHUB_TAG;
use crate::build_info::build_info::CI_BRANCH_NAME;
use crate::build_info::build_info::CI_BUILD_NUMBER;
use crate::build_info::build_info::CI_BUILD_DATETIME;
use crate::build_info::build_info::CI_COMMIT_REF;
use serde_json::json;

mod local_utils;
mod wallet_owner;
mod mobile_money;
mod build_info;

pub(crate) use tonic::transport::Server;
use tonic::{
    transport::{Identity, ServerTlsConfig},
    Request, Response, Status, Code,
    metadata::MetadataValue,
    metadata::MetadataMap,
};
use tonic::metadata::KeyAndValueRef;

use std::{env, error::Error, fmt, time::SystemTime};

mod mas;
mod message_formatters;
mod message_translators;

use mas::{masapi::mas_server::MasServer, MasIpml, TokenData, VersionConfig};

mod crediverse;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Print build_info variables at the start
    init_logger();
    log::info!("CI_DOCKER_TAG: {}, CI_GITHUB_TAG: {}, CI_BRANCH_NAME: {}, CI_BUILD_NUMBER: {}, CI_BUILD_DATETIME: {}, CI_COMMIT_REF: {}",
    CI_DOCKER_TAG, CI_GITHUB_TAG, CI_BRANCH_NAME, CI_BUILD_NUMBER, CI_BUILD_DATETIME, CI_COMMIT_REF);


    run_live().await
}

fn validate_token(
    token: String,
    secret: String,
) -> Result<TokenData, Box<dyn std::error::Error + Send + Sync>> {
    let validation = Validation::new(Algorithm::HS256);

    let token_data = decode::<TokenData>(
        &token,
        &DecodingKey::from_secret(secret.as_bytes()),
        &validation,
    )?;

    Ok(token_data.claims)
}

#[derive(Debug)]
pub struct TimeSyncError {
    pub description: String,
}

impl fmt::Display for TimeSyncError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Time Sync Error: {:?}", self.description.to_string())
    }
}

impl From<TimeSyncError> for Status {
    fn from(error: TimeSyncError) -> Self {
        Status::failed_precondition(format!("FAILED_PRECONDITION: SYNC_CLIENT_TIME: {}", error))
    }
}

impl Error for TimeSyncError {}

fn validate_client_time(client_time: u64) -> Result<(), TimeSyncError> {
    let allowable_delta: i64 = match env::var("MAS_ALLOWABLE_CLIENT_TIME_DELTA") {
        Ok(allowable_delta) => allowable_delta,
        Err(_e) => {
            log::warn!("The MAS_ALLOWABLE_CLIENT_TIME_DELTA environment variable was not set, defaulting to `30 seconds`");
            "30".to_string()
        }
    }.parse().unwrap_or_default() ;

    let time_now = SystemTime::now()
        .duration_since(SystemTime::UNIX_EPOCH)
        .unwrap_or_default()
        .as_secs();

    let drift: i64 = time_now as i64 - client_time as i64;

    if allowable_delta > drift.abs() {
        Ok(())
    } else {
        Err(TimeSyncError {
            description: format!(
                "Client time and server time are not synchronized to within {} seconds ",
                allowable_delta
            ),
        })
    }
}

fn extract_function_name(request: &Request<()>) -> Option<&str> {
    let metadata = request.metadata();
    if let Some(function_name) = metadata.get("grpc-target") {
        // The "grpc-target" header contains the requested function name
        if let Ok(function_name) = function_name.to_str() {
            return Some(function_name);
        }
    }
    None
}


fn print_all_headers(metadata: &MetadataMap) {
    for key_and_value in metadata.iter() {
        match key_and_value {
            KeyAndValueRef::Ascii(key, value) =>
                println!("Header: {:?} = {:?}", key, value),
            KeyAndValueRef::Binary(key, value) =>
                println!("Header: {:?} = {:?}", key, value),
        }
    }
}

fn run_interceptors(mut request: Request<()>, version_config: Arc<Mutex<VersionConfig>>) -> Result<Request<()>, Status> {
    let mut check_time = false; // only check the time if there is a auth token

    let version_code_header = request.metadata().get("version_code");
    if let Some(version_code_header) = version_code_header {
        if let Ok(version_code_str) = version_code_header.to_str() {
            if let Ok(version_code) = version_code_str.parse::<u32>() {

                let mut version_config_safe = match version_config.lock() {
                    Ok(config) => config,
                    Err(_) => return Err(Status::internal("Internal service error")),
                };

                match version_config_safe.has_changed() {
                    Ok(changed) => {
                        if changed {
                            log::info!("interceptor: app version configuration '{}' has changed, reloading ...", &version_config_safe.filename);
                            if let Ok(config) = mas::VersionConfig::load_from_file(&version_config_safe.filename) {
                                *version_config_safe = config;
                            } else {
                                log::error!("interceptor: app version configuration '{}', failed to load.", &version_config_safe.filename);
                                return Err(Status::internal("Internal service error"));
                            }
                        }
                    },
                    Err(_) => {
                        log::error!("interceptor: app version configuration file '{}', failed to determine if it has changed.", &version_config_safe.filename);
                        return Err(Status::internal("Internal service error"));
                    }
                };

                if let Some(app_version) = version_config_safe.get_version(version_code) {
/*
                    if let Some(function_name) = extract_function_name(&request) {
                        println!("Requested function name: {}", function_name);
                       } else {
                        println!("Function name not found in the request metadata.");
					}
                    let metadata = request.metadata();
                    print_all_headers(&metadata);

                    let method_name = request.metadata().get(":path");
                    log::info!("interceptor: request '{:?}'.", request);
                    log::info!("interceptor: request '{:?}'.", request);
                    log::info!("interceptor: method name '{:?}'.", method_name);
*/
                    if app_version.supported == false {
                        log::info!("interceptor: incompatible app version '{}'.", version_code);
                        //return Err(Status::failed_precondition(format!("UPGRADE_REQUIRED: Incompatible app version {}", version_code)));

                        // FIXME not using this as it is apparently not consistent with how other errors are returned
                        /*
                        let json_obj = json!({
                            "reason_code": "UPGRADE_REQUIRED"
                        });
                        let json_bytes = serde_json::to_vec(&json_obj).unwrap();
                        return Err(Status::with_details(Code::FailedPrecondition, format!("Incompatible app version {}", version_code), json_bytes.into()));
                        */
                    }
                }
                //}
            } else {
                log::info!("interceptor: the `version_code` value '{}' cannot be converted to a number.", version_code_str);
                return Err(Status::failed_precondition("`version_code` metadata field is not numeric."));
            }
        } else {
            log::info!("interceptor: the `version_code` value cannot be converted to string.");
            return Err(Status::failed_precondition("`version_code` metadata field is of invalid type."));
        }
    } else {
        log::info!("interceptor: the `version_code` metadata field is missing.");
        return Err(Status::failed_precondition("`version_code` metadata field is missing."));
    }

    let request = match request.metadata().get("authorization") {
        Some(token) => {
            let token = match token.to_str() {
                Err(err) => {
                    log::error!("{:?}", err);
                    return Err(Status::permission_denied(err.to_string()));
                }

                Ok(token) => token,
            };

            match validate_token(token.to_string(), crate::MAS_JWT_SECRET.to_string()) {
                Err(err) => {
                    log::error!("Validating token {:?} failed with {:?} ", token, err);
                    return Err(Status::permission_denied(err.to_string()));
                }
                Ok(claims) => {
                    check_time = true;
                    request
                        .extensions_mut()
                        .insert(mas::AuthorizationStatus::Authorized(
                            claims.msisdn.to_string(),
                            claims.session_id,
                            claims.mobile_money_token.to_string(),
                        ));
                    request
                }
            }
        }
        None => request,
    };

    let request = if check_time {
        match request.metadata().get("client_time") {
            Some(client_time) => match client_time.to_str() {
                Ok(client_time) => {
                    let client_time: u64 = client_time.parse().unwrap_or_default();
                    log::debug!("validating client time");
                    log::debug!("client time = {}", client_time);

                    validate_client_time(client_time)?;
                    request
                }
                Err(e) => return Err(Status::failed_precondition(e.to_string())),
            },
            None => {
                return Err(Status::permission_denied(
                    "gRPC client_time metadata not available.",
                ))
            }
        }
    } else {
        request
    };

    Ok(request)
}

fn init_logger() {
    let _ = env_logger::builder()
        .filter(Some("mas_service"), log::LevelFilter::Trace)
        // Include all events in tests
        // Ensure events are captured by `cargo test`
        .is_test(true)
        // Ignore errors initializing the logger if tests race to configure it
        .try_init();
}

fn get_secret_key() -> String {
    match env::var("MAS_JWT_SECRET") {
        Ok(secret_key) => secret_key,
        Err(_e) => {
            log::warn!("The MAS_JWT_SECRET environment variable was not set, defaulting to who knows what!");

            let who_knows_what: String = rand::thread_rng()
                .sample_iter(&Alphanumeric)
                .take(17)
                .map(char::from)
                .collect();

            who_knows_what
        }
    }
}

lazy_static! {
    static ref MAS_JWT_SECRET: String = get_secret_key();
}

async fn run_live() -> Result<(), Box<dyn std::error::Error>> {
    /*
     * this block of code is the implemntation of tls.  It's left commented out untill we have a
     * tls capable kotlin client in the mobile app
     */

    let cert = tokio::fs::read("./tls/server.certificate")
        .await
        .expect("Could not find the server certificate");

    let key = tokio::fs::read("./tls/server.private.key")
        .await
        .expect("Could not find the server certifiacate key");

    let identity = Identity::from_pem(cert, key);

    let api_port = match env::var("MAS_API_PORT") {
        Ok(port) => port,
        Err(_e) => {
            log::warn!("The MAS_API_PORT environment variable was not set, defaulting to 5000");
            "5000".to_string()
        }
    };

    let feedback_path = match env::var("MAS_FEEDBACK_PATH") {
        Ok(path) => path,
        Err(_e) => {
            log::warn!("The MAS_FEEDBACK_PATH environment variable was not set, defaulting to ./output/feedback");
            "./output/feedback".to_string()
        }
    };

    let analytics_path = match env::var("MAS_ANALYTICS_PATH") {
        Ok(path) => path,
        Err(_e) => {
            log::warn!("The MAS_ANALYTICS_PATH environment variable was not set, defaulting to ./output/analytics");
            "./output/analytics".to_string()
        }
    };

    let app_version_config = match env::var("MAS_APP_VERSION_CONFIG") {
        Ok(path) => path,
        Err(_e) => {
            log::warn!("The MAS_APP_VERSION_CONFIG environment variable was not set, defaulting to ./appversions.conf");
            "./appversions.conf".to_string()
        }
    };

    log::info!("MAS Running on port: {}", api_port);
    log::info!("MAS Feedback path: {}", feedback_path);
    log::info!("MAS Analytics path: {}", analytics_path);
    log::info!("MAS App versions config: {}", app_version_config);

    let addr = format!("0.0.0.0:{}", api_port).parse()?;

    #[allow(clippy::redundant_closure)]
    static HXC: Lazy<Hxc> = Lazy::new(|| Hxc::new());

    static STATISTICS: Lazy<Statistics> = Lazy::new(|| Statistics::new(&HXC));
    static HISTORY: Lazy<History> = Lazy::new(|| History::new(&HXC));
    static HXC_DB: Lazy<HxcDb> = Lazy::new(|| HxcDb::new(&HXC));

    #[allow(clippy::redundant_closure)]
    static CREDIVERSE: Lazy<Crediverse> = Lazy::new(|| Crediverse::new());

    #[allow(clippy::redundant_closure)]
    static WALLET_OWNER: Lazy<WalletOwner> = Lazy::new(|| WalletOwner::new());

    let root_certificate_file_name = "tls/ca.root.certificate";

    let pem = tokio::fs::read(root_certificate_file_name)
        .await
        .unwrap_or_else(|_| {
            panic!(
                "Could not read {} as root authority certificate",
                root_certificate_file_name
            )
        });

    let credivault_client_cert = tokio::fs::read("./tls/credivault_client/client.certificate")
        .await
        .expect("Could not read the Client Certificate");

    let credivault_client_key = tokio::fs::read("./tls/credivault_client/client.key")
        .await
        .expect("Could not read the Client Key");

    static MOVIVY_MOBILE_MONEY: Lazy<MovIvyMobileMoney> = Lazy::new(|| MovIvyMobileMoney::new());

    let version_config_result = mas::VersionConfig::load_from_file(&app_version_config);

    let version_config = match version_config_result {
        Ok(config) => config,
        Err(e) => {
            log::error!("Failed to load app version configuration file '{}': {}", app_version_config, e);
            std::process::exit(1);
        }
    };
    let version_config = Arc::new(Mutex::new(version_config));

    let mas = MasIpml::new(MasConfig {
        crediverse: &CREDIVERSE,
        statistics: &STATISTICS,
        history: &HISTORY,
        hxc_db: &HXC_DB,
        wallet_owner: &WALLET_OWNER,
        credivault_client_cert,
        credivault_client_key,
        movivy_mobile_money: &MOVIVY_MOBILE_MONEY,
        pem,
        feedback_path,
        analytics_path,
		app_version_config: version_config.clone(),
        //app_version_config: app_version_config.clone(),
    });

    Server::builder()
        // tls functionality commented out until the client is capable
        .tls_config(ServerTlsConfig::new().identity(identity))?
        .add_service(MasServer::with_interceptor(mas, move |request| {
            run_interceptors(request, version_config.clone())
        }))
        .serve(addr)
        .await?;

    Ok(())
}
