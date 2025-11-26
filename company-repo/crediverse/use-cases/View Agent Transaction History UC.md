---
aliases: [ViewTxHistUC]
tags: [use-case]
---
# View Agent Transaction History

## Primary Actor
[[Crediverse Customer Care Agent]] 

## Scope
Crediverse Transaction Management

## Level
User goal  

## Stakeholders and Interests
- [[Crediverse Customer Care Agent]] wants to study transaction history to ascertain options to help resolve complaints  of the [[Crediverse Agent]]
- [[Crediverse Agent]] needs recourse to assistance when the outcome of a transaction with a subscriber is disputed
- [[Subscriber]] wants to check that his purchase has been successful 

## Preconditions
- [[Crediverse Customer Care Agent]] is logged into the System

## Success Guarantee
The System displays all recent [[Crediverse Agent]] transaction history 

## Minimal Guarantee
[[Crediverse Customer Care Agent]] is informed when the System is impaired 

## Main Success Scenario
- 1 - [[Crediverse Customer Care Agent]] navigates to the Transaction Management screen
- 2 - [[Crediverse Customer Care Agent]] enters [[MSISDN ]] 
- 3 - The System prominently displays the Agent ID and Agent Name, Surname as search history header and a list of transactions performed by the [[Crediverse Agent]] including: transaction ID, success/fail status, type of transaction, amount, channel, time, [[MSISDN]] of A and B party
- 4 - [[Crediverse Customer Care Agent]] filters transactions to identify the transaction of interest
- 5 - System displays filtered transaction history

## Extensions
### 2a -  [[Crediverse Customer Care Agent]] doesn't know what the [[MSISDN]] is:
- 2a1 - [[Crediverse Customer Care Agent]] navigates to advanced search 
- 2a2 - System displays advanced search criteria: B-Party [[MSISDN]], Agent Name, A or B Party Group, Tier, Transaction date/time, transaction amount, bonus amount, transaction type, channel, transaction status, transaction number.	
- 2a3 - [[Crediverse Customer Care Agent]] enters relevant criteria and submits to System
- 2a4 - Goto 3

### 3a - Quick search by [[MSISDN]] finds more than one Agent:
- 3a1 - System displays the [[Crediverse Agent]] ID, Name, Status (one and only one Active/Suspended agent/ one or more Deactivated) associated with each Agent on the UI
- 3b2 - [[Crediverse Customer Care Agent]] selects relevant party of interest from the UI
- 3b3 - System displays transactions conducted by the party of interest to [[Crediverse Customer Care Agent]]
- 3b4 - Go to Step 4

### 3b - Search by Agent identifies more than one transacting [[MSISDN]] assigned to Agent:
- 3b1 - System displays all transactions performed by Agent on different [[MSISDN]] including  displaying [[MSISDN]] of A
- 3b2 - Go to Step 4

	
