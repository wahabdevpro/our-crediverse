use chrono::{Duration, Local, NaiveDate, NaiveDateTime, NaiveTime};
use std::time::SystemTime;

use rand::{thread_rng, Rng};
use serde_json::json;
use std::fmt;
use tonic::metadata::MetadataValue;

// use masapi::mas_client::MasClient;
use tonic::{
    transport::{Certificate, Channel, ClientTlsConfig},
    Request, Status,
};

use crate::masapi::mas_client::MasClient;
//
//
//
pub mod masapi {
    include!(concat!(env!("OUT_DIR"), concat!("/", "masapi", ".rs")));
}

pub mod team_service_api {
    tonic::include_proto!("team_service_api");
}

use team_service_api::team_service_api_client::TeamServiceApiClient;
use team_service_api::*;

#[allow(dead_code)]
async fn create_team(team_lead_agent_id: u64, member_agent_ids: Vec<u64>) {
    let team_channel = _create_team_channel().await;

    let mut team_client = TeamServiceApiClient::new(team_channel);

    let team_lead = TeamLead {
        agent_id: team_lead_agent_id,
    };

    let members: Vec<Member> = member_agent_ids
        .into_iter()
        .map(|agent_id| Member { agent_id })
        .collect();

    for member in members {
        team_client
            .add_team_member(MembershipRequest {
                member: Some(member),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();
    }
}

async fn _create_team_channel() -> Channel {
    Channel::from_static("http://0.0.0.0:5100")
        .connect()
        .await
        .unwrap()
}

async fn create_channel() -> Channel {
    let pem = tokio::fs::read("tls/ca.root.certificate")
        .await
        .expect("No pem found!");

    let ca = Certificate::from_pem(pem);

    let tls_config = ClientTlsConfig::new()
        .domain_name("demo.gcp.concurrent.systems")
        .ca_certificate(ca);

    Channel::from_static("http://127.0.0.1:5000")
        //Channel::from_static("https://demo.gcp.concurrent.systems:5000")
        .tls_config(tls_config)
        .unwrap()
        .connect()
        .await
        .unwrap()
}

async fn login(msisdn: String, pin: String) -> (String, String, String) {
    let channel = create_channel().await;

    let mut client = MasClient::new(channel);

    let login_request = masapi::LoginRequest {
        msisdn,
        username: "".into(),
        password: "".into(),
        pin,
        one_time_pin: "".into(),
    };

    let login_response = client.login(login_request).await.unwrap();
    let login_response = login_response.get_ref();

    (
        login_response.agent_id.clone(),
        login_response.agent_msisdn.clone(),
        login_response.login_token.clone(),
    )
}

pub fn add_token_to_metadata(
    mut request: Request<()>,
    jwt_token: String,
) -> Result<Request<()>, Status> {
    request.metadata_mut().insert(
        "authorization",
        tonic::metadata::MetadataValue::try_from(jwt_token).unwrap(),
    );
    Ok(request)
}

pub fn add_client_time_to_metadata(mut request: Request<()>) -> Result<Request<()>, Status> {
    let time_now = SystemTime::now()
        .duration_since(SystemTime::UNIX_EPOCH)
        .unwrap_or_default()
        .as_secs();

    request.metadata_mut().insert(
        "client_time",
        tonic::metadata::MetadataValue::try_from(time_now).unwrap(),
    );
    Ok(request)
}

fn add_metadata(request: Request<()>, jwt_token: String) -> Result<Request<()>, Status> {
    let request = add_token_to_metadata(request, jwt_token)?;
    add_client_time_to_metadata(request)
}

#[allow(dead_code)]
async fn distribute_credit(distributer: String, msisdns: Vec<String>) {
    let channel = create_channel().await;
    let amount_to_sell = 100.0;

    let (_agent_id, _msisdn, token) = login(distributer.clone(), "12345".to_string()).await;
    let otp_request = masapi::LoginRequest {
        msisdn: distributer, //"0820000014".into(),
        username: "".into(),
        password: "".into(),
        pin: "".into(),
        one_time_pin: "99999".into(),
    };

    let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
        add_metadata(request, token.to_string())
    });

    client.submit_otp(otp_request).await.unwrap();

    for msisdn in msisdns {
        client
            .sell_airtime(masapi::SellAirtimeRequest {
                amount: amount_to_sell.to_string(),
                msisdn,
                latitude: Some(78.789),
                longitude: Some(14.456),
            })
            .await
            .unwrap();
    }
}

