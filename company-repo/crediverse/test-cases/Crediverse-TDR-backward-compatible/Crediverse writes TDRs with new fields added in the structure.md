# Summary
## Test Description
As a Crediverse Administrator who has enabled the latest TDR option, I want to confirm the TDRs written now include the new fields available in the TDR structure.
## Preconditions
-	Crediverse Administrator has access to the TDR Storage folder
-	TDR written for each transaction	
## Dependencies
-	 Enable the system to write latest TDR structure option
## Postcondition
-  n/a

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to the TDR Storage Folder | /var/opt/cs/ecds/tdr/{CompanyID} | File {CompanyID}-{TimeStamp}.tdr is present  |  | |
| 2 | Admin open the TDR file | | System writes TDR with new fields available in the TDR structure|  | |  |
