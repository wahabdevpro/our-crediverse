fn main() -> Result<(), Box<dyn std::error::Error>> {
    tonic_build::compile_protos("proto/team_service_api.proto")?;
    Ok(())
}
