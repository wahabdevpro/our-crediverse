# Summary
## Test Description
The details for the created agent can be viewed.
## Preconditions
-   Admin user must be able to login and view the agents.
-   The agent must be created.
## Dependencies
[[Add a new agent with the MSISDN not in use]]
## Postcondition
-   [[New Agent can trade]]
-   Transaction history of the new agent is available on the GUI

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to the Agents Profile screen | | Page loads and displays successfully with a search field | Agent Management page displayed successfully | Pass | |
| 2 | Enter New Agent created MSISDN in the search field to search for the agent | Search for MSISDN: 23456781090 | Search result with search Agent MSISDN returned in search | Results containing added Agent return in search results |	Pass |  |
| 3 | Click the view option to view the Agents' full details. | Click view details on the interested Agent under test: 23456781090 | The Admin is presented with a screen with full details of the Agent using the MSISDN | Agent details page loads successfully <br/>Agent Account full details displayed. | Pass | |
