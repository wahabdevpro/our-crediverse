# Summary
## Test Description
As a retailer agent with sufficient funds, I want to successfully sell airime to an active subscriber
## Preconditions
-	Agent has sufficient funds for transaction
-	Agent is logged in to the smartapp
## Dependencies
-	[[Login to SmartApp with valid Credentials]]
## Postcondition


# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to Airtime tab on Smartapp |  | Agent is asked to enter the subscriber MSISDN and amount | |  | |
| 2 | Enter the MSISDN of the subscriber and the amount and tap Sell| Subscriber MSISDN | Applications asks for the confirmation of the transaction |  | | |
| 3 | Confirm the transaction | Subscriber MSISDN | Airtime sold to the subscriber\n Agent account is debited with transaction amount\n Sms is sent to the agent and the subscriber\n Agent balance is updated on the smartapp\n TDR written for the transaction |  | | |
