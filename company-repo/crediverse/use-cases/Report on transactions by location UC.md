#draft #UseCase 

## Primary Actor
[[Crediverse Administrator]]

## Scope
Crediverse Reporting Module

## Level
User goal

## Stakeholders and Interests
[[Crediverse Administrator]] wants to set up location based reports that are emailed to business stakeholders

## Precondition 
[[Crediverse Administrator]] is logged into [[Crediverse]]
[[Crediverse Administrator]] has requisite permissions to manage reports
Location in the form of Cell IDs and areas have been configured on [[Crediverse]]
Transactions based on location have been processed by [[Crediverse]]
CGI information for location based transaction was retrieved and populated in [[Crediverse]]
[[Mobile Operator]] site set up supports location retrieval 

## Minimal Guarantee
KPI report totals are available even if no location based breakdown is available

## Success Guarantee
Reports reflect KPI information per locations defined in [[Crediverse]] 
Global totals including transactions with no CGI information available
Recipients receive the report via email at given intervals

## Main Success Scenario
- 1 - [[Crediverse Administrator]] navigates to the reports module in the UI 
- 2 - [[Crediverse Administrator]] selects option on UI to create daily/weekly/monthly reports per location breakdown
- 3 - The System presents report structure and view options to the [[Crediverse Administrator]] 
- 4 - [[Crediverse Administrator]] sets up the structure and view of the report and saves his selection to [[Crediverse]]
- 5 - [[Crediverse]] generates an example of the report structure and view for verification by the [[Crediverse Administrator]]
- 6 - Upon verification of the report structure by [[Crediverse Administrator]], [[Crediverse]] provides option to email the report to stakeholders
- 7 - [[Crediverse Administrator]] loads one or many email recipients that will receive the report from [[Crediverse]] at selected intervals
- 8 - [[Crediverse]] confirms to [[Crediverse Administrator]] that the recipients are saved to the System 

## Extensions

### - 2a - No location configured or location option disabled
- 2a1 - [[Crediverse]] displays error message informing that location based reporting is available only when location has been enabled and configured on the System

### - 2b - Location not fetched during transactions
- 2b1 - Totals of transactions with no location available are displayed in reports in default category such as  "unknown location"

### - 5a - [[Crediverse Administrator]] dissatisfied with example report structure or view 
- 5a1 - [[Crediverse Administrator]] can edit the report view and structure and retry steps 3-6

### - 7a - Invalid email address loaded
- 7a1 - The validity of email addresses are not checked and emails to invalid addresses will fail
- 7a2- The [[Crediverse Administrator]] can edit the email recipient details at any time and redo step 7-8



