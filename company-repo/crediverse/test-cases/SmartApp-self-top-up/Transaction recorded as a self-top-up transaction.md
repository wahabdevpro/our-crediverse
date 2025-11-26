# Summary
## Test Description
As a Crediverse Administrator , i want to verify if a successful transaction performed by an Agent is recorded as a self-top-up transaction.
## Preconditions
-   The Admin is logged into Crediverse and has permission to view Transactions
-   Agent to be queried has performed a successful selftop up transaction
## Dependencies
-   Agent has a sufficient balance
## Postcondition
-   
# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Click on the Transaction tab  | | The Transaction page is displayed with a list of last 10 perfomed transactions displayed |  |  | |
| 2 | Enter Agent MSISDN in the MSISDN field and press enter to search |   | Agents last 10 transactions performed is displayed |  |    |  |
| 3 | Click on the transaction number of the transaction to be queried |   | Full record of the transaction is displayed successfully |  |	 |  |
| 4 | Inspect the record |   | Transaction type is set as Self Top-up  |  |    |  |