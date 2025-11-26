# Summary
## Test Description
As an agent, I want to verify that the monthly sales summary volume for bundle sales for this month and last month in the stats section of the SmartApp are calculated correctly. 
## Preconditions
-   The Agent is logged in to SmartApp
-   The MSISDN is not used by any other agent

## Dependencies

## Postcondition
-   The Agent can view the monthly sales summary
-   The Agent is assigned with a unique agent-id by the system.

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Agent has logged in to the SmartApp | | At the bottom the agent is presented with more button |  |  | |
| 2 | Click on the more button | | The stats section is dispalyed  |  |  | |
| 3 | Navigate to monthly tab under the stats section |  | The bundle sales for this month and last month are dispalyed successfully |  |	 |  |
| 4 | Verify the volume total for this month and last month  |  | The volume for bundle sales for this month and last month should have the correct values |  |	 |  | 