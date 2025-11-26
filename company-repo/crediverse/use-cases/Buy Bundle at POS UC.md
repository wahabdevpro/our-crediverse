## Primary Actor
[[Mobile Subscriber]]

[[Crediverse Reseller Agent]]

[[Mobile Sales Support Center]]

## Scope
Airtime agent POS business

## Level
Business

## Stakeholders and Interests
- [[Mobile Subscriber]] wants an agent to buy a bundle on their behalf.
- [[Crediverse Agent]] wants to earn profit by selling bundles to [[Mobile Subscriber]].
- [[Mobile Network Operator]] wants to leverage airtime [[Point of Sale|POS]] to sell bundles.
- [[Mobile Sales Support Center]] to handle agent and subscriber complaints.

## Precondition 
- [[Mobile Subscriber]] has cash.
- [[Mobile Subscriber]] is on the network and eligible to buy bundles.
- [[Mobile Subscriber]] doesn’t know how to buy the bundle through available channels (USSD Self-care, SMS, Web, SmartApp, etc…).
- [[Mobile Subscriber]] doesn’t want to take the risk to buy a bundle by him/herself.
- [[Crediverse Agent]] has access to [[Crediverse]] and it is online.
- [[Crediverse Agent]] has sufficient credit to pay for bundle fees.
- [[Mobile Sales Support Center]] has suitable authorization to Crediverse GUI.

## Minimal Guarantee
- Transaction detail is saved into the Crediverse warehouse.
- There is record and audit trail of the transaction.
- The [[Mobile Subscriber]] cash is returned on failure to receive the bundle
- The [[Crediverse Agent]] is notified of all failures and any pending reversals / reimbursement

## Success Guarantee
- [[Mobile Subscriber]] obtains desired bundle.
- [[Crediverse Reseller Agent]] obtains cash in exchange and gains commission.
- [[Mobile Network Operator]] sells services to enhance unearned revenues when selling bundles.

## Main Success Scenario
1 . Mobile Subscriber approaches the agent and waits to get their attention.
2 . Mobile Subscriber requests an agent to purchase a bundle.
3 . Crediverse Reseller Agent indicates the bundle price.
4 . Mobile Subscriber hands money to agent.
5 . Mobile Subscriber shares the MSISDN with the agent
6 . Crediverse Reseller Agent dials the short code by using Sell Bundle Crediverse UC to attempt to sell the bundle to subscriber and choose the requested bundle and inserts the subscriber’s MSISDN.
7 . Crediverse Reseller Agent submits the transaction after confirmation and receives a transaction status notification with the outstanding balance in the agent account.
8 . Mobile Subscriber receives an SMS notification indicating the bundle name and benefits.

## Extensions
- 7a - Transaction has failed outright
	- 7a1 - Agent attempts 6 & 7 a few times
	- 7a2 - On continued failure agent gives money back to [[Mobile Subscriber]]
- 7b - Agent has been debited, but [[Mobile Subscriber]] has not received the bundle
	- 7b1 - Agent verifies again the balance and records the transaction ID (TID)
	- 7b2 - Agent attempts 6 and 7 again 
	- 7b3 - Agent gives money back to [[Mobile Subscriber]]
	- 7b4 - "Optional" Agent calls [[Mobile Sales Support Center]] to complain and provides the recorded TID and/or transaction DateTime and Subscriber MSISDN + Bundle name.
	- 7b5 - [[Mobile Sales Support Center]] records the complaint ticket and proceeds with the investigation.
	- 7b6 - [[Mobile Sales Support Center]] takes action to reimburse agent
- 7c - Agent has not been debited, but [[Mobile Subscriber]] has received the bundle
	- 7c1 - Agent verifies the balance and records the transaction ID (TID)
	- 7c2 - "Optional" Agent gives money back to [[Mobile Subscriber]]
	- 7c3 - Agent calls [[Mobile Sales Support Center]] to inform the operator and provides the recorded TID and/or transaction DateTime and Subscriber MSISDN + Bundle name.