#[allow(dead_code)]
async fn run_sales(seller_msisdns: Vec<String>) {
    let channel = create_channel().await;

    for seller_msisdn in seller_msisdns {
        let (_agent_id, _msisdn, token) = login(seller_msisdn.clone(), "12345".to_string()).await;
        let otp_request = masapi::LoginRequest {
            msisdn: seller_msisdn,
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };
        let mut client = MasClient::with_interceptor(channel.clone(), |request: Request<()>| {
            add_metadata(request, token.to_string())
        });
        client.submit_otp(otp_request).await.unwrap();

        for i in 0..10 {
            client
                .sell_airtime(masapi::SellAirtimeRequest {
                    amount: "8.000".to_string(),
                    msisdn: format!("082000002{}", i),
                    latitude: Some(78.789),
                    longitude: Some(14.456),
                })
                .await
                .unwrap();
        }
    }
}

#[allow(dead_code)]
async fn get_transactions(distributer: String) {
    let channel = create_channel().await;

    let (_agent_id, _msisdn, token) = login(distributer.clone(), "00000".to_string()).await;
    let otp_request = masapi::LoginRequest {
        msisdn: distributer.clone(),
        username: "".into(),
        password: "".into(),
        pin: "".into(),
        one_time_pin: "99999".into(),
    };

    let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
        add_metadata(request, token.to_string())
    });

    client.submit_otp(otp_request).await.unwrap();

    let tsts = client
        .get_transactions(masapi::GetTransactionsRequest {
            start_page: 3 as u32,
            transactions_per_page: 10 as u32,
        })
        .await
        .unwrap();

    println!("report: {:?}\n\n", tsts);
}

#[allow(dead_code)]
async fn get_global_stats(distributer: String) {
    let channel = create_channel().await;

    let (_agent_id, _msisdn, token) = login(distributer.clone(), "00000".to_string()).await;
    let otp_request = masapi::LoginRequest {
        msisdn: distributer.clone(),
        username: "".into(),
        password: "".into(),
        pin: "".into(),
        one_time_pin: "99999".into(),
    };

    let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
        add_metadata(request, token.to_string())
    });

    client.submit_otp(otp_request).await.unwrap();

    let now = Local::now();
    let start_of_day = now.date_naive().and_hms_opt(0, 0, 0).unwrap();

    let tomorrow_start = start_of_day.checked_add_signed(Duration::days(1)).unwrap();

    let report = client
        .get_global_sales_summary(masapi::SalesSummaryRequest {
            msisdn: None,
            start_time: start_of_day.timestamp() as u64,
            end_time: tomorrow_start.timestamp() as u64,
        })
        .await
        .unwrap();

    println!("report: {:?}\n\n", report);
}

#[allow(dead_code)]
async fn get_global_hourly_stats(distributer: String) {
    let channel = create_channel().await;

    let (_agent_id, _msisdn, token) = login(distributer.clone(), "00000".to_string()).await;
    let otp_request = masapi::LoginRequest {
        msisdn: distributer.clone(),
        username: "".into(),
        password: "".into(),
        pin: "12345".into(),
        one_time_pin: "99999".into(),
    };

    let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
        add_metadata(request, token.to_string())
    });

    client.submit_otp(otp_request).await.unwrap();

    let now = Local::now();
    let start_of_day = now.date_naive().and_hms_opt(0, 0, 0).unwrap();

    let _tomorrow_start = start_of_day.checked_add_signed(Duration::days(1)).unwrap();

    let date = NaiveDate::parse_from_str("2023-05-29", "%Y-%m-%d").unwrap();
    let datetime = NaiveDateTime::new(date, NaiveTime::from_hms_opt(0, 0, 0).unwrap());
    let timestamp = datetime.timestamp();

    let report = client
        .get_global_hourly_sales_summary(masapi::SalesSummaryRequest {
            msisdn: None,
            start_time: (timestamp - 10800) as u64,
            end_time: (timestamp + 86400 + 86359 - 10800) as u64,
        })
        .await
        .unwrap();

    println!("report: {:?}\n\n", report);
}

#[allow(dead_code)]
async fn get_team(distributer: String) {
    let channel = create_channel().await;

    let (_agent_id, _msisdn, token) = login(distributer.clone(), "00000".to_string()).await;
    let otp_request = masapi::LoginRequest {
        msisdn: distributer.clone(),
        username: "".into(),
        password: "".into(),
        pin: "".into(),
        one_time_pin: "99999".into(),
    };

    let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
        add_metadata(request, token.to_string())
    });

    client.submit_otp(otp_request).await.unwrap();

    let team = client.get_team(masapi::NoParam {}).await.unwrap();

    println!("team: {:?}\n\n", team);
}

