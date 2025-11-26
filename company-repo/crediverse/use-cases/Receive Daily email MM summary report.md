#draft 

# Use Case Slice: Receive Daily Email MM report 
Starts when System is woken up by arrival of report generation time and ends when the Email is delivered to the Inbox of the Report Recipient

## Primary Actor
Report Recipient

## Scope
Reporting Module

## Level
User

## Stakeholders and Interests
Report Recipient - wants to receive MM sales data

## Preconditions
-   Report recipient is eligible to get Email reports
-   Email delivery configurations and set up have been done on the customer site 
-   Daily Email report has been scheduled for delivery
    
## Minimal Guarantee
System logs actions of generating and sending the report and the outcome of these

## Success Guarantee
- The Daily Email report body displays Airtime, Non-Airtime, Combined Sales and Mobile Money Transfer criteria as defined on the  Daily Sales Summary Report UI
- The selected data is presented in the email body in an attractive, human readable format
- All available airtime, non-airtime, combined sales and mobile money transfer data is attached as a .csv file
- The .csv file has a header in French language as per:

| French     | English              |
| ------------ | ------------------ |
| NOMBRE_SUCCES_DE_TRANSACTIONS_CUMULE_MM | COUNT_SUCCESS_TRANSFER_MM |
| MONTANTـTOTAL |TOTAL_AMOUNT |
| MONTANT_TOTAL_AIRTIME|TOTAL_AIRTIME_AMOUNT |
| MONTANT_TOTAL_NON-AIRTIME |TOTAL_AMOUNT_NON_AIRTIME |
| NOMBRE_D'AGENTS|AGENT_COUNT|
| NOMBRE_D'AGENTS_AIRTIME |AGENT_COUNT_AIRTIME |
| NOMBRE_D'AGENTS_NON-AIRTIME |AGENT_COUNT_NON_AIRTIME |
| NOMBRE_DE_TRANSACTIONS_RÉUSSIES |SUCCESSFUL_TRANSACTION_COUNT |
| NOMBRE_DE_TRANSACTIONS_RÉUSSIES_AIRTIME |SUCCESSFUL_AIRTIME_TRANSACTION_COUNT |
| NOMBRE_DE_TRANSACTIONS_RÉUSSIES_NON-AIRTIME |SUCCESSFUL_NON_AIRTIME_TRANSACTION_COUNT |
| NOMBRE_DE_TRANSACTIONS_ÉCHOUÉES |FAILED_TRANSACTION_COUNT |
|NOMBRE_DE_TRANSACTIONS_ÉCHOUÉES_AIRTIME |FAILED_AIRTIME_TRANSACTION_COUNT |
|NOMBRE_DE_TRANSACTIONS_ÉCHOUÉES_NON-AIRTIME |FAILED_NON_AIRTIME_TRANSACTION_COUNT |
|MONTANT_MOYEN_PAR_AGENT |AVERAGE_AMOUNT_PER_AGENT |
|MONTANT_MOYEN_PAR_AGENT_AIRTIME|AVERAGE_AIRTIME_AMOUNT_PER_AGENT |
|MONTANT_MOYEN_PAR_AGENT_NON-AIRTIME |AVERAGE_NON_AIRTIME_AMOUNT_PER_AGENT |
| MONTANT_MOYEN_PAR_TRANSACTION |AVERAGE_AMOUNT_PER_TRANSACTION |
| MONTANT_MOYEN_PAR_TRANSACTION_AIRTIME |AVERAGE_AIRTIME_AMOUNT_PER_TRANSACTION |
|MONTANT_MOYEN_PAR_TRANSACTION_NON-AIRTIME |AVERAGE_NON_AIRTIME_AMOUNT_PER_TRANSACTION |
|NOMBRE_SUCCES_DE_TRANSACTIONS_CUMULE_MM | COUNT_SUCCESS_TRANSFER_MM |
| NOMBRE_ECHEC_DE_TRANSACTIONS_CUMULE_MM |COUNT_FAIL_TRANSFER_MM|
| NOMBRE_AGENT_UNIQUE_CUMULE_MM |COUNT_UNIQUE_AGENT_MM |
| MONTANT_TOTAL_CUMULE_MM | AMOUNT_TOTAL_TRANSFER_MM |
| MONTANT_MOYENNE_PAR_AGENT_MM |AVE_TRANSFER_AMOUNT_PER_AGENT_MM |
| MONTANT_MOYENNE_PAR_TRANSACTION_MM | AVE_TRANSFER_AMOUNT_PER_TRANSACTION_ MM |


## Main Success Scenario
1.  System generates the Email report at the scheduled time and sends the message to the Report Recipient Inbox    
2.  Report Recipient receives Email containing Mobile Money sales criteria in the Email body as defined in the Daily Sales Summary Report UI 
3. Report Recipient downloads the .csv file

## Extensions
### 2a: Email report does not arrive:
2a1: Report Recipient (or their Proxy) contacts Concurrent to troubleshoot
2a2: Concurrent checks logs and reverts to Report Recipient

  
  

