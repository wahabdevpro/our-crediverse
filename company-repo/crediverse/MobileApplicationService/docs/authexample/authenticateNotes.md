# Athentication Process

Crediverse ecds-ts uses a stateful multi step authentication process. 

@startuml

participant CrediverseMobileApp as CMA
participant MobileApplicationService as MAS

CMA -> MAS: /ecds/authentication/authenticate
MAS --> CMA: REQUIRE_UTF8_USERNAME and a sessionID

CMA -> MAS: /ecds/authentication/authenticate | sessionID and MSISDN
MAS --> CMA : REQUIRE_RSA_PIN and RSA encryption key 

CMA -> MAS: /ecds/authentication/authenticate | sessiondID and encripted 
MAS --> CMA : REQUIRE_UTF8_OTP 

CMA -> MAS: /ecds/authentication/authenticate | sessionID and base64 encripted OTP 
MAS --> CMA : AUTHENTICATED and token?

@enduml

## 1. Get session id

### Make service call to ecds-ts
make a HTTP POST to `/ecds/authentication/authenticate`
with request body.
```
{
  "sessionID": null,
  "inboundTransactionID": null,
  "inboundSessionID": null,
  "version": "1",
  "mode": "N",
  "companyID": 2,
  "channel": "W",
  "hostName": null,
  "macAddress": null,
  "ipAddress": "0:0:0:0:0:0:0:1",
  "data": null,
  "username": null,
  "password": null,
  "oneTimePin": null,
  "userType": "AGENT",
  "coSignForSessionID": null,
  "coSignatoryTransactionID": null,
  "customPinChangeMessage": null
}
```

### service returns: 
```
{
  "transactionNumber": null,
  "inboundTransactionID": null,
  "inboundSessionID": null,
  "returnCode": "REQUIRE_UTF8_USERNAME",
  "additionalInformation": null,
  "sessionID": "1c23206926eb4aff8bb14abb2c908cd4",
  "moreInformationRequired": true,
  "key1": null,
  "key2": null,
  "value": null
}
```
The `sessionID` returned is associated with session state kept server side and therefore should be added as `sessionID` to all subsequent requests to the server.


## Step 2 Use session id and MSISDN to get key

### Make service call to ecds-ts

Make a HTTP POST to `/ecds/authentication/authenticate`
with request body:
```
{
  "sessionID": "1c23206926eb4aff8bb14abb2c908cd4",
  "inboundTransactionID": null,
  "inboundSessionID": null,
  "version": "1",
  "mode": "N",
  "companyID": 2,
  "channel": "W",
  "hostName": null,
  "macAddress": null,
  "ipAddress": "0:0:0:0:0:0:0:1",
  "data": "MDgyMDAwMDAxNQ==", 
  "username": null,
  "password": null,
  "oneTimePin": null,
  "userType": "AGENT",
  "coSignForSessionID": null,
  "coSignatoryTransactionID": null,
  "customPinChangeMessage": null
}
```

With the sessionID from *Step 1* and the `data` field set to the base 64 representation of the MSISDN logged in with.

### service returns: 
```
{
  "transactionNumber": null,
  "inboundTransactionID": null,
  "inboundSessionID": null,
  "returnCode": "REQUIRE_RSA_PIN",
  "additionalInformation": null,
  "sessionID": "1c23206926eb4aff8bb14abb2c908cd4",
  "moreInformationRequired": true,
  "key1": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQClPXKQNVysqDrMiZlOtzeTDPbbnmEmLDW/p8k7z1VfTtaVm3mGuIFzctKZye+C8eKhuOyOPY5TesqcfoQ4NRvJvkt83mMF39bkkLH2/deVQdLWk6WhhNYkGLzU7q01ZpdPIVMTurf6NiPr1hXD0FJJ+sPWGaZqBIGXhyyzX87t8QIDAQAB",
  "key2": null,
  "value": null
}
```

The `key1` field is the key returned from ecds-ts.

## Step 3 Use the key to encrypt the pin and submit it to ecds-ts

### Make service call to ecds-ts
Make a HTTP POST to `/ecds/authentication/authenticate`
with request body:
```
{
  "sessionID": "1c23206926eb4aff8bb14abb2c908cd4",
  "inboundTransactionID": null,
  "inboundSessionID": null,
  "version": "1",
  "mode": "N",
  "companyID": 2,
  "channel": "W",
  "hostName": null,
  "macAddress": null,
  "ipAddress": "0:0:0:0:0:0:0:1",
  "data": "ofK59p1EHbklCNlZsUo5DBbdw9L3dmNqiM+MLw1FgeW/hdW9pJQVB5SNbTRVqfsetxeE2OP2bcLYqioW6oUX30jpAy/AClokARYCePoqw3Zr47TwDrGbGeQKWqWNRgrw3FpnVM42Fqa2lxi/NTv0roP9jRiEgpfb4bX7HP6rANQ=",
  "username": null,
  "password": null,
  "oneTimePin": null,
  "userType": "AGENT",
  "coSignForSessionID": null,
  "coSignatoryTransactionID": null,
  "customPinChangeMessage": null
}
```

The `data` field is the pin, of the user associated with the session, encrypted with the key recieved from the server in *Step 2*

where `cid` is the companyID and `data` is the pin.

### service returns: 
```
{
  "returnCode": "REQUIRE_UTF8_OTP",
  "sessionID": "c54dbe4a18234c31a33287e731f8add7",
  "moreInformationRequired": true
}
```
to indicate that an OTP is required.

## Step 4 

### Make service call to ecds-ts
Make a HTTP POST to `/ecds/authentication/authenticate`
with request body:
```
{
  "sessionID": "c54dbe4a18234c31a33287e731f8add7",
  "version": "1",
  "mode": "N",
  "companyID": 2,
  "channel": "W",
  "ipAddress": "0:0:0:0:0:0:0:1",
  "data": "MjgzNDU=",
  "userType": "AGENT"
}
```
where `data` is set to the base64 representation of the OTP to submit. 

### service returns: 
```
{
  "returnCode": "AUTHENTICATED",
  "sessionID": "c54dbe4a18234c31a33287e731f8add7",
  "moreInformationRequired": false,
  "key1": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCvVpLRHI1lpzXcXendhqDOJz2lzYAxoGNwQaWyBcBVLeELBzo+QuCO1BQF2N0mrbDEoyes+tO6I1oyaNgvJQUjTSiN6oNacdctdYha0ysCywRaHqXRYnWXVPgZKVVKiIz9J/IHqe0pJAgnpmWrFCCCiwKk4WnB7LF48p6wddxCTQIDAQAB"
}

```

