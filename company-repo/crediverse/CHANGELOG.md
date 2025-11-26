## [UNRELEASED] - 2022-13-31

### Database Changes
- Upgrade OLTP DB to version 86 (via upgrade_ecds_oltp_85_to_86.sql) - Modifies ea_agent.es_agent_msisdn index
- Upgrade OLAP DB to version 15 (via upgrade_ecds_olap_14_to_15.sql) - Modifies ap_agent_account.ap_agent_account_msisdn index

### Functional Changes
#### [Add Agents UC](https://app.clickup.com/4631982/v/l/f/103380782?pr=19202742)
- Added new menu item and new Agent Account Management Screen in GUI
- Adjusted TS to provide functionality required Add Agent Calls

#### [Deactivate Agent UC](https://app.clickup.com/4631982/v/l/4dbde-13684)
- Added Deactivate Agent dialog to GUI on Agent Account Management Screen
- Adjusted TS to provide functionality required Deactivate Agent calls

## [1.11.9-rc-11] - 2021-11-30
### GUI v. 1.11.9-rc-11, API v. 1.11.9-rc-11

### Changed
- (ClickUp [1rwzg11](https://app.clickup.com/t/1rwzg11)) and (ClickUp [1vc98dk](https://app.clickup.com/t/1vc98dk)) Upgrade to Java 11.
- (ClickUp [1v1h1h2](https://app.clickup.com/t/1v1h1h2)) Fix mysqldump to use the DB host from the configuration.
- (ClickUp [1k4xyhq](https://app.clickup.com/t/1k4xyhq)) Initial DB data - Add database role to the ct_role table.

## [1.11.9-rc-8] - 2021-11-12
### GUI v. 1.11.9-rc-8, API v. 1.11.9-rc-8

### Added
- (ClickUp [p78dbu](https://app.clickup.com/t/p78dbu)) Implement 'request is stale' for non-airtime transactions.
- (ClickUp [1k4xyhq](https://app.clickup.com/t/1k4xyhq)) Add SQL files initializing the database.
- (ClickUp [ranzdf](https://app.clickup.com/t/ranzdf)) Enhance logging by calculating execution times for non-airtime transactions and IMEI/IMSI lookup.
- (ClickUp [1nn0fcd](https://app.clickup.com/t/1nn0fcd)) Make Garbage Collection (GC) tuning permanent.
- (ClickUp [pbeu3g](https://app.clickup.com/t/pbeu3g)) Start using IMSI provided in the Debit and Refund requests.
- (ClickUp [1pbz39j](https://app.clickup.com/t/1pbz39j)) Add non-airtime refund by debitClientTransactionId.
- (ClickUp [1q2trxe](https://app.clickup.com/t/1q2trxe)) Implement Transaction Status endpoint for Non-airtime.
- (ClickUp [1ug83hp](https://app.clickup.com/t/1ug83hp)) Add mysql-client to the Docker image.

### Changed
- (ClickUp [#chput6](https://app.clickup.com/t/#chput6)) Fixed by correcting value for hold_account_balance in csv dump.
- (ClickUp [1p626cw](https://app.clickup.com/t/1p626cw)) On transaction, choose rule with specific groups over such without groups.
- (ClickUp [pdb58y](https://app.clickup.com/t/pdb58y)) Restart Grizzly server on backlog size change.
- (ClickUp [1k4xyhq](https://app.clickup.com/t/1k4xyhq)) Change hibernate.hbm2ddl.auto from 'update' to 'validate'. For current sites it will remain 'update'. For new sites it will be 'none'.
- (ClickUp [1n9h8zd](https://app.clickup.com/t/1n9h8zd)) Return useful message to the GUI on "Ambiguous Transfer Rule".
- (ClickUp [1tzbprh](https://app.clickup.com/t/1tzbprh)) Fix a problem while saving the reports in Agent Portal.
- (ClickUp [1nn0h68](https://app.clickup.com/t/1nn0h68)) Fix wrong cumulative bonus in "All rules" view.
- (ClickUp [e561tk](https://app.clickup.com/t/e561tk)) and (ClickUp [1tz4tru](https://app.clickup.com/t/1tz4tru)) GUI ADMIN and Portal improvements:
  1. Removed title "Agent Accounts" from the breadcrumb.
  2. Added icons with breadcrumbs where needed.
  3. Changed Tier View i.e Moved tier details on the top. Incoming, outgoing rules are moved in a tabbed view.
  4. Added "Cancel" Button for new report.
  5. Added read-only view for reports.
  6. Changed Titles to Float on the top when a page is scrolled.
  7. Changed Wrong Error Messages in delete dialogues.

## [1.11.8-rc-5] - 2021-09-14
### GUI v. 1.11.8-rc-5, API v. 1.11.8-rc-5

### Added
- (ClickUp [pdb58y](https://app.clickup.com/t/pdb58y)) Make Grizzly server settings configurable in the C4U.
- (ClickUp [7x4u7a](https://app.clickup.com/t/7x4u7a)) Create batch adjudication script.
 
### Changed
- (ClickUp [11m8pkb](https://app.clickup.com/t/11m8pkb)) Include permanent agents in the account dump
- (ClickUp [p5ctk1](https://app.clickup.com/t/p5ctk1)) Improve fix of "Error while committing".
- Changes in the build process.
- Fix bug in the API related to incorrectly moved classes during changing of the build process.

## [1.11.7-rc-10] - 2021-08-24
### GUI v. 1.18.8-rc-5, API v. 1.2.10-rc-0
### Added
- (ClickUp [48rwm9](https://app.clickup.com/t/48rwm9)) Allow sell to self and make it configurable.
 
### Changed
- (ClickUp [1ck81qc](https://app.clickup.com/t/1ck81qc)) Fix problem with limitation of digits after decimal point in trade bonus percentage.
- (ClickUp [5cv55r](https://app.clickup.com/t/5cv55r)) Make default maxConnPerRoute 100 for the HTTP client in the API.
- (ClickUp [p5ctk1](https://app.clickup.com/t/p5ctk1)) Fix "Error while committing".

### Removed
- (ClickUp [41haan](https://app.clickup.com/t/41haan)) Remove invalid tampering warnings.

## [1.11.6] - 2021-06-04, 2021-06-16
### GUI v. 1.18.7, API v. 1.2.8
### Added
- (ClickUp [khd6eg](https://app.clickup.com/t/khd6eg)) Periodically print metrics in the API log.

### Changed
- (ClickUp [jv9y0b](https://app.clickup.com/t/jv9y0b)) SMPP Connector error handling improvement.
- (ClickUp [m55e96](https://app.clickup.com/t/m55e96)) Fix trade bonus digits after decimal point bug.
- (ClickUp [3k9u7k](https://app.clickup.com/t/3k9u7k)) Fix validation of Activity cutoff field in Account Balance Summary Report.

### Removed
- (ClickUp [jv9y0b](https://app.clickup.com/t/jv9y0b)) Removed reinitialization of SMPP configuration when all sessions are unfit in isFit().

## [1.11.5] - 2021-05-21
### GUI v. 1.18.6, API v. 1.2.7
### Added
- (ClickUp [jv9y0b](https://app.clickup.com/t/jv9y0b)) Reinitialize SMPP configuration when all sessions are unfit in isFit().
- (ClickUp [jn88uu](https://app.clickup.com/t/jn88uu)) Create script for data migration between two tables.
- (ClickUp [j9a348](https://app.clickup.com/t/j9a348)) Write test for Sales Summary report.

### Changed
- (ClickUp [33de9z](https://app.clickup.com/t/33de9z)) Exclude Non-airtime refunds from SalesSummaryReport. Optimize the changed query.
- (ClickUp [hz358u](https://app.clickup.com/t/hz358u)) Fix minor visual problem with date field in Sales Summary Report.

## [1.11.4] - 2021-04-30
### GUI v. 1.18.5, API v. 1.2.7
### Added
- Add auxiliary MySQL scripts for inserting records in OLAP DB (for test purposes).

### Changed
- (ClickUp [h73u8u](https://app.clickup.com/t/h73u8u)) Fix NPE in "Daily Group Sales Report" when agent A has a group but agent B has not or vice versa.
- (ClickUp [gh2k7y](https://app.clickup.com/t/gh2k7y)) Enhance "Daily Group Sales Report".
- (ClickUp [hh3p0f](https://app.clickup.com/t/hh3p0f)) Fix wrong agents count and always show all groups in Daily Group report.
- (ClickUp [hn3e9t](https://app.clickup.com/t/hn3e9t)) Sales Summary Report optimization.
