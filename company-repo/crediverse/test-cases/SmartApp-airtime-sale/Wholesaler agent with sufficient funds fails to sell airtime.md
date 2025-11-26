# Summary
## Test Description
As a wholesaler agent, I want to see correct error message when the transction fails due to no such transfer rule
## Preconditions
- 	Agent has sufficient funds for transaction
-	Agent is logged in to the smartapp
## Dependencies
-	[[Login to SmartApp with valid Credentials]]
## Postcondition


# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to Airtime tab on Smartapp |  | Agent is asked to enter the subscriber MSISDN and amount | |  | |
| 2 | Enter the MSISDN of the subscriber and the amount and tap Sell| Subscriber MSISDN | Applications asks for the confirmation of the transaction |  | | |
| 3 | Confirm the transaction | Subscriber MSISDN | Airtime sale transaction failed with error message "You are not allowed to perform this transaction"\n TDR written for the transaction |  | | |
