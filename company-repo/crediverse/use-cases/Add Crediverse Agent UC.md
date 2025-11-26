---
aliases: [AddAgentUC]
tags: [use-case]
---

## Add Agent UC
## Primary Actor
[[Crediverse Administrator]]

## Scope
Crediverse Agent Management

## Level
User goal  

## Stakeholders and Interests
- [[Crediverse Administrator|Admin]] wants to add a new [[Crediverse Agent]] to allow them to distribute airtime.
- [[Crediverse Agent|Agent]] wants to be activated on [[Crediverse]] so he can start trading.
- [[Mobile Network Operator]] want to be assured that no fraud is happening.

## Preconditions 
- [[Crediverse Administrator|Admin]] is logged into system. 
- [[Crediverse Administrator|Admin]] has requisite permission to add [[Crediverse Agent|agents]].

## Minimal Guarantee
There is a record of the attempted addition of the [[Crediverse Agent|agent]], related system actions and the outcome.

## Success Guarantee
- New [[Crediverse Agent|agent]] is added to the system and record retained of the action including date of activation.
- [[Crediverse Agent|Agent]] added has "Active" status and is able to transact.
- A given [[MSISDN]] is in use by only one active [[Crediverse Agent|agent]] at any time.
 
## Main Success Scenario
- 1 - [[Crediverse Administrator|Admin]] navigates to the agent management module and opts to add a new [[Crediverse Agent|agent]].
- 2 - System requests [[MSISDN]] and optional details of new [[Crediverse Agent|agent]].
- 3 - [[Crediverse Administrator|Admin]] submits minimally the [[MSISDN]]. 
- 4 - System checks that the [[MSISDN]] is not already in use by any other Active/Suspended [[Crediverse Agent|agent]], adds and activates the [[Crediverse Agent|agent]] and informs the [[Crediverse Administrator|admin]] of the successful addition.

## Extensions
### 4a - [[MSISDN]] submitted already in use by an active or suspended agent:
- 4a1 - System presents error message that [[MSISDN]] is assigned to an existing [[Crediverse Agent|agent]], displaying their details.
- 4a2 - The [[Crediverse Agent|agent]] performs [[Crediverse Deactivate Agent UC]] and goes back to step 1.