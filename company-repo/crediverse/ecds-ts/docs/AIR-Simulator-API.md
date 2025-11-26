# AIR Simulator Management API

This document provides a reference documentation for the Crediverse built-in AIR simulator management API. The management API is used primarily to update and query subscriber state.

[[_TOC_]]

## API Endpoint

All requests are sent to the following URL:
```
http://(crediverse-ts-hostname):10012/Air
```

The content type header must be set to `text/xml`, like this

```
Content-Type: text/xml
```

## HTTP Verb

All methods must be called using the `POST` HTTP verb.

## Authentication

There is no authentication for this API.

## Sample Request

This is an example SOAP request, without the function. Take a sample from one of the functions below and replace the inner content of `<S:Body>`, to compose a full function call:

```
<?xml version="1.0" encoding="UTF-8"?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/" xmlns="http://protocol.airsim.services.hxc/">
  <S:Header />
  <S:Body>
    (replace this with the function-specific example)
  </S:Body>
</S:Envelope>
```

## Sample Response

This is an example SOAP response, without the function-specific part:
```
<?xml version="1.0" encoding="UTF-8"?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
  <S:Body>
    (function-specific response goes here)
  </S:Body>
</S:Envelope>
```


## Methods

### `start`
----
Starts the AIR simulator. The Crediverse Transaction Server (TS) must be up and running at the time of issuing this request.

* **Parameters**

  (none)

* **Response Fields**
 
    | Field | Description |
    | ----- | ----------- |
    | return | `true` if the simulator was started successfully, `false` otherwise |

* **Sample Request**

    ```
    <start />
    ```

* **Sample Response**

    ```
    <ns2:startResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:startResponse>
    ```

* **Notes:**

    * Most likely reason for the simulator to fail to start is the port being already in use.
    * Making a `start` request while the simulator is running will restart it (effectively it will first stop it and then start it again), but will preserve its state. Issue `reset` call to reset the state of the simulator.


### stop
----
Stops the AIR simulator. The Crediverse Transaction Server (TS) must be up and running at the time of issuing this request. The request should be used after a successful `start`, otherwise it has no effect.

* **Parameters**

  (none)

* **Response Fields**
  
  (none)

* **Sample Request**

    ```
    <stop/>
    ```

* **Sample Response**

    ```
    <ns2:stopResponse xmlns:ns2="http://protocol.airsim.services.hxc/"/>
    ```

* **Notes:**

    * Executing `stop` while the simulator is not running (never started or already stopped) has no effect.


### reset
----
Resets the simulator state. All subscribers will be removed, along with any request history and other state-related information.

* **Parameters**

  (none)

* **Response Fields**
  
  (none)

* **Sample Request**

    ```
    <reset/>
    ```

* **Sample Response**

    ```
    <ns2:resetResponse xmlns:ns2="http://protocol.airsim.services.hxc/"/>
    ```

* **Notes:**

    * The reset request can be sent at any time, regardless if the AIR simulator is running or not.


### addSubscriber
----
Create a subscriber and initialise its language ID, service class ID, balance and state to the provided values. The remaing subscriber properties are initialised with defaults.

* **Parameters**

    | Field | Description |
    | ----- | ----------- |
    | msisdn | The subscriber's MSISDN number |
    | languageID | The subscriber's language ID (1-4) |
    | serviceClass | The subscriber's service class ID |
    | accountValue | The subscriber's main account balance |
    | state | The subscriber's state, can be one of: `inActive`, `active`, `passive`, `grace`, `pool`, `disconnect` |

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    | accountActivatedFlag |  |
    | accountValue1 |  |
    | accountValue2 |  |
    | activationDate |  |
    | creditClearanceDate |  |
    | currency1 |  |
    | currency2 |  |
    | internationalNumber |  |
    | languageIDCurrent |  |
    | masterAccountNumber |  |
    | masterSubscriberFlag |  |
    | nationalNumber |  |
    | serviceClassCurrent |  |
    | serviceClassOriginal |  |
    | serviceFeeExpiryDate |  |
    | serviceRemovalDate |  |
    | supervisionExpiryDate |  |

* **Sample Request**

    ```
    <addSubscriber>       
        <msisdn>830010002</msisdn>
        <languageID>1</languageID>
        <serviceClass>1</serviceClass>
        <accountValue>500000</accountValue>
        <state>active</state>
    </addSubscriber>
    ```

