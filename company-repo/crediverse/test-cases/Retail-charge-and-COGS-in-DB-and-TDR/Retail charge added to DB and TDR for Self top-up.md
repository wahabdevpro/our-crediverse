# Summary
## Test Description
Retail charge is populated to the TDRs and also to ec_transact and ap_transact table for all the self top-up transactions
## Preconditions
-	Agent has sufficient funds to perform self top-up transaction
## Dependencies

## Postcondition


# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Dial USSD to perform self to-up transaction |  | self top-up transaction is successful | |  | |
| 2 | Enter the OLTP database and run select query for last transaction |  | The ec_transact table contains the Retail_charge column with the value equal to the amount sold |  | |  |
| 3 | Wait for OLAP sync and Enter the OLAP database and run select query for last transaction |  | The ap_transact table contains the Retail_charge column with the value equal to the amount sold |  | |  |
| 3 | Navigate to TDR directory and open the TDR for the last transaction |  | The TDR contains the retail_charge value equal to the amount sold at column # 39 |  | |  |
