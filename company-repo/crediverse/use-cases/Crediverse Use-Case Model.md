

##  Manage Web Users
| Actor | Name             | Short Description   | 
| ----- | ---------------- | ------------------------------|
| [[Crediverse Administrator]]| Manage Web Users | Add, Edit, Delete, View, Search, Import, Export web users |
| [[Crediverse Administrator]]| Manage Departments | Add, Edit, Delete, View, Search, Import, Export Departments |
| [[Crediverse Administrator]]| Manage Roles | Add, Edit, Delete, View, Search, Import, Export web user Roles |
| [[Crediverse Administrator]]| Manage Permissions | Add, Edit, Delete, View, Search, Import, Export role permissions | 

## Manage Agents  
| Actor | Name             | Short Description   | 
| ----- | ---------------- | ------------------------------|
| [[Crediverse Administrator]]| Add Agents | Add new agents to the System |
| [[Crediverse Administrator]]| Edit Agents | Amend existing Agent details  |
| [[Crediverse Administrator]]| Search for Agents | Find Agents using search criteria |
| [[Crediverse Administrator]]| View Agents | Study the Agent account  |
| [[Crediverse Administrator]]| Import Agents  | Add new agents to the system in bulk |
| [[Crediverse Administrator]]| Export Agents | Export agent details from the system in bulk |
| [[Crediverse Administrator]]| Manage Agent State | Set state to active, suspended, deactivated, delete|

## Manage Agent Transactions
| Actor | Name             | Short Description   | 
| ----- | ---------------- | ------------------------------|
| [[Crediverse Administrator]]| Search / View Agent Accounts  | Find Agent account details |
| [[Crediverse Administrator]]| Adjust Agent Accounts  | Adjust account balance up or down|
| [[Crediverse Administrator]]| Adjudicate Agent Accounts  | Inspect outcome of transaction and adjust account accordingly  |
| [[Crediverse Administrator]]| Reverse transactions  | Execute full or partial reversals adjusting agent account balance |

## Manage Business Rules
| Actor | Name             | Short Description   | 
| ----- | ---------------- | ------------------------------|
| [[Crediverse Administrator]]| Configure transfer rules | Specify who can transfer to whom and at what trade bonus %|
| [[Crediverse Administrator]]| Configure Tiers | Set up trade hierarchy |
| [[Crediverse Administrator]]| Configure Groups | Classify Agents into segments |
| [[Crediverse Administrator]]| Configure Service Classes | Perform agent segmentation|
| [[Crediverse Administrator]]| Configure location | Set up the geographical location structure  |
| [[Crediverse Administrator]]| Configure Promotion & Reward | Define promotion period, criteria and specify rewards to provision to Agents |
| [[Crediverse Administrator]]| Configure Campaigns | Specify campaign period, criteria and the resulting benefit to A-Party and/or B-Party|

## Manage System Configurations
| Actor | Name             | Short Description   | 
| ----- | ---------------- | ------------------------------|
| [[Crediverse Administrator]]| Configure data retention | Specify how long data is retained on the live system and what happens to the data thereafter |
| [[Crediverse Administrator]]| Define number format | Specify accepted MSISDN format |
| [[Crediverse Administrator]]| Configure cluster management | Configure live, maintenance and test modes |

