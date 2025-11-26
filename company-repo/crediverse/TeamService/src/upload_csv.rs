use std::error::Error;
use std::fs::File;
use std::io::{self, BufRead};

// use masapi::mas_client::MasClient;
use tonic::transport::Channel;

pub mod team_service_api {
    tonic::include_proto!("team_service_api");
}

use team_service_api::team_service_api_client::TeamServiceApiClient;
use team_service_api::*;

use std::{env, panic};

async fn create_team_channel() -> Channel {
    Channel::from_static("http://0.0.0.0:5100")
        .connect()
        .await
        .unwrap()
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    // Specify the path to your CSV file

    let args: Vec<String> = env::args().collect();

    if args.len() < 2 {
        panic!("You must supply a path tho the tesing file.");
    }

    let file_path = args[1].clone();

    let team_channel = create_team_channel().await;

    let mut team_client = TeamServiceApiClient::new(team_channel);

    // Open the file
    let file = File::open(&file_path)?;
    let reader = io::BufReader::new(file);

    // Read the file line by line
    for line in reader.lines() {
        let line = line?;

        // Split each line into columns
        let columns: Vec<&str> = line.split(',').collect();

        // Extract the member and team_lead values
        let member = columns.get(0).unwrap().parse::<u64>().unwrap();
        let team_lead = columns.get(1).unwrap().parse::<u64>().unwrap();

        // Create a MembershipRequest and call add_team_member
        let request = MembershipRequest {
            member: Some(Member { agent_id: member }),
            team_lead: Some(TeamLead {
                agent_id: team_lead,
            }),
        };

        team_client
            .add_team_member(request)
            .await
            .expect("could not add team");
    }

    Ok(())
}
