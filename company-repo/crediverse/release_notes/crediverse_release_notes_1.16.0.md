
# Crediverse-1.16.0 Release Notes

#draft 
#pro-forma

### 🌟 New Features
An Android-native smartphone app for Crediverse retailer agents allows secure sales to subscribers and streamlined self-management.
- Secure log in with 2-factor authentication using MSISDN and PIN, and OTP sent via SMS
- View up to date available credit (account balance) and on-hold balance. For security purposes, the balances can also be hidden using a toggle
- Perform retail airtime sales to subscribers and purchase credit for own use (self top-up)
- Sell bundles directly to subscribers using the smartapp
- Individual agent performance summary displaying the value and number of sales transactions performed over the following periods:
	- Today and yesterday
	- This week and last week
	- This month and last month
- View agent profile details including agent name, tier name, account state, language and contact email address
- Update agent profile details including the agent name, contact email address, language
- Change PIN from the agent profile screen
- View transaction history for transactions performed by the agent from any sales channel
- Request confirmation for transactions 
- Allow agents to provide in-app feedback related to their smartapp use experience
- Factory settings support customisation of:
	- company logo and branding colours
	- start of week day 
	- currency (locale) settings
	- test and prod node

### ✨Enhancements
- Added a default tier trade bonus used in the calculation of the cost of goods sold (COGS) for airtime sales, bundle sales and self top-up transactions
- Store gross sales and cost of goods sold in the system and write to the Transaction Data Record (TDR) 
- Add support for specifying the origin channel when transactions originate from the API channel, identifying different origin node types in the TDR and the database
- When a transaction originates from the smartapp channel, the GPS location (longitude and latitude) of the transacting agent is written to the database and TDR for downstream processing

### 🐛 Bug Fixes
None

### 🔧 Internal

### ⚠️ Advisory

#### TDR CHANGES

Several unused fields now have a purpose:
* Field 39-41 were originally provisioned for the seller bonus feature, which was removed in an earlier version of Crediverse. Field 39 is now repurposed for the Gross Sales Amount, while field 40 is used for the Cost Of Goods Sold (COGS). Both fields are populated for Airtime Sell (SL), Bundle Sell (debit/ND & refund/NR), Selt Top-up (ST) and Transfer (TX) transactions. Field 41 remains unused.  
* Field 42 was originally intended for a field named Charge_Levied, but the the Charge feature was not implemented and the field is now repurposed for the `origin_channel` field. The field is populated only for API-channel (channel=A) transactions.  
* Field 58: a_gps - the field is set to the "latitude|longitude" GPS coordinates of the agent at the time of performing an Airtime Sell, Bundle Sell or Self Top-up transaction.  

The changes made to the TDRs are not expected to have any impact on the customer, unless they were relying on the affected fields to be blank or be limited to certain values.