* **Sample Response**

    ```
    <ns2:addSubscriberResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
    <return>
        <accountActivatedFlag>true</accountActivatedFlag>
        <accountValue1>500000</accountValue1>
        <accountValue2>500000</accountValue2>
        <activationDate>2022-02-16T00:00:00+02:00</activationDate>
        <creditClearanceDate>2022-02-22T00:00:00+02:00</creditClearanceDate>
        <currency1>CFR</currency1>
        <currency2>CFR</currency2>
        <internationalNumber>830010002</internationalNumber>
        <languageIDCurrent>1</languageIDCurrent>
        <masterAccountNumber>830010002</masterAccountNumber>
        <masterSubscriberFlag>true</masterSubscriberFlag>
        <nationalNumber>830010002</nationalNumber>
        <serviceClassCurrent>1</serviceClassCurrent>
        <serviceClassOriginal>1</serviceClassOriginal>
        <serviceFeeExpiryDate>2022-02-20T00:00:00+02:00</serviceFeeExpiryDate>
        <serviceRemovalDate>2022-02-24T00:00:00+02:00</serviceRemovalDate>
        <supervisionExpiryDate>2022-02-18T00:00:00+02:00</supervisionExpiryDate>
    </return>
    </ns2:addSubscriberResponse>
    ```

* **Notes:**

    * If the subscriber already exists, it is repleced with a new one, with the provided property values + defaults.


### addSubscriber2
----
Create a subscriber and initialise its language ID, service class ID, balance and lifecycle dates to the provided values. The remaing subscriber properties are initialised with defaults.

* **Parameters**

    | Field | Description |
    | ----- | ----------- |
    | msisdn | The subscriber's MSISDN number |
    | languageID | The subscriber's language ID (1-4) |
    | serviceClass | The subscriber's service class ID |
    | accountValue | The subscriber's main account balance |
    | activationDate | The subscriber's activation date |
    | supervisionExpiryDate | The subscriber's supervision expiry date |
    | serviceFeeExpiryDate | The subscriber's service fee expiry date |
    | creditClearanceDate | The subscriber's credit clearance date |
    | serviceRemovalDate | The subscriber's service removal date |

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    | accountActivatedFlag |  |
    | accountValue1 |  |
    | accountValue2 |  |
    | activationDate |  |
    | creditClearanceDate |  |
    | currency1 |  |
    | currency2 |  |
    | internationalNumber |  |
    | languageIDCurrent |  |
    | masterAccountNumber |  |
    | masterSubscriberFlag |  |
    | nationalNumber |  |
    | serviceClassCurrent |  |
    | serviceClassOriginal |  |
    | serviceFeeExpiryDate |  |
    | serviceRemovalDate |  |
    | supervisionExpiryDate |  |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### addSubscribers
----
Create a specified number of subscribers with sequential MSISDN numbers, starting with the specified MSISDN. This method is otherwise equivalent to `addSubscriber`.

* **Parameters**

    | Field | Description |
    | ----- | ----------- |
    | msisdn | The subscriber MSISDN number |
    | count | The number of subscribers to create |
    | languageID | The subscriber language ID (1-4) |
    | serviceClass | The subscriber service class ID |
    | accountValue | The subscriber main account balance |
    | state | The subscriber state, can be one of: `inActive`, `active`, `passive`, `grace`, `pool`, `disconnect` |

* **Response Fields**
  
  (none)

* **Sample Request**

    ```
    <addSubscribers>       
        <msisdn>830020000</msisdn>
        <count>100</count>
        <languageID>1</languageID>
        <serviceClass>1</serviceClass>
        <accountValue>500000</accountValue>
        <state>active</state>
    </addSubscribers>
    ```

* **Sample Response**

    ```
    <ns2:addSubscribersResponse xmlns:ns2="http://protocol.airsim.services.hxc/"/>
    ```

* **Notes:**

    * ...


### addSubscribers2
----
Create a specified number of subscribers with sequential MSISDN numbers, starting with the specified MSISDN. This method is otherwise equivalent to `addSubscriber2`.

* **Parameters**

    | Field | Description |
    | ----- | ----------- |
    | msisdn | The subscriber's MSISDN number |
    | count | The number of subscribers to create |
    | languageID | The subscriber's language ID (1-4) |
    | serviceClass | The subscriber's service class ID |
    | accountValue | The subscriber's main account balance |
    | activationDate | The subscriber's activation date |
    | supervisionExpiryDate | The subscriber's supervision expiry date |
    | serviceFeeExpiryDate | The subscriber's service fee expiry date |
    | creditClearanceDate | The subscriber's credit clearance date |
    | serviceRemovalDate | The subscriber's service removal date |

* **Response Fields**

  (none)

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### cloneSubscriber
----
Clone a subscriber, associate the clone with new MSISDN.

* **Parameters**

    | Field | Description |
    | ----- | ----------- |
    | msisdn | The MSISDN of the subscriber to clone |
    | newMsisdn | The MSISDN to assign to the new subscriber |

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    | accountActivatedFlag |  |
    | accountValue1 |  |
    | accountValue2 |  |
    | activationDate |  |
    | creditClearanceDate |  |
    | currency1 |  |
    | currency2 |  |
    | internationalNumber |  |
    | languageIDCurrent |  |
    | masterAccountNumber |  |
    | masterSubscriberFlag |  |
    | nationalNumber |  |
    | serviceClassCurrent |  |
    | serviceClassOriginal |  |
    | serviceFeeExpiryDate |  |
    | serviceRemovalDate |  |
    | supervisionExpiryDate |  |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### cloneSubscribers
