---
tags: [use-case-slice]
---

# Create Report by Agent UC

## Primary Actor
[[Crediverse Administrator]]

## Scope
[[Crediverse Reporting]]

## Level
User

## Stakeholder and Interests
1. [[Crediverse Administrator]] wants to create report for the activities of a specific Agent
2. Report Recipient - wants to get reports for the correct Agent

## Preconditions
-   [[Crediverse Administrator]] is logged in and has permissions to manage reports

## Minimal Guarantee
-   Report as created is saved to the System

## Success Guarantee
- The System is ready to generate a scheduled report, provide a preview view or file for export for a specified Agent

## Main Success Scenario
 - 1 - [[Crediverse Administrator]] navigates to the Reporting Module
 - 2- System displays available report types
 - 3- If supported in the report type, [[Crediverse Administrator]] configures a report for a specified Agent by inserting MSISDN and saves the report
 - 4- System saves the configuration
 
## Alternate Path
### 3a: MSISDN assigned to more than one Agent:
- 3a1- System displays the Name, Agent Id and Agent Status of current Agent and all Agents previously assigned with the MSISDN
- 3a2- [[Crediverse Administrator]] selects the Agent of interest
- 3a3- Go to Step 4

## Extensions
### 4: Mandatory fields not completed:
- 4a1- System displays error identifying missed mandatory fields
- 4a2- [[Crediverse Administrator]] completes fields and saves
- 4a3- Go to Step 4