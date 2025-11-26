---
tags: [use-case]
#draft 
---

# Receive Sales by Area Report UCS

## Primary Actor
[[Report Recipient]]

## Scope
Reporting Module

## Level
User Goal

## Stakeholders and Interests
Operator [[Executive]] - wants to receive KPI reports that inform of success of airtime distribution and self top up sales across the country
Operator [[Marketer]] - wants to use sales performance data to inform the strategies to boost sales in various geographical locations

## Preconditions
- Email delivery mechanism has been set up 
- Reports have been created on the System and scheduled for delivery 
- The [[Report Recipient]] is eligible to receive the Report

## Minimal Guarantee
System logs report generation and sending actions and their outcome

## Success Guarantee
- A .csv file is attached to the email report delivered to the recipient
- The selected reporting period is clearly indicated as:
	- Daily report - for current calendar day
	- Weekly report - for current calendar week
	- Monthly report - for current calendar month
- Report shows summary totals for the sales performance criteria for airtime sales and separate fields for self top-up transactions for the following criteria:
	- Total Transactions - cumulative count of total transactions (Success + Fail) 
	- Success Count - This is the cumulative count of successful transactions   
	- Failed Count - This is the cumulative count of failed transactions  
	- Agent Count - This is the cumulative count of unique transacting agents  
	- Total Amount - cumulative sales amount for the day  
	- Average Amount Per Agent - This is the average amount per transacting Agent  (Total Amount / Agent Count)  
	- Average Amount Per Transaction - This is the average transaction amount (Total Amount / Success Count)
- The .csv file includes a French language header such as below with identical fields for Airtime and for Self Top Up totals: 

| French | English |
| --- | --- |
| NOMBRE_DE_TRANSACTIONS | TOTAL_TRANSACTIONS |
| NOMBRE_DE_TRANSACTIONS_SUCCES | COUNT_SUCCESS_TRANSACTIONS |
| NOMBRE_DE_TRANSACTIONS_ECHEC  | COUNT_FAIL_TRANSACTIONS |
| NOMBRE_DE_CABINES | COUNT_AGENTS |
| MONTANT_TOTAL | AMOUNT_TOTAL |
| MOYENNE_PAR_CABINE | AVE_AMOUNT_PER_AGENT |
| MONYENNE_PAR_TRANSACTION | AVE_AMOUNT_PER_TRANSACTION |
- Summary totals are displayed per Area_Name (in no particular order) 
- The Area type per area is displayed similar to: 
`Area_Name\Nom de la zone,Area_Type\Type de zone` 
-   System provides summary totals for performance criteria where the location was **unknown/ inconnue** when the Agent transaction was made
-   The creation/generation of the report does not impair system performance

## Main Success Scenario
1. System generates the report at the scheduled time and sends the email to the [[Report Recipient]]
2. [[Report Recipient]] receives the email report in their Inbox, with an attached .csv report containing sales totals by Area

## Extensions
### 1a - Reports fail to arrive:
- 1a1 - [[Report Recipient]] opens a Support Ticket

### 1b - Reports arrive late:
1b1 - [[Report Recipient]] opens a Support Ticket