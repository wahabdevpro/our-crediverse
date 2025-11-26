
# Crediverse-1.17.0 Release Notes

#draft 
#pro-forma

### 🌟 New Features

For the Android SmartApp
- The minium supported android version is now 8.0 (from 8.1)
- A Version Check is made with MAS, the App will offer an update if possible, or prevent login if versions are incompatible
- The App now includes Analytics. It will report to the MAS all actions, exceptions, and crashes that occur
- Added mobile money deposit with login Proof Of Concept (disabled in production)

### ✨Enhancements

For the Android SmartApp
- Page navigation has been significantly improved both visually and internally
- Separated 'Airtime' and 'Transfer' (wholesale) into dedicated pages
- Team Dashboard has been added to get an overview of your team if you are a team lead, previously the same information was found in several places

For Crediverse
- Adjustments to timeouts have been made in require.js, with values changing from 300 to 30, and from 7 to 30.  
- Changes related to Cost of Goods Sold (COGS) have been implemented, with the base trade bonus now dependent on the most recent transfer.
- Software version and build number now shows in the admin and agent portal, as well as in the product logs, on startup.

### 🐛 Bug Fixes

For the Android SmartApp
- Team Lead information on home page will no longer be shown for non-team leads
- Multiple incorrect 'state' issues resolved to prevent intermitent App crashes

For Crediverse
- A follow-up indicator is included in transaction responses from Crediverse.
- The removal of an agent no longer involves searching for transactions with the agent's MSISDN as the B-party or the requester's MSISDN for the prerequisites.
- Crediverse now produces passwords that adhere to the password rules.
- Web User export in the admin portal no longer exports duplicate entries.
- Table schema for ec_transact_ex has been updated, the ec_transact foreign key set to CASCADES on update/delete, so that OLTP cleanup process works without complaining about FK constraints.

### 🔧 Internal
- Changes in MAS and Team Service to reduce the image size

### ⚠️ Advisory
- The ec_transact_ex table schema change is done in such a way that it will be effective only for new site installation and existing sites running DB version 82 or 
  earlier (before the ec_transact_ex was introduced).  
  For existing sites running with DB version 83 or later, the change must be applied manually with consideration of the size of the ec_transact_ex table size (number of records).  
  The ALTER TABLE operation may cause a lock up of the database for a period of time, so it should be done during a maintenance window, unless the table is very small, say < 1M records.  
  Here are the relevant ALTER TABLE statements for manual update:

  ```
  ALTER TABLE ec_transact_ex
  DROP FOREIGN KEY FK_Transcation;
  
  ALTER TABLE ec_transact_ex
  ADD CONSTRAINT FK_Transcation
  FOREIGN KEY (transaction_id) 
  REFERENCES ec_transact(id) ON DELETE CASCADE ON UPDATE CASCADE;
  ```