#[allow(dead_code)]
async fn get_team_membership(distributer: String) {
    let channel = create_channel().await;

    let (_agent_id, _msisdn, token) = login(distributer.clone(), "00000".to_string()).await;
    let otp_request = masapi::LoginRequest {
        msisdn: distributer.clone(),
        username: "".into(),
        password: "".into(),
        pin: "".into(),
        one_time_pin: "99999".into(),
    };

    let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
        add_metadata(request, token.to_string())
    });

    client.submit_otp(otp_request).await.unwrap();

    let membership = client
        .get_team_membership(masapi::NoParam {})
        .await
        .unwrap();

    println!("team membership: {:?}\n\n", membership);
}

#[allow(dead_code)]
async fn set_team_member_sales_target(
    distributer: String,
    member: String,
    period: masapi::Period,
    target_amount: Option<String>,
) {
    let channel = create_channel().await;

    let (_agent_id, _msisdn, token) = login(distributer.clone(), "12345".to_string()).await;
    let otp_request = masapi::LoginRequest {
        msisdn: distributer.clone(),
        username: "".into(),
        password: "".into(),
        pin: "".into(),
        one_time_pin: "99999".into(),
    };

    let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
        add_metadata(request, token.to_string())
    });

    client.submit_otp(otp_request).await.unwrap();

    let response = client
        .set_team_member_sales_target(masapi::SetTeamMemberSalesTargetRequest {
            msisdn: member.clone(),
            period: period as i32,
            target_amount: target_amount.clone(),
        })
        .await
        .unwrap();

    println!("response: {:?}\n\n", response);
}


impl fmt::Display for masapi::AnalyticsEventType {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            masapi::AnalyticsEventType::Crash => write!(f, "Crash"),
            masapi::AnalyticsEventType::Exception => write!(f, "Exception"),
            masapi::AnalyticsEventType::Action => write!(f, "Action"),
        }
    }
}

#[allow(dead_code)]
async fn submit_analytics(
    distributer: String,
) {
    let channel = create_channel().await;

    let (_agent_id, _msisdn, token) = login(distributer.clone(), "00000".to_string()).await;
    let otp_request = masapi::LoginRequest {
        msisdn: distributer.clone(),
        username: "".into(),
        password: "".into(),
        pin: "".into(),
        one_time_pin: "99999".into(),
    };

    let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
        add_metadata(request, token.to_string())
    });

    client.submit_otp(otp_request).await.unwrap();

    // Create an instance of AnalyticsRequest
    let mut analytics = masapi::AnalyticsRequest {
        app_version: String::from("1.0.0"),
        events: Vec::new(),
    };

    let mut current_timestamp = 1694771444; // Starting timestamp

    for _ in 0..5 {
        // Generate random data for AnalyticsEvent
        let endpoint = match thread_rng().gen_range(0..4) {
            0 => "HOME",
            1 => "SELL",
            2 => "STATS",
            _ => "PROFILE",
        };
        let what = if thread_rng().gen_bool(0.5) { "Nav" } else { "Button" };
        let event_type = if thread_rng().gen_bool(0.5) {
            masapi::AnalyticsEventType::Action
        } else {
            masapi::AnalyticsEventType::Exception
        };
        let timestamp = current_timestamp + thread_rng().gen_range(1..10);

        // Create the JSON content
        let content = json!({
            "content": {
                "endpoint": endpoint,
                "what": what,
                "ts": timestamp,
                "type": event_type.to_string(),
            }
        });

        // Create an AnalyticsEvent and add it to the request
        let event = masapi::AnalyticsEvent {
            time: timestamp,
            r#type: event_type as i32,
            content: serde_json::to_string(&content).unwrap(),
        };

        analytics.events.push(event);

        current_timestamp = timestamp; // Update current timestamp
    }

    let response = client
        .submit_analytics(analytics)
        .await
        .unwrap();

    println!("response: {:?}\n\n", response);
}

