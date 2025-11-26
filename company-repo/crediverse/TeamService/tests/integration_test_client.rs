#[cfg(test)]
mod integration_tests {

    use mysql_async::params;
    use rand::{distributions::Alphanumeric, Rng}; // 0.8

    fn get_random_agent_id() -> u64 {
        rand::thread_rng().gen::<u64>()
    }

    use mysql_async::prelude::{Query, WithParams};

    pub mod team_service_api {
        tonic::include_proto!("team_service_api");
    }

    use team_service_api::team_service_api_client::TeamServiceApiClient;
    use team_service_api::*;

    use tonic::transport::Channel;
    use tonic::Code;

    async fn create_channel() -> Channel {
        Channel::from_static("http://0.0.0.0:5100")
            .connect()
            .await
            .unwrap()
    }

    #[tokio::test]
    async fn getting_sales_targets_returns_amounts_as_none_or_formatted_to_4_places() {
        truncate_agents().await;

        let channel = create_channel().await;
        let mut client = TeamServiceApiClient::new(channel);

        // let before_member_count = get_member_agent_count_sql.map(conn)

        let member1 = Member {
            agent_id: get_random_agent_id(),
        };
        let member2 = Member {
            agent_id: get_random_agent_id(),
        };
        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        client
            .add_team_member(MembershipRequest {
                member: Some(member1.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();
        client
            .add_team_member(MembershipRequest {
                member: Some(member2.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        client
            .set_team_member_sales_target(MemberSetSalesTargetRequest {
                member: Some(member1.clone()),
                team_lead: Some(team_lead.clone()),
                period: Period::Day as i32,
                target_amount: Some("100".to_string()),
            })
            .await
            .unwrap();

        let team = client.get_team_members(team_lead.clone()).await.unwrap();

        let team = team.get_ref();

        team.members.iter().for_each(|member| {
            if member.agent_id == member1.agent_id {
                match member.sales_targets.clone() {
                    None => panic!("There should be sales targets"),
                    Some(sales_target) => {
                        match &sales_target.daily_amount {
                            None => panic!("There should be a daily sales targets"),
                            Some(amount) => assert_eq!(amount, "100.0000"),
                        };
                        match &sales_target.weekly_amount {
                            None => {}
                            Some(_) => panic!("There should not be a weekly sales target"),
                        };

                        match &sales_target.monthly_amount {
                            None => {}
                            Some(_) => panic!("There should not be a weekly sales target"),
                        }
                    }
                }
            } else if member.agent_id == member2.agent_id {
                match member.sales_targets.clone() {
                    None => (),
                    Some(_sales_target) => panic!("There should not be sales targets"),
                }
            } else {
                panic!("Team have more members than what was added")
            }
        });
    }

    #[tokio::test]
    async fn adding_sales_targets_returns_ok() {
        truncate_agents().await;

        let channel = create_channel().await;
        let mut client = TeamServiceApiClient::new(channel);

        // let before_member_count = get_member_agent_count_sql.map(conn)

        let member1 = Member {
            agent_id: get_random_agent_id(),
        };
        let member2 = Member {
            agent_id: get_random_agent_id(),
        };
        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        client
            .add_team_member(MembershipRequest {
                member: Some(member1.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();
        client
            .add_team_member(MembershipRequest {
                member: Some(member2.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        client
            .set_team_member_sales_target(MemberSetSalesTargetRequest {
                member: Some(member1.clone()),
                team_lead: Some(team_lead.clone()),
                period: Period::Day as i32,
                target_amount: Some("100".to_string()),
            })
            .await
            .expect("set_team_member_sales_target should return OK");
    }

    #[tokio::test]
    async fn deleting_a_member_from_a_team_returns_ok() {
        truncate_agents().await;

        let channel = create_channel().await;
        let mut client = TeamServiceApiClient::new(channel);

        // let before_member_count = get_member_agent_count_sql.map(conn)

        let member1 = Member {
            agent_id: get_random_agent_id(),
        };
        let member2 = Member {
            agent_id: get_random_agent_id(),
        };
        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        client
            .add_team_member(MembershipRequest {
                member: Some(member1),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();
        client
            .add_team_member(MembershipRequest {
                member: Some(member2.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        match client
            .remove_member(MembershipRequest {
                member: Some(member2.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
        {
            Ok(_) => {}
            Err(e) => {
                panic!("error was unexpected: {}", e)
            }
        }
    }

    #[tokio::test]
    async fn deleting_a_team_returns_ok() {
        truncate_agents().await;

        let channel = create_channel().await;
        let mut client = TeamServiceApiClient::new(channel);

        // let before_member_count = get_member_agent_count_sql.map(conn)

        let member1 = Member {
            agent_id: get_random_agent_id(),
        };

        let member2 = Member {
            agent_id: get_random_agent_id(),
        };

        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        client
            .add_team_member(MembershipRequest {
                member: Some(member1),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        client
            .add_team_member(MembershipRequest {
                member: Some(member2),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        let team_agent_ids = client
            .get_team_agent_ids(team_lead.clone())
            .await
            .unwrap()
            .into_inner();

        assert_eq!(team_agent_ids.agent_ids.len(), 2);

        client.delete_team(team_lead.clone()).await.unwrap();

        match client.get_team_agent_ids(team_lead).await {
            Ok(_agent_ids) => panic!("The team should have been deleted"),
            Err(_e) => {}
        }
    }

    #[tokio::test]
    async fn deleting_a_non_existant_team_returns_an_error() {
        let channel = create_channel().await;
        let mut client = TeamServiceApiClient::new(channel);

        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        match client.delete_team(team_lead.clone()).await {
            Ok(_) => {
                panic!("membership is not valid; an error was expected")
            }
            Err(e) => {
                println!("getting team agent_ids error: {}", e);
                assert_eq!(e.code(), Code::NotFound);
                assert!(
                    e.message()
                        == format!(
                            "No team membership found for team_lead: {}",
                            team_lead.agent_id
                        )
                );
            }
        }
    }

    #[tokio::test]
    async fn deleting_a_member_from_a_team_they_are_not_part_of_returns_an_error() {
        truncate_agents().await;

        let channel = create_channel().await;
        let mut client = TeamServiceApiClient::new(channel);

        // let before_member_count = get_member_agent_count_sql.map(conn)

        let member1 = Member {
            agent_id: get_random_agent_id(),
        };
        let member2 = Member {
            agent_id: get_random_agent_id(),
        };

        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        let other_team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        let other_team_member = Member {
            agent_id: get_random_agent_id(),
        };

        client
            .add_team_member(MembershipRequest {
                member: Some(member1.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        client
            .add_team_member(MembershipRequest {
                member: Some(member2.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        client
            .add_team_member(MembershipRequest {
                member: Some(other_team_member.clone()),
                team_lead: Some(other_team_lead.clone()),
            })
            .await
            .unwrap();

        match client
            .remove_member(MembershipRequest {
                member: Some(member1.clone()),
                team_lead: Some(other_team_lead.clone()),
            })
            .await
        {
            Ok(_) => {
                panic!("membership is not valid; an error was expected")
            }
            Err(e) => {
                println!("getting team agent_ids error: {}", e);
                assert_eq!(e.code(), Code::NotFound);
                assert!(
                    e.message()
                        == format!(
                            "No team membership found for member {} of team_lead: {}",
                            member1.agent_id, other_team_lead.agent_id
                        )
                );
            }
        }
    }

    #[tokio::test]
    async fn deleting_a_member_from_a_non_existant_team_returns_an_error() {
        truncate_agents().await;

        let channel = create_channel().await;
        let mut client = TeamServiceApiClient::new(channel);

        // let before_member_count = get_member_agent_count_sql.map(conn)

        let member = Member {
            agent_id: get_random_agent_id(),
        };
        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        match client
            .remove_member(MembershipRequest {
                member: Some(member.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
        {
            Ok(_) => {
                panic!("membership is not valid; an error was expected")
            }
            Err(e) => {
                println!("getting team agent_ids error: {}", e);
                assert_eq!(e.code(), Code::NotFound);
                assert_eq!(
                    e.message(),
                    format!(
                        "No team membership found for member {} of team_lead: {}",
                        member.clone().agent_id,
                        team_lead.clone().agent_id
                    )
                );
            }
        }
    }

    #[tokio::test]
    async fn is_team_lead_returns_true_for_team_lead_and_false_for_not_team_lead() {
        truncate_agents().await;

        let channel = create_channel().await;

        let mut client = TeamServiceApiClient::new(channel);

        // let before_member_count = get_member_agent_count_sql.map(conn)

        let member = Member {
            agent_id: get_random_agent_id(),
        };
        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        let non_team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        client
            .add_team_member(MembershipRequest {
                member: Some(member.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        let is_team_lead_team_lead = client.is_team_lead(team_lead.clone()).await.unwrap();
        let is_non_team_lead_team_lead = client.is_team_lead(non_team_lead).await.unwrap();

        assert_eq!(is_team_lead_team_lead.get_ref().is_team_lead, true);
        assert_eq!(is_non_team_lead_team_lead.get_ref().is_team_lead, false);
    }

    #[tokio::test]
    async fn add_a_member_twice_doesnt_add_two_members() {
        let pool = mysql_async::Pool::new("mysql://root:ussdgw@0.0.0.0:3306/team_service_db");
        truncate_agents().await;

        let conn = pool.get_conn().await.unwrap();

        let channel = create_channel().await;

        let mut client = TeamServiceApiClient::new(channel);

        // let before_member_count = get_member_agent_count_sql.map(conn)

        let member = Member {
            agent_id: get_random_agent_id(),
        };
        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        let before_member_count: u64 =
            "select count(*) from Agent where agent_id=:member_agent_id;"
                .with(params! {"member_agent_id" => member.agent_id.clone()})
                .map(conn, |record_count| record_count)
                .await
                .unwrap()[0];

        client
            .add_team_member(MembershipRequest {
                member: Some(member.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        client
            .add_team_member(MembershipRequest {
                member: Some(member.clone()),
                team_lead: Some(team_lead),
            })
            .await
            .unwrap();

        let conn = pool.get_conn().await.unwrap();

        let after_member_count: u64 = "select count(*) from Agent where agent_id=:member_agent_id;"
            .with(params! {"member_agent_id" => member.agent_id.clone()})
            .map(conn, |record_count| record_count)
            .await
            .unwrap()[0];

        assert_eq!(before_member_count + 1, after_member_count)
    }

    async fn truncate_agents() -> () {
        let pool = mysql_async::Pool::new("mysql://root:ussdgw@0.0.0.0:3306/team_service_db");
        let conn = pool.get_conn().await.unwrap();
        match "delete from Agent".ignore(conn).await {
            Ok(_) => (),
            Err(e) => panic!("Error while truncating Agent table {}", e),
        }
    }

    #[tokio::test]
    async fn getting_agent_ids_returns_list_of_agent_id_strings() {
        truncate_agents().await;

        let channel = create_channel().await;

        let mut client = TeamServiceApiClient::new(channel);

        // let before_member_count = get_member_agent_count_sql.map(conn)

        let member1 = Member {
            agent_id: get_random_agent_id(),
        };
        let member2 = Member {
            agent_id: get_random_agent_id(),
        };
        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        client
            .add_team_member(MembershipRequest {
                member: Some(member1.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        client
            .add_team_member(MembershipRequest {
                member: Some(member2.clone()),
                team_lead: Some(team_lead.clone()),
            })
            .await
            .unwrap();

        let agent_ids = client.get_team_agent_ids(team_lead.clone()).await.unwrap();
        let agent_ids = agent_ids.into_inner().agent_ids;

        assert_eq!(agent_ids.len(), 2);
        assert_eq!(agent_ids[0], member1.clone().agent_id);
        assert_eq!(agent_ids[1], member2.clone().agent_id);
    }

    #[tokio::test]
    async fn getting_agent_ids_of_non_existant_team_returns_an_error() {
        truncate_agents().await;

        let channel = create_channel().await;
        let mut client = TeamServiceApiClient::new(channel);

        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        match client.get_team_agent_ids(team_lead.clone()).await {
            Ok(_agent_ids) => panic!("Error was expected, there are no members in this team"),
            Err(e) => {
                println!("getting team agent_ids error: {}", e);
                assert_eq!(e.code(), Code::NotFound);
                assert!(
                    e.message() == format!("No team found for team_lead: {}", team_lead.agent_id)
                )
            }
        }
    }

    #[tokio::test]
    async fn add_member_to_team_returns_ok() {
        truncate_agents().await;

        let pool = mysql_async::Pool::new("mysql://root:ussdgw@0.0.0.0:3306/team_service_db");

        let conn = pool.get_conn().await.unwrap();

        let channel = create_channel().await;

        let mut client = TeamServiceApiClient::new(channel);

        // let before_member_count = get_member_agent_count_sql.map(conn)

        let member = Member {
            agent_id: get_random_agent_id(),
        };
        let team_lead = TeamLead {
            agent_id: get_random_agent_id(),
        };

        let before_member_count: u64 =
            "select count(*) from Agent where agent_id=:member_agent_id;"
                .with(params! {"member_agent_id" => member.agent_id.clone()})
                .map(conn, |record_count| record_count)
                .await
                .unwrap()[0];

        client
            .add_team_member(MembershipRequest {
                member: Some(member.clone()),
                team_lead: Some(team_lead),
            })
            .await
            .unwrap();

        let conn = pool.get_conn().await.unwrap();
        let after_member_count: u64 = "select count(*) from Agent where agent_id=:member_agent_id;"
            .with(params! {"member_agent_id" => member.clone().agent_id})
            .map(conn, |record_count| record_count)
            .await
            .unwrap()[0];

        assert_eq!(before_member_count + 1, after_member_count)
    }
}
