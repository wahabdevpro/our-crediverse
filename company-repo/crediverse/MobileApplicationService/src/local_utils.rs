use std::env;

use chrono::{DateTime, NaiveDateTime, Utc};

pub fn date_from_epoch(epoch: u64) -> String {
    let naive = NaiveDateTime::from_timestamp_opt(epoch as i64, 0).unwrap();
    let datetime: DateTime<Utc> = DateTime::from_utc(naive, Utc);
    let newdate = datetime.format("%Y-%m-%d %H:%M:%S");

    format!("{}", newdate)
}

pub fn get_env_config(var_name: String, default_value: String) -> String {
    match env::var(var_name.clone()) {
        Ok(value) => value,
        Err(_e) => {
            println!("The {} environment variable was not set", var_name);
            default_value
        }
    }
}
