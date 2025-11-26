# Summary
## Test Description
The newly created Agent is able to transact in the system.
## Preconditions
-   The Agent must have sufficient balance and is in active state
-   The Agent is registered with a PIN: 88990
-   Active MSISDN B party
## Dependencies
[[Add a new agent with the MSISDN not in use]]
## Postcondition
-   Transaction history of the new agent is available on the GUI

# Execution
| **Step Num** | **Test Steps** | **Test Data** | **Expected Result** | **Actual Result** | **Status** | **Notes** |
| ------------ | ---------------| ------------- | ------------------- | ----------------- | ---------- | --------- |
| 1 | New Agent dials *910*2*_80963550_*200*88990# to sell airtime. | New Agent MSISDN: 23456781090<br/>Subscriber MSISDN: 80963550 | The balance of the agent is deducted and the agent is presented with a message similar to " You have sold 200 airtime to 80963000. Your new balance is 800. " | The correct amount deducted from Agent Account<br/>Success message:<br/>"You have Sold CFA200 Airtime to 80963550. Your new Balance is CFA4,600. Ref 00000019007" | Pass | |
