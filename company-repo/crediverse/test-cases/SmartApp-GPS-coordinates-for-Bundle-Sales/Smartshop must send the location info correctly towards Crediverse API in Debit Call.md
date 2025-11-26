# Summary
## Test Description
When an Agent performs bundle sales, it's precise location must be sent towards SmartShop, and SmartShop must send the info i.e. latitude and longitute parameters towards Crediverse API in Debut call
## Preconditions
-   The Agent must enable the location access and select the Precise location
-   The Agent must be able to login and have sufficient credit to perform a Bundle Sales
-   MSISDN-B must be enabled on CS

## Dependencies

## Postcondition
-   Location information i.e. Latitude/Longitude must be written in crediverse TDRs

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Agent has enabled the location access for the application | Agent MSISDN: 81234560| Precise location access is enabled when the app is launched |  |  | |
| 2 | Agent selects the Bundle option from the app | | A new screen is opened, where Agent is asked to enter the Recipient Number  |  |  | |
| 3 | Agent enters the Recipient number and Click on 'Select a Bundle' button  | MSISDN-B: 81234562 | The list of bundles will be displayed to the Agent |  |	 |  |
| 4 | Agent selects the bundle and click on sell|  | Bundle sale is successful and a prompt message is displayed similar to: "You have successfully sold the bundle to 81234562" |  |	 |  |
| 5 | Check the smartshop logs|  | Debit call sent towards crediverse Api must have the logitute and latitude parameters in it |  |	 |  |
