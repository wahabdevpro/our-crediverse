#draft 

## Primary Actor
[[Crediverse Administrator]]

## Scope
[[Crediverse Admin Feature]]

## Level
User goal  

## Stakeholders and Interests
[[Crediverse Administrator]] wants to see which web user made what material changes  on the System at which time

## Precondition 
The [[Crediverse Administrator]] is logged into the System and has the requisite permissions to view the activity audit information

## Minimal Guarantee
It is possible to see which system user made changes to important  configurations and data such as changes to agent account details and balances, amending transfer rules and commissions.

## Success Guarantee
- The changed data can be viewed (before and after view)
- The activity audit data shows only the changed data and does not care about data that was not changed during the session
- The activity audit data consumes less than xx system resources (what is an acceptable footprint?) per (audit record or time period?)
 
## Main Success Scenario
1 - [[Crediverse Administrator]] navigates to  Activity Audit  in the web application and is able to view changes made to the master data records and the transaction configurations and identify who made the changes

## Extensions
### 1a - There are many audit records and the change the [[Crediverse Administrator]] is interested in is not displayed on the first page of entries:

1a1 - The [[Crediverse Administrator]] navigates to the next available pages to view the entries
1a2 - [[Crediverse Administrator]] finds the record he is interested in  by sorting according to the relevant identification field such as time, user type, user name, IP address, MAC address, machine name, domain name, data type, action
1a3 - [[Crediverse Administrator]] is able to perform an advanced search for specific attribute(s) to narrow down the search.
	
### 4a - The "Create" operation was performed by the web user and there is no previous data:
4a1 - The "before update" data is blank

### 4b - The "Delete" operation was performed by the web user and there is no data after the change:
4b1 - The "after update" data is blank