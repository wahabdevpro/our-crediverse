# Crediverse Team Service (*draft*)

## Run

```
cargo run --bin team-service
```


## User story
As a team leader and user of the Smart app I would like to get the usage statistics and stock levels of my team. 

## Questions
* Team Service or Groups Service?
* gRPC or REST?

## Assumptions
* An Agent can only belong to one team.
* An Agent can only be the team lead of one team. 
* An team lead can be member of another team. 
* The validity of msisdns are assumed to be managed by the clients of the Team Service.
* This is a back-end service and the assumption is that it's clients have the authority to address it in any way, it doesn't do any authorization by itself.

## Model
@startuml

Class Agent {
        msisdn: String
        teamlead_msisdn: String
    }

Agent --> Agent: teamlead

@enduml

---

## Interface

### Getting the MSISDNs of a team
`get_team_msisdns(teamlead_msisdn) -> [member_msisdn] or ERR` 
* returns all the msisdns that have the supplied teamlead_msisdn as teamlead_msisdn 
* returns error when the teamlead_msisdn does not exist. 


**NOTE:** 
*The next 3 interface methods are not strictly needed but might be
useful to populate and maintain the DB while assuring integrity, it should not
be more difficult to use them than pure SQL*

### Adding a MSISDN to a team
`add_team_member(teamlead_msisdn, new_member_msisdn) -> OK or ERR` 
* adding a member to a team and removing him from current team if any and returns OK
* returns error if teamlead_msisdn does not exist.


### Deleting a team from the team lead MSISDN 
`delete_team(teamlead_msisdn) -> OK or ERR` 
* removing a team and returns OK
* returns error if teamlead_msisdn does not exist.

### Deleting a member MSISDN from a team
`remove_member(teamlead_msisdn, member_to_remove_msisdn) -> OK or ERR` 
* removing the supplied team member from the team
* returns error if the team_lead_msisdn doesn't exist
* returns error if the member_to_remove_msisdn doesn't exist or is not in the team

## Service context


@startuml
[MAS] as mas 

[Team Service] as ts 

() "HTTP" as tsAPI

ts -> tsAPI



database "Maria DB" {
  database "Teams DB"  as tsdb
  database "Crediverse Query Slave"  as qs
}

mas ..> tsAPI: gets team\nmsisdns

mas .> qs: gets stats and \n stock levels for msisdns

ts ..> tsdb: team\n CRUD

@enduml

---
## Implementation Notes

* Rust sidecar service to Crediverse 
* Performance is a primary concern

 
