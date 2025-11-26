---
aliases: [DeacAgentUC]
tags: [use-case]
---

## Primary Actor
[[Crediverse Administrator]]

## Scope
[[Crediverse]] Agent Management

## Level
User goal  

## Stakeholders and Interests
- [[Crediverse Administrator|Admin]] wants to deactivate an agent to block them from trading.

## Preconditions 
- [[Crediverse Administrator|Admin]] is logged into system. 
- [[Crediverse Administrator|Admin]] has requisite permission to deactivate agents.

## Minimal Guarantee
There is a record of the attempted deactivation, related system actions and the outcome of the deactivation attempt.

## Success Guarantee
- The deactivated [[Crediverse Agent|agent]] is blocked from access or transactions.
- The [[MSISDN]] of the agent is free for reuse for new agents in [[Add Crediverse Agent UC]].
 
## Main Success Scenario
- 1 - [[Crediverse Administrator|Admin]] navigates to the agent management area and enters the [[MSISDN]] for the [[Crediverse Agent|agent]] they want to deactivate in the search or agent selection mechanism.
- 2 - System displays the agent profile including, Agent ID, Agent Name, Agent Status, account balance, on-hold balance, list of associated proxies. There is a choice of actions to perform on the [[Crediverse Agent|agent]] including deactivate option.
- 3 - [[Crediverse Administrator|Admin]] selects the deactivate option.
- 4 - System requests [[Crediverse Administrator|Admin]] to confirm deactivation.
- 5 - [[Crediverse Administrator|Admin]] confirms deactivation of the [[Crediverse Agent|agent]].
- 6 - System processes instruction and displays success message to [[Crediverse Administrator|Admin]]

## Extensions
### 2a - MSISDN is invalid or not found:
- 2a1 - System displays error message that [[MSISDN]] is invalid and allows user to go back to step 1.

### 2b - Agent is already deactivated:
- 2b1 - System displays [[Crediverse Agent|agent]] profile indicating that they are deactivated and there it no option available to deactivate the agent. 

### 5a - Admin cancels deactivation:
- 5a1 - System aborts [[Crediverse Agent|agent]] deactivation.




