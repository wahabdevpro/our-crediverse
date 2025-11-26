# GPS Coordinates for Agent Bundle Sales via SmartApp - Interface Changes
## Introduction
A Crediverse agent can sell a bundle to subscribers via SmartApp. The call flow for this transaction is shown below:
```
SmartApp -> SSAPI -> SmartShop -> Crediverse API -> Crediverse TS
```
The interfaces between the components given above are the following:
```
REST API between SmartApp and SSAPI.
eHuX between SSAPI and SmartShop.
REST API (Crediverse API) between SmartShop and Crediverse API.
REST API between Crediverse API and Crediverse TS
```
This document explains the changes in each of the interfaces required to store the location information of the agent in Crediverse TDRs and the OLTP database. The TDR and OLTP database changes are provided [here](https://github.com/Concurrent-Systems/Crediverse/blob/main/docs/design/agent-location-smartapp/agent-location-smartapp.md).

## Changes to Interfaces
### SSAPI REST API
The following endpoint in the SSAPI is used for provisioning a bundle via the POST method:
```
/bundles/{bundle-code}/{method}/{beneficiary}
```
A new **optional** parameter `geo_coordinates` will be added to the `requestBody` parameter of the POST method. The structure of this new parameter will be:
```
geo_coordinates: 
  type: object
  nullable: true
  properties:
	longitude: 
	  type: double 
	latitude: 
	  type:  double
```
The values for `longitude` and `latitude` will be accurate up to 8 decimal places. For details, check out the spec [here](https://github.com/Concurrent-Systems/SmartShop-API/blob/main/docs/smartshop-api-2.0-specification.yaml)
### eHuX
The eHuX request will now also have an additional **optional** parameter named `location`:
```
<name>location</name>
  <value>
    <struct>
      <member>
       <name>latitude</name>
       <value>
         <double>${latitude}</double>
       </value>
      </member>
      <member>
        <name>longitude</name>
        <value>
          <double>${longitude}</double>
        </value>
      </member> 
    </struct>
  </value>
</member>
```
### Crediverse API
The following endpoint is used on Crediverse API to perform debits from agent accounts via the POST method:
```
/api/service/agent/${Agent MSISDN}/debit
```
The JSON data associated with this call will now have two new **optional** parameters `latitude` and `longitude`. The values of these parameters will be string values. For instance, the following is an example of the data sent to the above endpoint:
```
{"clientTransactionId":"121","amount":1.5,"agentPin":"00000","itemDescription":"F 150 (1day)","consumerMsisdn":"711946302","latitude":"-85.12365122","longitude":"-1.11166667"}
```
The values of the `latitude` and `longitude` parameters, which are accurate up to 8 decimal points, are converted to string. The Crediverse API will simply pass on these values to Crediverse TS.
### Crediverse TS REST API
The following endpoint is used for performing debits from agent accounts: 
```
/ecds/service/agent/${Agent MSISDN}/debit
```
The JSON data associated with this endpoint will now have four **optional** parameters i.e. `latitude`, `longitude`, `gpsAccuracy` and `gpsAge`. The last two parameters are unused at the moment. The following is an example of the JSON data sent to the above endpoint:
```
{"sessionID":null,"inboundTransactionID":null,"inboundSessionID":null,"version":"1","mode":"N","msisdn":null,"clientTransactionId":"120","consumerMsisdn":"711946302","itemDescription":"F 150 (1day)","amount":1.5,"expiryTimeInMillisecondsSinceUnixEpoch":null,"imsi":null,"agentPin":"00000","latitude":-85.12365122,"longitude":-1.11166667,"gpsAccuracy":null,"gpsAge":null}
```