----
Clone a range of subscribers identified by MSISDN, associate the new subscriber with a new range of MSISDNs.

* **Parameters**

    | Field | Description |
    | ----- | ----------- |
    | msisdn | The MSISDN of the subscriber to clone |
    | newMsisdn | The MSISDN to assign to the new subscriber |
    | count | The number of subscribers to clone |

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    | return | `true` if the cloning is successful, `false` otherwise |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### getSubscriber
----
Retrieve the properties of a subscriber.

* **Parameters**

    | Field | Description |
    | ----- | ----------- |
    | msisdn | The MSISDN of the subscriber to clone |

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    | accountActivatedFlag |  |
    | accountValue1 |  |
    | accountValue2 |  |
    | activationDate |  |
    | creditClearanceDate |  |
    | currency1 |  |
    | currency2 |  |
    | internationalNumber |  |
    | languageIDCurrent |  |
    | masterAccountNumber |  |
    | masterSubscriberFlag |  |
    | nationalNumber |  |
    | serviceClassCurrent |  |
    | serviceClassOriginal |  |
    | serviceFeeExpiryDate |  |
    | serviceRemovalDate |  |
    | supervisionExpiryDate |  |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### updateSubscriber
----
Update subscriber properties.

* **Parameters**

	TODO

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    | return | `true` if the update is successful, `false` otherwise |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### adjustBalance
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    |  |  |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### setBalance
----
Set the balance of a subscriber by MSISDN.

* **Parameters**

    | Field | Description |
    | ----- | ----------- |
    | msisdn | The MSISDN of the subscriber whose balance is to be update |
    | newAmount | The new balance for the subscriber |

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    |  |  |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### deleteSubscriber
----
Delete subscriber and all its properties.

* **Parameters**

    | Field | Description |
    | ----- | ----------- |
    | msisdn | The MSISDN of the subscriber to be deleted |

* **Response Fields**
    
	| Field | Description |
    | ----- | ----------- |
    | return | `true` if the delete is successful, `false` otherwise |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### getLifecycle
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    |  |  |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### getLifecycles
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    |  |  |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### updateLifecycle
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    |  |  |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### deleteLifecycle
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
    | ----- | ----------- |
    |  |  |

* **Sample Request**

    ```
    ```

* **Sample Response**

    ```
    ```

* **Notes:**

    * ...


### adjustLifecycle
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### deleteLifecycles
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### hasMemberLifecycle
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getMembersLifecycle
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### addMemberLifecycle
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### deleteMemberLifecycle
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getTemporalTriggers
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### updateTemporalTrigger
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### deleteTemporalTrigger
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getOffers
----
Retrieve all offers for a particular subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to retrieve offers for |


* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | an array of Offers, see `updateOffer` method for details on the individual offer fields |

* **Sample Request**

    ```
    <prot:getOffers>
        <msisdn>830010003</msisdn>
    </prot:getOffers>
	```

* **Sample Response**

    ```
    <ns2:getOffersResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>
            <offerID>1</offerID>
        </return>
        <return>
            <offerID>7</offerID>
        </return>
        <return>
            <offerID>13</offerID>
        </return>
    </ns2:getOffersResponse>
	```

* **Notes:**

    * ...


### getOffer
----
Retrieve a specific offer for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber getting offer for |
	| offerID | The ID of the offer to retrieved details for |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | empty response if the offer is not found, an `Offer` object otherwise, see `updateOffer` method for details on the individual offer fields |

* **Sample Request**

    ```
    <prot:getOffer>
        <msisdn>830010003</msisdn>
        <offerID>15</offerID>
    </prot:getOffer>
	```

* **Sample Response**

    ```
    <ns2:getOfferResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>
            <offerID>15</offerID>
            <offerType>2</offerType>
            <startDate>2022-02-23T00:00:00+02:00</startDate>
        </return>
    </ns2:getOfferResponse>
	```

* **Notes:**

    * If the offer is not available for the subscriber, blank response is returned.


### hasOffer
----
Check if a subscriber has a specific offer.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber checking offer for |
	| offerID | The ID of the offer to check |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | method appears to always return `false`, even if the offer exists |

* **Sample Request**

    ```
    <prot:hasOffer>
        <msisdn>830010003</msisdn>
        <offerID>15</offerID>
    </prot:hasOffer>

	```

* **Sample Response**

    ```
    <ns2:hasOfferResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>false</return>
    </ns2:hasOfferResponse>
	```

