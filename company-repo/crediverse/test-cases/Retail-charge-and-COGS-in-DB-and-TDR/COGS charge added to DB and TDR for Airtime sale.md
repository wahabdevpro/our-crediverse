# Summary
## Test Description
Cost of Goods Sold (COGS) is populated to the TDRs and also to ec_transact and ap_transact table for all the Airtime sales transactions
## Preconditions
-	Agent has sufficient funds to perform airtime sale transaction
## Dependencies

## Postcondition


# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Perform a airtime sale transaction from Smartapp |  | Airtime sale transaction is successful | |  | |
| 2 | Enter the OLTP database and run select query for last transaction |  | The ec_transact table contains the COGS column with the value equal to the amount slightly less than the retail_charge (not clear) |  | |  |
| 3 | Wait for OLAP sync and Enter the OLAP database and run select query for last transaction |  | The ap_transact table contains the COGS column with the value equal to the amount slightly less than the retail_charge (not clear) |  | |  |
| 3 | Navigate to TDR directory and open the TDR for the last transaction |  | The TDR contains the retail_charge value equal to the amount slightly less than the retail_charge (not clear) at column # 40 |  | |  |
