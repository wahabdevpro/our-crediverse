# Summary
## Test Description
As a Crediverse Administrator, I want to configure the system to adopt the new TDR structure which will include all the available fields when writing TDRs. 
## Preconditions
-	Crediverse Administrator is logged in and has requisite permissions to edit TDR settings.
## Dependencies
-	[[System makes new TDR structure template available for download.]]
## Postcondition
-	System writes TDRs with new fields added in the structure.

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to the Configurations module |  | A list of sub configurations modules available in the system is listed  |  | |
| 2 | Click on the Transactions sub module | | Transaction configuration page is displayed|  | |  |
| 3 | Click on the Edit Button | | Update Transaction Configuration page is displayed with an option to enable writing TDRs in the latest format |  | |  |
| 4 | Click on the check box to enable writing latest TDR format | | Check box checked and the system displays message dialogue to confirm the selection|  | |  |
| 5 | Click on the check box to confirm selection | | The systems marks the check box as selected |  | |  |
| 6 | Click on the Save button to save selection | | The system saves the request for TDRs to be written in the new selected structure with immediate effect. TDR file is rotated immediately |  | |  |