* **Notes:**

    * This method needs to be fixed to return `true` if the offer exists for subscriber.


### updateOffer
----
Create or update a subscriber offer.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to create/update the offers for |
	| offer | The details of the offer |

	The `offer` parameter is a structure with the following fields

	| Field | Description |
	| ----- | ----------- |
	| offerID | The offer ID, valid range is `1` to `2147483647`. This parameter is required. |
	| startDate | Offer start date |
	| expiryDate | Offer expiry date |
	| startDateTime | Offer start date and time |
	| expiryDateTime | Offer expiry date and time |
	| pamServiceID | PAM service ID associated with the offer |
	| offerType | Offer type, valid range is `1` to `7` |
	| offerState | Offer state, valid range is `0` to `99` |
	| offerProviderID | Offer provider ID |
	| productID | Offer product ID |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the create/update is successful, `false` otherwise |

* **Sample Request**

    ```
    <prot:updateOffer>
        <msisdn>830010003</msisdn>
        <offer>
            <offerID>15</offerID>
            <offerType>2</offerType>
            <startDate>2022-02-23</startDate>
        </offer>
    </prot:updateOffer>
	```

* **Sample Response**

    ```
    <ns2:updateOfferResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:updateOfferResponse>
	```

* **Notes:**

    * ...


### deleteOffer
----
Delete an offer for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to delete offers for |
	| offerID | The ID of the offer to delete |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the delete is successful, `false` otherwise |

* **Sample Request**

    ```
    <prot:deleteOffer>
        <msisdn>830010003</msisdn>
        <offerID>1</offerID>
    </prot:deleteOffer>
	```

* **Sample Response**

    ```
    <ns2:deleteOfferResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:deleteOfferResponse>
	```

* **Notes:**

    * Most likely reason for this call to fail is if the MSISDN is not found or the offer is not found.


### getDedicatedAccounts
----
Get all dedicated accounts for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to get Dedicated Accounts for |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | array of dedicated account details |
	
	The `return` parameter is a structure with the following fields

	| Field | Description |
	| ----- | ----------- |
	| dedicatedAccountID | The dedicated account ID. |
	| dedicatedAccountValue1 | The dedicated account value, primary currency. |
	| dedicatedAccountValue2 | The dedicated account value, secondary currency. |
	
	If no dedicated accounts exist for a subscriber, blank response is returned.

* **Sample Request**

    ```
    <prot:getDedicatedAccounts>
        <msisdn>830010003</msisdn>
    </prot:getDedicatedAccounts>
	```

* **Sample Response**

    ```
    <ns2:getDedicatedAccountsResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>
            <dedicatedAccountID>100</dedicatedAccountID>
            <dedicatedAccountValue1>2000</dedicatedAccountValue1>
            <dedicatedAccountValue2>0</dedicatedAccountValue2>
        </return>
        <return>
            <dedicatedAccountID>101</dedicatedAccountID>
            <dedicatedAccountValue1>4000</dedicatedAccountValue1>
            <dedicatedAccountValue2>0</dedicatedAccountValue2>
        </return>
    </ns2:getDedicatedAccountsResponse>
	```

* **Notes:**

    * ...


### getDedicatedAccount
----
Get a specific dedicated account for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to retrieve Dedicated Account for |
	| dedicatedAccountID | The Dedicated Account ID to retrieve |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | the dedicated account details. Refer to `getDedicatedAccounts` for the returned dedicated account properties. |
	
	If the dedicated account is not found, blank response is returned.

* **Sample Request**

    ```
    <prot:getDedicatedAccount>
        <msisdn>830010003</msisdn>
        <dedicatedAccountID>100</dedicatedAccountID>
    </prot:getDedicatedAccount>
	```

* **Sample Response**

    ```
    <ns2:getDedicatedAccountResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>
            <dedicatedAccountID>100</dedicatedAccountID>
            <dedicatedAccountValue1>2000</dedicatedAccountValue1>
            <dedicatedAccountValue2>0</dedicatedAccountValue2>
        </return>
    </ns2:getDedicatedAccountResponse>

	```

* **Notes:**

    * ...


### hasDedicatedAccount
----
Check if a subscriber has a specific dedicated account.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to retrieve Dedicated Account for |
	| dedicatedAccountID | The Dedicated Account ID to retrieve |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | 'true' if the dedicated account exists, 'false' otherwise |

* **Sample Request**

    ```
    <prot:hasDedicatedAccount>
        <msisdn>830010003</msisdn>
        <dedicatedAccountID>111</dedicatedAccountID>
    </prot:hasDedicatedAccount>
	```

* **Sample Response**

    ```
    <ns2:hasDedicatedAccountResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:hasDedicatedAccountResponse>
	```

* **Notes:**

    * ...


