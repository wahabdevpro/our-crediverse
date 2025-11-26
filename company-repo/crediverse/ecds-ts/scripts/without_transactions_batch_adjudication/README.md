## Description
`without_transactions_batch_adjudication.py` is a Python 3 script which fixes the balance and
hold_on balance for provided transactions.
The exact algorithm is:
```
1. For every line in the CSV file:
    1.1 Extract:
        1.1.1 Transaction_ID
        1.1.2 Amount
        1.1.3 A_MSISDN
        
    1.2 Find the active agent against the msisdn.
    1.3 Find the account of the agent.
    1.4 Subtract the amount from the account on hold balance.
    1.5 Add the amount to the account balance.
    1.6 Write the result in a report file.
```
## Prerequisite
If the package `pymysql` is already not installed, install it using pip.
`pip install pymysql`

## Command line arguments
`--help` - Prints available arguments and their description.

`--db-host` - default="localhost" - DB host name.

`--db-port` - default=3306 - DB port.

`--db-user` - **required** - DB user name.

`--db-password` - **required** - DB user password.

`--db-database` - **required** - DB database name.

`--company-id` - **required** - Company ID as per es_company table.

`--transactions-file` - **required** - The input batch CSV file with the 
records. The format should be same as in `template.csv` file

`--output-file` - The file where the report of the execution is stored.

## Example
**Command:**

`python3 without_transactions_batch_adjudication.py --db-host localhost --db-port 3306 --db-user root --db-password ussdgw --db-database hxc --company-id 2 --transactions-file records.csv --output-file output.csv`

**Output:**
```
ADJUDICATING 2023-03-28 13:34:20.740370
Reading transactions from: records.csv
Saving output to: output.csv
10 records already processed. Time for last 10: 1.34 seconds
20 records already processed. Time for last 10: 1.35 seconds
Done 2023-03-28 13:34:23.561714

```

## Note
`agent_id` was not provided by the customer, so, `agent_id` has been fetched from the `ea_agent` table using MSISDN and if the status of the agent is active, but there is no surety that this active agent has been activated prior the transaction, so we can end up updating the account of the agent who has not done the transaction. Although, the probability of this is very low but this can happen.