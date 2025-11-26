## Primary Actor
[[Crediverse Agent]]

## Scope
Crediverse Bundle Sales

## Level
User Goal

## Stakeholders and Interests
- [[Crediverse Customer]] wants to buy a bundle via a [[Crediverse Agent]]
- [[Crediverse Agent]] wants to earn income by selling bundles to [[Crediverse Customer]]
- [[Crediverse Customer Care Agent]] wants to have a clear record of each transaction available during calls
- [[Customer Support Agent]]  want audit trails of transactions for troubleshooting
- [[Mobile Network Operator]] wants to sell bundles at the [[Point of Sale]]

## Precondition 
- [[Crediverse Customer]] has provided cash to [[Crediverse Agent]] and specified a bundle they would like to purchase

## Minimal Guarantee
- There a logs and audit trail of the transaction and its disposition.
- The [[Crediverse Customer]] never gets provisioned with the purchased bundle in any scenario when [[Crediverse Agent]] fails to be charged

## Success Guarantee
- [[Crediverse Customer]] is provisioned with the desired bundle after paying.
- [[Crediverse Agent]] is debited for the price of the bundle and receives commission

## Main Success Scenario
- 1 - [[Crediverse Agent]] dials the USSD short code
- 2 - System obtains the [[MSISDN]] of the [[Crediverse Agent]] and the selected bundle
- 3 - System debits the [[Crediverse Agent]] account by the price of the bundle (discount)
- 4 - System Provisions the [[Crediverse Customer]] with the chosen bundle
- 5 - System notifies the parties
- 
## Extensions
- 3a - There is an internal timeout during the transaction to debit the [[Crediverse Agent]]
- 3a1 - The system skips provisioning the [[Crediverse Customer]] with the purchased bundle and writes a record of the error as a transaction detail record, and does not reverse the [[Crediverse Agent]] debit
- 3a2 - The system returns an error to [[Crediverse Agent]] (who must decide how to handle the [[Crediverse Customer]]) - (What does this error say?)
- 3a3 - The system does nothing else hoping someone, somewhere will help the agent to be reimbursed (sweeps problem under rug)

- 3b - The [[Crediverse Agent]] does not have enough credit


