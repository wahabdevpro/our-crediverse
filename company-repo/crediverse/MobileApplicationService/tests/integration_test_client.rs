#[cfg(test)]
mod integration_tests {
    use std::time::SystemTime;

    use masapi::mas_client::MasClient;
    use tonic::{
        transport::{Certificate, Channel, ClientTlsConfig},
        Code, Request, Status,
    };

    use crate::integration_tests::masapi::{
        NoParam, SalesSummaryRequest, StockBalance, TeamMember,
    };

    pub mod masapi {
        tonic::include_proto!("masapi");
    }

    async fn create_channel() -> Channel {
        let pem = tokio::fs::read("tls/ca.root.certificate")
            .await
            .expect("No pem found!");

        let ca = Certificate::from_pem(pem);

        let tls_config = ClientTlsConfig::new()
            .domain_name("demo.gcp.concurrent.systems")
            .ca_certificate(ca);

        Channel::from_static("https://demo.gcp.concurrent.systems:5000")
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

    /*
     *
     * Team tests START
     *
     */
    #[tokio::test]
    async fn teams_test_get_team_member_sales_summary() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0828280001".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0820000001".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let _otp_response = otp_response.get_ref();

        let sales_summary_request = SalesSummaryRequest {
            msisdn: Some("0828280002".to_string()),
            start_time: 0,
            end_time: 1694035670,
        };
        let stats_result = client.get_sales_summary(sales_summary_request).await;

        let stats = stats_result.unwrap();

        let stats = stats.get_ref();

        let values = stats.sales_summary.as_ref().unwrap();

        let airtime_sales = values.airtime_sales_value.clone();
        let bundle_sales = values.bundle_sales_value.clone();
        let airtime_cost_of_goods_sold = values.airtime_cost_of_goods_sold.clone();
        let bundle_cost_of_goods_sold = values.bundle_cost_of_goods_sold.clone();

        let airtime_count = values.airtime_sales_count;
        let bundle_count = values.bundle_sales_count;
        let airtime_unknown_cost_count = values.airtime_unknown_cost_count;
        let bundle_unknown_cost_count = values.bundle_unknown_cost_count;

        let inbound_transfers_value = values.inbound_transfers_value.clone();
        let inbound_transfers_count = values.inbound_transfers_count;

        println!("{:?}", values);

        assert_eq!(airtime_sales, "80.0000");
        assert_eq!(airtime_count, 10);
        assert_eq!(bundle_sales, "0.0000");
        assert_eq!(bundle_count, 0);

        assert_eq!(airtime_unknown_cost_count, 10);
        assert_eq!(bundle_unknown_cost_count, 0);
        assert_eq!(airtime_cost_of_goods_sold, "0.0000");
        assert_eq!(bundle_cost_of_goods_sold, "0.0000");

        assert_eq!(inbound_transfers_value, "100.0000");
        assert_eq!(inbound_transfers_count, 1);
    }

    #[tokio::test]
    async fn teams_test_is_team_lead() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000014".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let _otp_response = otp_response.get_ref();

        let is_team_lead_request = NoParam {};

        let is_team_lead = client.is_team_lead(is_team_lead_request).await;

        let is_team_lead = is_team_lead.unwrap();

        let is_team_lead = is_team_lead.get_ref();

