---
tags: [use-case]
---

# Undo last airtime transaction UCS
## Primary Actor
[[Crediverse Retail Agent]]

## Scope
[[Crediverse]]

## Level
User Goal

## Stakeholders and Interests
- [[Crediverse Retail Agent]] wants to quickly undo his last transaction when making an error so that he does not lose money if the subscriber starts using the credit erroneously transferred to them by the time [[Crediverse Customer Care Agent]] has completed the reversal.
- [[Consumer Subscriber]] wants to get the correct amount of airtime without being unnecessarily delayed by agent calling customer care before receiving the airtime.
- [[Invalid Receiver Subscriber]] does not want to be hassled and confused by the erroneous transfer.
- [[Crediverse Customer Care Agent]] appreciates allowing [[Crediverse Agent]] to self-manage and correct his own errors, reducing the load and strain on the call centre.

## Preconditions
- The [[Unlock Premium Feature UC]] has been executed 
- The [[Configure Quick Undo UC]] is completed - the feature is enabled on the UI and a USSD shortcode assigned to access the feature e.g. *999#
- [[Crediverse Agent]] has performed [[Sell Airtime UC]] transaction to a B-Party subscriber and realises he has used an incorrect MSISDN in the transaction
- The B-Party subscriber has been provisioned with airtime

## Minimal Guarantee
System logs all actions related to the attempt at reversing the airtime sale and the outcome

## Success Guarantee
- [[Crediverse Agent]] and [[Invalid Receiver Subscriber]] account balance is returned to the state it was in prior to the reversed transaction
- [[Crediverse Agent]] is notified of the outcome of the reversal via USSD response and SMS summarising the reversed transaction
- [[Invalid Receiver Subscriber]] is notified of the reversal and subsequent change to his account balance via SMS
- System creates a  TDR which reflects the transaction and reversal actions which is compatible with manual [[Reverse Transaction UC]]

## Main Success Scenario
- 1 - [[Crediverse Agent]] dials dedicated Quick Undo shortcode e.g. *999#
- 2 - System responds with details of the last successful airtime transaction performed by the Agent MSISDN within the last 90 seconds, and requests confirmation for the reversal of the transaction 
   e.g. {agent name} {agent MSISDN} sold {transaction amount} airtime to {B-Party MSISDN} at {transaction time hh:mm} with {airtime sale TID}. Confirm reversal 1=Yes, 2=Cancel
- 3 - [[Crediverse Agent]] confirms to proceed with the reversal
- 4 - System restores the balances of both parties to what they were prior to the erroneous transaction, and
	- sends USSD success response to the [[Crediverse Agent]] , and
	- sends SMS notification to the [[Invalid Receiver Subscriber]] similar to: “{transaction amount} airtime was allocated to you in error by {Agent MSISDN}. {Airtime sale TID}. This has been corrected {Reversal TID} . Dial {cc} * 111# for help”
- 5 - [[Crediverse Agent]] receives SMS and reads a message similar to:
  “Error {Airtime sale TID} to {B-Party MSISDN} resolved. {transaction amount} has been returned to your account {Reversal TID}. {balance before reversal}{balance after reversal}

## Extensions
### 2a - Agent is not in Retailer tier type:
- 2a1 - System responds to USSD request with “not eligible for this service” and ends the session

### 2b - Last transaction was not an airtime sale:
- 2b1 - System responds to USSD request with “Transaction cannot be reversed. {TID}  Dial {cc} for help” and ends the session.

### 2c - Last transaction was not a successful airtime sale:
- 2c1 - System responds to USSD request with “Transaction {TID} was not successful and cannot be reversed. Dial {cc} for help” and ends the session.

### 2d - No transactions performed by the Agent in last 90 seconds:
- 2d1 - System responds to USSD request with "No transactions available for reversal" and ends the session.

### 3a: Agent does not process confirmation: 
- 3a1 - System times out and ends session

### 3b: Agent opts to cancel the reversal:
- 3b1 - System processes cancellation and ends session

### 4a: Subscriber has insufficient balance for a full reversal:
- 4a1 - System deducts maximum available amount from B and continues with Step 5



