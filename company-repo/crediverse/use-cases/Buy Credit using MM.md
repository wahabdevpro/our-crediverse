---
tags: [use-case-slice]
---

# Buy Credit using MM 
Starts when System receives “transfer” API call from Mobile Money (MM) and ends when a transaction response is sent to the Crediverse Agent at the conclusion of the transaction.

Out of bounds
The transaction on the MM platform is out of bounds. We are concerned with the interaction between API User and Crediverse and the effect on the Crediverse Agent account.

## Primary Actor
API User (Mobile Money 3PP)

## Scope
Crediverse API - Perform Transfer 

## Level
User

## Stakeholders and Interests
Crediverse Agent - wants to buy Crediverse Credit using MM account 

MNO - wants unhampered flow of credit through the distribution hierarchy and makes stock available to Agents via Mobile Money API in addition to upstream Agents transferring credit

## Precondition
- The Crediverse Agent has an account and is Active on Crediverse
- The API User has an Account on Crediverse and has sufficient balance [[Create API User]]
- The API User (MM)  is authenticated and access the Crediverse API via secure connection [[Integrate with Crediverse API UC]] is authenticated 
- A Transfer rule between source and destination tier exists 
- The Crediverse Agent has an account on Mobile Money (out of system bounds)

## Minimal Success Guarantee
The System has a record of the API calls and the outcomes

## Success Guarantee
- Crediverse Agent account balance is increased with the correct airtime credit purchased via MM
- Crediverse Agent is not charged from his Crediverse account 

## Main Success Scenario
(Out of System bounds but included for context -  Crediverse Agent dials the MM shortcode, and submits request to buy Crediverse Airtime to the MM platform.)
1.  Authenticated MM platform sends a `Transfer` transaction request to Crediverse API 
2.  Crediverse verifies the request and debits the API User Account
3.  Crediverse checks transfer rules, applies optional trade bonus and credits the Crediverse Agent with the correct airtime amount
4.  Crediverse sends success API response to the MM platform  
5.  Crediverse sends SMS to the recipient [[Crediverse Agent]]
    
(Out of System bounds but included for context - MM sends a success USSD notification to the Crediverse Agent)

## Extensions
### 2a: Wrong API format:
2a1 - System sends error response to 3PP and ends the transaction

### 3a: Ineligible transaction:
3a1 - System sends appropriate error response to 3PP and ends the transaction

### 4a: System error has occurred:
4a1 - System sends an appropriate, descriptive error to the 3PP**