---
tags: [use-case-slice]
---

# Create Daily SMS MM Transfer Report
Starts when Crediverse Admin accesses the System with the goal of configuring the content of the Daily Sales Summary SMS and ends when the intended configuration reflects on the UI. This gives assurance to the Admin that the intended report will be generated and sent by the System at the configured times. 

## Primary Actor
Crediverse Admin

## Scope
Reporting Module
3PP API

## Level
User

## Stakeholders and Interests
Report Recipient - wants to get a daily SMS reflecting MM transfers

## Preconditions
Crediverse Admin is logged in and has permissions to manage reports 
    
## Minimal Guarantee
System logs actions and the outcome of these

## Success Guarantee
-   System displays available report variables for use in the SMS report: 
	-   {Date} - Current Date
	-   {Time} - Time when the SMS/Email is generated
	-   {Period} - The time period which the report reflects
-   System displays option to configure and use an abbreviated currency identifier in reports only, instead of the system configured identifier 
-   System displays option to configure and use a decimal separator option in reports e.g.  “no separator” / “.”  /  or "," instead of using the system defined separator 
-   System UI displays the sales criteria available for inclusion in the SMS report:
	-   {MM TransferCountSuccess} - This is the cumulative count of successful transfer transactions from MM at the end of the period reported
	-   {TotalAmountMMTransfer} - This is the cumulative MM transfer amount at the end of the reporting period
	-   System accurately displays the selections made by the Crediverse Admin 
	-   System generates a SMS report at scheduled times
	-   System sends the SMS report no more than 5 minutes after the end of the reporting period
    
### Main Success Scenario
1.  Crediverse Admin navigates to the `Configuration` module and selects the `Reporting` option
2.  System displays configuration options for `3PP Report` delivered via SMS
3.  Crediverse Admin Creates or Edits SMS Report content to include desired variables  for MM Transfers
4.  System displays selection of report variables and saves the selection
5.  Crediverse Admin navigates to Reporting module and selects `Daily Sales Summary’ option
6.  System displays scheduling options
7.  Crediverse Admin sets times for sending the report, adds report recipients and saves the selection  
8.  System displays the individual schedules on the UI
    

## Extensions
### 7a: Intended report recipient(s) not displayed under Recipient list :
7a1 - Crediverse Admin performs [[Add Admin User]] for each intended Report Recipient
7a2 - System creates Users
7a3 - Crediverse Admin goes back to Step 5

