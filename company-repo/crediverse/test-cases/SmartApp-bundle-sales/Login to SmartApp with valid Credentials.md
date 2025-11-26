# Summary
## Test Description
Login to Crediverse SmartApp with valid MSISDN and pin
## Preconditions
-	Crediverse agent has smartphone channel enabled.
## Dependencies

## Postcondition
-	Agent can view the balances

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Open the Credivese SmartApp |  MSISDN\n PIN | Login screen is presented to the agent | |  | |
| 2 | Enter the MSISDN and pin of the agent and tap Login| | OTP is sent to the Agent's mobile via SMS\n Agent is asked to enter the OTP |  | |  |
| 4 | Enter the OTP and tap Login | | Agent is successfully logged in to smartapp |  | |  |
