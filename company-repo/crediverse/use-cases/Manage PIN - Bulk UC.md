# Use-Case 
Change Agent PIN using batch upload

## Primary Actor
[[Crediverse Administrator]] 

## Scope
[[Crediverse]] batch processing

## Level
User Goal

## Stakeholders and Interests
- [[Crediverse Administrator]] wants to update or reset the PINs of [[Crediverse Agent]] in bulk 
- [[Crediverse Agent]] wants to be aware when his PIN is reset or changes 

## Precondition
[[Crediverse Administrator]] is logged in and has permissions to manage PINs

## Minimal Guarantee
System records actions and the outcome

## Success Guarantee
- Agent PIN is updated
- Agent receives SMS notification for the PIN change

## Main Success Scenario
- 1 - [[Crediverse Administrator]] uploads batch file to update Agent PINs
- 2 - System processes the batch file, updating Agent details, and displays success confirmation on the UI
- 3 - System sends change of PIN notification to Agents 

## Extensions
### - 2a: Batch fails:
- 2a1 - System displays error message identifying problematic record in the batch file
- 2a2 - [[Crediverse Administrator]] corrects the record and tries again
- 2a3 - Go to Step 1.
