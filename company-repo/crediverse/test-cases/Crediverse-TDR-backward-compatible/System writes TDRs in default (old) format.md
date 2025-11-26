# Summary
## Test Description
As a Crediverse Administrator, I want to confirm TDRs are written in the default (old) structure even when new fields are introduced when I upgrade the product.
## Preconditions
-	Crediverse Administrator has access to the TDR storage folder.
-	An upgrade of product has performed and the new product version has a changed TDR structure.
## Dependencies
-	n/a
## Postcondition
-	System retains old structure.

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to the TDR Storage Folder | /var/opt/cs/ecds/tdr/{CompanyID} | File {CompanyID}-{TimeStamp}.tdr is present |  |  | |
| 2 | Admin open the TDR file | | System retains old structure |  | |  |