### updateDedicatedAccount
----
Create or update dedicated account for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber whose dedicated account is to be created / updated |
	| dedicatedAccount | The details of the dedicated account |
	
	The `dedicatedAccount` parameter is a structure with the following fields

	| Field | Description |
	| ----- | ----------- |
	| dedicatedAccountID | The dedicated account ID, valid range is `1` to `2147483647`. This parameter is required. |
	| dedicatedAccountValue1 | The dedicated account value, primary currency. |
	| dedicatedAccountValue2 | The dedicated account value, secondary currency. |
	| expiryDate | The dedicated account expiry date. |
	| startDate | The dedicated account activation date. |
	| pamServiceID | PAM service ID associated with the dedicated account. |
	| offerID | Offer ID associated with the dedicated account. |
	| productID | Product ID associated with the dedicated account. |
	| dedicatedAccountRealMoneyFlag | |
	| closestExpiryDate | |
	| closestExpiryValue1 | |
	| closestExpiryValue2 | |
	| closestAccessibleDate | |
	| closestAccessibleValue1 | |
	| closestAccessibleValue2 | |
	| dedicatedAccountActiveValue1 | |
	| dedicatedAccountActiveValue2 | |
	| dedicatedAccountUnitType | |
	| compositeDedicatedAccountFlag | |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the create/update is successful, `false` otherwise |

* **Sample Request**

    ```
    <prot:updateDedicatedAccount>
        <msisdn>830010003</msisdn>
        <dedicatedAccount>
            <dedicatedAccountID>104</dedicatedAccountID>
            <dedicatedAccountValue1>4000</dedicatedAccountValue1>
            <dedicatedAccountValue2>0</dedicatedAccountValue2>
        </dedicatedAccount>
    </prot:updateDedicatedAccount>
	```

* **Sample Response**

    ```
    <ns2:updateDedicatedAccountResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:updateDedicatedAccountResponse>
	```

* **Notes:**

    * ...


### deleteDedicatedAccount
----
Delete dedicated account for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to delete Dedicated Account for |
	| dedicatedAccountID | The ID of the dedicated account to be deleted |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the delete is successful, `false` otherwise |

* **Sample Request**

    ```
    <prot:deleteDedicatedAccount>
        <msisdn>830010003</msisdn>
        <dedicatedAccountID>103</dedicatedAccountID>
    </prot:deleteDedicatedAccount>
	```

* **Sample Response**

    ```
    <ns2:deleteDedicatedAccountResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
       <return>true</return>
    </ns2:deleteDedicatedAccountResponse>
	```

* **Notes:**

    * This method fails if the subscriber is not found or the dedicated account is not found for the subscriber.


### updateSubDedicatedAccounts
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### createDedicatedAccount
----
Create a dedicated account for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to create Dedicated Account for |
	| dedicatedAccountID | The ID of the dedicated account. This field is required. |
	| unitType | The type of the units stored in the account. Optional. Defaults to 0. |
	| value | The initial dedicated account value, both value1 and value2 are set to this value. Optional. |
	| startDate | The start date for the account. Optional. |
	| expiryDate | The expiry date for the account. Optional. |


* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the dedicated account is created successfully, `false` otherwise |

* **Sample Request**

    ```
    <prot:createDedicatedAccount>
        <msisdn>830010003</msisdn>
        <dedicatedAccountID>101</dedicatedAccountID>
        <unitType>2</unitType>
        <value>4000</value>
        <startDate>2022-03-01</startDate>
        <expiryDate>2022-04-01</expiryDate>
    </prot:createDedicatedAccount>
	```

* **Sample Response**

    ```
    <ns2:createDedicatedAccountResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:createDedicatedAccountResponse>
	```

* **Notes:**

    * Attempt to create a dedicated account, which already exists, will result in a failure.


### getServiceOfferings
----
Retrieve all Service Offerings (PSO) for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to retrieve Service Offerings for |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | an array of Service Offerings, see the ServiceOffering section below for details on the individual fields |

    Each `return` element in the array is a `ServiceOffering` structure with the following properties

    | Field | Description |
	| ----- | ----------- |
	| serviceOfferingID | The service offering ID. |
	| serviceOfferingActiveFlag | The service offering active flag. |

* **Sample Request**

    ```
    <prot:getServiceOfferings>
        <msisdn>830010003</msisdn>
    </prot:getServiceOfferings>
	```

* **Sample Response**

    ```
    <ns2:getServiceOfferingsResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>
            <serviceOfferingActiveFlag>false</serviceOfferingActiveFlag>
            <serviceOfferingID>10</serviceOfferingID>
        </return>
        <return>
            <serviceOfferingActiveFlag>false</serviceOfferingActiveFlag>
            <serviceOfferingID>12</serviceOfferingID>
        </return>
    </ns2:getServiceOfferingsResponse>
	```

* **Notes:**

    * ...


