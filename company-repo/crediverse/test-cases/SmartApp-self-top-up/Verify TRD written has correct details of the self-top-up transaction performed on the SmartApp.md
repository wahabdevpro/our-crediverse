# Summary
## Test Description
As a Crediverse Administrator, i want to verify that the TDRs written reflect the transaction as a self-top-up transaction.
## Preconditions
-   The Administrator has permissions to access and read TDRs
-   Agent to be queried has performed a successful selftop up transaction
## Dependencies
-   Perform self-top up via SmartApp - Agent has sufficient balance
## Postcondition

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to the TDRs directory  | /srv/tdr/ecds/| Latest available TDRs in csv format listed |  |  | |
| 2 | Inspect TDR record for MSISDN of agent who performed a self top up |   | The TDRs record reflects the transaction as a self top up transactions |  |    |  |