Refer to the [Crediverse TDR Specification](https://github.com/Concurrent-Systems/Crediverse/blob/main/docs/Transaction%20Data%20Records%20\(TDR\)%20Specification.md) for an up to date field reference.

#### OLTP DB SCHEMA CHANGES

Due to changes to the `ec_transact` table schema, a table reconstruction is required.

```
-- 92 to 93: Removing seller bonus functionality and associated
ALTER TABLE ec_transact CHANGE buyer_trade_bonus_pct bonus_pct DECIMAL(20,8) NULL DEFAULT NULL;
ALTER TABLE ec_transact DROP COLUMN seller_trade_bonus_pct;

ALTER TABLE ec_transact CHANGE buyer_trade_bonus bonus  DECIMAL(20,4) NULL DEFAULT NULL;
ALTER TABLE ec_transact DROP COLUMN seller_trade_bonus;

ALTER TABLE ec_transact CHANGE buyer_trade_bonus_prov bonus_prov  DECIMAL(20,4) NULL DEFAULT NULL;
ALTER TABLE ec_transact DROP COLUMN seller_trade_bonus_prov;

ALTER TABLE et_rule CHANGE buyer_trade_bonus_pct bonus_pct  DECIMAL(20,8) NULL DEFAULT NULL;
ALTER TABLE et_rule DROP COLUMN seller_trade_bonus_pct;
ALTER TABLE ea_account DROP COLUMN on_hold_bonus_provision;

-- 93 to 94: Introduce location recording
CREATE TABLE `ec_transact_location` (
  `transaction_id` bigint(20) NOT NULL,
  `latitude` double(11,8) NOT NULL,
  `longitude` double(11,8) NOT NULL,
  KEY `FK_Location_Transaction` (`transaction_id`),
  CONSTRAINT `FK_Location_Transaction` FOREIGN KEY (`transaction_id`) REFERENCES `ec_transact` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 94 to 95: Recording gross sales amount and COGS for SL, ST, ND, NR and TX transactions
ALTER TABLE `ec_transact`
  ADD `gross_sales_amount` DECIMAL(20,4) NULL DEFAULT NULL AFTER `amount`,
  ADD `cost_of_goods_sold` DECIMAL(20,4) NULL DEFAULT NULL AFTER `gross_sales_amount`;

-- 95 to 96: Added Default Trade Bonus percentage
ALTER TABLE `et_tier` ADD `default_bonus_pct` DECIMAL(20,8) NULL DEFAULT NULL AFTER `allow_intratier_transfer`;
```

#### OLAP DB Schema Changes

Due to changes to the `ap_transact` table schema, a table reconstruction is required.

```
-- 17 to 18: Removing seller bonus functionality and associated
ALTER TABLE `ap_transact` CHANGE buyer_trade_bonus_pct bonus_pct  DECIMAL (20,8) ;
ALTER TABLE `ap_transact` CHANGE buyer_trade_bonus bonus DECIMAL (20,4) ;
ALTER TABLE `ap_transact` CHANGE buyer_trade_bonus_prov bonus_prov DECIMAL (20,4) ;

ALTER TABLE `ap_transact` DROP COLUMN seller_trade_bonus_pct;
ALTER TABLE `ap_transact` DROP COLUMN seller_trade_bonus;
ALTER TABLE `ap_transact` DROP COLUMN seller_trade_bonus_prov;

-- 18 to 19: Recording gross sales amount and COGS for SL, ST, ND, NR and TX transactions
ALTER TABLE `ap_transact`
  ADD `gross_sales_amount` DECIMAL(20,4) NULL DEFAULT NULL AFTER `amount`,
  ADD `cost_of_goods_sold` DECIMAL(20,4) NULL DEFAULT NULL AFTER `gross_sales_amount`;
```

#### DEFAULT TIER TRADE BONUS INITIALISATION

New Tier configuration parameter has been introduced, named **Default Trade Bonus** (`default_bonus_pct`). The field is used in the calculation of the Cost Of Goods Sold (COGS).

Notes:
* The field should be initialised to the Trade Bonus configured for the Transfer Rule(s) terminating at the tier, if there is only one such Transfer Rule or if there are multiple rules having the same Trade Bonus. In cases where there are multiple Transfer Rules terminating at the Tier and the Trade Bonus between the Transfer Rules differs, it is recommended to use the Trade Bonus that is applicable to the largest number of transactions or use an average Trade Bonus.
* Leaving the Default Trade Bonus blank will result in the Cost Of Goods Sold value not being populated (left as NULL) for any transactions where the target agent tier has no Default Trade Bonus configured.
* The Default Trade Bonus for a Tier should be updated when the Trade Bonus for any of the Transfer Rule terminating at the Tier changes.


#### CHANNEL TYPE CHANGES

In Crediverse, the `channel_type` field has been extended to accommodate additional values, representing the SmartApp and the USSD channels. Known values are:
* **MM** - Mobile Money (has been introduced earlier)
* **SA** - SmartApp (new)
* **US** - USSD (new) - it shall be noted that any SmartShop transactions coming without an explicit `channel` specified in the incoming HuX request, will be assigned by default the USSD (US) channel type.

Notes: 
* The `channel_type` is now recorded into the TDR, field #42.
* In the SSAPI and Crediverse API the field was added under the name `origin_channel` / `originChannel` respectively.  
* In the (e)HuX protocol the field is named 'channel`, it is an existing field, no changes were made to the protocol.  
* In the Crediverse TS the field remains with its original name of `channel_type`.  
* If an API client sends a **channel type** value longer than 2 characters, Crediverse TS will only take the first 2 characters from the incoming `channelType` value and record this as `channel_type`.  
* No validation is performed on the `origin_channel` / `channel_type` values.
* Using different `origin_channel` / `channel_type` values, for different origin channels, starting with the same two letters, will result in multiple origins sharing the same `channel_type` value, reducing the usefulness of the field.
* If SmartShop receives a transaction without `channel` parameter in the incoming eHuX request, `USSD` channel is assumed and SmartShop forwards `USSD` to Crediverse API / Crediverse TS, resulting in `channel_type` being set to `US` in the OLTP / OLAP DBs and in the TDRs.


#### JWT SECRET FOR MAS & SSAPI

MAS and SSAPI must share the same JWT Secret key for the JWT authentication to work.  
On the SSAPI, the JWT Secret is configured via the `SMARTSHOP_API_JWT_SECRET` environment variable.
On the MAS, the JWT Secret is configured via the `MAS_JWT_SECRET` environment variable.

#### NEW SERVICES

##### Mobile Application Service API (MAS) Deployment

The MAS service is released as a docker image and it is configured via environment variables.  
Refer to the [MAS Service README.md](https://github.com/Concurrent-Systems/Crediverse/blob/main/MobileApplicationService/README.md) for more information on the configuration and other aspects of the service.  
Use the sample [docker-compose.yml](https://github.com/Concurrent-Systems/Crediverse/blob/main/MobileApplicationService/docker-compose.yml) as a baseline, update the **environment** variables values and the host part of the **volume mappings** as appropriate for the target environment.
The MAS service connects to Crediverse TS via the Crediverse TS Internal API and to the OLTP database (replica can be used).  
MAS only reads from the OLTP database, it is advisable to configure it with a read-only (SELECT-only) database user, restricted to the MAS host.
The MAS service is only used by the SmartApp and has no impact on the overal operation of the system, beyond the SmartApp.  
The SmartApp cannot operate without MAS.

##### SmartShop API v2.0 (SSAPI) Deployment

The SSAPI service is released as a docker image and it is configured via environment variables, plus it needs access to the SmartShop configuration files (site-config.xml and pcc-config.xml).  
Changes to the SmartShop configuration files (via GUI or in another way), will not take effect in SSAPI unless the later is restarted, as the configuration files are only read by SSAPI on startup.
Refer to the [SSAPI Service README.md](https://github.com/Concurrent-Systems/SmartShop-API/blob/main/README.md) for more information on the configuration and other aspects of the service.  
Use the sample [docker-compose.yml](https://github.com/Concurrent-Systems/SmartShop-API/blob/main/docker-compose.yml) as a baseline, update the **environment** variables values and the host part of the **volume mappings** as appropriate for the target environment.
The SSAPI service connects to CoaleSCE, for bundle hierarchy retrieval, and to SmartShop for bundle provision, using eHuX for both.
The SSAPI service is currently only used by the SmartApp and has no impact on the overal operation of the system, beyond the SmartApp. 
The SmartApp cannot list of sell bundles without the SSAPI service.


#### NEW PROTOCOLS & PROTOCOL CHANGES

##### SmartShop API v2.0 (SSAPI)

The SSAPI protocol is an entirely new REST-based protocol.  
Refer to the [SmartShop API v2.0 Specification](https://github.com/Concurrent-Systems/SmartShop-API/blob/main/docs/smartshop-api-2.0-specification.yaml) for the formal protocol specification.  
The protocol is used for communication between the SmartApp and the SSAPI Service.
The authentication of the caller is done via JWT token and the authentication scope is an Agent.

##### Crediverse API

The Crediverse API has been updated as follows:

**DebitRequest (Non-airtime Debit)**

The following new fields have been added:  

* **latitude** - the latitude of the requester at the time of transaction, if known (optional).
* **longitude** - the longitude of the requester at the time of transaction, if known (optional).
* **gpsAccuracy** - for future use, not poulated.
* **gpsAge** - for future use, not populated.
* **grossSalesAmount** - the sales amount (retail value) of the product being charged for in this debit request.
* **originChannel** - the originating node type name, typically set by the node the transaction originated from (NB: for transactions coming through SmartShop, **USSD** is assumed, if the value was not set in the incoming request).

**RefundRequest (Non-airtime Refund)**

The following new fields have been added:  

* **originChannel** - the originating node type name, typically set by the node the transaction originated from (NB: for transactions coming through SmartShop, **USSD** is assumed, if the value was not set in the incoming request).


##### Crediverse TS Internal API

The Crediverse TS Internal API has been updated in the same way as the Crediverse API. All field names are the same. 


##### SmartShop API v2.0 -> Coalesce eHuX

This is an eHuX request with the following requirements:

**Request:**
* **USSDRequestString** must have a single parameter, the MSISDN B (beneficiary) bundle hierarchy to be returned for, e.g. `*831231234#`.

**Response:**
* **USSDResponseString** must contain the bundle hierarchy in JSON format. Example response:
  ```
  {
    "categories": [
      {
        "name": "Izy Hours plus",
        "code": "izy_hours_plus",
        "bundles": [
          "Mini_150F",
          "Mini_200F"
        ]
      }
    ],
    "bundles": [
      {
        "code": "Mini_150F",
        "name": "Mini (150F)",
        "ussd_code": "111",
        "option": "1"
      },
      {
        "code": "Mini_200F",
        "name": "Mini+ (200F)",
        "ussd_code": "111",
        "option": "2"
      }
    ]
  }
  ```

NB: It is extremely important that the bundles `ussd_code` and `option` correspond to the relevant bundle configuration in the pcc_config.xml given to the SSAPI service. The service constructs a view of the bundles merging information from the CoaleSCE response with information from the pcc_config.xml. Check the SSAPI Service logs for any warning relating to the mapping between the two sources.


#### TRANSACTION FLOW FOR SMARTAPP TRANSACTIONS

This is a short summary of the path SmartApp transactions take:  
(NB: this is not a call sequence, some legs may involve multiple API calls)

* Airtime sell (SL), Self Top-up (ST) and Transfer (TX)

  ```
  SmartApp 
    ----(MAS API)----> 
	  Mobile Application Service (MAS) 
	    ----(Crediverse TS Internal API)----> 
		  Crediverse
  ```

* Bundle Sale

  ```
  SmartApp 
    ----(SSAPI)----> 
	  SmartShop API Service 
	    ----(eHuX)----> 
		  SmartShop 
		    ----(Crediverse API)----> 
			  Crediverse API 
			    ----(Crediverse TS Internal API)----> 
				  Crediverse
  ```

* Bundle List
  ```
  SmartApp 
    ----(SSAPI)----> 
	  SmartShop API Service 
	    ----(eHuX)----> 
		  CoaleSCE
  ```

