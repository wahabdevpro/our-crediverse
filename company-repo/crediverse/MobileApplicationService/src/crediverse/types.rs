use std::error::Error;
use std::fmt;

#[derive(Clone)]
pub enum Subject {
    Agent(u64),
    Team(Vec<u64>),
}

impl fmt::Display for Subject {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Subject::Agent(msisdn) => write!(f, "Agent Id {}", msisdn),
            Subject::Team(team) => {
                let team_agent_ids_string: String = team
                    .iter()
                    .map(|num| num.to_string())
                    .collect::<Vec<String>>()
                    .join(",");

                write!(f, "Team Agent ids ({}),", team_agent_ids_string)
            }
        }
    }
}
#[derive(Default, Debug, Clone, PartialEq, Eq)]
pub struct CrediverseAgent {
    pub first_name: String,
    pub surname: String,
    pub msisdn: String,
    pub stock_balance: CrediverseStockBalance,
}

#[derive(Default, Debug, Clone, PartialEq, Eq)]
pub struct CrediverseStockBalance {
    pub balance: String,
    pub bonus_balance: String,
    pub on_hold_balance: String,
}

#[derive(Debug)]
pub struct CrediverseDbError {
    pub description: String,
}

impl fmt::Display for CrediverseDbError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(
            f,
            "Crediverse Database Error: {:?}",
            self.description.to_string()
        )
    }
}

impl Error for CrediverseDbError {}

impl From<mysql_async::Error> for CrediverseDbError {
    fn from(error: mysql_async::Error) -> Self {
        CrediverseDbError {
            description: format!("CrediverseDbError Error: {}", error),
        }
    }
}
