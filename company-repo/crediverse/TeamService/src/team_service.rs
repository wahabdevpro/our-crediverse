pub mod team_service_api {
    tonic::include_proto!("team_service_api");
}

use team_service_api::*;
use tonic::Response;
use tonic::Status;

use crate::team_service::team_service_api_server::TeamServiceApi;

use mysql_async::params;
use mysql_async::prelude::{Query, WithParams};
use std::error::Error;
use std::fmt;

#[derive(Debug)]
pub struct TeamServiceError {
    pub description: String,
}

impl fmt::Display for TeamServiceError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "TeamService Error: {:?}", self.description.to_string())
    }
}

impl Error for TeamServiceError {}

impl From<mysql_async::Error> for TeamServiceError {
    fn from(error: mysql_async::Error) -> Self {
        TeamServiceError {
            description: format!("TeamService Error: {}", error.to_string()),
        }
    }
}

impl From<mysql_async::FromRowError> for TeamServiceError {
    fn from(error: mysql_async::FromRowError) -> Self {
        TeamServiceError {
            description: format!("TeamService Error: {}", error.to_string()),
        }
    }
}

fn format_amount(amount: i64) -> String {
    let mut amount = amount.to_string();
    amount.push_str(".0000");
    amount
}

pub struct TeamServiceImpl {}