## Perform Transactions
| Actor | Name             | Short Description                                                                                                                             |
| ----- | ---------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| [[Crediverse Agent]] | Sell Airtime     | Retailers sell credit to subscribers                                                                                                          |
| [[Crediverse Agent]] | Sell Bundle      | Retailers sell bundles to subscribers                                                                                                         |
| [[Crediverse Agent]] | Transfer Credit  | Wholesaler moves credit to other wholesalers or retailers                                                                                     |
| [[Crediverse Agent]] | Topup Self       | Agent "buys" credit for own use. No commission earned.                                                                                        |
| [[Crediverse Administrator]] | Replenish Credit | New credit created in Root account for distribution through agent network                                                                     |
| [[Crediverse Administrator]] | Transfer         | Operator moves credit from Root to in-house accounts or to Wholesalers                                                                        |
| [[Crediverse Administrator]] | Adjustment       | Agent balance changed (up / down) resulting in debit / credit on agent account and corresponding action (credit / debit) is made to Root      |
| [[Crediverse Administrator]] | Reversal         | Sales, Transfer, Self TopUps can be reversed resulting in crediting source account and debiting buyer account with full amount of transaction |
| [[Crediverse Administrator]] | Partial Reversal | Some credit has been used and full reversal cannot be made, resulting in Partial reversal of an amount less than transaction value            |
| [[Crediverse Administrator]] | Adjudication     | Release On-hold/Follow-up amounts back to the Agent account if investigation shows that transaction with undetermined status failed           |
| [[Crediverse Agent]] | PIN Registration | Set selected PIN after initial registration or after PIN reset to authorise agent transactions                                                |
| [[Crediverse Agent]] | PIN change       | Agent can alter existing PIN                                                                                                                  |

## Transaction Queries
| Actor | Name               | Short Description                                           |
| ----- | ------------------ | ----------------------------------------------------------- |
| [[Crediverse Agent]] | Balance            | Check available Crediverse account balance                  |
| [[Crediverse Agent]] | Transaction status | View outcome of Transfer, Sales or Self TopUp transaction   |
| [[Crediverse Agent]] | Last transaction   | Check details of the last transaction performed             |
| [[Crediverse Agent]] | Sales              | View summary of sales performed since midnight              |
| [[Crediverse Agent]] | Deposits           | View transfers into agent Crediverse account since midnight |

## Provide Analytics
| Actor | Name                               | Short Description                                                                                                                                       |
| ----- | ---------------------------------- | --------------------------------------------------------------------------- |
| [[Crediverse Administrator]] | Produce BAM | View near real time analytics to keep business informed of performance |
| [[Crediverse Administrator]] | Generate TDR | Use for downstream data analysis |
| [[Crediverse Administrator]] | Inspect Audit Log | Investigate changes to agent details, account balances, business rules configuration  |

## Produce Reports
| Actor | Name                               | Short Description                                                                                                                                       |
| ----- | ---------------------------------- | --------------------------------------------------------------------------- |
| [[Crediverse Administrator]] | Report on performance by Location  | Create, View, Edit, Schedule, Delete, Export|
| [[Crediverse Administrator]] | Summarise daily sales activity | Create, View, Edit, Schedule, Delete, Export |
| [[Crediverse Administrator]] | Report on daily group sales | Create, View, Edit, Schedule, Delete, Export |
| [[Crediverse Administrator]] | Report on monthly sales performance| Create, View, Edit, Schedule, Delete, Export |
| [[Crediverse Administrator]] | Produce account balance report | Create, View, Edit, Schedule, Delete, Export |
| [[Crediverse Administrator]] | Report on retailer performance | Create, View, Edit, Schedule, Delete, Export |                                                                                                                              |
| [[Crediverse Administrator]] | Report on wholesaler performance  | Create, View, Edit, Schedule, Delete, Export |
| [[Crediverse Administrator]] | Report on mobile money transfers  | Create, View, Edit, Schedule, Delete, Export |


## API
| Actor | Name   | Short Description                                                                                                                                                                                                            |
| ----- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 3PP   | Transfer Credit |  Transfer credit between accounts.     |
| 3PP   | Sell Airtime  | Perform airtime sales      |
| 3PP   | Sell Bundle  | Perform bundles sales. Debit agent account                    |
| 3PP   | Access | Retrieve agent account profile data, Retrieve the API user profile, Authenticate against Crediverse                                                                                                                          |
| 3PP   | Update | Amend and update the account or API user profile, Update password                                                                                                                                                            |
| 3PP   | Query  | Perform single transaction query, balance enquiry or transaction notification query. Retrieve a list of transactions or transaction notifications. Poll for notifications. Check Agent segmentation and eligibility details. |