#[allow(dead_code)]
async fn get_version_status() { 
	let channel = create_channel().await;

/*
    let (_agent_id, _msisdn, token) = login(distributer.clone(), "00000".to_string()).await;
    let otp_request = masapi::LoginRequest {
        msisdn: distributer.clone(),
        username: "".into(),
        password: "".into(),
        pin: "".into(),
        one_time_pin: "99999".into(),
    };
*/	


    let mut client = MasClient::new(channel);
    
    let version = masapi::VersionStatusRequest {
        app_version_code: 1,
    };
	let mut request = Request::new(version);
	let version_code = MetadataValue::from_str("1").unwrap();
	request.metadata_mut().insert("version_code", version_code);

    match client
        .get_version_status(request)
        .await {
	        Ok(status) => {
	            println!("get_version_status response: {:?}", status);
	        }
	        Err(error) => {
	            println!("get_version_status ERROR: {}", error);
				/*
				let details = error.details();
				if let Ok(details_str) = std::str::from_utf8(details) {
				//let details_str = std::str::from_utf8(&details).unwrap();
			    	println!("get_version_status details: {}", details_str);
					let parsed_json: serde_json::Value = serde_json::from_str(details_str).unwrap();
					if let Some(reason_code) = parsed_json.get("reason_code") {
						if let Some(reason_code_str) = reason_code.as_str() {
							println!("Reason Code: {}", reason_code_str);
					    } else {
					        println!("Reason Code is not a string.");
					    }
					} else {
					    println!("No 'reason_code' field found.");
					}
				} else {
			    	println!("get_version_status details: failed to convert to string." );
				}
				//let details = error.details();
				//let details_str = std::str::from_utf8(&details.to_bytes()).unwrap();
				//println!("get_version_status details: {}", details_str);
	            return;
				*/
	        }
	    };


    //println!("response: {:?}\n\n", response);
}


#[allow(dead_code)]
async fn buy_with_mobile_money() {
    let channel = create_channel().await;

    let (_agent_id, _msisdn, token) = login("0828280001".into(), "12345".to_string()).await;
    let otp_request = masapi::LoginRequest {
        msisdn: "0828280001".into(),
        username: "".into(),
        password: "".into(),
        pin: "12345".into(),
        one_time_pin: "99999".into(),
    };

    let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
        add_metadata(request, token.to_string())
    });

    client.submit_otp(otp_request).await.unwrap();

    let request = masapi::BuyWithMobileMoneyRequest {
        mobile_money_amount: "10".into(),
    };

    match client.buy_airtime_with_mobile_money(request).await {
        Ok(receipt) => {
            println!("buy_airtime_with_mobile_money receipt: {:?}", receipt);
        }
        Err(error) => {
            println!("buy_airtime_with_mobile_money ERROR: {}", error);
            return;
        }
    };

    match client.get_mobile_money_balance(masapi::NoParam {}).await {
        Ok(balance) => {
            println!(
                "mobile_money_balance: {}",
                balance.get_ref().mobile_money_balance
            );
        }
        Err(error) => {
            println!("buy_airtime_with_mobile_money ERROR: {}", error);
        }
    };
}

async fn set_integration_testing_sales_targets() {
    for msisdn in [
        "0828280002".to_string(),
        "0828280003".to_string(),
        "0828280004".to_string(),
        "0828280005".to_string(),
        "0828280006".to_string(),
    ] {
        set_team_member_sales_target(
            "0828280001".to_string(),
            msisdn.clone(),
            masapi::Period::Day,
            Some("100".to_string()),
        )
        .await;

        set_team_member_sales_target(
            "0828280001".to_string(),
            msisdn.clone(),
            masapi::Period::Week,
            Some("800".to_string()),
        )
        .await;

        set_team_member_sales_target(
            "0828280001".to_string(),
            msisdn.clone(),
            masapi::Period::Month,
            Some("30000".to_string()),
        )
        .await;
    }
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    //    let team_agent_ids = [124, 125, 126, 127, 128].to_vec();
    //   let team_lead_agent_id = 123;
    
	//submit_analytics("0140821780".to_string()).await;
	get_version_status().await;

    //  create_team(team_lead_agent_id, team_agent_ids).await;

    //set_integration_testing_sales_targets().await;

    // run_sales(team_msisdns.clone()).await;

    //    Ok(buy_with_mobile_money().await)
    //get_global_hourly_stats("0140821780".to_string()).await;
    //

     //get_transactions("0140821780".to_string()).await;
    /*
        let team_lead_msisdn = "0828280001".to_string();

        let team_msisdns = [
            "0828280002".to_string(),
            "0828280003".to_string(),
            "0828280004".to_string(),
            "0828280005".to_string(),
            "0828280006".to_string(),
        ]
        .to_vec();

        create_team(team_lead_msisdn.clone(), team_msisdns).await;
    */
    Ok(())
}

