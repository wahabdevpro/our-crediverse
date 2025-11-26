---
title: Transaction Data Records (TDR) Specification
classification: Confidential
created: 2024-08-29
updated: 2025-10-30
authors:
  - "[[Lynton Hayns]]"
version: 1.0.1
next-review: 2025-10-31
tags:
  - TDR
  - EVD
  - Specification
---

# Transaction Data Records (TDR) Specification

## Document Specification

### Purpose
This document specifies the structure and content of Transaction Data Records (TDRs) for the Crediverse Electronic Value Distribution (EVD) system. It provides a comprehensive guide to the data captured for each transaction, ensuring consistency and completeness in transaction logging and reporting.

### Scope
<!--
This specification covers all transaction types processed by the Crediverse EVD system, including but not limited to airtime transfers, sales, PIN management, balance inquiries, and administrative operations. It details the data fields, their descriptions, and sample formats for TDRs.
-->
This specification covers all transaction types processed by the Crediverse EVD system, including but not limited to airtime transfers, sales, balance inquiries, and bundle sales. It details the data fields, their descriptions, and sample formats for TDRs.

### Target Audience
- System Architects
- Software Developers
- Database Administrators
- Business Analysts
- Compliance Officers
- Auditors

## Table of Contents

- [[#1. Introduction]]
- [[#2. TDR Structure]]
- [[#3. TDR Fields]]
- [[#4. Sample TDR]]
- [[#5. Data Integrity and Security]]
- [[#6. TDR Processing and Analysis]]
- [[#7. Compliance and Auditing]]
- [[#8. Integration with Other Systems]]

## 1. Introduction

### 1.1 What are Transaction Data Records (TDRs)?
Transaction Data Records (TDRs) are comprehensive logs of all transactions processed by the Crediverse EVD system. They serve as the primary source of truth for transaction details, essential for auditing, reconciliation, and business intelligence purposes.

### 1.2 Importance of TDRs
TDRs play a crucial role in:
- Ensuring transaction traceability
- Facilitating financial reconciliation
- Supporting customer dispute resolution
- Enabling business analytics and reporting
- Assisting in fraud detection and prevention
- Ensuring compliance with regulatory requirements

## 2. TDR Structure

### 2.1 Record Format
TDRs are stored in CSV (Comma-Separated Values) format, allowing for easy parsing and integration with various data processing tools.

### 2.2 File Naming Convention
The TDRs are named as follows:`002-<date>T<timestamp>.tdr`, where:
- "002" is the company id 
- "date" in YYYYMMDD format (e.g. 20240904) 
- "timestamp" in HHMMSS format (e.g. 081350).


### 2.3 TDR File Generation and Movement
2.3.1 **Initial Storage**: TDR files are initially written to:
  ```
  /srv/edr/ecds-ts-server/
  ```

2.3.2 **Temporary Relocation**: 
- A script, triggered by crontab, runs every 15 minutes past the hour.
- This script moves the TDR files to:
  ```
  /srv/edr/ecds-ts-server/temp/
  ```
2.3.3 **TDR Transfer and Archiving**: 
- A transfer EDR script, scheduled via crontab to run every 20 minutes past the hour, performs the following actions:

 ***FTP Transfer***: 
- TDR files are transferred via FTP to an externally configured location.

 ***Archiving***:
- As part of the FTP transfer process, the TDR files are archived in:
  ```
  /srv/archive/edr/ecds/
  ```
2.3.4 **Crontab Schedule Summary**

| Action | Schedule | Description |
|--------|----------|-------------|
| TDR Movement | Every 15 minutes past the hour | Moves TDRs to temp directory |
| TDR Transfer & Archive | Every 20 minutes past the hour | FTPs TDRs and archives them |

### 2.4 TDR structure version
By default, the TDR structure version on the Admin GUI is set to *Prior Version 1.13.0* under the Configuration management --> Transaction settings tab. To enable the latest TDRs, select the *Version 1.13.0 and later* setting 

![TDR structure version setting](image%20(6).png)

![Version 1.13.0 and later option](image%20(7).png)

## 3. TDR Fields

### 3.1 Field Definitions
The following table defines all fields recorded in a TDR when the latest version has been enabled:

| No. | Property                     | Description                                                                                                                     | Sample Value                                      |     |
| --- | ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- | --- |
| 1   | Hostname                     | The name of the host on which the Transaction was executed                                                                      | ECDS-02                                           |     |
| 2   | Transaction_ID               | The generated number for the transaction                                                                                        | 000010201                                         |     |
| 3   | Transaction_Type             | The type of the transaction (see section 3.2 for full list)                                                                     | TX                                                |     |
| 4   | Channel                      | The Channel on which the transaction was initiated                                                                              | USSD                                              |     |
| 5   | Caller_ID                    | The identity of the transaction initiator                                                                                       | 0141120736                                        |     |
| 6   | Start_Time                   | The local time at which the transaction started                                                                                 | 20151225T101213                                   |     |
| 7   | End_Time                     | The local time at which the transaction completed                                                                               | 20151225T101213                                   |     |
| 8   | Inbound_Transaction_ID       | Optional inbound Transaction ID from access channel                                                                             | 28367498263                                       |     |
| 9   | Inbound_Session_ID           | Optional inbound Session ID from access channel                                                                                 | 39784                                             |     |
| 10  | Request_Mode                 | The transaction mode (N = Normal, T = TestOnly)                                                                                 | N                                                 |     |
| 11  | A_Party_ID                   | The Account Number of the A-Party or the Web User name if performed by Web User                                                 | eCab_22541120736                                  |     |
| 12  | A_MSISDN                     | The MSISDN of the A-Party                                                                                                       | 0141120736                                        |     |
| 13  | A_Tier                       | Tier to which the A-Party belongs                                                                                               | eCabine                                           |     |
| 14  | A_Service_Class              | The Service Class the A-Party belongs to                                                                                        | ServiceClass01                                    |     |
| 15  | A_Group                      | The Group the A-Party belongs to                                                                                                | ONEMART_YOP_eCabineGP                             |     |
| 16  | A_Owner_ID                   | The owner id linked to the A-Party                                                                                              | 0140399088                                        |     |
| 17  | A_Area                       | The Area of the A-Party from the A-Party Account data                                                                           | YOPOUGON                                          |     |
| 18  | A_IMSI                       | The IMSI number of the A-Party's SIM Card                                                                                       | 655014901224869                                   |     |
| 19  | A_IMEI                       | The IMEI number of the A-Party's Phone                                                                                          | 357460060598388                                   |     |
| 20  | A_Cell_ID                    | The GSM Cell ID of the A-Party at the time the transaction was made                                                             | 50215                                             |     |
| 21  | A_Balance_Before             | The balance of the A-Party before the Transaction                                                                               | 19350578.5                                        |     |
| 22  | A_Balance_After              | The balance of the A-Party after the Transaction                                                                                | 19349578.5                                        |     |
| 23  | B_Party_ID                   | The Account Number of the B-Party or the Web User name if performed by Web User                                                 | 0141058815                                        |     |
| 24  | B_MSISDN                     | The MSISDN of the B-Party                                                                                                       | 0141058815                                        |     |
| 25  | B_Tier                       | Tier to which the B-Party belongs                                                                                               | Subscriber                                        |     |
| 26  | B_Service_Class              | The Service Class the B-Party belongs to                                                                                        | ServiceClass01                                    |     |
| 27  | B_Group                      | The Group the B-Party belongs to                                                                                                | Group01                                           |     |
| 28  | B_Owner_ID                   | The owner id linked to the B-Party                                                                                              | 0140399088                                        |     |
| 29  | B_Area                       | The Area of the B-Party from the B-Party Account data                                                                           | YOPOUGON                                          |     |
| 30  | B_IMSI                       | The IMSI number of the B-Party's SIM Card                                                                                       | 655014901235869                                   |     |
| 31  | B_IMEI                       | The IMEI number of the B-Party's Phone                                                                                          | 357460030498388                                   |     |
| 32  | B_Cell_ID                    | The GSM Cell ID of the B-Party at the time the transaction was made                                                             | 11743                                             |     |
| 33  | B_Balance_Before             | The balance of the B-Party before the Transaction                                                                               | 1500                                              |     |
| 34  | B_Balance_After              | The balance of the B-Party after the Transaction                                                                                | 1500                                              |     |
| 35  | Amount                       | The monetary value of the transaction charged to Crediverse account                                                             | 1000                                              |     |
| 36  | Buyer_Trade_Bonus            | Buyer Trade Bonus amount associated with the transaction                                                                        | 0.00                                              |     |
| 37  | Buyer_Trade_Bonus_Percentage | Buyer Trade Bonus percentage associated with the transaction                                                                    | 0.00                                              |     |
| 38  | Buyer_Bonus_Provision        | The amount provisioned to Buyer for downstream trade bonuses                                                                    | 0.00                                              |     |
| 39  | Gross_Sales_Amount           | Retail charge of goods                                                                                                          | 0.00                                              |     |
| 40  | COGS                         | Cost of goods sold                                                                                                              | 189.4736844                                       |     |
| 41  | Empty                        | Future use                                                                                                                      |                                                   |     |
| 42  | Origin_Channel               | The channel from which the transaction originated                                                                               | SA                                                |     |
| 43  | Return_Code                  | The result code returned from the transaction execution                                                                         | SUCCESS                                           |     |
| 44  | Last_External_Result_Code    | The error code returned from an external system                                                                                 | 100                                               |     |
| 45  | Rolled_Back                  | Flag to indicate that this transaction had to be rolled back (0,1)                                                              | 0                                                 |     |
| 46  | Follow_Up                    | Flag to indicate that manual follow-up is required (0,1)                                                                        | 0                                                 |     |
| 47  | Bundle_Name                  | Name of the bundle sold to the subscriber in non-airtime sale transaction                                                       | Sociaux Jour 200                                  |     |
| 48  | Promotion_Name               | The Name of the Promotion which is relevant in the transaction                                                                  | Data Bundles                                      |     |
| 49  | Requester_MSISDN             | The MSISDN of the Transaction requesting user                                                                                   | 0141120736                                        |     |
| 50  | Requester_Type               | This provides information to describe the user indicated in the RequesterMSISDN field                                           | Agent                                             |     |
| 51  | A_Hold_Balance_Before        | The Hold account balance of the A-Party before the Transaction                                                                  | 0.00                                              |     |
| 52  | A_Hold_Balance_After         | The Hold account balance of the A-Party after the Transaction                                                                   | 0.00                                              |     |
| 53  | Original_TID                 | The optional original TransactionID, populated in the case of a Reversal or Adjudication Transaction                            | 03504225219                                       |     |
| 54  | Additional_Information       | An optional text message or a semi-colon separated list of additional named parameter pairs, depending on the transaction type. | CAMPAIGN_ID=CrediverseActions;RULE_ID=ExtraBonus; |     |
| 55  | B_Transfer_Bonus_Amount      | The transfer bonus amount given to the receiving party on Charging system, via Refill call                                      | 100                                               |     |
| 56  | B_Transfer_Bonus_Profile     | The transfer bonus refill profile used for the Refill call                                                                      | AB                                                |     |
| 57  | a_cgi                        | The full CGI (MCC-MNC-LAC-Cell_ID) of the A-Party at the time the transaction was made                                          | 655-1-161-13352                                   |     |
| 58  | a_gps                        | Precise GPS location (latitude **&#124;** longitude ) of the A-Party at the time of the transaction                             | 12.450000 **&#124;** 15.450000                    |     |

### 3.2 Transaction Types
The following transaction types are supported:

| Code | Description                |
| ---- | -------------------------- |
| RP   | REPLENISH                  |
| TX   | TRANSFER                   |
| SL   | SELL                       |
| PR   | REGISTER_PIN               |
| CP   | CHANGE_PIN                 |
| BE   | BALANCE_ENQUIRY            |
| ST   | SELF_TOPUP                 |
| TS   | TRANSACTION_STATUS_ENQUIRY |
| LT   | LAST_TRANSACTION_ENQUIRY   |
| AJ   | ADJUST                     |
| SQ   | SALES_QUERY                |
| DQ   | DEPOSITS_QUERY             |
| FR   | REVERSE                    |
| PA   | REVERSE_PARTIALLY          |
| SB   | TYPE_SELL_BUNDLE           |
| RW   | TYPE_PROMOTION_REWARD      |
| AD   | TYPE_ADJUDICATION          |
| ND   | TYPE_NON_AIRTIME_DEBIT     |
| NR   | TYPE_NON_AIRTIME_REFUND    |

### 3.3 Channel Types
The following channels are supported:
- USSD
- SMS
- APP
- WUI
- API
- BATCH

### 3.4 Return Codes
The following return codes are supported:
- SUCCESS
- REFILL_FAILED
- TECHNICAL_PROBLEM
- INVALID_CHANNEL
- FORBIDDEN
- CO_AUTHORIZE
- DAY_AMOUNT_LIMIT
- MONTH_COUNT_LIMIT
- MONTH_AMOUNT_LIMIT
- ALREADY_REGISTERED
- NOT_REGISTERED
- INVALID_STATE
- INVALID_PIN
- NOT_SELF
- TX_NOT_FOUND
- IMSI_LOCKOUT
- INVALID_AGENT
- INVALID_AMOUNT
- INVALID_TRANSACTION_TYPE
- TRANSACTION_ALREADY_REVERSED
- OTHER_ERROR

Note: This list may extend as the product evolves.

# 4. Sample TDRs for All Transaction Types

To ensure that our Transaction Data Record (TDR) structure can accommodate all possible transaction scenarios, this section provides sample TDRs for each transaction type supported by the Crediverse EVD system. These samples serve as references for developers, analysts, and auditors to understand how different transaction types are represented in the TDR format.

## 4.1 Sample TDR Structure

Each sample TDR will be presented in the following format:

1. Transaction Type Code and Description
2. Brief scenario description
3. CSV format TDR
4. Table format TDR with field explanations

## 4.2 Sample TDRs

### 4.2.1 RP - REPLENISH

Scenario: Replenishment performed for root via Web User Interface

```csv
ecdsprts01,03503860699,RP,WUI,atraore,20240831T090950,20240831T090952,,,N,,,,,,,,,,,,,ROOT,0101024646,Root,,,,,,,,34180312.716,3034180312.716,3000000000,0.00,0.00,205929390,,,,,SUCCESS,,0,0,,,0101000440,Web User,,,,,,,,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi |
|----------|----------------|------------------|---------|-----------|------------|----------|------------------------|---------------------|--------------|------------|----------|--------|-----------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|------------|----------|--------|------------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|--------|-------------------|-------------------------------|------------------------|---------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|-------------------|----------------|------------------------|----------------------|--------------|------------------------|--------------------------|---------------------------|-------|
| ecdsprts01 | 03503860699 | RP | WUI | atraore | 20240831T090950 | 20240831T090952 | | | N | | | | | | | | | | | | | ROOT | 0101024646 | Root | | | | | | | | 34180312.716 | 3034180312.716 | 3000000000 | 0.00 | 0.00 | 205929390 | | | | | SUCCESS | | 0 | 0 | | | 0101000440 | Web User | | | | | | | |

</div>


### 4.2.2 TX - TRANSFER

Scenario: An agent transfers to another agent (Wholesale transaction)

```csv
ecdsprts01,03509764507,TX,USSD,0102918116,20240904T100904,20240904T100904,1869081030,,N,eInter_22502918116,0102918116,eIntermed,,GBD_ABENGOUROU_eintermedGP,0103053030,ABENGOUROU,612025202014978,3583040756011401,11743,2857186,2847186,A431853,0171209377,eCabine,,GBD_ABENGOUROU_eCabineGP,0103053030,ABENGOUROU,612028301546597,3506062622664478,,954.5,11404.5,10000,450,4.5,0.00,10000,9900.9901,,,SUCCESS,,0,0,,,0102918116,Agent,0.00,0.00,,,150,98,612-2-1093-11743,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi |
|----------|----------------|------------------|---------|-----------|------------|----------|------------------------|---------------------|--------------|------------|----------|--------|-----------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|------------|----------|--------|------------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|--------|-------------------|-------------------------------|------------------------|---------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|-------------------|----------------|------------------------|----------------------|--------------|------------------------|--------------------------|---------------------------|-------|
| ecdsprts01 | 03509764507 | TX | USSD | 0102918116 | 20240904T100904 | 20240904T100904 | 1869081030 | | N | eInter_22502918116 | 0102918116 | eIntermed | | GBD_ABENGOUROU_eintermedGP | 0103053030 | ABENGOUROU | 612025202014978 | 3583040756011401 | 11743 | 2857186 | 2847186 | A431853 | 0171209377 | eCabine | | GBD_ABENGOUROU_eCabineGP | 0103053030 | ABENGOUROU | 612028301546597 | 3506062622664478 | | 954.5 | 11404.5 | 10000 | 450 | 4.5 | 0.00 | 10000 | 9900.9901 | | | SUCCESS | | 0 | 0 | | | 0102918116 | Agent | 0.00 | 0.00 | | | 150 | 98 | 612-2-1093-11743 |

</div>

### 4.2.3 SL - SELL

Scenario: An agent sells airtime to a subscriber

```csv
ecdsprts01,02370742007,SL,USSD,0141120736,20221107T154553,20221107T154553,1007,2008,N,eCab_22541120736,0141120736,eCabine,,ONEMART_YOP_eCabineGP,0140399088,YOPOUGON,612027118662882,3532461171214601,13352,19350578.5,19349578.5,0141058815,0141058815,Subscriber,,,,,,,,1500,1500,1000,0.00,0.00,0.00,0.00,,,,SUCCESS,,0,0,,,0141120736,Agent,0.00,0.00,,,,,655-1-161-13352,12.450000|15.450000
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi | a_gps |
|----------|----------------|------------------|---------|-----------|------------|----------|------------------------|---------------------|--------------|------------|----------|--------|-----------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|------------|----------|--------|------------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|--------|-------------------|-------------------------------|------------------------|---------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|-------------------|----------------|------------------------|----------------------|--------------|------------------------|--------------------------|---------------------------|-------|-------|
| ecdsprts01 | 02370742007 | SL | USSD | 0141120736 | 20221107T154553 | 20221107T154553 | 1007 | 2008 | N | eCab_22541120736 | 0141120736 | eCabine | | ONEMART_YOP_eCabineGP | 0140399088 | YOPOUGON | 612027118662882 | 3532461171214601 | 13352 | 19350578.5 | 19349578.5 | 0141058815 | 0141058815 | Subscriber | | | | | | | | 1500 | 1500 | 1000 | 0.00 | 0.00 | 0.00 | 0.00 | | | | SUCCESS | | 0 | 0 | | | 0141120736 | Agent | 0.00 | 0.00 | | | | | 655-1-161-13352 | 12.450000\|15.450000 |

</div>

### 4.2.4 BE - BALANCE_ENQUIRY

Scenario: An agent performs a balance enquiry

```csv
ecdsprts01,03509917614,BE,USSD,0101239226,20240904T121035,20240904T121035,1869329737,,N,22501239226,0101239226,eCabine,,SODITEL_TREICH_EcabineGP,0102427576,TREICHVILLE,612021206205587,3535540906346701,,21456.75,21456.75,,,,,,,,,,,,,0.00,,,,,,,,SUCCESS,,0,0,,,0101239226,Agent,0.00,0.00,,,,,,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi |
|----------|----------------|------------------|---------|-----------|------------|----------|------------------------|---------------------|--------------|------------|----------|--------|-----------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|------------|----------|--------|------------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|--------|-------------------|-------------------------------|------------------------|---------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|-------------------|----------------|------------------------|----------------------|--------------|------------------------|--------------------------|---------------------------|-------|
| ecdsprts01 | 03509917614 | BE | USSD | 0101239226 | 20240904T121035 | 20240904T121035 | 1869329737 | | N | 22501239226 | 0101239226 | eCabine | | SODITEL_TREICH_EcabineGP | 0102427576 | TREICHVILLE | 612021206205587 | 3535540906346701 | | 21456.75 | 21456.75 | | | | | | | | | | | | | 0.00 | | | | | | | | SUCCESS | | 0 | 0 | | | 0101239226 | Agent | 0.00 | 0.00 | | | | | |

</div>

### 4.2.5 ST - SELF_TOPUP

Scenario: An agent performs a self top-up

```csv
ecdsprts01,03510773042,ST,USSD,0151278243,20240904T211608,20240904T211608,1907575413,,N,A222384,0151278243,eCabine,,NOUR_BOUAKE_eCabineGP,0141797777,BOUAKE,612027120927886,,14341,46160,36160,A222384,0151278243,eCabine,,NOUR_BOUAKE_eCabineGP,0141797777,BOUAKE,612027120927886,,,,,10000,0.00,0.00,0.00,10000,9569.378,,,SUCCESS,,0,0,,,0151278243,Agent,0.00,0.00,,,,,612-2-302-14341,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi |
|----------|----------------|------------------|---------|-----------|------------|----------|------------------------|---------------------|--------------|------------|----------|--------|-----------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|------------|----------|--------|------------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|--------|-------------------|-------------------------------|------------------------|---------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|-------------------|----------------|------------------------|----------------------|--------------|------------------------|--------------------------|---------------------------|-------|
| ecdsprts01 | 03510773042 | ST | USSD | 0151278243 | 20240904T211608 | 20240904T211608 | 1907575413 | | N | A222384 | 0151278243 | eCabine | | NOUR_BOUAKE_eCabineGP | 0141797777 | BOUAKE | 612027120927886 | | 14341 | 46160 | 36160 | A222384 | 0151278243 | eCabine | | NOUR_BOUAKE_eCabineGP | 0141797777 | BOUAKE | 612027120927886 | | | | | 10000 | 0.00 | 0.00 | 0.00 | 10000 | 9569.378 | | | SUCCESS | | 0 | 0 | | | 0151278243 | Agent | 0.00 | 0.00 | | | | | 612-2-302-14341 |

</div>


### 4.2.6 FR - REVERSE

Scenario: Full reversal actioned by authorised user on the Web User Interface (WUI)

```csv
ecdsprts01,03509750878,FR,WUI,atuzie,20240904T095929,20240904T095929,,,N,eInter_22541256456,0141256456,eIntermed,,PROVINOV_DALOA_EintermedGP,0150745454,DALOA,612026217876421,3566988000685101,,910000,930000,eCab_22540733934,0140733934,eCabine,,CIT_DIST_DAL_eCabineGP,0101408327,DALOA,612026303946499,3553211136026878,,20935.5,35.5,-20000,-900,4.5,0.00,,,,,SUCCESS,,0,0,,,0101000926,Web User,0.00,0.00,03504225219,,,,,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi |
|----------|----------------|------------------|---------|-----------|------------|----------|------------------------|---------------------|--------------|------------|----------|--------|-----------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|------------|----------|--------|------------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|--------|-------------------|-------------------------------|------------------------|---------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|-------------------|----------------|------------------------|----------------------|--------------|------------------------|--------------------------|---------------------------|-------|
| ecdsprts01 | 03509750878 | FR | WUI | atuzie | 20240904T095929 | 20240904T095929 | | | N | eInter_22541256456 | 0141256456 | eIntermed | | PROVINOV_DALOA_EintermedGP | 0150745454 | DALOA | 612026217876421 | 3566988000685101 | | 910000 | 930000 | eCab_22540733934 | 0140733934 | eCabine | | CIT_DIST_DAL_eCabineGP | 0101408327 | DALOA | 612026303946499 | 3553211136026878 | | 20935.5 | 35.5 | -20000 | -900 | 4.5 | 0.00 | | | | | SUCCESS | | 0 | 0 | | | 0101000926 | Web User | 0.00 | 0.00 | 03504225219 | | | | |

</div>

### 4.2.7 PA - REVERSE_PARTIALLY

Scenario: Partial reversal actioned by authorised user on the Web User Interface (WUI)

```csv
ecdsprts01,03509755367,PA,WUI,atuzie,20240904T100120,20240904T100120,,,N,A207561,0153942677,eIntermed,,NOUR_KGO_eintermedGP,0103283737,KORHOGO,612027117270374,3547940931745778,,324000,332430,A124327,0141484686,eCabine,,NOUR_KGO_eCabineGP,0103283737,KORHOGO,612025200065983,3536880579765239,,8885.75,76.4,-8430,-379.35,4.5,0.00,,,,,SUCCESS,,0,0,,,0101000926,Web User,0.00,0.00,03462521459,,,,,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi |
|----------|----------------|------------------|---------|-----------|------------|----------|------------------------|---------------------|--------------|------------|----------|--------|-----------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|------------|----------|--------|------------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|--------|-------------------|-------------------------------|------------------------|---------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|-------------------|----------------|------------------------|----------------------|--------------|------------------------|--------------------------|---------------------------|-------|
| ecdsprts01 | 03509755367 | PA | WUI | atuzie | 20240904T100120 | 20240904T100120 | | | N | A207561 | 0153942677 | eIntermed | | NOUR_KGO_eintermedGP | 0103283737 | KORHOGO | 612027117270374 | 3547940931745778 | | 324000 | 332430 | A124327 | 0141484686 | eCabine | | NOUR_KGO_eCabineGP | 0103283737 | KORHOGO | 612025200065983 | 3536880579765239 | | 8885.75 | 76.4 | -8430 | -379.35 | 4.5 | 0.00 | | | | | SUCCESS | | 0 | 0 | | | 0101000926 | Web User | 0.00 | 0.00 | 03462521459 | | | | |

</div>

### 4.2.8 ND - TYPE_NON_AIRTIME_DEBIT

Scenario: An agent purchases a bundle for a subscriber (bundle sales)

```csv
ecdsprts02,03510714692,ND,API,smartshop,20240904T205050,20240904T205050,2014929728,,N,eCab_22502283972,0102283972,eCabine,,ONEMART_ABOBO_eCabine,0150305007,ABOBO,612026117772907,3597739715592720,,17392.9,17194.9,0101696721,0101696721,,,,,,,,,,,198,0.00,0.00,0.00,200,189.4736844,,US,SUCCESS,,0,0,Jour Mini (200 F),,2,Service User,0.00,0.00,,,,,,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi |
|----------|----------------|------------------|---------|-----------|------------|----------|------------------------|---------------------|--------------|------------|----------|--------|-----------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|------------|----------|--------|------------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|--------|-------------------|-------------------------------|------------------------|---------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|-------------------|----------------|------------------------|----------------------|--------------|------------------------|--------------------------|---------------------------|-------|
| ecdsprts02 | 03510714692 | ND | API | smartshop | 20240904T205050 | 20240904T205050 | 2014929728 | | N | eCab_22502283972 | 0102283972 | eCabine | | ONEMART_ABOBO_eCabine | 0150305007 | ABOBO | 612026117772907 | 3597739715592720 | | 17392.9 | 17194.9 | 0101696721 | 0101696721 | | | | | | | | | | | 198 | 0.00 | 0.00 | 0.00 | 200 | 189.4736844 | | US | SUCCESS | | 0 | 0 | Jour Mini (200 F) | | 2 | Service User | 0.00 | 0.00 | | | | | |

</div>

### 4.2.9 NR - TYPE_NON_AIRTIME_REFUND

Scenario: Reversal of a transaction whereby an agent attempted to purchase a bundle for a subscriber (bundle sales)

```csv
ecdsprts02,03510745308,NR,API,smartshop,20240904T210334,20240904T210335,2014985828,,N,A190628,0140367529,eCabine,,SODITEL_SP_EcabineGP,0150745656,SAN PEDRO,612028400469933,3505956566980678,,19046,19244,0161646961,0161646961,Subscriber,,,,,,,,,,198,0.00,0.00,0.00,200,189.4737,,US,SUCCESS,,0,0,Rx Sociaux Jour 200,,2,Service User,0.00,0.00,03510745300,,,,,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi |
|----------|----------------|------------------|---------|-----------|------------|----------|------------------------|---------------------|--------------|------------|----------|--------|-----------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|------------|----------|--------|------------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|--------|-------------------|-------------------------------|------------------------|---------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|-------------------|----------------|------------------------|----------------------|--------------|------------------------|--------------------------|---------------------------|-------|
| ecdsprts02 | 03510745308 | NR | API | smartshop | 20240904T210334 | 20240904T210335 | 2014985828 | | N | A190628 | 0140367529 | eCabine | | SODITEL_SP_EcabineGP | 0150745656 | SAN PEDRO | 612028400469933 | 3505956566980678 | | 19046 | 19244 | 0161646961 | 0161646961 | Subscriber | | | | | | | | | | 198 | 0.00 | 0.00 | 0.00 | 200 | 189.4737 | | US | SUCCESS | | 0 | 0 | Rx Sociaux Jour 200 | | 2 | Service User | 0.00 | 0.00 | 03510745300 | | | | |

</div>

### 4.2.10 SL - SELL (SOS Credit)

#### Example Sell for SOS-Credit

Scenario: An agent sells to a subscriber (retail sales transaction) who has a negative balance and the amount is used to replenish the subscriber's main account

```csv
ecdsprts01,00565582632,SL,USSD,79670196,20251030T110606,20251030T110606,5396077563,,N,22879670196,79670196,eMoov,,,,,,,,432319,432019,79791696,79791696,Subscriber,,,,,,,,-1981200,-1980900,300,0.00,0.00,0.00,,,,,SUCCESS,,0,0,,,79670196,Agent,0.00,0.00,,garnishSOS=true,,,,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi | a_gps |
|-----------|----------------|------------------|---------|-----------|------------------|------------------|------------------------|--------------------|--------------|------------|------------|------------------|-----------------|----------------------------------|------------|---------|-----------------|--------------------|-----------|------------------|-----------------|------------|-----------|------------|----------------|---------|------------|--------|--------|---------|-----------|------------------|-----------------|--------|-------------------|------------------------------|-----------------------|--------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|------------------|----------------|------------------------|-----------------------|--------------|--------------------------------------------------------------------------------------------------------|--------------------------|--------------------------|-------|-------|
| ecdsprts01  | 00565582632     | SL               | USSD    | 79670196 | 20251030T110606  | 20251030T110606  | 5396077563 |                    | N            | 22879670196    | 79670196 | eMoov |                 |            |            |             |                |             |           | 432319           | 432319          | 79791696  | 79791696 | Subscriber |                |         |            |        |        |         |           | -1981200              | -1980900             | 300     | 0.00                 |       0.00                       | 0.00                     |          300          |    |  |   |SUCCESS     |                           | 0           | 0         |             |                | 79670196        | Agent          | 0.00                      | 0.00                    |              | garnishSOS=true |                          |                          |       |       |

<!--

<!--
### 4.2.10 TX - TRANSFER (Campaign induced actions with override bonus)

Scenario: An agent transfers to another agent (Wholesale transaction) and is awarded an additional campaign induced bonus. The TX request will have its original TDR, while the override trade bonus will generate an additional TX TDR as follows

```csv
ecdsprts01,03509764508,TX,USSD,0102918116,20240904T100904,20240904T100904,1869081030,,N,A344244,8001231234,CampaignAccount,,GBD_COCODY,173140101,COCODY,612025202014978,3583040756011401,11743,474725,474675,A431853,0171209377,eCabine,,GBD_COCODY,0103053030,COCODY,612028301546597,3506062622664478,,150650,150700,50,0,,2.25,,,,,SUCCESS,,0,0,,,0102918116,Agent,0.00,0.00,CAMPAIGN_ID=CrediverseActions;RULE_ID=ExtraBonus;RELATED_TO_TRANSACTION_NO=03509764507,,150,98,612-2-1093-11743,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi |
|----------|----------------|------------------|---------|-----------|------------|----------|------------------------|---------------------|--------------|------------|----------|--------|-----------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|------------|----------|--------|------------------|---------|------------|--------|--------|--------|-----------|-------------------|------------------|--------|-------------------|-------------------------------|------------------------|---------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|-------------------|----------------|------------------------|----------------------|--------------|------------------------|--------------------------|---------------------------|-------|
| ecdsprts01 | 03509764508 | TX | USSD | 0102918116 | 20240904T100904 | 20240904T100904 | 1869081030 | | N | A344244 | 8001231234 | CampaignAccount | | GBD_COCODY | 173140101 | COCODY | 612025202014978 | 3583040756011401 | 11743 | 474725 | 474675 | A431853 | 0171209377 | eCabine | | GBD_COCODY | 0103053030 | COCODY | 612028301546597 | 3506062622664478 | | 150650 | 150700 | 50 | 0 | | 2.25 | | | | | SUCCESS | | 0 | 0 | | | 0102918116 | Agent | 0.00 | 0.00 | CAMPAIGN_ID=CrediverseActions;RULE_ID=ExtraBonus;RELATED_TO_TRANSACTION_NO=03509764507 | | 150 | 98 | 612-2-1093-11743 |

</div>

### 4.2.10 SL - SELL (SOS Credit)

#### Example Sell for SOS-Credit

Scenario: An agent sells to a subscriber (retail sales transaction) and is awarded an additional bonus. The SL request will have its original TDR, while the bonus will generate an additional SL TDR as follows

```csv
```
ecdsprts01,00565582632,SL,USSD,79670196,20251030T110606,20251030T110606,5396077563,,N,22879670196,79670196,eMoov,,,,,,,,432319,432019,79791696,79791696,Subscriber,,,,,,,,-1981200,-1980900,300,0.00,0.00,0.00,,SUCCESS,,0,0,,,79670196,Agent,0.00,0.00,,garnishSOS=true,,,,
```
```

<div style="width: 100%; overflow-x: auto;">

| Hostname  | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time       | End_Time         | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN   | A_Tier           | A_Service_Class | A_Group                          | A_Owner_ID | A_Area  | A_IMSI          | A_IMEI             | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN  | B_Tier     | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information                                                                                   | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi | a_gps |
|-----------|----------------|------------------|---------|-----------|------------------|------------------|------------------------|--------------------|--------------|------------|------------|------------------|-----------------|----------------------------------|------------|---------|-----------------|--------------------|-----------|------------------|-----------------|------------|-----------|------------|----------------|---------|------------|--------|--------|---------|-----------|------------------|-----------------|--------|-------------------|------------------------------|-----------------------|--------------------|------|-------|----------------|-------------|---------------------------|-------------|-----------|-------------|----------------|------------------|----------------|------------------------|-----------------------|--------------|--------------------------------------------------------------------------------------------------------|--------------------------|--------------------------|-------|-------|
| ecdsprts01  | 00565582632     | SL               | USSD    | 79670196 | 20241029T104239  | 20241029T104239  | 20241029T10:42:39+0200 |                    | N            | A344244    | 8001231234 | Campaign Account |                 | GBD_COCODY_Latrille_eIntermedGP | 173140101  | COCODY  | 614030071288124 | 3557022296679778   |           | 472890           | 472880          | 151001001  | 151001001 | Subscriber |                |         |            |        |        |         |           | 150              | 160             | 10     | 0                 |                              | 0                     |                    |      |       |                | SUCCESS     |                           | 0           | 0         |             |                | 142308356        | Agent          | 0                      | 0                     |              | CAMPAIGN_ID=CrediverseActions1;RULE_ID=ExtraRetailBonus;RELATED_TO_TRANSACTION_NO=02371011003 |                          |                          |       |       |

#### Example Sell with Discount

Scenario: An agent sells to a subscriber (retail sales transaction) and is awarded a discount. The SL request will have its original TDR, while the discount will generate an additional SL TDR as follows

```csv
ecdsprts01,2371099002,TX,USSD,142308356,20241108T143022,20241108T143023,20241108T14:30:22+0200,,N,A344244,8001231234,Campaign Account,,GBD_COCODY_Latrille_eIntermedGP,173140101,COCODY,614030071288124,3557022296679778,,468890,468865,151001001,151001001,Subscriber,,,,,,,,275,300,25,0,0,0,,,,,SUCCESS,,0,0,,,142308356,Agent,0,0,,CAMPAIGN_ID=CrediverseActions2;RULE_ID=ExtraRetailBonus;RELATED_TO_TRANSACTION_NO=02371099001,,,,
```

<div style="width: 100%; overflow-x: auto;">

| Hostname | Transaction_ID | Transaction_Type | Channel | Caller_ID | Start_Time | End_Time | Inbound_Transaction_ID | Inbound_Session_ID | Request_Mode | A_Party_ID | A_MSISDN | A_Tier | A_Service_Class | A_Group | A_Owner_ID | A_Area | A_IMSI | A_IMEI | A_Cell_ID | A_Balance_Before | A_Balance_After | B_Party_ID | B_MSISDN | B_Tier | B_Service_Class | B_Group | B_Owner_ID | B_Area | B_IMSI | B_IMEI | B_Cell_ID | B_Balance_Before | B_Balance_After | Amount | Buyer_Trade_Bonus | Buyer_Trade_Bonus_Percentage | Buyer_Bonus_Provision | Gross_Sales_Amount | COGS | Empty | Origin_Channel | Return_Code | Last_External_Result_Code | Rolled_Back | Follow_Up | Bundle_Name | Promotion_Name | Requester_MSISDN | Requester_Type | A_Hold_Balance_Before | A_Hold_Balance_After | Original_TID | Additional_Information | B_Transfer_Bonus_Amount | B_Transfer_Bonus_Profile | a_cgi | a_gps |
|----------|----------------|------------------|----------|------------|------------|----------|----------------------|-------------------|--------------|------------|-----------|---------|-----------------|----------|------------|---------|---------|---------|------------|------------------|-----------------|------------|-----------|---------|-----------------|----------|------------|---------|---------|---------|------------|------------------|-----------------|---------|-------------------|------------------------------|---------------------|-------------------|------|-------|----------------|-------------|--------------------------|-------------|------------|--------------|----------------|------------------|----------------|---------------------|-------------------|--------------|---------------------|------------------------|--------------------------|--------|--------|
| ecdsprts01 | 2371099002 | TX | USSD | 142308356 | 20241108T143022 | 20241108T143023 | 20241108T14:30:22+0200 | | N | A344244 | 8001231234 | Campaign Account | | GBD_COCODY_Latrille_eIntermedGP | 173140101 | COCODY | 614030071288124 | 3557022296679778 | | 468890 | 468865 | 151001001 | 151001001 | Subscriber | | | | | | | | 275 | 300 | 25 | 0 | 0 | 0 | | | | | SUCCESS | | 0 | 0 | | | 142308356 | Agent | 0 | 0 | | CAMPAIGN_ID=CrediverseActions2;RULE_ID=ExtraRetailBonus;RELATED_TO_TRANSACTION_NO=02371099001 | | | | |

</div


### 4.2.10 PR - REGISTER_PIN

[Placeholder for REGISTER_PIN transaction sample]

### 4.2.11 CP - CHANGE_PIN

[Placeholder for CHANGE_PIN transaction sample]

### 4.2.12 TS - TRANSACTION_STATUS_ENQUIRY

[Placeholder for TRANSACTION_STATUS_ENQUIRY transaction sample]

### 4.2.13 LT - LAST_TRANSACTION_ENQUIRY

[Placeholder for LAST_TRANSACTION_ENQUIRY transaction sample]

### 4.2.14 AJ - ADJUST

[Placeholder for ADJUST transaction sample]

### 4.2.15 SQ - SALES_QUERY

[Placeholder for SALES_QUERY transaction sample]

### 4.2.16 DQ - DEPOSITS_QUERY

[Placeholder for DEPOSITS_QUERY transaction sample]

### 4.2.17 SB - TYPE_SELL_BUNDLE

[Placeholder for TYPE_SELL_BUNDLE transaction sample]

### 4.2.18 RW - TYPE_PROMOTION_REWARD

[Placeholder for TYPE_PROMOTION_REWARD transaction sample]

### 4.2.19 AD - TYPE_ADJUDICATION

[Placeholder for TYPE_ADJUDICATION transaction sample]


## 5. Data Integrity and Security

### 5.1 Data Validation
TBD: Define data validation rules for each field to ensure data integrity.

### 5.2 Error Handling
TBD: Specify how errors in TDR generation or processing should be handled and logged.

### 5.3 Data Encryption
TBD: Define encryption requirements for TDR storage and transmission.

## 6. TDR Processing and Analysis

### 6.1 Real-time Processing
TBD: Describe any real-time processing requirements for TDRs.

### 6.2 Batch Processing
TBD: Outline batch processing procedures and schedules for TDRs.

### 6.3 Reporting and Analytics
TBD: Specify standard reports and analytics to be generated from TDRs.

## 7. Compliance and Auditing

### 7.1 Regulatory Compliance
TBD: Outline how TDRs support compliance with relevant financial and telecom regulations.

### 7.2 Audit Trail
TBD: Describe how TDRs contribute to the system's audit trail and how they can be used in audits.

## 8. Integration with Other Systems

TBD: Specify how TDRs integrate with other systems within the organization, such as billing, customer relationship management, or fraud detection systems.

## Related Documents
- [[EVD System Architecture]]
- [[Data Privacy and Security Policy]]
- [[Regulatory Compliance Guidelines]]

## Approvals
| Role/Department    | Name | Date | Signature |
| ------------------ | ---- | ---- | --------- |
| System Architect   |      |      |           |
| Compliance Officer |      |      |           |
| CTO                |      |      |           |

## Changelog

| Version | Date           | Author           | Changes                                                                                                      |
| ------- | -------------- | ---------------- | ------------------------------------------------------------------------------------------------------------ |
| 1.0.1   | [[2024-09-04]] | [[Faraz Ali]]    | Fixed sections 3.1 and 4.2.3, making table pretty by modifying the field #58 description which was breaking the table structure due to use of **&#124;** character    |
| 1.0.0   | [[2024-09-03]] | [[AI Assistant]] | Initial version of the refactored TDR Specification. Restructured to comply with the Root Document Template. |

-->
---
© 2025 Concurrent Systems. All Rights Reserved.
