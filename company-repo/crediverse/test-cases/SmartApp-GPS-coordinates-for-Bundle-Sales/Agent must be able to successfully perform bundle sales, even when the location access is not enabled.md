# Summary
## Test Description
When an Agent performs bundle sales, and the location is not enabled/allowed, Agent should still be able to perform transaction successfully, and in TDRs the value for the CGI-Agent should be empty 
## Preconditions
-   The Agent must disable the location access 
-   The Agent must be able to login and have sufficient credit to perform a Bundle Sales
-   MSISDN-B must be enabled on CS

## Dependencies

## Postcondition
-   Location information i.e. Latitude/Longitude must be written in crediverse TDRs, but should be empty

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Agent has enabled the location access for the application | Agent MSISDN: 81234560| Precise location access is enabled when the app is launched |  |  | |
| 2 | Agent selects the Bundle option from the app | | A new screen is opened, where Agent is asked to enter the Recipient Number  |  |  | |
| 3 | Agent enters the Recipient number and Click on 'Select a Bundle' button  | MSISDN-B: 81234562 | The list of bundles will be displayed to the Agent |  |	 |  |
| 4 | Agent selects the bundle and click on sell|  | Bundle sale is successful and a prompt message is displayed similar to: "You have successfully sold the bundle to 81234562" |  |	 |  |
| 5 | Check the crediverse api logs|   | Debit call sent towards crediverse Api must have no latitude and longitude parameters in it |  |	 |  |
| 6 | Check the crediverse tdrs|  | the tdrs have Agent's location parameters i.e latitude and longitude, but it should be empty |  |	 |  |