#[allow(dead_code)]
async fn full_main() -> Result<(), Box<dyn std::error::Error>> {
    println!("Dev bench client\n\n");

    /*
    set_team_member_sales_target(
        "0140821780".to_string(),
        "0101001154".to_string(),
        masapi::Period::Week,
        //None,
        Some("37500".to_string()),
    ).await;
    */
    /*
    get_team(
        "0140821780".to_string()
    ).await;
    set_team_member_sales_target(
        "0101001405".to_string(),
        "0140821780".to_string(),
        masapi::Period::Week,
        //None,
        Some("2500".to_string()),
    ).await;
    */
    /*
    get_team_membership(
        "0140821780".to_string()
    ).await;
    */
    /*
        let team_lead_msisdn = "0828280001".to_string();

        let team_msisdns = [
            "0828280002".to_string(),
            "0828280003".to_string(),
            "0828280004".to_string(),
            "0828280005".to_string(),
            "0828280006".to_string(),
        ]
        .to_vec();

        let distributer = "0820000015".to_string();
        distribute_credit(distributer, team_msisdns.clone()).await;

        run_sales(team_msisdns.clone()).await;

        create_team(team_lead_msisdn.clone(), team_msisdns).await;

        buy_with_mobile_money().await;


        get_global_stats("0140808000".to_string()).await;
    */

    Ok(())
}

/*
use std::fmt;

use tonic::transport::Channel;

use masapi::mas_client::MasClient;

pub mod masapi {
    // The string specified here must match the proto package name
    tonic::include_proto!("masapi");
}

#[derive(Debug)]
pub struct TestingError(String);

impl fmt::Display for TestingError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Testing Error: {}", self.0)
    }
}

impl std::error::Error for TestingError {}

pub fn add_token_to_metadata(
    mut request: tonic::Request<()>,
    jwt_token: String,
) -> Result<tonic::Request<()>, tonic::Status> {
    request.metadata_mut().insert(
        "authorization",
        tonic::metadata::MetadataValue::try_from(jwt_token).unwrap(),
    );
    Ok(request)
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("Dev bench client\n\n");

    let channel = Channel::from_static("http://0.0.0.0:5000")
        .connect()
        .await
        .unwrap();

    let mut client = MasClient::new(channel.clone());

    let msisdn = "0820000015".to_string();

    let login_response = client
        .login(masapi::LoginRequest {
            msisdn: msisdn.clone().into(),
            username: "".into(),
            password: "".into(),
            pin: "12345".into(),
            one_time_pin: "".into(),
        })
        .await?;

    let login_response = login_response.get_ref();

    println!("LoginResponse {:?}", login_response);

    let token = login_response.login_token.clone();

    println!("Server returned token {:?}", token);

    let mut client = MasClient::with_interceptor(channel, |request: tonic::Request<()>| {
        add_token_to_metadata(request, token.to_string())
    });

    let submit_otp_response = client
        .submit_otp(masapi::LoginRequest {
            msisdn: msisdn.into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".to_string(),
        })
        .await;

    println!("{submit_otp_response:?}");
    let mut agent_id = "".to_string();

    match submit_otp_response {
        Ok(submit_otp_response) => {
            agent_id = submit_otp_response.get_ref().agent_id.clone();

            println!("{submit_otp_response:?}");

            let get_account_info_response = client
                .get_account_info(masapi::AgentId {
                    agent_id: agent_id.clone().into(),
                })
                .await;

            match get_account_info_response {
                Ok(account_info) => {
                    println!("{account_info:?}");
                }
                Err(e) => {
                    println!("ERROR could not get account info: {}", e.to_string());
                }
            };
        }
        Err(e) => {
            println!("ERROR: {}", e.to_string())
        }
    };

    match client
        .get_account_balance(masapi::AgentId {
            agent_id: agent_id.clone().into(),
        })
        .await
    {
        Ok(balances) => {
            println!("Balances: {:?}", balances);
        }
        Err(e) => {
            println!("ERROR could not get account balances: {}", e.to_string());
        }
    };

    /*

    match client.get_account_info(
    masapi::AgentId{
    session_id: session_id.into(),
    agent_id: agent_id.clone().unwrap().into()}).await {

    Ok(agent_info) => {
    println!("agent_info    {:?}", agent_info);
    },
    Err(e) => println!("ERROR could not get account info: {}",e.to_string()),
    };

    match client.sell_airtime(SellAirtimeRequest {
    session_id: session_id.into(),
    amount: "8".to_string(),
    msisdn: "0820000020".to_string(),
    }).await {
    Ok(sale_response) => {
    println!("{:?}", sale_response);
    },
    Err (e) => {
    println!("{e:?}");
    }

    };

    println!("\n\n\n");


    match client.get_airtime_transactions(
    masapi::AgentId {
    session_id: session_id.into(),
    agent_id: agent_id.clone().unwrap().into()
    }).await {
    Ok(transactions) => {
    println!("{:?}", transactions);
    },
    Err (e) => {
    println!("{e:?}");
    }
    }
    */
    Ok(())
}
    */
