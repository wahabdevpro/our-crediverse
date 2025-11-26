use chrono::Local;
use std::env;
use std::fs;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let docker_tag = env::var("CI_DOCKER_TAG").unwrap_or("default_docker_tag".to_string());
    let github_tag = env::var("CI_GITHUB_TAG").unwrap_or("dev_".to_string() + &Local::now().format("%d%m%Y%H%M%S %:z").to_string());
    let branch_name = env::var("CI_BRANCH_NAME").unwrap_or("default_branch_name".to_string());
    let build_number = env::var("CI_BUILD_NUMBER").unwrap_or("default_build_number".to_string());
    let build_datetime = env::var("CI_BUILD_DATETIME").unwrap_or(Local::now().format("%Y-%m-%d %H:%M:%S %:z").to_string()); 
    let commit_ref = env::var("CI_COMMIT_REF").unwrap_or("default_commit_ref".to_string());
    
    
    // Print the values for verification
    println!("CI_DOCKER_TAG: {}", docker_tag);
    println!("CI_GITHUB_TAG: {}", github_tag);
    println!("CI_BRANCH_NAME: {}", branch_name);
    println!("CI_BUILD_NUMBER: {}", build_number);
    println!("CI_BUILD_DATETIME: {}", build_datetime);
    println!("CI_COMMIT_REF: {}", commit_ref);

    let build_info = format!(
        r#"pub mod build_info {{
            pub const CI_DOCKER_TAG: &str = "{0}";
            pub const CI_GITHUB_TAG: &str = "{1}";
            pub const CI_BRANCH_NAME: &str = "{2}";
            pub const CI_BUILD_NUMBER: &str = "{3}";
            pub const CI_BUILD_DATETIME: &str = "{4}";
            pub const CI_COMMIT_REF: &str = "{5}";
        }}"#,
        docker_tag, github_tag, branch_name, build_number, build_datetime, commit_ref
    );

    fs::write("src/build_info.rs", build_info).expect("Failed to write build_info.rs");

    tonic_build::compile_protos("proto/masapi.proto")?;
    tonic_build::compile_protos("team_service_api_proto/team_service_api.proto")?;
    tonic_build::compile_protos("credivault_api_proto/credivault_api.proto")?;
    
    Ok(())
}
