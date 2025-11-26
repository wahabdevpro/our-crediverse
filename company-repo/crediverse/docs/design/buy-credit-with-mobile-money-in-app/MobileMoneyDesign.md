# Mobile Money in the SmartApp and MAS

## Participants

## User Story:  

As a Crediverse Retailer Agent I would like to buy Crediverse Credit from my Mobile Money (Flooz) account.


## Participants

### Crediverse Retailer Agent
The retailer agent is a SmartApp user that has a Mobile Money account.  They sell airtime from their Crevdiverse account and need to buy more credit to resell from time to time. In this case they buy Crediverse Credit from their Mobile Money account. 

### Crediverse Mobile Money Agent
The mobile money agent manages a Crediverse account and a Mobile Money account on behalf of the operator.  In this use case the Mobile Money agent is represented by the Mobile Application Service(MAS).  The MAS logs into Crediverse as the mobile money agent and into the Mobile Money service with the credentials of the mobile money agent as configured in MAS.

## Assumptions
* The mobile money service can affect transactions between mobile money users.
* The operator have a mobile money user that the mobile money agent can login as on the mobile money service and request transfers from other mobile money users. 
* The retailer_agent has a mobile money account that the mobile_money_agent can affect transfer from. 

## Buying Process 

*Note:* The Flooz Moov Mobile Money wsdl shared with us documents a call that seems to allow us to make the call between MAS and the MobileMoneyService in the diagram. 

@startuml 

participant APP  as app

participant MAS as mas

participant Crediverse as cred

participant "Flooz \nMobile Money Service" as mms

app -> mas: BuyCreditWithMobileMoney(\n buyer: retailer_agent, \n amount:mm_amount)

mas -> mms: transfer_mobile_money(\n from: retailer_agent, \n to: mobile_money_agent ,\n amount: mobile_money_amount)

mms --> mas: transferred_ok 
mas -> cred: login(mobile_money_agent)
cred --> mas: session_id

mas -> cred: transfer (\n from:mobile_money_agent , \n to:retailer_agent , \n amount: crediverse_credit_amount)
cred --> mas: transferred_ok

mas --> app: transferred_ok

@enduml

>  **NOTE:** For simplicity the diagram doesn't document the process atomicity assurance mechanisms.
 
## Accounting 
In the tables below CC refers to Crediverse Stock and MM to mobile money.

This is the accounting entries for a 'Retailer Agent' with 220 MM and 110CC buying 100 CC for 100 MM  from a 'Mobile Money Agent' that has 2205 MM and 1205 CC. 

1. **Before the transaction**

| Account              | Debit (CC) | Debit (MM) | Credit (CC) | Credit (MM) |
|----------------------|------------|------------|-------------|-------------|
| Retailer Agent's CC  |            |            |  110 CC     |             |
| Retailer Agent's MM  |            |            |             |  220 MM     |
| Mobile Money Agent's CC |          |            |  1205 CC    |             |
| Mobile Money Agent's MM |          |            |             |  2205 MM    |

2. **The transaction**

| Account              | Debit (CC) | Debit (MM) | Credit (CC) | Credit (MM) |
|----------------------|------------|------------|-------------|-------------|
| Retailer Agent's CC  |  100 CC    |            |             |             |
| Retailer Agent's MM  |            |            |             |  100 MM     |
| Mobile Money Agent's CC |          |            |  100 CC     |             |
| Mobile Money Agent's MM |          |  100 MM    |             |             |


3. **After the transaction**

| Account              | Debit (CC) | Debit (MM) | Credit (CC) | Credit (MM) |
|----------------------|------------|------------|-------------|-------------|
| Retailer Agent's CC  |            |            |  210 CC     |             |
| Retailer Agent's MM  |            |            |             |  120 MM     |
| Mobile Money Agent's CC |          |            |  1105 CC    |             |
| Mobile Money Agent's MM |          |            |             |  2305 MM    |

*Note:*
* In the "Before the transaction" diagram, the credit column shows the balance for the CC account and the MM wallet.
* In the "Transaction" diagram, the Retailer Agent's CC account is debited (increasing its balance) and their MM wallet is credited (decreasing its balance). Conversely, the Mobile Money Agent's CC account is credited (decreasing their balance) and their MM wallet is debited (increasing their balance).
* In the "After the transaction" diagram, the new balances are shown.


## Mas Changes 

The MAS will include a Mobile Money integration layer, and it's interface will be extended to allow for Mobile Money sales. 

### New MAS configuration

MAS need the following to be configured as environment variables: 

NOTE: This needs to be the Crediverse credentials of the agent that sells credit for Mobile Money on behalf of the operator.  It should be a service account. 

* MOBILE_MONEY_AGENT_MSISDN 
* MOBILE_MONEY_AGENT_PIN

* MOBILE_MONEY_AGENT_PASSWORD
* MOBILE_MONEY_AGENT_USER_NAME

### New Proto remote procedure calls and messages 

```
...

  rpc getMobileMoneyBalance(NoParam) returns (MobileMoneyBalance);
  rpc buyAirtimeWithMobileMoney(BuyWithMobileMoneyRequest) returns (Ok);

...

message Ok {}

message MobileMoneyBalance { 
  string mobile_money_balance = 1;
}

message BuyWithMobileMoneyRequest {
  string mobile_money_amount = 1;
}

message TeamMemberSalesSummaryRequest{
    string msisdn = 1;
    uint64 startTime = 2;
    uint64 endTime = 3;
}

message NoParam {}

... 

```


## Potential Crediverse changes
Currently a agent needs to supply a OTP when logging in to Crediverse.  This mechanism will not work for the Mobile Money Agent.  It's unclear at the time of writing this document whether the current Crediverse service account mechanism supports this. 


## SmartApp Changes 

The SmartApp will include a Buy button on the home screen. This buy button will open a dialog with the Mobile Money (Flooz) credit visible and an input box. You can specify the Floox amount that will be deducted for your Crediverse Credit purchase. You can then click the Buy button to make the purchase.


### New SmartApp configuration

Inside the app configuration file (`strings.xml`), you can set `mobile_money_integration_enabled` to `true` or `false` so as to display or hide the Buy button on the home screen respectively.


### Method Calls

Two new method calls will be added:

```
getMobileMoneyBalance()
buyAirtimeWithMobileMoney(amount: Double)
```

The `getMobileMoneyBalance()` call will be made when the button on the home screen is clicked, it will retrieve your Mobile Money (Flooz) balance for displaying on the dialog.  
The `buyAirtimeWithMobileMoney(amount)` will be called when selecting the Buy button on the dialog, it will communicate with MAS and first deduct the Flooz amount, then if successful, it will credit the logged in Agent's Crediverse Account (see above MAS description for more details on MAS side for these calls).

