# Bundle Sales Comission Report Script

This script processes previous day transaction data to calculate commissions, generate a CSV report, and send SMS notifications to agents. It is designed to work with multiple MariaDB databases and batch SMS dispatching using SMSQD.

## Table of Contents

- [Bundle Sales Comission Report Script](#bundle-sales-comission-report-script)
  - [Table of Contents](#table-of-contents)
  - [Prerequisites](#prerequisites)
  - [Configuration](#configuration)
    - [Configuration Details](#configuration-details)
  - [Setup and Installation](#setup-and-installation)
  - [Usage](#usage)
  - [Logging](#logging)
  - [Notes](#notes)

## Prerequisites

- **Python 3.6**
- **Compatibility with the latest Crediverse schema**
- **No Internet Connection Required** (optional package bundling steps included)

The following Python libraries are required:

- `pymysql`
- `dateutil`

These dependencies are included in a `requirements.txt` file for easy installation.

## Configuration

The configuration is stored in a `config.json` file, structured as follows:

```json

{
    "oltp_db": {
        "host": "127.0.0.1",
        "port": 3306,
        "user": "your_oltp_user",
        "password": "your_password",
        "database": "oltp_database_name"
    },
    "olap_db": {
        "host": "127.0.0.1",
        "port": 3306,
        "user": "your_olap_user",
        "password": "your_password",
        "database": "olap_database_name"
    },
    "smsq_db": {
        "host": "127.0.0.1",
        "port": 3306,
        "user": "your_smsq_user",
        "password": "your_password",
        "database": "smsq_database_name"
    },
    "general": {
        "output_dir": "./output",          // Directory for CSV output files
        "batch_size": 100,                 // Number of SMS messages to send per batch
        "delay_seconds": 1,                // Delay (in seconds) between each SMS batch
         "max_connection_retries": 3,       // maximum number of retries to re-connect with a new for database connection
         "internationalFormat": "228",
         "sms_templates": {
            "en": "Dear agent {msisdn}, your bundle sales commission for {prev_date} is {commission}.",
            "fr": "Cher agent {msisdn}, votre commission sur les ventes de forfaits pour {prev_date} est {commission}."
          }
    }
}
```

### Configuration Details

- **`oltp_db`**: Connection details for the OLTP (Online Transaction Processing) database, storing agent data.
- **`olap_db`**: Connection details for the OLAP (Online Analytical Processing) database, used to fetch transaction data.
- **`smsq_db`**: Connection details for the SMS queue database to manage SMS dispatch.
- **`general`**:
  - `output_dir`: Directory to store generated CSV files.
  - `batch_size`: Number of SMS messages to process per batch.
  - `delay_seconds`: Delay between SMS batches (in seconds).
  - `max_connection_retries` database connection retires
  - `internationalFormat` The prefix digits to be added at the beginning of each MSISDN to ensure it conforms to the international format.
  - `sms_templates`:  SMS message templates in two languages (en and fr). Each template includes placeholders that can be dynamically replaced with specific values at runtime.
    - `SMS placeholder:` comission is mandatory ,msisdn and prev_date are optional if not provided will not be populated no error thrown just a warning in the logs.
      - `{msisdn}`: The agent MSISDN.
      - `{prev_date}`: The date for which the sales commission is being calculated.
      - `{commission}:` The commission amount calculated for the previous day.

## Setup and Installation

1. **Install dependencies** from `requirements.txt` (ensure to bundle if no internet connection is available):

   ```bash
   pip install -r requirements.txt
   ```
2. **Bundling Dependencies for Offline Use**:

   - If the server lacks internet access, you can bundle the packages locally and transfer them.
   - Run this command to create a local package directory:

     ```bash
     pip download -r requirements.txt -d ./packages
     ```
   - Transfer the `packages` folder to the server and install dependencies:

     ```bash
     pip install --no-index --find-links=./packages -r requirements.txt
     ```
3. **Prepare the Configuration File**:

   - Copy the `config.json` example provided above.
   - Update connection details and other configurations as necessary.
4. **Set Up Logging** (optional):

   - The script logs events and errors to `commission_process_{timestamp}.log` in the `logs` directory.

## Usage

1. **How to use Script**:

   1. **Manually run the script**

      ```bash
      python bundle_sales_agents_comission_report.py

      ```

      You can run the script with --help to get the available commands and options, including how to set the config path:

      ```bash
      python bundle_sales_agents_comission_report.py --help
      ```

      **Provide Config Path:** If you'd like to provide a custom config file path, use the --config option followed by the path:

   ```bash
      python bundle_sales_agents_comission_report.py --config /path/to/your/config_file.json
   ```

   2. **Add it to Cronjob**
      ```
          0 2 * * * /path/to/python3 /path/to/your/bundle_sales_agents_comission_report.py --config /path/to/your/config_file.json
      ```
2. **Outputs**:

   - CSV report/data is generated in the specified `output_dir`.
   - total 4 files are generated in the specified `output_dir`

     - active_agents_with_bundle_reports_disabled_{timestamp}.csv ( active agents msisdn for which `send_daily_bundle_commission_report` is disabled or bit 0)
     - active_agents_with_bundle_reports_enabled_{timestamp}.csv ( active agents msisdn for which `send_daily_bundle_commission_report` is enabled or bit 1)
     - ap_transact_raw_{timestamp}.csv (transaction data (AgentMSISDN, Total_commission (gross_sales_amount - amount)) extracted from `ap_transact`)
     - smsq_sms_{timestamp}.csv (contain MSISDN and SMS notification data inserted into the `smsq_queue` table it's stored to be safe in case of any issue )
   - SMS messages are dispatched to agents using the template provided in the config.

## Logging

The script generates a log file (`commission_process_{timestamp}.log`) in dir `logs` which will be auto created if doesn't exist, to capture key events, errors, and warnings. This log file is essential for troubleshooting and monitoring the script’s activities, especially for tracking SMS dispatch batches and any errors during database queries.

## Notes

- Please note that `send_daily_bundle_commission_report` column is not added yet to the `ea_agent` schema you have to apply manually, once the GUI part is done then it will be added to the upgrade process.By default enabled for all agents

```
 alter table `hxc`.`ea_agent` add column `send_daily_bundle_commission_report` BIT NOT NULL DEFAULT 1;

```
  The query below disables SMS sending for agents who are not active.

```

  UPDATE ea_agent
  SET send_daily_bundle_commission_report = 0
  WHERE status != 'A';

```