#[tonic::async_trait]
impl TeamServiceApi for TeamServiceImpl {
    async fn is_team_lead(
        &self,
        request: tonic::Request<TeamLead>,
    ) -> Result<tonic::Response<IsTeamLead>, Status> {
        log::trace!("is team lead request: {:?}", request.get_ref());
        match crate::TEAM_SERVICE_DB_CONNECTION_POOL.get_conn().await {
            Ok(conn) => {
                let request = request.get_ref();

                let team_lead_agent_id = &request.agent_id.to_string();

                let test_team_lead_sql =
                    "SELECT EXISTS (SELECT * FROM Agent WHERE team_lead_agent_id=:team_lead_agent_id)";

                let test_team_lead_sql = test_team_lead_sql.with(params! {
                    "team_lead_agent_id" =>team_lead_agent_id
                });

                match test_team_lead_sql.map(conn, |is_lead: u32| is_lead).await {
                    Ok(is_lead) => Ok(Response::new(IsTeamLead {
                        is_team_lead: is_lead[0] > 0,
                    })),
                    Err(e) => Err(Status::internal(e.to_string())),
                }
            }
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    async fn get_team_agent_ids(
        &self,
        request: tonic::Request<TeamLead>,
    ) -> Result<tonic::Response<AgentIds>, tonic::Status> {
        log::trace!("get team agent_ids: {:?}", request.get_ref());
        match crate::TEAM_SERVICE_DB_CONNECTION_POOL.get_conn().await {
            Ok(conn) => {
                let request = request.get_ref();

                let team_lead_agent_id = &request.agent_id.to_string();

                let get_team_agent_ids_sql =
                    "SELECT agent_id from Agent where team_lead_agent_id = :team_lead_agent_id;";

                let get_team_agent_ids_sql = get_team_agent_ids_sql.with(params! {
                    "team_lead_agent_id" =>team_lead_agent_id
                });

                match get_team_agent_ids_sql.map(conn, |agent_id| agent_id).await {
                    Ok(agent_ids) => {
                        if agent_ids.len() == 0 {
                            Err(Status::not_found(format!(
                                "No team found for team_lead: {}",
                                team_lead_agent_id
                            )))
                        } else {
                            Ok(Response::new(AgentIds { agent_ids }))
                        }
                    }
                    Err(e) => Err(Status::internal(e.to_string())),
                }
            }
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    async fn add_team_member(
        &self,
        request: tonic::Request<MembershipRequest>,
    ) -> Result<tonic::Response<Ok>, tonic::Status> {
        log::trace!("add team member: {:?}", request.get_ref());
        match crate::TEAM_SERVICE_DB_CONNECTION_POOL.get_conn().await {
            Ok(conn) => {
                let request = request.get_ref();
                let member_agent_id = request
                    .member
                    .clone()
                    .ok_or(Status::invalid_argument(
                        "a member agent_id is required, consult the team_service_api.proto file",
                    ))?
                    .agent_id
                    .to_string();

                let team_lead_agent_id = &request
                    .team_lead
                    .clone()
                    .ok_or(Status::invalid_argument(
                        "a team_lead agent_id is required, consult the team_service_api.proto file",
                    ))?
                    .agent_id
                    .to_string();

                let insert_team_member_sql = "INSERT INTO Agent (agent_id, team_lead_agent_id) \
                    VALUES(:member_agent_id,:team_lead_agent_id) \
                    ON DUPLICATE KEY UPDATE agent_id=:member_agent_id, team_lead_agent_id=:team_lead_agent_id";

                let insert_team_member_sql = insert_team_member_sql.with(params! {
                    "member_agent_id" => member_agent_id ,
                    "team_lead_agent_id" =>team_lead_agent_id
                });

                match insert_team_member_sql.ignore(conn).await {
                    Ok(()) => Ok(Response::new(team_service_api::Ok {})),
                    Err(e) => Err(Status::internal(e.to_string())),
                }
            }
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }
    async fn delete_team(
        &self,
        request: tonic::Request<TeamLead>,
    ) -> Result<tonic::Response<Ok>, tonic::Status> {
        log::trace!("delete team request: {:?}", request.get_ref());
        match crate::TEAM_SERVICE_DB_CONNECTION_POOL.get_conn().await {
            Ok(conn) => {
                let request = request.get_ref();
                let team_lead_agent_id = &request.agent_id;

                let delete_team_sql = "DELETE FROM Agent \
                    WHERE team_lead_agent_id = :team_lead_agent_id;";

                let delete_team_sql = delete_team_sql.with(params! {
                    "team_lead_agent_id" =>team_lead_agent_id.clone()
                });

                match delete_team_sql.run(conn).await {
                    Ok(result) => {
                        if result.affected_rows() <= 0 {
                            Err(Status::not_found(format!(
                                "No team membership found for team_lead: {}",
                                team_lead_agent_id
                            )))
                        } else {
                            Ok(Response::new(team_service_api::Ok {}))
                        }
                    }
                    Err(e) => Err(Status::internal(e.to_string())),
                }
            }
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }
    async fn remove_member(
        &self,
        request: tonic::Request<MembershipRequest>,
    ) -> Result<tonic::Response<Ok>, tonic::Status> {
        log::trace!("remove member request: {:?}", request.get_ref());
        match crate::TEAM_SERVICE_DB_CONNECTION_POOL.get_conn().await {
            Ok(conn) => {
                let request = request.get_ref();
                let member_agent_id = request
                    .member
                    .clone()
                    .ok_or(Status::invalid_argument(
                        "a member agent_id is required, consult the team_service_api.proto file",
                    ))?
                    .agent_id
                    .to_string();

                let team_lead_agent_id = &request
                    .team_lead
                    .clone()
                    .ok_or(Status::invalid_argument(
                        "a team_lead agent_id is required, consult the team_service_api.proto file",
                    ))?
                    .agent_id
                    .to_string();

                let delete_team_member_sql = "DELETE FROM Agent \
                    WHERE agent_id = :member_agent_id AND team_lead_agent_id = :team_lead_agent_id;";

                let delete_team_member_sql = delete_team_member_sql.with(params! {
                    "member_agent_id" => member_agent_id.clone() ,
                    "team_lead_agent_id" =>team_lead_agent_id.clone()
                });

                match delete_team_member_sql.run(conn).await {
                    Ok(result) => {
                        if result.affected_rows() <= 0 {
                            Err(Status::not_found(format!(
                                "No team membership found for member {} of team_lead: {}",
                                member_agent_id, team_lead_agent_id
                            )))
                        } else {
                            Ok(Response::new(team_service_api::Ok {}))
                        }
                    }
                    Err(e) => Err(Status::internal(e.to_string())),
                }
            }
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    async fn get_team_members(
        &self,
        request: tonic::Request<TeamLead>,
    ) -> Result<tonic::Response<TeamMembers>, tonic::Status> {
        log::trace!("get team members request: {:?}", request.get_ref());
        match crate::TEAM_SERVICE_DB_CONNECTION_POOL.get_conn().await {
            Ok(conn) => {
                let request = request.get_ref();

                let team_lead_agent_id = &request.agent_id.to_string();

                let get_team_members_sql =
                    "SELECT agent_id, daily_sales_target_amount, weekly_sales_target_amount, monthly_sales_target_amount  from Agent where team_lead_agent_id = :team_lead_agent_id;";

                let get_team_members_sql = get_team_members_sql.with(params! {
                    "team_lead_agent_id" =>team_lead_agent_id
                });

                match get_team_members_sql
                    .map(conn, |row| {
                        let (
                            agent_id,
                            daily_sales_target_amount,
                            weekly_sales_target_amount,
                            monthly_sales_target_amount,
                        ): (u64, Option<i64>, Option<i64>, Option<i64>) =
                            mysql_async::from_row(row);

                        let sales_targets = if daily_sales_target_amount.is_none()
                            && weekly_sales_target_amount.is_none()
                            && monthly_sales_target_amount.is_none()
                        {
                            None
                        } else {
                            Some(SalesTargets {
                                daily_amount: daily_sales_target_amount
                                    .map(|amount| format_amount(amount)),
                                weekly_amount: weekly_sales_target_amount
                                    .map(|amount| format_amount(amount)),
                                monthly_amount: monthly_sales_target_amount
                                    .map(|amount| format_amount(amount)),
                            })
                        };

                        TeamMember {
                            agent_id,
                            sales_targets: sales_targets,
                        }
                    })
                    .await
                {
                    Ok(members) => {
                        if members.len() == 0 {
                            Err(Status::not_found(format!(
                                "No team found for team_lead: {}",
                                team_lead_agent_id
                            )))
                        } else {
                            Ok(Response::new(TeamMembers { members }))
                        }
                    }
                    Err(e) => Err(Status::internal(e.to_string())),
                }
            }
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    async fn get_membership(
        &self,
        request: tonic::Request<Member>,
    ) -> Result<tonic::Response<TeamMembership>, tonic::Status> {
        match crate::TEAM_SERVICE_DB_CONNECTION_POOL.get_conn().await {
            Ok(conn) => {
                let request = request.get_ref();

                let team_member_agent_id = request.agent_id.to_string();

                let get_team_members_sql =
                    "SELECT agent_id, team_lead_agent_id, daily_sales_target_amount, weekly_sales_target_amount, monthly_sales_target_amount from Agent where agent_id= :team_member_agent_id;";

                let get_team_members_sql = get_team_members_sql.with(params! {
                    "team_member_agent_id" => team_member_agent_id.clone(),
                });

                match get_team_members_sql
                    .map(conn, |row| {
                        let (
                            agent_id,
                            team_lead_agent_id,
                            daily_sales_target_amount,
                            weekly_sales_target_amount,
                            monthly_sales_target_amount,
                        ): (
                            u64,
                            u64,
                            Option<i64>,
                            Option<i64>,
                            Option<i64>,
                        ) = mysql_async::from_row(row);

                        log::trace!("daily_sales_target_amount: {:?}", daily_sales_target_amount);
                        log::trace!(
                            "weekly_sales_target_amount: {:?}",
                            weekly_sales_target_amount
                        );
                        log::trace!(
                            "monthly_sales_target_amount: {:?}",
                            monthly_sales_target_amount
                        );

                        let sales_targets = if daily_sales_target_amount.is_none()
                            && weekly_sales_target_amount.is_none()
                            && monthly_sales_target_amount.is_none()
                        {
                            None
                        } else {
                            Some(SalesTargets {
                                daily_amount: daily_sales_target_amount
                                    .map(|amount| format_amount(amount)),
                                weekly_amount: weekly_sales_target_amount
                                    .map(|amount| format_amount(amount)),
                                monthly_amount: monthly_sales_target_amount
                                    .map(|amount| format_amount(amount)),
                            })
                        };

                        let member = Member { agent_id };

                        let team_lead = TeamLead {
                            agent_id: team_lead_agent_id,
                        };

                        TeamMembership {
                            member: Some(member),
                            team_lead: Some(team_lead),
                            sales_targets: sales_targets,
                        }
                    })
                    .await
                {
                    Ok(members) => {
                        println!("team members: {:?}", members);
                        if members.len() == 0 {
                            Err(Status::not_found(format!(
                                "No team membership found for agent_id: {}",
                                team_member_agent_id
                            )))
                        } else if members.len() > 1 {
                            Err(Status::not_found(format!(
                                "Multiple memberships found for agent_id {}, this should never happen",
                                team_member_agent_id.clone()
                            )))
                        } else {
                            Ok(Response::new(members[0].clone()))
                        }
                    }
                    Err(e) => Err(Status::internal(e.to_string())),
                }
            }
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    async fn set_team_member_sales_target(
        &self,
        request: tonic::Request<MemberSetSalesTargetRequest>,
    ) -> Result<tonic::Response<Ok>, tonic::Status> {
        log::trace!("set team sales target request: {:?}", request.get_ref());
        match crate::TEAM_SERVICE_DB_CONNECTION_POOL.get_conn().await {
            Ok(conn) => {
                let request = request.get_ref();
                let team_member_agent_id = &request
                    .member
                    .clone()
                    .ok_or(Status::invalid_argument(
                        "a member agent_id is required, consult the team_service_api.proto file",
                    ))?
                    .agent_id
                    .to_string();

                let team_lead_agent_id = &request
                    .team_lead
                    .clone()
                    .ok_or(Status::invalid_argument(
                        "a team_lead agent_id is required, consult the team_service_api.proto file",
                    ))?
                    .agent_id
                    .to_string();

                let target_column = match Period::from_i32(request.period) {
                    Some(Period::Day) => String::from("daily_sales_target_amount"),
                    Some(Period::Week) => String::from("weekly_sales_target_amount"),
                    Some(Period::Month) => String::from("monthly_sales_target_amount"),
                    None => {
                        String::from("weekly_sales_target_amount") // default to weekly for now
                    }
                };

                // check if team member exists
                let get_team_members_sql =
                    "SELECT COUNT(*) row_count FROM Agent \
                        WHERE team_lead_agent_id = :team_lead_agent_id AND agent_id = :team_member_agent_id;";

                let get_team_members_sql = get_team_members_sql.with(params! {
                    "team_lead_agent_id" => team_lead_agent_id.clone(),
                    "team_member_agent_id" => team_member_agent_id.clone(),
                });

                let result = get_team_members_sql.first::<i64, _>(conn).await;

                match result {
                    Ok(row) => {
                        let row_count = row.unwrap_or(0);
                        if row_count != 1 {
                            return Err(Status::not_found(format!(
                                "No team membership found for team lead {}, agent_id: {}",
                                team_lead_agent_id.clone(),
                                team_member_agent_id.clone()
                            )));
                        }
                    }
                    Err(err) => {
                        return Err(Status::internal(err.to_string()));
                    }
                }
                ////

                let update_team_member_sql = format!("UPDATE Agent \
                    SET {} = :target_amount  WHERE agent_id = :member_agent_id AND team_lead_agent_id = :team_lead_agent_id;", 
                    target_column);

                let update_team_member_sql = update_team_member_sql.with(params! {
                    "target_amount" => request.target_amount.clone(),
                    "member_agent_id" => team_member_agent_id .clone() ,
                    "team_lead_agent_id" =>team_lead_agent_id.clone()
                });

                match crate::TEAM_SERVICE_DB_CONNECTION_POOL.get_conn().await {
                    Ok(conn) => match update_team_member_sql.run(conn).await {
                        Ok(_) => Ok(Response::new(team_service_api::Ok {})),
                        Err(e) => Err(Status::internal(e.to_string())),
                    },
                    Err(e) => Err(Status::internal(e.to_string())),
                }
            }
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }
}
