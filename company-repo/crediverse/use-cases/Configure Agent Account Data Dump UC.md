---
tags: [use-case]
---

# Configure Agent Account Data Dump
## Primary Actor
[[Crediverse Administrator]]

## Scope
[[Crediverse]]

## Level
User Goal

## Stakeholders and Interests
Revenue Assurance teams want to process agent account data and compare against different systems to perform verification of revenue reported

## Preconditions
[[Crediverse Agent]] is logged in and has requisite permissions

## Minimal Guarantee
- The system logs the generation of an account dump and the outcome
- The time taken for account dump generation is logged

## Success Guarantee
- Account Dump is generated in accordance with the settings, and the file sent to the defined location without materially impacting system performance.

## Main Success Scenario
- 1 - [[Crediverse Administrator]] navigates to the agent account dump section
- 2 - System displays option to enable/disable account dump generation
- 3 - [[Crediverse Administrator]] enables account dump generation
- 4 - System displays configuration options:
	- Start Time
	- Include Deactivated and Suspended accounts (Yes/No)
	- Repeat No / Yes (and Repeat Interval)
	- File location
- 5 - [[Crediverse Administrator]] configures desired options 
- 6 - System displays selection on UI

## Extensions