### getServiceOffering
----
...
Retrieve a specific Service Offering (PSO) for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to retrieve Service Offerings for |
	| serviceOfferingID | The Service Offering to be retrieved |


* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | the Service Offering details. Refer to the `getServiceOfferings` API call for details on the `ServiceOffering` properties. |

* **Sample Request**

    ```
    <prot:getServiceOffering>
        <msisdn>830010003</msisdn>
        <serviceOfferingID>10</serviceOfferingID>
    </prot:getServiceOffering>
	```

* **Sample Response**

    ```
    <ns2:getServiceOfferingResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>
            <serviceOfferingActiveFlag>false</serviceOfferingActiveFlag>
            <serviceOfferingID>10</serviceOfferingID>
        </return>
    </ns2:getServiceOfferingResponse>
	```

* **Notes:**

    * ...


### hasServiceOffering
----
Check if a specific Service Offering (PSO) is provisioned for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to retrieve Service Offerings for |
	| serviceOfferingID | The Service Offering to be retrieved |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the subscriber has the Service Offering, `false` otherwise |

* **Sample Request**

    ```
    <prot:hasServiceOffering>
        <msisdn>830010003</msisdn>
        <serviceOfferingID>11</serviceOfferingID>
    </prot:hasServiceOffering>
	```

* **Sample Response**

    ```
    <ns2:hasServiceOfferingResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:hasServiceOfferingResponse>
	```

* **Notes:**

    * ...


### updateServiceOffering
----
Create or update Service Offering (PSO) for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to create / update Service Offerings for |
	| serviceOffering | The Service Offering details |


* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the Service Offering has been created or updated successfully, `false` otherwise |

* **Sample Request**

    ```
    <prot:updateServiceOffering>
        <msisdn>830010003</msisdn>
        <serviceOffering>
            <serviceOfferingID>10</serviceOfferingID>
            <serviceOfferingActiveFlag>true</serviceOfferingActiveFlag>
        </serviceOffering>
    </prot:updateServiceOffering>

	```

* **Sample Response**

    ```
    <ns2:updateServiceOfferingResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:updateServiceOfferingResponse>
	```

* **Notes:**

    * ...


### deleteServiceOffering
----
Delete a Service Offering (PSO) for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to delete Service Offerings for |
	| serviceOfferingID | The ID of the Service Offering to be deleted |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the Service Offering was deleted successfully, `false` otherwise |

* **Sample Request**

    ```
    <prot:deleteServiceOffering>
        <msisdn>830010003</msisdn>
        <serviceOfferingID>1</serviceOfferingID>
    </prot:deleteServiceOffering>
	```

* **Sample Response**

    ```
    <ns2:deleteServiceOfferingResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:deleteServiceOfferingResponse>
	```

* **Notes:**

    * Typical reasons for this call to return `false` are subscriber not found or Service Offering not provisioned for the subscriber.


### getUsageCounters
----
Retrieve all Usage Counters (UC) for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to retrieve Usage Counters for |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | an array of Usage Counters, see the `UsageCounter` section below for details on the individual fields |

    Each `return` element in the array is a `UsageCounter` structure with the following properties

    | Field | Description |
	| ----- | ----------- |
	| usageCounterID | The usage counter ID. |
	| usageCounterValue | The usage counter current value. |
	| usageCounterMonetaryValue1 | |
	| usageCounterMonetaryValue2 | |
	| associatedPartyID | |
	| productID | |

* **Sample Request**

    ```
    <prot:getUsageCounters>
        <msisdn>830010003</msisdn>
    </prot:getUsageCounters>
	```

* **Sample Response**

    ```
    <ns2:getUsageCountersResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>
            <usageCounterID>20</usageCounterID>
            <usageCounterValue>5000</usageCounterValue>
        </return>
        <return>
            <usageCounterID>24</usageCounterID>
            <usageCounterValue>7500</usageCounterValue>
        </return>
    </ns2:getUsageCountersResponse>
	```

* **Notes:**

    * ...


### getUsageCounter
----
Retrieve a specific Usage Counterg (UC) for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to retrieve a Usage Counter for |
	| UsageCounterID | The ID of the Usage Counter to be retrieved |


* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | the Usage Counter details. Refer to the `getUsageCounters` API call for details on the `UsageCounter` properties. |

* **Sample Request**

    ```
    <prot:getUsageCounter>
        <msisdn>830010003</msisdn>
        <UsageCounterID>20</UsageCounterID>
    </prot:getUsageCounter>
	```

* **Sample Response**

    ```
    <ns2:getUsageCounterResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>
            <usageCounterID>20</usageCounterID>
            <usageCounterValue>5000</usageCounterValue>
        </return>
    </ns2:getUsageCounterResponse>
	```

* **Notes:**

    * Returns blank response if the subscriber or Usage Counter is not found.


