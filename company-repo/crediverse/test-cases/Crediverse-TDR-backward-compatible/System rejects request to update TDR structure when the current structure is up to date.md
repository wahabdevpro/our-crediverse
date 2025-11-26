# Summary
## Test Description
As a Crediverse Administrator,i want the system to notify me that the TDR structure is up to date.
-	Crediverse Administrator is logged in and has requisite permissions to edit TDR settings
## Dependencies
-	[[System writes TDRs with new fields added in the structure]]
## Postcondition
-  Crediverse writes TDRs with new fields added in the TDR.

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to the Configurations module |  | List of sub configurations modules available in the system are listed |  |  | |
| 2 | Click on the Transactions sub module | | Transaction configuration page is displayed |  | |  |
| 3 | Click on the Edit Button | | Update Transaction Configuration page is displayed |  | |  |
| 4 | Scroll and locate TDR Structure Version | | The latest TDR Structure Version check box must be checked indicating no update is required |  | |  |
| 5 | Click on the Save Button | | No changes saved by the system. No file rotation must occur |  | |  |
