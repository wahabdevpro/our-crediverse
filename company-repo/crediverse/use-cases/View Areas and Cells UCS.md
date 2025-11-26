---
tags: [use-case]
#draft 
---

# View Areas and Cells UCS

## Primary Actor
[[Crediverse Administrator]] 

## Scope
Location Module

## Level
User Goal

## Stakeholders and Interests
[[Crediverse Administrator]] wants a way to visually inspect the Location (Cells and Areas) loaded to the System by [[Import Location UC]]

## Preconditions
[[Crediverse Administrator]]is logged in an has requisite permissions to manage locations 
Areas and Cells have been successfully imported to the System [[Import Location UC]]

## Minimal Guarantee
System logs actions and their outcome

## Success Guarantee
System displays the imported Area and Cell data on the UI

## Main Success Scenario
1.  [[Crediverse Administrator]]  navigates to the `Location Information` module and selects the `Areas` option
2.  The System displays the  `Areas Management` screen
3.  [[Crediverse Administrator]]  inspects the Areas displayed
4.  [[Crediverse Administrator]] navigates back to the `Location Information` module and selects the `Cells` option
5.  The System displays the CGI information on the `Cells Management` screen
6.  [[Crediverse Administrator]]  inspects the Cells displayed 
7.  [[Crediverse Administrator]] navigates away from the Location module
	
## Alternate Flows:
### 3a - Error spotted:
- 3a1 - [[Crediverse Administrator]] corrects error(s) from the UI as per [[Edit Areas and Cells UCS]] 
- 3a2 - System saves changes and reflects them on UI
- Go to Step 3 to continue inspection

### 6a - Cells not assigned to Areas:
- 6a1 - [[Crediverse Administrator]] assigns Cells to Areas in bulk via [[Import Location UC]]
- 6a2 - System displays changes on UI
- Go to Step 6 to continue inspection

## Extensions:
 


