# Use-Case Template
Set up agent performance reporting criteria

## Primary Actor
[[Crediverse Administrator]] 

## Scope
[[Crediverse]] reporting module

## Level
User Goal

## Stakeholders and Interests
[[Crediverse Administrator]] wants to set up report content in line with the KPIs that the Revenue Assurance team is interested in
[[Crediverse Administrator]] wants to configure a time and interval at which reports will be generated and sent to selected recipients

## Precondition
[[Crediverse Administrator]] is logged in and has permissions to set up reports

## Minimal Guarantee
Transaction data is available within the System

## Success Guarantee
Report structure is saved to the System 
Report schedule, if set, is saved to the System

## Main Success Scenario
- 1 - [[Crediverse Administrator]] navigates to the reporting module 
- 2 - System displays selection of report types: Retailer, Wholesaler, Monthly Sales, Daily Sales, Daily Group Sales, Account Balance Summary
- 3 - [[Crediverse Administrator]] selects the type of report they want to create 
- 4 - System displays the reporting criteria available for the selected report type
- 5 - [[Crediverse Administrator]] sets a report name, description, reporting period, defines reporting criteria and saves the selection on the UI
- 6 - System confirms selection has been saved

## Extensions
### 5a - [[MSISDN]] used as report criteria:
- 5a1 -  System displays the Agent Id, Agent Name, Agent Status alongside the MSISDN 
- 5a2 - Go to Step 6

### 5b - More than one Agent found when MSISDN is used as report criteria:
- 5b1 - System displays selectable options of the Agent Id, Agent Name, Agent Status of each Agent found against the MSISDN where one and only one Agent of active/suspended status is found and/or one or more deactivated Agents may be found
- 5b2 - [[Crediverse Administrator]] selects Agent Id of interest
- 5b3 - Go to Step 6





