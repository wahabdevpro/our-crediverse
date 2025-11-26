# Summary
## Test Description
As a Crediverse Agent with a sufficient balance, i want to perform a self-top-up on the SmartApp.
## Preconditions
-   The Agent is logged into the SmartApp
-   The Agent has a sufficient balance to perform a self top up
- 
## Dependencies

## Postcondition
-   Verify TDR written has correct details of the self top-up transaction performed on the SmartApp
# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Click the Sell button  | | Sell Airtime screen is displayed with Agent`s Balance displayed |  |  | |
| 2 | Enter Agent`s MSISDN in the recipient field |   | MSISDN captured successfully |  |    |  |
| 3 | Enter amount in the sale amount field |   | Amount captured successfully and Sell button is enabled |  |    |  |
| 4 | Click on the Sell button |   | A confirmation screen with details of the purchase to be made is displayed |  |    |  |
| 5 | Click OK button |   | A success message is displayed with details of the purchase made |  |    |  |