### hasUsageCounter
----
Check if a specific Usage Counter (UC) is provisioned for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to check Usage Counter for |
	| UsageCounterID | The ID of the Usage Counter to be checked |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the subscriber has the Usage Counter, `false` otherwise |

* **Sample Request**

    ```
    <prot:hasUsageCounter>
        <msisdn>830010003</msisdn>
        <UsageCounterID>20</UsageCounterID>
    </prot:hasUsageCounter>
	```

* **Sample Response**

    ```
    <ns2:hasUsageCounterResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:hasUsageCounterResponse>
	```

* **Notes:**

    * Also returns `false` if the subscriber is not found.


### updateUsageCounter
----
Create or update Usage Counter for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to create / update Usage Counter for |
	| UsageCounter | The Usage Counter details. See `getUsageCounters` for detailsi on the available properties. |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the Usage Counter has been created or updated successfully, `false` otherwise |

* **Sample Request**

    ```
    <prot:updateUsageCounter>
        <msisdn>830010003</msisdn>
        <UsageCounter>
            <usageCounterID>20</usageCounterID>
            <usageCounterValue>5000</usageCounterValue>
        </UsageCounter>
    </prot:updateUsageCounter>
	```

* **Sample Response**

    ```
    <ns2:updateUsageCounterResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:updateUsageCounterResponse>
	```

* **Notes:**

    * If the Usage Counter already exists, it is updated by replacing all its properties with the new ones or defaults.


### deleteUsageCounter
----
Delete a Usage Counter (UC) for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to delete a Usage Counter for |
	| UsageCounterID | The ID of the Usage Counter to be deleted |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the Usage Counter has been deleted successfully, `false` otherwise |

* **Sample Request**

    ```
    <prot:deleteUsageCounter>
        <msisdn>830010003</msisdn>
        <UsageCounterID>24</UsageCounterID>
    </prot:deleteUsageCounter>
	```

* **Sample Response**

    ```
    <ns2:deleteUsageCounterResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:deleteUsageCounterResponse>
	```

* **Notes:**

    * ...


### getUsageThresholds
----
Retrieve all Usage Thresholds (UTs) for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to retrieve Usage Thresholds for |


* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | an array of Usage Thresholds, see the `UsageThreshold` section below for details on the individual fields |

    Each `return` element in the array is a `UsageThreshold` structure with the following properties

    | Field | Description |
	| ----- | ----------- |
	| usageThresholdID | The usage threshold ID. |
	| usageThresholdValue | The usage threshold current value. |
	| usageThresholdMonetaryValue1 | |
	| usageThresholdMonetaryValue2 | |
	| usageThresholdSource | 1 = Personal, 2 = Common, 3 = Default |
	| associatedPartyID | |


* **Sample Request**

    ```
    <prot:getUsageThresholds>
        <msisdn>830010003</msisdn>
    </prot:getUsageThresholds>
	```

* **Sample Response**

    ```
    <ns2:getUsageThresholdsResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>
            <usageThresholdID>10</usageThresholdID>
            <usageThresholdSource>3</usageThresholdSource>
            <usageThresholdValue>7500</usageThresholdValue>
        </return>
        <return>
            <usageThresholdID>11</usageThresholdID>
            <usageThresholdSource>3</usageThresholdSource>
            <usageThresholdValue>12000</usageThresholdValue>
        </return>
        <return>
            <usageThresholdID>12</usageThresholdID>
            <usageThresholdSource>3</usageThresholdSource>
            <usageThresholdValue>15000</usageThresholdValue>
        </return>
    </ns2:getUsageThresholdsResponse>
	```

* **Notes:**

    * ...


### getUsageThreshold
----
Retrieve a specific Usage Threshold (UT) for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to retrieve a Usage Threshold for |
	| UsageThresholdID | The ID of the Usage Threshold to be retrieved |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | the Usage Threshold details. Refer to the `getUsageThresholds` API call for details on the `UsageThreshold` properties. |

* **Sample Request**

    ```
    <prot:getUsageThreshold>
        <msisdn>830010003</msisdn>
        <UsageThresholdID>11</UsageThresholdID>
    </prot:getUsageThreshold>
	```

* **Sample Response**

    ```
    <ns2:getUsageThresholdResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>
            <usageThresholdID>11</usageThresholdID>
            <usageThresholdSource>3</usageThresholdSource>
            <usageThresholdValue>12000</usageThresholdValue>
        </return>
    </ns2:getUsageThresholdResponse>
	```

* **Notes:**

    * ...


### hasUsageThreshold
----
Check if a specific Usage Threshold (UT) is provisioned for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to check Usage Threshold for |
	| UsageCounterID | The ID of the Usage Threshold to be checked |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the subscriber has the Usage Threshold, `false` otherwise |

