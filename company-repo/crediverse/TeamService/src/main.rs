#[macro_use]
extern crate lazy_static;

pub(crate) use tonic::transport::Server;

use std::env;

mod team_service;
use team_service::team_service_api::team_service_api_server::TeamServiceApiServer;

use mysql_async::Pool;

mod local_utils;
use local_utils::*;

lazy_static! {
    static ref TEAM_SERVICE_DB_CONNECTION_POOL: Pool = create_connection_pool();
}

// mod mas;

// use mas::masapi::mas_server::MasServer;
//
//
//
use crate::team_service::TeamServiceImpl;
//

fn init_logger() {
    let _ = env_logger::builder()
        .filter(Some("teams_service"), log::LevelFilter::Trace)
        // Include all events in tests
        // Ensure events are captured by `cargo test`
        .is_test(true)
        // Ignore errors initializing the logger if tests race to configure it
        .try_init();
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    init_logger();
    run_live().await
}

async fn run_live() -> Result<(), Box<dyn std::error::Error>> {
    let api_port = match env::var("TEAM_SERVICE_API_PORT") {
        Ok(port) => port,
        Err(_e) => {
            log::warn!(
                "The TEAM_SERVICE_API_PORT environment variable was not set, defaulting to 5100"
            );
            "5100".to_string()
        }
    };

    log::info!("Team Service Running on port: {}", api_port);

    let addr = format!("0.0.0.0:{}", api_port).parse()?;

    let team_service = TeamServiceImpl {};

    Server::builder()
        // tls functionality commented out until the client is capable
        .add_service(TeamServiceApiServer::new(team_service))
        .serve(addr)
        .await?;

    Ok(())
}
