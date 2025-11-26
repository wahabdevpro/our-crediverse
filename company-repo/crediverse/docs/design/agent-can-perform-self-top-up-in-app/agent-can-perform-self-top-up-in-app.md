# Agent Can Self Top-up Using the Mobile App
## 1. Introduction
This document describe the changes required in the Crediverse Transaction Server to facilitate an Agent to perform a Self top-up transaction from the SmartApp. The TDRs for the transaction should reflect this as a self top-up transactions (not as an airtime sale) and the transaction details also capture this as a self top-up transaction, distinct from an airtime sale to a subscriber.

## 2. Background
The agent has two options to perform the top-up process in Crediverse via USSD: either by using the "sell-to-self" option or the "self top-up" option. The option "sell-to-self" was introduced in Crediverse to address a scenario where a customer on a particular site wanted to utilize the same USSD code for both self top-ups and airtime sales. 

Although both these options (self top-up and sell-to-self) are identical for the agents, they have different outcomes for operators. If the agent selects the "sell-to-self" option, it is considered a "Sell" transaction. Therefore, in reports, we cannot fetch actual `self_topup` reports by setting the filter.

## 3. Proposed Design
An agent using SmartApp will navigate to the "Airtime" tab and perform a normal "Sell" transaction with his own MSISDN entered in the "Recipient" field. The process will initiate a regular "Sell" request from SmartApp to MAS through to Crediverse using the `/sell` endpoint on Crediverse and Crediverse will determine whether the target MSISDN and agent MSISDN are identical. If they are the same, Crediverse will internally generate a `self_topup` request. In this way, the transaction will be recorded as `self_topup` in database and TDRs.

### Important Considerations
1. The transaction will be treated as a `sell` request by SmartApp and MAS but Crediverse will resolve this to `self_topup` request.
2. The "Allow sell to self" option on the Crediverse Admin GUI under the "Airtime Sales" configuration must be enabled.
3. The customer using the "sell-to-self" option via USSD won't be affected in any way.
4. The Transaction history page on the SmartApp should display these transactions as "Self Top-Up" transactions instead of "Sell" transactions.
5. This method of implementation will require extensive testing to make sure nothing has been affected, as we are changing the `sell` endpoint.
