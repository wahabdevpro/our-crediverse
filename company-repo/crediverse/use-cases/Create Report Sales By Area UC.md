---
tags: [use-case-slice]
---

# Create and Schedule Sales by Area Report
Starts when the Crediverse access the system and ends when the desired report management has been completed and the system has saved the configurations.

## Primary Actor
[[Crediverse Administrator]]

## Scope
[[Crediverse Reporting]]

## Level
User

## Stakeholder and Interests
1. Report Recipient - Awaiting the receipt of regular accurate reports
2. Crediverse Admin - Creating and scheduling reports

## Preconditions
-   [[Crediverse Administrator]] is logged in and has permissions to manage reports
-   Valid [[Add UI User]] are created on the System that are eligible to be report recipients

## Minimal Guarantee
-   Reports as named are saved to the System

## Success Guarantee
-   Sales by Area Report is created and scheduled on the System, ready to be generated and sent when the scheduled time arrives
-   Daily reports displaying totals the current calendar day can be created and scheduled
-   Weekly reports displaying totals for the current calendar week can be created and scheduled
-   Monthly reports displaying totals for the current calendar month can be created and scheduled
-   The default report period when no other reporting period is selected is the current calendar day 
-   Reports can be created, edited and deleted
-   The structure of the report content is displayed on the UI 
-   Schedules can be created, edited and deleted
-   Report includes totals for Airtime Sales by Area 
-   Report includes totals for Self Top-Up transactions by Area
-   Report displays the Area Type associated with each Area
-   Report will include data for each transaction type separately (Airtime Sales and Self Top-Ups):
	- Total Transactions - Cumulative count of total transactions (success + fail)
	- Success Count - Cumulative count of successful transactions
	- Failed Count - Cumulative count of failed transactions
	- Unique Agent Count  - Cumulative count of unique transacting Agents
	- Total Airtime Sales Amount / Total Self Top-Up Amount - Cumulative sales / Self top ups for the day
	- Average Amount per Agent - This is the ave amount per unique transacting agent (Total amount / Agent Count)
	- Average Amount per successful Transaction - Ave transaction amount (Total Amount / Agent Count)
- for Airtime and Self Top-Up transactions generated from 2G/3G and 4G networks
- an "unknown" category when location was not known when airtime sale transaction was made
- the "unknown" category does not exceed 2% of the total transactions reported on

## Main Success Scenario
 - 1 - [[Crediverse Administrator|Admin]] navigates to the `Reports` module
 - 2 - System displays the available report types including `Sales by Area` report
 - 3 - Crediverse Admin selects the `Sales by Area` option and selects the `New Report` option
 - 4 - System requests user to name and describe the report, and select a report period (today (calendar day), this week (calendar week),  this month(calendar month)
 - 5-  [[Crediverse Administrator|Admin]] enters report name and description details chooses a report period and saves the selection
 - 6 - The System displays a message confirming successful saving of the details and displays the new report on the `Sales By Area` screen
 - 7 - Crediverse Admin clicks on `Schedule` option
 - 8 - System displays `Add Schedule` options 
 - 9 - Crediverse Admin sets schedule and adds report recipients and saves the selections
 - 10 - Systems confirms the report has been successfully scheduled

## Extensions
### 6a - Report not saved:
- 6a1 - System displays error message if report name, report description have not been added
- 6a2 - Crediverse Admin rectifies and saves
- 6a3 - Go to step 7