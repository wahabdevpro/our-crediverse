# Summary
## Test Description
As a Crediverse Administrator, I want to view and download the latest available TDR structure version.
## Preconditions
-   Crediverse Administrator is logged in and has requisite permissions to view and download templetes from batch processing module.
## Dependencies
-	n/a
## Postcondition
-	Configure the system to adopt new TDR structure.

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Navigate to the Configurations module |  | A list of sub configurations modules available in the system is listed |  |  | |
| 2 | Click on the Transactions sub module | | Transaction configuration page is displayed|  | |  |
| 3 | Click on the Edit Button | | Update Transaction Configuration page is displayed |  | |  |
| 4 | Click on the latest TDR Structure Version check box | | Latest TDR structure version number is displayed with new fields added and their position within the structure information displayed. A hyperlink to download a template is displayed|  | |  |
| 5 | Click Download | | A download is initiated|  | |  |
| 6 | User locates downloaded file on their computer | | CSV file successfully downloaded|  | |  |
| 6 | User inspects the CSV | | CSV file contains the new fields added and their position within the structure|  | |  |
