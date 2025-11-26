use std::{
    env,
    sync::{Arc, Mutex, Once},
};

use mysql_async::Pool;

use super::types::*;

fn create_connection_pool() -> Pool {
    let hostname = match env::var("CREDIVERSE_STATS_DB_HOST") {
        Ok(hostname) => hostname,
        Err(_e) => {
            log::warn!("The CREDIVERSE_STATS_DB_HOST environment variable was not set, defaulting to `localhost`");
            "localhost".to_string()
        }
    };

    let db_name = match env::var("CREDIVERSE_STATS_DB_NAME") {
        Ok(db_name) => db_name,
        Err(_e) => {
            log::warn!("The CREDIVERSE_STATS_DB_NAME environment variable was not set, defaulting to `hxc`");
            "hxc".to_string()
        }
    };

    let db_user = match env::var("CREDIVERSE_STATS_DB_USER") {
        Ok(db_user) => db_user,
        Err(_e) => {
            log::warn!("The CREDIVERSE_STATS_DB_USER environment variable was not set, defaulting to `root`");
            "root".to_string()
        }
    };

    let db_password = match env::var("CREDIVERSE_STATS_DB_PASSWORD") {
        Ok(password) => password,
        Err(_e) => {
            log::warn!("The CREDIVERSE_STATS_DB_PASSWORD environment variable was not set, defaulting to ... like i'm gonna tell you");
            "ussdgw".to_string()
        }
    };

    let db_port = match env::var("CREDIVERSE_STATS_DB_PORT") {
        Ok(port) => port,
        Err(_e) => {
            log::warn!(
                "The CREDIVERSE_STATS_DB_PORT environment variable was not set, defaulting to 3306"
            );
            "3306".to_string()
        }
    };

    mysql_async::Pool::from_url(format!(
        "mysql://{}:{}@{}:{}/{}",
        db_user, db_password, hostname, db_port, db_name
    ))
    .expect("Panic! Could not connect to the Crediverse Statistics Database.")
}
#[derive(Debug)]
pub struct Hxc {
    db_connection_pool: Arc<Mutex<Option<Pool>>>,
    init_db_connection_pool: Once,
}

impl Hxc {
    pub fn new() -> Self {
        Hxc {
            db_connection_pool: Arc::new(Mutex::new(None)),
            init_db_connection_pool: Once::new(),
        }
    }

    fn ensure_db_connection_pool(&self) {
        let db_connection_pool = self.db_connection_pool.clone();

        self.init_db_connection_pool.call_once(|| {
            let mut locked = db_connection_pool.lock().unwrap();
            *locked = Some(create_connection_pool());
        });
    }

    pub async fn get_connection_pool(&self) -> Result<Pool, CrediverseDbError> {
        self.ensure_db_connection_pool();

        let db_connection_pool = self.db_connection_pool.clone();
        let locked_pool = db_connection_pool.lock().unwrap();

        match (*locked_pool).clone() {
            Some(pool) => Ok(pool),
            None => Err(CrediverseDbError {
                description: "Failed to get a database connection pool".to_string(),
            }),
        }
        /*
        let connection = match (*locked_pool).clone() {
            Some(pool) => pool.get_conn().await,
            None => {
                return Err(CrediverseDbError {
                    description: format!("Failed to get a database connection"),
                })
            }
        };

        match connection {
            Ok(connection) => Ok(connection),
            Err(e) => Err(CrediverseDbError {
                description: format!("Failed to get a database connection: {}", e),
            }),
        } */
    }
}
