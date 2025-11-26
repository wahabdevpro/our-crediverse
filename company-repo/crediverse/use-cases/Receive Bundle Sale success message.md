---
tags: [use-case]
---

# Receive bundle sale success message 
Starts with a Crediverse Agent initiating a bundle sale, and ends with the Agent receiving a confirmation message for the successful sale.  

## Primary Actor
Crediverse Agent

## Scope
Bundle Sale 
Messaging Module

## Level
User Goal

## Stakeholders and Interests
Marketing Exec wants to know that the Crediverse Agent is motivated to boost bundle sales during the promotional period and is suitably informed of his rewards.

## Precondition
-   The Crediverse Agent is Active and eligible to sell bundles, with sufficient balance in his Crediverse account
-   The pre-paid subscriber is eligible to purchase a bundle and is found on Charging System
-   Bundle Sale Solution systems are integrated via API
    

## Minimal Guarantee
The System retains a log of attempted actions and their outcome

## Success Guarantee
Crediverse Agent receives a confirmation message displaying the % discount received for the bundle sale  
The message received by the Agent includes the discount % awarded, Agent balance and text relating to the bundle sale promotional period such as "You received {5%} discount from {weekend} promotion. Your balance is {1234}" where optional parameters are populated as per:
	- {5%} is the discount % applicable during the current scheduled promotional period
	- {weekend} is the name of the scheduled promotional period
	-{1234} is the Agents' Crediverse account balance after the successful completion of the bundle sale transaction

## Main Success Scenario
1. [[Crediverse Agent]] sends command to purchase a bundle to the Bundle Sale solution System
2. System performs eligibility checks, deducts bundle charge less discount % from Crediverse Agent and provisions bundle to subscriber
3. System sends configurable success SMS notification to Agent 

## Exceptions
### 3a: Bundle sale fails:
- 3a1 - System sends failure notification to Agent