# Summary
## Test Description
As an admin of Crediverse, I want to add a new Crediverse Agent using the MSISDN which is not used by any other agent in the system.
## Preconditions
-   The Admin is logged in and has the permission to add or delete an agent
-   The MSISDN is not used by any other agent
## Dependencies

## Postcondition
-   [[New Agent can trade]]
-   The agent is assigned with a unique agent-id by the system.

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to Agent Management Module | | The user is presented with the option to add a new agent | Add option presented | Pass | |
| 2 | Select the add agent option | | The user is presented with a screen to add the details including MSISDN, name state, and address of the agent | The screen loaded successfully and options were presented to the user |	Pass | Mandatory fields were highlighted |
| 3 | Enter the details of the agent | Name: John Doe<br/> MSISDN: 23456781090<br/>Address: 28 valley road, Block -1, Johannesburg, South Africa<br/>Tier: eCabine<br/>Agent Role: Agent_All<br/>Account_Status: Active | The agent is created successfully and the web user is presented with a success message | Agent details were captured and saved successfully<br/>Unique ID: 7 assigned<br/> SMS sent with pin reset information | Pass | The agent was created and Agent details appear on the Agent Account management page|
