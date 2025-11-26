 

## Use Case Slice: Create Daily Sales Summary Email Report 
Starts when Crediverse Admin accesses the System with the goal of configuring the content of the Daily Sales Summary Email and ends when the intended configuration reflects on the UI. This gives assurance to the Admin that the intended report will be generated and sent by the System at the configured times. 

## Primary Actor
Crediverse Admin

## Scope
Reporting Module

## Level
User

## Stakeholders and Interests
- Report Recipient - wants to get a daily Email reflecting airtime, non-airtime and MM transfers
- MNO Exec - wants to keep tabs on the sales performance across the board
- Marketing Exec - wants to stay in touch with changes in the market 

## Preconditions
-   Crediverse Admin is logged in and has permissions to manage reports 
    
## Minimal Guarantee
System logs actions and the outcome of these

## Success Guarantee
-   System displays available email report detail variables such as:
-   {Date} - Current Date
-   {Time} - Time when the SMS/Email is generated
-   {Period} - The time period which the report reflects, for example, an hourly report would show 09:00 to 10:00 etc.
-   System displays the sales criteria available for inclusion in the Email report in the UI for Airtime, Non-Airtime, Combined Sales and MM Transfers:
	-   {TotalAmount} - This is the cumulative sales amount at the end of the period reported 
	-   {AgentCount} - This is the cumulative count of unique transacting agents at the end of the period reported
	-   {SuccessfulTransactionCount} - This is the cumulative count of successful transactions at the end of the period reported
	-   {FailedTransactionCount} - This is the cumulative count of failed transactions at the end of the period reported
	-   {AverageAmountPerAgent} - This is the average amount per transacting Agent at the end of the period reported
	-   {AverageAmountPerTransaction} - This is the average transaction amount at the end of the period reported
	-   {COUNT_SUCCESS_TRANSFER_MM} - This is the cumulative count of successful transfer transactions from MM at the end of the period reported 
	-   {COUNT_FAIL_TRANSFER_MM} - This is the cumulative count of  failed transfers from MM account to Agents within the reporting period 
	-   {AMOUNT_TOTAL_TRANSFER_MM} - This is the cumulative MM transfer amount at the end of the reporting period
	-   {COUNT_UNIQUE_AGENT_MM} - This is the cumulative count of the number of unique Agents who received transfers from MM account within the reporting period
	-   {AVE_TRANSFER_AMOUNTPER_AGENT_MM} - This is the average transfer amount per receiving agent at the end of the reporting period
	-   {AVE_TRANSFER_AMOUNT_PER_TRANSACTION_ MM} - This is the average transaction amount per successful transfer transaction within the reporting period 
-   System accurately displays the selections made by the Crediverse Admin 
-   Email contains selected data in the body of the email 
-   Email contains a .csv file attachment as per [[Receive Daily Email MM Report]]
   
## Main Success Scenario
1.  Crediverse Admin navigates to the daily summary report module 
2.  System displays configuration options for Email Reports  
3.  Crediverse Admin configures desired options for the Email body for Airtime, Non-Airtime, Combined Sales and MM Transfers 
4.  System displays selection of report variables and saves the selection
5.  Crediverse Admin navigates to reporting schedule configuration   
6.  System displays scheduling options   
7.  Crediverse Admin sets times for sending the report, adds Email report recipients and saves the selection   
8.  System displays the schedules on the UI
    

## Extensions
### 7a: Intended report recipient(s) not displayed under Recipient list :
7a1 - Crediverse Admin performs [[Add Admin User]] for each intended Report Recipient
7a2 - System creates Users
7a3 - Crediverse Admin goes back to Step 5

  
