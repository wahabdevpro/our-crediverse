## Description
`batch_adjudication.py` is a Python 3 script which fixes the balance and
hold_on balance for provided transactions.
The exact algorithm is:
```
1. For every line in the CSV file:
    1.1 Extract:
        1.1.1 transaction_no
        1.1.2 amount
        1.1.4 additional_information
        
    1.2 Find the a_agent for the given transaction.
    1.3 Find the account of the agent.
    1.4 Subtract the amount from the account on hold balance.
    1.5 If additional_information contains the string 'May have failed - Follow Up!':
       1.5.1 Add the amount to the account balance.
    
    1.6 Set follow_up = 0 for the given transaction.
    1.7 Write the result in a report file.
```

## Command line arguments
`--help` - Prints available arguments and their description.

`--db-host` - default="localhost" - DB host name.

`--db-port` - default=3306 - DB port.

`--db-user` - **required** - DB user name.

`--db-password` - **required** - DB user password.

`--db-database` - **required** - DB database name.

`--db-database` - **required** - Company ID as per es_company table.

`--transactions-file` - **required** - The input batch CSV file with the 
transactions. The format is the same as the format in the exported from 
the ADMIN file with transactions.

`--output-file` - The file where the report of the execution is stored.

## Example
**Command:**

`python3 batch_adjudication.py --db-user root --db-password 123 --db-database hxc --company-id 2 --transactions-file ne_ecds_tdr_20210825_170457.csv`

**Output:**
```
ADJUDICATING 2021-08-27 13:34:20.740370
Reading transactions from: ne_ecds_tdr_20210825_170457.csv
Saving output to: ne_ecds_tdr_20210825_170457_adjudication_report.csv
10 records already processed. Time for last 10: 1.34 seconds
20 records already processed. Time for last 10: 1.35 seconds
Done 2021-08-27 13:34:23.561714

```