        assert!(!is_team_lead.is_team_lead);
    }

    #[tokio::test]
    async fn teams_test_get_team_sales_summary() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0828280001".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let _otp_response = otp_response.get_ref();

        let sales_summary_request = SalesSummaryRequest {
            msisdn: None,
            start_time: 0,
            end_time: 1694035670,
        };
        let stats_result = client
            .get_team_sales_summary(sales_summary_request.clone())
            .await;

        let stats = stats_result.unwrap_or_else(|e| {
            panic!(
                "The stats could not be retrieved for: {:?}, error: {}",
                sales_summary_request, e
            )
        });

        let stats = stats.get_ref();

        let values = stats.sales_summary.as_ref().unwrap();

        let airtime_sales = values.airtime_sales_value.clone();
        let bundle_sales = values.bundle_sales_value.clone();

        let airtime_count = values.airtime_sales_count;
        let bundle_count = values.bundle_sales_count;

        println!(
            "sales: \n  sales: {} \n  sales: {} \n\nbundles:\n  value: {}\n  sales: {}",
            airtime_sales, airtime_count, bundle_sales, bundle_count
        );

        assert!(airtime_sales == "400.0000");
        assert!(airtime_count == 50);
        assert!(bundle_sales == "0.0000");
        assert!(bundle_count == 0);
    }

    #[tokio::test]
    async fn teams_test_get_team_members() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0828280001".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0828280001".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        client.submit_otp(otp_request).await.unwrap();

        match client.get_team(masapi::NoParam {}).await {
            Ok(team) => {
                let team = team.into_inner();
                println!("Team: {:?}", team);
                assert_eq!(team.members.len(), 5);
                assert!(
                    team.members.contains(&TeamMember {
                        first_name: "Ben".to_string(),
                        surname: "Benson".to_string(),
                        msisdn: "0828280002".to_string(),
                        stock_balance: Some(StockBalance {
                            balance: "20.0000".to_string(),
                            bonus_balance: "0.0000".to_string(),
                            on_hold_balance: "0.0000".to_string(),
                        }),
                        sales_targets: Some(masapi::SalesTargets {
                            daily_amount: Some("100.0000".into()),
                            monthly_amount: Some("30000.0000".into()),
                            weekly_amount: Some("800.0000".into()),
                        }),
                    }),
                    "Ben is not in team"
                );
                assert!(
                    team.members.contains(&TeamMember {
                        first_name: "Charles".to_string(),
                        surname: "Charleston".to_string(),
                        msisdn: "0828280003".to_string(),
                        stock_balance: Some(StockBalance {
                            balance: "20.0000".to_string(),
                            bonus_balance: "0.0000".to_string(),
                            on_hold_balance: "0.0000".to_string(),
                        }),
                        sales_targets: Some(masapi::SalesTargets {
                            daily_amount: Some("100.0000".into()),
                            monthly_amount: Some("30000.0000".into()),
                            weekly_amount: Some("800.0000".into()),
                        }),
                    }),
                    "Charles is not in team"
                );
                assert!(
                    team.members.contains(&TeamMember {
                        first_name: "Donald".to_string(),
                        surname: "Donaldson".to_string(),
                        msisdn: "0828280004".to_string(),
                        stock_balance: Some(StockBalance {
                            balance: "20.0000".to_string(),
                            bonus_balance: "0.0000".to_string(),
                            on_hold_balance: "0.0000".to_string(),
                        }),
                        sales_targets: Some(masapi::SalesTargets {
                            daily_amount: Some("100.0000".into()),
                            monthly_amount: Some("30000.0000".into()),
                            weekly_amount: Some("800.0000".into()),
                        })
                    }),
                    "Donald is not in team"
                );
                assert!(
                    team.members.contains(&TeamMember {
                        first_name: "Edward".to_string(),
                        surname: "Edwardson".to_string(),
                        msisdn: "0828280005".to_string(),
                        stock_balance: Some(StockBalance {
                            balance: "20.0000".to_string(),
                            bonus_balance: "0.0000".to_string(),
                            on_hold_balance: "0.0000".to_string(),
                        }),
                        sales_targets: Some(masapi::SalesTargets {
                            daily_amount: Some("100.0000".into()),
                            monthly_amount: Some("30000.0000".into()),
                            weekly_amount: Some("800.0000".into()),
                        })
                    }),
                    "Edward is not in team"
                );
                assert!(
                    team.members.contains(&TeamMember {
                        first_name: "Fred".to_string(),
                        surname: "Fredricson".to_string(),
                        msisdn: "0828280006".to_string(),
                        stock_balance: Some(StockBalance {
                            balance: "20.0000".to_string(),
                            bonus_balance: "0.0000".to_string(),
                            on_hold_balance: "0.0000".to_string(),
                        }),
                        sales_targets: Some(masapi::SalesTargets {
                            daily_amount: Some("100.0000".into()),
                            monthly_amount: Some("30000.0000".into()),
                            weekly_amount: Some("800.0000".into()),
                        })
                    }),
                    "Fred is not in team"
                );
            }
            Err(e) => println!("Get Team Test Error: {}", e),
        }
    }

    /*
     *
     * Team tests END
     *
     */

    #[tokio::test]
    async fn format_msisdn_test() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000015".to_string(), "12345".to_string()).await;
        let otp_request = masapi::LoginRequest {
            msisdn: "0820000015".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        client.submit_otp(otp_request).await.unwrap();

        let msisdns = [
            "0820000000".to_string(),
            "+270820000000".to_string(),
            "00270820000000".to_string(),
        ];

        for msisdn in msisdns {
            let formatted_msisdn = client
                .format_msisdn(masapi::Msisdn { msisdn })
                .await
                .unwrap()
                .get_ref()
                .clone();

            assert_eq!(formatted_msisdn.msisdn, "0820000000".to_string());
        }
    }

    #[tokio::test]
    async fn token_update_test() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000015".to_string(), "12345".to_string()).await;
        let otp_request = masapi::LoginRequest {
            msisdn: "0820000015".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();
        let otp_response = otp_response.get_ref();

        let token = &otp_response.login_token;

        let new_token = client.update_login_token(masapi::NoParam {}).await.unwrap();

        assert_ne!(
            token.to_string(),
            new_token.get_ref().login_token.to_string()
        );
    }

    #[tokio::test]
    async fn get_last_10_transactions_test() {
        println!("Testing transaction history");
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0828280004".to_string(), "12345".to_string()).await;
        let otp_request = masapi::LoginRequest {
            msisdn: "0828280004".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let _ = client.submit_otp(otp_request).await.unwrap();

        let last_10_transactions = client
            .get_transactions(masapi::GetTransactionsRequest {
                start_page: 0,
                transactions_per_page: 10,
            })
            .await
            .unwrap();

        let last_10_transactions = last_10_transactions.get_ref();

        println!("last 10 transactions {:?}", last_10_transactions);
        println!(
            "first transaction {:?}",
            last_10_transactions.transactions[0]
        );

        println!(
            "{} Transactions Found",
            last_10_transactions.transactions.len()
        );

        assert!(!last_10_transactions.transactions.is_empty());

        let next_10_transactions = client
            .get_transactions(masapi::GetTransactionsRequest {
                start_page: 1,
                transactions_per_page: 10,
            })
            .await
            .unwrap();
        let next_10_transactions = next_10_transactions.get_ref();

        println!("next 10 transactions {:?}", next_10_transactions);

        assert!(next_10_transactions.transactions.len() > 0);
    }

    #[tokio::test]
    async fn get_sales_summary() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000014".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let _otp_response = otp_response.get_ref();

        let sales_summary_request = SalesSummaryRequest {
            msisdn: None,
            start_time: 0,
            end_time: 1694035670,
        };
        let stats_result = client.get_sales_summary(sales_summary_request).await;

        let stats = stats_result.unwrap();

        let stats = stats.get_ref();

        let values = stats.sales_summary.as_ref().unwrap();

        let airtime_sales = values.airtime_sales_value.clone();
        let bundle_sales = values.bundle_sales_value.clone();

        let airtime_count = values.airtime_sales_count;
        let bundle_count = values.bundle_sales_count;

        println!(
            "sales: \n  sales: {} \n  sales: {} \n\nbundles:\n  value: {}\n  sales: {}",
            airtime_sales, airtime_count, bundle_sales, bundle_count
        );

        assert!(airtime_sales != "0");
        assert!(airtime_count != 0);
        assert!(bundle_sales == "0.0000");
        assert!(bundle_count == 0);
    }

    #[tokio::test]
    async fn agent_feedback_test() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000014".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let otp_response = otp_response.get_ref();

        let agent_id = &otp_response.agent_id;

        let feedback_result = client
            .get_agent_feedback(masapi::FeedBackRequest {
                name: "Agent".to_string(),
                agent_id: agent_id.to_string(),
                tier: "eCabine".to_string(),
                feed_back_request_msg: "This is another feedback message".to_string(),
            })
            .await;

        let _feedback_response = feedback_result.unwrap();
    }

    #[tokio::test]
    async fn get_balance_then_sell_to_self_and_get_balance_test() {
        let channel = create_channel().await;
        let amount_to_sell: f64 = 5.0;

        let (_agent_id, _msisdn, token) =
            login("0820000014".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        client.submit_otp(otp_request).await.unwrap();

        let opening_balance = client
            .get_stock_balance(masapi::GetStockBalanceRequest { msisdn: None })
            .await
            .unwrap()
            .get_ref()
            .clone()
            .balance
            .parse::<f64>()
            .unwrap();

        client
            .sell_airtime(masapi::SellAirtimeRequest {
                amount: amount_to_sell.to_string(),
                msisdn: "0820000014".to_string(),
                latitude: Some(78.789),
                longitude: Some(14.456),
            })
            .await
            .unwrap();

        let new_balance = client
            .get_stock_balance(masapi::GetStockBalanceRequest { msisdn: None })
            .await
            .unwrap()
            .get_ref()
            .clone()
            .balance
            .parse::<f64>()
            .unwrap();

        println!(
            "opening_balance: {},\n amount_sold : {},\n new_balance: {}",
            opening_balance, amount_to_sell, new_balance
        );

        println!(
            "(opening_balance + amount_to_sell) : {}",
            opening_balance + amount_to_sell
        );
        assert!((new_balance - (opening_balance - amount_to_sell)).abs() < 0.0001);

        let last_transactions = client
            .get_transactions(masapi::GetTransactionsRequest {
                start_page: 0,
                transactions_per_page: 1,
            })
            .await
            .unwrap();
        let last_transactions = last_transactions.get_ref();

        let last_transaction = last_transactions.transactions.get(0).unwrap();

        assert_eq!(
            last_transaction.transaction_type,
            masapi::TransactionType::SelfTopup as i32
        );
        assert_eq!(last_transaction.status, "SUCCESS");
    }

    #[tokio::test]
    async fn get_balance_then_sell_wholesale_and_get_balance_test() {
        let channel = create_channel().await;
        let amount_to_sell = 5.0;

        let (_agent_id, _msisdn, token) =
            login("0820000014".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        client.submit_otp(otp_request).await.unwrap();

        let opening_balance = client
            .get_stock_balance(masapi::GetStockBalanceRequest { msisdn: None })
            .await
            .unwrap()
            .get_ref()
            .clone()
            .balance
            .parse::<f64>()
            .unwrap();

        client
            .sell_airtime(masapi::SellAirtimeRequest {
                amount: amount_to_sell.to_string(),
                msisdn: "+27820000016".to_string(),
                latitude: Some(78.789),
                longitude: Some(14.456),
            })
            .await
            .unwrap();

        let new_balance = client
            .get_stock_balance(masapi::GetStockBalanceRequest { msisdn: None })
            .await
            .unwrap()
            .get_ref()
            .clone()
            .balance
            .parse::<f64>()
            .unwrap();

        println!(
            "opening_balance: {},\n amount_sold : {},\n new_balance: {}",
            opening_balance, amount_to_sell, new_balance
        );

        assert!((new_balance - (opening_balance - amount_to_sell)).abs() < 0.0001);

        let last_transactions = client
            .get_transactions(masapi::GetTransactionsRequest {
                start_page: 0,
                transactions_per_page: 1,
            })
            .await
            .unwrap();
        let last_transactions = last_transactions.get_ref();

        let last_transaction = last_transactions.transactions.get(0).unwrap();

        assert_eq!(
            last_transaction.transaction_type,
            masapi::TransactionType::Transfer as i32
        );
        assert_eq!(last_transaction.status, "SUCCESS");
    }

    #[tokio::test]
    async fn sell_more_than_ballance() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000015".to_string(), "12345".to_string()).await;
        let otp_request = masapi::LoginRequest {
            msisdn: "0820000015".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        client.submit_otp(otp_request).await.unwrap();

        let opening_balance = client
            .get_stock_balance(masapi::GetStockBalanceRequest { msisdn: None })
            .await
            .unwrap()
            .get_ref()
            .clone()
            .balance
            .parse::<f64>()
            .unwrap();

        let amount_to_sell = opening_balance + 5.0;

        match client
            .sell_airtime(masapi::SellAirtimeRequest {
                amount: amount_to_sell.to_string(),
                msisdn: "0820000020".to_string(),
                latitude: Some(78.789),
                longitude: Some(14.456),
            })
            .await
        {
            Ok(_) => panic!("Should have failed, can't sell more that balance."),
            Err(_e) => (),
        }
    }

    #[tokio::test]
    async fn get_balance_then_sell_retail_and_get_balance_test() {
        let channel = create_channel().await;
        let amount_to_sell = 5.0;

        let (_agent_id, _msisdn, token) =
            login("0820000015".to_string(), "12345".to_string()).await;
        let otp_request = masapi::LoginRequest {
            msisdn: "0820000015".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        client.submit_otp(otp_request).await.unwrap();

        let opening_balance = client
            .get_stock_balance(masapi::GetStockBalanceRequest { msisdn: None })
            .await
            .unwrap()
            .get_ref()
            .clone()
            .balance
            .parse::<f64>()
            .unwrap();

        let sell_response = client
            .sell_airtime(masapi::SellAirtimeRequest {
                amount: amount_to_sell.to_string(),
                msisdn: "+270820000020".to_string(),
                latitude: Some(78.789),
                longitude: Some(14.456),
            })
            .await
            .unwrap();

        log::info!("{:?}", sell_response);

        let new_balance = client
            .get_stock_balance(masapi::GetStockBalanceRequest { msisdn: None })
            .await
            .unwrap()
            .get_ref()
            .clone()
            .balance
            .parse::<f64>()
            .unwrap();

        println!(
            "opening_balance: {},\n amount_sold : {},\n new_balance: {}",
            opening_balance, amount_to_sell, new_balance
        );

        println!(
            "(opening_balance + amount_to_sell) : {}",
            opening_balance + amount_to_sell
        );

        assert!((new_balance - (opening_balance - amount_to_sell)).abs() < 0.0001);
    }

    #[tokio::test]
    async fn submit_otp_with_no_msisdn_test() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000015".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        match client.submit_otp(otp_request).await {
            Err(err) => {
                assert_eq!(err.code(), Code::InvalidArgument);
                assert_eq!(err.message(), "otp and msisdn are mandatory");
            }
            Ok(opt_response) => {
                let opt_response = opt_response.get_ref();

                panic!(
                    "No response should have been received.  Got {:?}  instead",
                    opt_response
                );
            }
        };
    }

    #[tokio::test]
    async fn submit_otp_test() {
        let (_agent_id, msisdn, token) = login("0820000015".to_string(), "12345".to_string()).await;

        let channel = create_channel().await;

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_request = masapi::LoginRequest {
            msisdn,
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        match client.submit_otp(otp_request).await {
            Err(err) => {
                assert_eq!(0, 1, "submit_otp_response should not be {:?}", err);
            }
            Ok(opt_response) => {
                let opt_response = opt_response.get_ref();

                let auth_status: masapi::AuthenticationStatus =
                    masapi::AuthenticationStatus::from_i32(opt_response.authentication_status)
                        .unwrap();
                assert_eq!(auth_status, masapi::AuthenticationStatus::Authenticated);
            }
        };
    }

    #[tokio::test]
    async fn successful_login_test() {
        let channel = create_channel().await;
        let valid_login_request = masapi::LoginRequest {
            msisdn: "0820000015".into(),
            username: "".into(),
            password: "".into(),
            pin: "12345".into(),
            one_time_pin: "".into(),
        };
        let mut client = MasClient::new(channel);

        let login_response = client.login(valid_login_request).await.unwrap();

        let login_response = &login_response.get_ref();

        let auth_status: masapi::AuthenticationStatus =
            masapi::AuthenticationStatus::from_i32(login_response.authentication_status).unwrap();

        assert_eq!(auth_status, masapi::AuthenticationStatus::RequireOtp);
    }

    #[tokio::test]
    async fn wrong_msisdn_login_test() {
        let channel = create_channel().await;

        let login_request = masapi::LoginRequest {
            msisdn: "0820900015".into(),
            username: "".into(),
            password: "".into(),
            pin: "12345".into(),
            one_time_pin: "".into(),
        };

        let mut client = MasClient::new(channel);

        let login_response = client.login(login_request).await;

        match login_response {
            Ok(_login_response) => {
                assert_eq!(0, 1, "login_response should not be Ok!");
            }
            Err(error) => {
                assert_eq!(error.code(), Code::Unauthenticated);
                assert_eq!(
                    error.message(),
                    "Crediverse Error: \"CREDENTIALS_INVALID\"".to_string()
                );
            }
        };
    }

    #[tokio::test]
    async fn wrong_pin_login_test() {
        let channel = create_channel().await;
        let login_request = masapi::LoginRequest {
            msisdn: "0820000015".into(),
            username: "".into(),
            password: "".into(),
            pin: "99999".into(),
            one_time_pin: "".into(),
        };

        let mut client = MasClient::new(channel);

        let login_response = client.login(login_request).await;

        match login_response {
            Ok(_login_response) => {
                assert_eq!(0, 1, "login_response should not be Ok!");
            }
            Err(status) => {
                assert_eq!(status.code(), Code::Unauthenticated);
                assert_eq!(
                    status.message(),
                    "Crediverse Error: \"CREDENTIALS_INVALID\"".to_string()
                );
            }
        };
    }

    #[tokio::test]
    async fn change_pin_test() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000014".to_string(), "12345".to_string()).await;
        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let _otp_response = otp_response.get_ref();

        match client
            .change_pin(masapi::ChangePinRequest {
                new_pin: "54321".to_string(),
            })
            .await
        {
            Ok(_) => (),
            Err(e) => {
                println!("change pin error:  {}", e);
                panic!()
            }
        }
    }

    #[tokio::test]
    async fn update_profile_test() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0140821780".to_string(), "00000".to_string()).await;
        let otp_request = masapi::LoginRequest {
            msisdn: "0140821780".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let _otp_response = otp_response.get_ref();

        match client
            .update_profile(masapi::UpdateProfileRequest {
                title: "Mr".into(),
                language: "English".into(),
                first_name: "Robert".into(),
                surname: "Hank".into(),
                email: "robertfrank@gmail.com".into(),
                agent_id: "1034".into(),
            })
            .await
        {
            Ok(_) => (),
            Err(e) => {
                println!("Update profile Error:  {}", e);
                panic!()
            }
        }
    }

    #[tokio::test]
    async fn change_pin_invalid_test() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000014".to_string(), "12345".to_string()).await;
        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let _otp_response = otp_response.get_ref();

        match client
            .change_pin(masapi::ChangePinRequest {
                new_pin: "222221234".to_string(),
            })
            .await
        {
            Ok(result) => {
                println!("change pin result = {:?}", result);
                panic!("Change pin should have failed for the invalid pin")
            }
            Err(e) => {
                assert_eq!(e.code(), Code::Internal);
            }
        }
    }

    #[tokio::test]
    async fn mobile_money_successful_login_test() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000014".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let otp_response = otp_response.get_ref();

        let agent_id = &otp_response.agent_id;

        let mm_login_response = client
            .mobile_money_login(masapi::MobileMoneyLoginRequest {
                username: String::from("CREDIVERSE_TEST"),
                password: String::from("crediverse@2023"),
            })
            .await
            .unwrap();

        let mm_login_response = &mm_login_response.get_ref();

        let token: String = mm_login_response.login_token.clone();
        println!("Status code: {}", mm_login_response.status.clone());
        println!("Token received: {}", token);
        assert_ne!(token.len(), 0);
    }

    #[tokio::test]
    async fn mobile_money_failed_login_test() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000014".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let otp_response = otp_response.get_ref();

        let agent_id = &otp_response.agent_id;

        let mm_login_response = client
            .mobile_money_login(masapi::MobileMoneyLoginRequest {
                username: String::from("CREDIVERSE_TEST"),
                password: String::from("crediverse@202"),
            })
            .await;

        match mm_login_response {
            Ok(result) => {
                let result_rec = result.into_inner();
                println!("Login result = {:?}", result_rec);
                if result_rec.status == masapi::MobileMoneyResponseStatus::MmSuccess.into() {
                    panic!("Login should have failed for the invalid password")
                }
            }
            Err(e) => {
                assert_eq!(e.code(), Code::Unauthenticated);
            }
        }
    }

    #[tokio::test]
    async fn mobile_money_cash_deposit_test() {
        let channel = create_channel().await;

        let (_agent_id, _msisdn, token) =
            login("0820000014".to_string(), "12345".to_string()).await;

        let otp_request = masapi::LoginRequest {
            msisdn: "0820000014".into(),
            username: "".into(),
            password: "".into(),
            pin: "".into(),
            one_time_pin: "99999".into(),
        };

        let mut client = MasClient::with_interceptor(channel.clone(), |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let otp_response = client.submit_otp(otp_request).await.unwrap();

        let otp_response = otp_response.get_ref();

        let agent_id = &otp_response.agent_id;

        let mm_login_response = client
            .mobile_money_login(masapi::MobileMoneyLoginRequest {
                username: String::from("CREDIVERSE_TEST"),
                password: String::from("crediverse@2023"),
            })
            .await
            .unwrap();

        let mm_login_response = &mm_login_response.get_ref();

        let token: String = mm_login_response.login_token.clone();
        assert_ne!(token.len(), 0);

        let mut client = MasClient::with_interceptor(channel, |request: Request<()>| {
            add_metadata(request, token.to_string())
        });

        let mm_deposit_response = client
            .mobile_money_deposit(masapi::MobileMoneyDepositRequest {
                amount: String::from("100"),
                destination_msisdn: String::from("0820000015"),
            })
            .await
            .unwrap();

        let mm_deposit_response = &mm_deposit_response.get_ref();

        let status: masapi::MobileMoneyResponseStatus =
            masapi::MobileMoneyResponseStatus::from_i32(mm_deposit_response.status).unwrap();

        assert_eq!(status, masapi::MobileMoneyResponseStatus::MmSuccess);
    }
}
