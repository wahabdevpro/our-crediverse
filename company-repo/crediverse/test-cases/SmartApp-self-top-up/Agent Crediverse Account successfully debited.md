# Summary
## Test Description
As a Crediverse Administrator , i want to verify if the correct amount has been deducted from a Crediverse Agent's account according to the self-top-up transaction the agent performed.
## Preconditions
-   The Admin is logged into Crediverse and has permissions to view Transactions
## Dependencies
- Agent has sufficient balance
## Postcondition
-  Verify TDR written has correct details of the self top-up transaction performed on the SmartApp
# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Click on the Transaction tab  | | The Transaction page is displayed with list of last 10 transactions in the system displayed |  |  | |
| 2 | Enter agent MSISDN in the MSISDN field and press enter to search |   | Agents last 10 transactions performed is displayed |  |    |  |
| 3 | Click on the transaction number of the transaction to be queried |   | Full record of the transaction is displayed |  |	 |  |
| 4 | Inspect the balance before and balance after of the A-party |   | The difference between the balance before and balance After must be equal to the amount the agent purchased  |  |    |  |