* **Sample Request**

    ```
    <prot:hasUsageThreshold>
        <msisdn>830010003</msisdn>
        <UsageThresholdID>11</UsageThresholdID>
    </prot:hasUsageThreshold>
	```

* **Sample Response**

    ```
    <ns2:hasUsageThresholdResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:hasUsageThresholdResponse>
	```

* **Notes:**

    * ...


### updateUsageThreshold
----
Create or update Usage Threshold for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to create / update Usage Threshold for |
	| UsageThreshold | The Usage Threshold details. See `getUsageThresholds` for details on the available properties. |


* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the Usage Threshold has been created or updated successfully, `false` otherwise |

* **Sample Request**

    ```
    <prot:updateUsageThreshold>
        <msisdn>830010003</msisdn>
        <UsageThreshold>
            <usageThresholdID>10</usageThresholdID>
            <usageThresholdValue>7500</usageThresholdValue>
        </UsageThreshold>
    </prot:updateUsageThreshold>
	```

* **Sample Response**

    ```
    <ns2:updateUsageThresholdResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:updateUsageThresholdResponse>
	```

* **Notes:**

    * If the Usage Threshold already exists, it is updated by replacing all its properties with the new ones or defaults.


### deleteUsageThreshold
----
Delete a Usage Threshold (UT) for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to delete a Usage Threshold for |
	| UsageThresholdID | The ID of the Usage Threshold to be deleted |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the Usage Threshold has been deleted successfully, `false` otherwise |

* **Sample Request**

    ```
    <prot:deleteUsageThreshold>
        <msisdn>830010003</msisdn>
        <UsageThresholdID>11</UsageThresholdID>
    </prot:deleteUsageThreshold>
	```

* **Sample Response**

    ```
    <ns2:deleteUsageThresholdResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:deleteUsageThresholdResponse>
	```

* **Notes:**

    * ...


### getAccumulators
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
    <prot:getAccumulators>
        <msisdn>830010003</msisdn>
    </prot:getAccumulators>
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getAccumulator
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### hasAccumulator
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### updateAccumulator
----
Create or update Accumulator for a subscriber.

* **Parameters**

    | Field | Description |
	| ----- | ----------- |
	| msisdn | The MSISDN of the subscriber to create / update Accumulator for |
	| Accumulator | The Accumulator details. See `getAccumulators` for details on the available properties. |

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	| return | `true` if the Accumulator has been created or updated successfully, `false` otherwise |

* **Sample CURL Request**

    ```
	echo '<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/" xmlns:prot="http://protocol.airsim.services.hxc/"><S:Header/><S:Body><prot:updateAccumulator><msisdn>830010003</msisdn><Accumulator><accumulatorID>30</accumulatorID><accumulatorValue>3000</accumulatorValue></Accumulator></prot:updateAccumulator></S:Body></S:Envelope>' | xmllint --format - | tee /dev/stderr | curl -H "Content-Type: text/xml" --trace-ascii /dev/stderr  --data @/dev/stdin 'http://localhost:10012/Air'
    ```

* **Sample Request**

    ```
    <prot:updateAccumulator>
        <msisdn>830010003</msisdn>
        <Accumulator>
            <accumulatorID>30</accumulatorID>
            <accumulatorValue>3000</accumulatorValue>
        </Accumulator>
    </prot:updateAccumulator>
	```

* **Sample Response**

    ```
    <ns2:updateAccumulatorResponse xmlns:ns2="http://protocol.airsim.services.hxc/">
        <return>true</return>
    </ns2:updateAccumulatorResponse>
	```



* **Notes:**

    * ...


### deleteAccumulator
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getFafEntries
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### deleteFafEntry
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### deleteFafEntries
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### updateFafEntry
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### addFafEntry
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### updateCommunityIDs
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### updatePAM
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### deletePAM
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### setHlrData
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### addTnpThreshold
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### produceDedicatedAccountFile
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### produceSubscriberFile
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getCallHistory
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### clearCallHistory
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### restoreState
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### saveState
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### injectResponse
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### injectSelectiveResponse
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### resetInjectedResponse
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### startUsageTimers
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### stopUsageTimers
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getUsageTimers
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getNextMSISDN
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### isCloseTo
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### parseTime
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### setup
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getSmsHistory
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### clearSmsHistory
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### restoreBackup
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### injectMOSms
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### injectMOUssd
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getUssdMenuLine
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getLastCdr
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getCdr
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getCdrHistory
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### clearCdrHistory
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getLastAlarm
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### getAlarmHistory
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### optionalCommand
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### nonQuery
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


### tearDown
----
...

* **Parameters**

  (none)

* **Response Fields**
  
    | Field | Description |
	| ----- | ----------- |
	|  |  |

* **Sample Request**

    ```
	```

* **Sample Response**

    ```
	```

* **Notes:**

    * ...


