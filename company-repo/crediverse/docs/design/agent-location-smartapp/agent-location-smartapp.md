# Save Agent Location information in Database
## 1. Introduction
This document outlines the modifications needed in the `ecds-ts` and `database` schema to store location information and enable adding it to TDRs. The SmartApp will retrieve GPS location data, which Crediverse should be able to store.

### 2. ECDS-TS Changes
### 2.1 Save Location Information
In order to save the agent's location data during transactions via SmartApp, some changes need to be made to the existing Requests in `ecds-ts`. Specifically, two new optional parameters - `Longitude` and `Latitude` - will be added to the `SellRequest` and `TransferRequest` Classes. The location information is saved only when these parameters (Longitude, Latitude) are not empty.

### 2.2 Rename Column in TDR
There is an existing column `b_cgi` in TDR, that will be renamed to `a_gps` and the lat-long values will be saved as pipe `|` separated e.g (22.9068|-43.1729)

### 3. Database Changes
We will add a new table called `ec_transact_location` to the OLTP Database, which will store the location information of the agent. This table will contain four fields:

1. `id` - int(11) NOT NULL AUTO_INCREMENT 
2. `latitude` - decimal(11,8) NOT NULL
3. `longitude` - decimal(11,8) NOT NULL
4. `transaction_id` - int(11) NOT NULL

The `id` field is the primary key of the table, while the `transaction_id` field is a foreign key that links this table to the `ec_transact` table.

