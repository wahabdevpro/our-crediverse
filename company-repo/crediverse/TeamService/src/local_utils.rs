use mysql_async::Pool;
use std::env;

pub fn create_connection_pool() -> Pool {
    let hostname = match env::var("TEAM_SERVICE_DB_HOST") {
        Ok(hostname) => hostname,
        Err(_e) => {
            log::warn!("The TEAM_SERVICE_DB_HOST environment variable was not set, defaulting to `localhost`");
            "localhost".to_string()
        }
    };

    let db_name = match env::var("TEAM_SERVICE_DB_NAME") {
        Ok(db_name) => db_name,
        Err(_e) => {
            log::warn!("The TEAM_SERVICE_DB_NAME environment variable was not set, defaulting to `team_service_db`");
            "team_service_db".to_string()
        }
    };

    let db_user = match env::var("TEAM_SERVICE_DB_USER") {
        Ok(db_user) => db_user,
        Err(_e) => {
            log::warn!(
                "The TEAM_SERVICE_DB_USER environment variable was not set, defaulting to `root`"
            );
            "root".to_string()
        }
    };

    let db_password = match env::var("TEAM_SERVICE_DB_PASSWORD") {
        Ok(password) => password,
        Err(_e) => {
            log::warn!("The TEAM_SERVICE_DB_PASSWORD environment variable was not set, defaulting to ... like i'm gonna tell you");
            "ussdgw".to_string()
        }
    };

    let db_port = match env::var("TEAM_SERVICE_DB_PORT") {
        Ok(port) => port,
        Err(_e) => {
            log::warn!(
                "The TEAM_SERVICE_DB_PORT environment variable was not set, defaulting to 3306"
            );
            "3306".to_string()
        }
    };

    format!(
        "mysql://{}:{}@{}:{}/{}",
        db_user, db_password, hostname, db_port, db_name
    );

    mysql_async::Pool::from_url(format!(
        "mysql://{}:{}@{}:{}/{}",
        db_user, db_password, hostname, db_port, db_name
    ))
    .expect("Panic! Could not connect to the Crediverse Statistics Database.")
}
