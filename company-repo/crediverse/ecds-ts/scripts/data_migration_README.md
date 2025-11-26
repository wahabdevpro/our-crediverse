## Description
`data_migration.py` is a Python 3 script which transfers rows from one 
MySQL database table 
to another, providing different options to control the process and 
option to resume in case of termination. The execution can be 
terminated by pressing Ctrl+C and after that resumed by passing 
command line argument `--resume`.

## Command line arguments
`--help` - Prints available arguments and their description.

`--db-host` - default="localhost" - DB host name.

`--db-port` - default=3306 - DB port.

`--db-user` - **required** - DB user name.

`--db-password` - **required** - DB user password.

`--db-database` - **required** - DB database name.

`--src-table` - **required** - Source table.

`--dst-table` - **required** - Destination table.

`--number-of-records` - default=0 - How many records to migrate. 0 means all of them.

`--batch-size` - default=100 - Batch size (how many records to proceed at once).

`--insert-batch-size` - default=10 - How many records to contain every 
INSERT statement. Not applicable when --multiple-selects option is used.

`--offset` - default=0 - Skip first N records.

`--ordering-column` - On which column to order the records. Default is PRIMARY KEY.

`--order-direction` - default='ASC' - Order ASC or DESC.

`--resume` - Detect last inserted record based on ordering column and direction and continue from there. Can be used only with ordering by column with unique values, e.g. primary key. When this flag is passed, `--start-from` argument is discarded.

`--start-from` - ID or date (yyyy-mm-dd_hh:mm:ss) from which to start 
(excluding). This adds: WHERE [ordering-column] > [start-from] or WHERE 
[ordering-column] < [start-from] depending on `--order-direction`.

`--ignore-duplicates` - Silently ignore (skip) duplicate entries.

`--sleep` - default=0 - How much time to sleep between batches in milliseconds.

`--table-creation-sql-file` - File containing table creation statement.

`--multiple-selects` - Use select per batch instead of single unbuffered select.

`--column-mapping` - Mapping between a column name in the source table and destination table in case a column name has been changed. One entry per column is needed. Format: [--column-mapping src_table_column1 dst_table_column1 --column-mapping src_table_column2 dst_table_column2 ...]

`--debug` - Print each insert statement.

## Examples
**Command:**

`python3 data_migration.py --db-user root --db-password 123 --db-database hxc --src-table ec_transact --dst-table ec_transact_new --sleep 100 --order-direction DESC`

**Output:**
```
Checking preconditions...
Problem with destination table hxc.ec_transact_new. (1146, "Table 'hxc.ec_transact_new' doesn't exist")
Exiting...
Closing DB connection...
Done.
```
---
**Command:**

`python3 data_migration.py --db-user root --db-password 123 
--db-database hxc --src-table ec_transact --dst-table ec_transact_new 
--sleep 100 --order-direction DESC --table-creation-sql-file create_ec_transact_new.sql`

**Output:**
```
New table created.
Checking preconditions...
Checking preconditions done.
Migrating 42627 records from hxc.ec_transact to hxc.ec_transact_new with 100 per batch, sleep of 100 milliseconds, ordering by id DESC. Do you want to continue? (y/n): y
Data migration started. 2021-05-20 16:56:34.359987
Migrated: 100%, Records per second: 880, Remaining time: 00:00:00
Done. 2021-05-20 16:57:44.895300
Closing DB connection...
Done.
```

If you execute the same command again the output will be:
```
Problem while creating table. (1050, "Table 'ec_transact_new' already exists") Do you want to continue? (y/n): y
Checking preconditions...
Destination table is not empty. If you continue and "--ignore-duplicates" flag is passed duplicate rows will be skipped. If the flag is not passed the script will stop and exit if hits "Duplicate entry" error. Do you want to continue? (y/n): y
Checking preconditions done.
Migrating 42627 records from hxc.ec_transact to hxc.ec_transact_new with 100 per batch, sleep of 100 milliseconds, ordering by id DESC. Do you want to continue? (y/n): y
Data migration started. 2021-05-20 16:58:28.897208
Cannot migrate batch: (1062, "Duplicate entry '10585000768' for key 'PRIMARY'")
Closing DB connection...
Done.
```
---
If the new table is created and empty we can use the following 
command.

**Command:**

`python3 data_migration.py --db-user root --db-password 123 --db-database hxc --src-table ec_transact --dst-table ec_transact_new --ordering-column "id" --order-direction DESC --sleep 500 --ignore-duplicates`

We can terminate the execution by pressing Ctrl+C. 

**Output:**
```
Checking preconditions...
Checking preconditions done.
Migrating 42627 records from hxc.ec_transact to hxc.ec_transact_new with 100 per batch, sleep of 500 milliseconds, ordering by id DESC. Do you want to continue? (y/n): y
Data migration started. 2021-05-20 17:02:29.542959
Migrated: 18%, Records per second: 1204, Remaining time: 00:03:03 ^C
Terminated. 2021-05-20 17:03:09.738547
To continue migration from here use: "--resume" flag
Closing DB connection...
Done.
```

Now if we use the same command with added `--resume` the migration 
will continue from last inserted record.

**Command:**

`python3 data_migration.py --db-user root --db-password 123 
--db-database hxc --src-table ec_transact --dst-table ec_transact_new --ordering-column "id" --order-direction DESC --sleep 500 --ignore-duplicates --resume`

**Output:**
```
Checking preconditions...
Checking preconditions done.
Migrating 34927 records from hxc.ec_transact to hxc.ec_transact_new with 100 per batch, sleep of 500 milliseconds, ordering by id DESC, starting from id="10584993069". Do you want to continue? (y/n): y
Data migration started. 2021-05-20 17:14:19.504888
Migrated: 100%, Records per second: 956, Remaining time: 00:00:00
Done. 2021-05-20 17:17:33.418925
Closing DB connection...
Done.
```
