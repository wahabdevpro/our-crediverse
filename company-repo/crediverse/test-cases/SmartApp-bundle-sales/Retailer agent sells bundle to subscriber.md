# Summary
## Test Description
View all the bundles available for the subscriber to sell
## Preconditions
-	Agent is logged in to the smartapp
## Dependencies
-	[[Login to SmartApp with valid Credentials]]
## Postcondition


# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to Bundle Sale on Smartapp |  | Agent is asked to enter the subscriber MSISDN | |  | |
| 2 | Enter the MSISDN of the subscriber and tap enter| Subscriber MSISDN | List of bundles available for the subscriber are presented on the screen |  | | |
| 3 | Select bundle from the list| Subscriber MSISDN | Bundle is sold to the subscriber\n Agent account is debited with bundle charge\n Sms is sent to the agent and the subscriber\n Agent balance is updated on the smartapp |  | | |
