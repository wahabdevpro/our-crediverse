---
tags: [use-case-slice]
---

# Receive Daily SMS Sales Summary  MM  Report 
Starts when System is woken up by arrival of report generation time and ends when the SMS is delivered to the handset of the Report Recipient

## Primary Actor
Report Recipient

## Scope
Reporting Module

## Level
User

## Stakeholders and Interests
Marketing Exec - wants to get regular updates on sales
MNO - wants to see performance of their MM platform

## Precondition
-   Report recipient is eligible to get SMS reports 
-   SMS delivery configurations are set up on the customer site 
-   Daily SMS report has been scheduled for delivery 
-   Mobile Money (MM) platform has been integrated with Crediverse API and API User exists on Crediverse
-   Crediverse Agent has purchased Crediverse credit on the MM platform as per UC [[Buy Credit through MM]]

## Minimal Guarantee
There is a record of the creation of the report, sending of the report and the outcome of the actions

## Success Guarantee
-   The Daily SMS report displays variables  as set up in the UI
-   A single concatenated SMS is delivered to the recipient if the maximum character limit for one SMS is exceeded 
-   Special characters in the SMS are handled as per SMPP standard protocol  
-   The SMS is sent within 5 minutes after the end of the reporting period

## Main Success Scenario
1.  At the scheduled time the System generates the SMS report and sends the message to the Report Recipient
2.  Report Recipient receives a single concatenated SMS containing the UI configured report content for mobile money transfers 

## Extensions
### 2a - SMS report does not arrive:
2a1 - Report Recipient (or their Proxy) contacts Concurrent to troubleshoot
2a2 - Concurrent checks logs and reverts to Report Recipient

### 2b - SMS report is sent more than 5 minutes after the end of the reporting period:
2b1 - Report Recipient (or their Proxy) contacts Concurrent to complain
2b2 - Concurrent investigates delay and resolves system error or improves report creation performance

### 2c - SMS is truncated:
2c1 - Report Recipient (or their Proxy) contacts Concurrent to complain
2c2 - Concurrent investigates