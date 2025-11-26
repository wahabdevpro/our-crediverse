# Summary
## Test Description
A Crediverse Agent with an insufficient balance fails to perform a self-top-up via the SmartApp.
## Preconditions
-   The Agent is logged into the SmartApp
-   The Agent has insufficient balance to perform self-top-up
## Dependencies

## Postcondition
-   
# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Click the Sell button  | | Sell Airtime screen is displayed with Agent`s Balance displayed |  |  | |
| 2 | Enter Agent`s MSISDN in the recipient field |   | MSISDN captured successfully |  |    |  |
| 3 | Enter amount in the sale amount field |   | Amount captured successfully and Sell button is enabled |  |    |  |
| 4 | Click on the Sell button |   | A confirmation screen with details of the purchase to be made is displayed |  |    |  |
| 5 | Click OK button |   | A success message similar to "Sorry, you have insufficient funds to purchase" is displayed |  |    |  |