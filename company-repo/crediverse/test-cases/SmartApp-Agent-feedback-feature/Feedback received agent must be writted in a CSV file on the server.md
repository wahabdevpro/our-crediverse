# Summary
## Introduction
An Agent should have the option to provide Feedback. The feedback should be written in free text format 
## Preconditions 
- The Agent must be logged in
- The SmartApp must give the option to provide feedback
## Dependencies

## Postcondition
-   System will remain as it is

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | Agent logged into the System| Agent MSISDN: 81234560| Agent can navigate in the app and explore different options |  |  | |
| 2 | Agent navigate to the Feedback option and click on it | | A new screen is opened, where Agent is asked to enter his feedback  |  |  | |
| 3 | Agent provides feedback in free text format and click on submit button  | "Hi, as an agent Xereo i am facing issues with the transfer screen, please look into it :) "  | The Agent is given a prompt message similar to: "Thank you for your valuable feedback" |  |	 |  |
| 4 | Navigate to the directory /usr/src/mobile_application_server/output/feedback on the demo server |  | The feedback provided by Agent must be written in csv format|  |	 |  |
| 5 | Navigate to the directory and check the CSV file created | | The CSV file must have the following fields: Time, Agent ID, Agent MSISDN, Agent Tier, Agent Name, Feedback | | | |
