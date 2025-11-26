# Summary
## Test Description
As a Crediverse Administrator,i want to confirm TDRs are being written with new fields added and their position within the structure is identical to the latest template available.
## Preconditions
-	Crediverse Administrator has access to the TDR storage folder.
-	TDR written for each transaction.
## Dependencies
-	Enable the system to adopt the new TDR structure.
-	[[System makes new TDR structure templete available for download]]
## Postcondition
-	n/a

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to the TDR Storage Folder | /var/opt/cs/ecds/tdr/{CompanyID} | File {CompanyID}-{TimeStamp}.tdr is present  |  | |
| 2 | Admin open the TDR file | | File has TDRs written |  | |  |
| 4 | User inspects the file | | file contains the new fields added and their position within the structure as per latest templete available |  | |  |
