import pymysql
import json
import datetime
import csv
import os
import time
import logging
from dateutil import parser
import argparse
from pymysql import OperationalError
import re

def setup_logging():
    # Get the directory of the current script file
    script_dir = os.path.dirname(os.path.abspath(__file__))
    # Create 'logs' directory within the script's directory
    logs_dir = os.path.join(script_dir, "logs")
    os.makedirs(logs_dir, exist_ok=True)  # Create the logs directory if it doesn't exist

    # Configure logging to write logs to the logs directory
    log_filename = os.path.join(logs_dir, f"commission_process_{datetime.datetime.now().strftime('%Y%m%d_%H%M%S')}.log")
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s - %(levelname)s - %(message)s",
        handlers=[
            logging.FileHandler(log_filename),
            logging.StreamHandler()
        ]
    )
    logging.info("Logging setup complete. Logs will be saved to the 'logs' directory.")
setup_logging()
script_start_time = datetime.datetime.now()
logging.info(f"Script started at {script_start_time}")
# Sample configuration to display for help
SAMPLE_CONFIG = """
save below JSON to config.json in the same dir as the script and update value where necessary

{
  "oltp_db": {
    "host": "localhost",
    "port": 3306,
    "user": "username",
    "password": "password",
    "database": "oltp_database_name"
  },
  "olap_db": {
    "host": "localhost",
    "port": 3306,
    "user": "username",
    "password": "password",
    "database": "olap_database_name"
  },
  "smsq_db": {
    "host": "localhost",
    "port": 3306,
    "user": "username",
    "password": "password",
    "database": "smsq_database_name"
  },
  "general": {
    "output_dir": "path/to/output",
    "batch_size": 100,
    "delay_seconds": 1,
    "internationalFormat": "228",
    "sms_templates": {
    "en": "Dear agent {msisdn}, your bundle sales commission for {prev_date} is {commission}.",
    "fr": "Cher agent {msisdn}, votre commission sur les ventes de forfaits pour le {prev_date} est de {commission}."
  }
  }
}
"""
def parse_arguments():
    parser = argparse.ArgumentParser(description="Commission processing script.")
    parser.add_argument('--show-sample-config', action='store_true', help="Show sample configuration and usage.")
    parser.add_argument('--config', type=str, help="Path to the configuration JSON file.", default='config.json')
    args = parser.parse_args()
    
    if args.show_sample_config:
        print("Sample Configuration:")
        print(SAMPLE_CONFIG)
        exit(0)
    return args
# Load configuration from JSON file
def load_config(config_file='config.json'):
    logging.info(f"Loading configuration from {config_file}.")
    try:
        with open(config_file, 'r') as file:
            config = json.load(file)
        logging.info("Configuration loaded successfully.")
        
        for db_key in ['oltp_db', 'olap_db', 'smsq_db']:
            for field in ['host', 'port', 'user', 'password', 'database']:
                if field not in config[db_key]:
                    raise ValueError(f"Missing required field '{field}' in '{db_key}' configuration section.")

        # Ensure output_dir is provided in the configuration
        if 'output_dir' not in config['general']:
            raise ValueError("Configuration missing required 'output_dir' path in 'general' section.")
        else:
            os.makedirs(config['general']['output_dir'], exist_ok=True)    

        if not os.access(config['general']['output_dir'], os.W_OK):
            raise PermissionError(f"Output directory '{config['general']['output_dir']}' is not writable.")

        if 'sms_templates' not in config['general']:
            raise ValueError("Configuration missing required 'sms_templates' in 'general' section.")
        
        required_placeholders = {'{commission}'}
        optional_placeholders = {'{msisdn}', '{prev_date}'}

        # Regular expression to extract placeholders like {placeholder}
        placeholder_pattern = re.compile(r"\{.*?\}")

        for language, template in config['general'].get('sms_templates', {}).items():
            # Extract placeholders using regex
            template_placeholders = set(placeholder_pattern.findall(template))
            
            # Validate required placeholders
            missing_placeholders = required_placeholders - template_placeholders
            if missing_placeholders:
                raise ValueError(f"The SMS template for language '{language}' is missing required placeholders: {', '.join(missing_placeholders)}")
            
            # Optional placeholders are not mandatory, but log a warning if absent
            missing_optional = optional_placeholders - template_placeholders
            if missing_optional:
                logging.warning(f"The SMS template for language '{language}' is missing optional placeholders: {', '.join(missing_optional)}")

        # Ensure 'en' default template exists
        if 'en' not in config['general']['sms_templates']:
            raise ValueError("Default 'en' SMS template is missing.")
        # Set default values for batch_size and delay_seconds
        config['general']['batch_size'] = config['general'].get('batch_size', 100)
        config['general']['delay_seconds'] = config['general'].get('delay_seconds', 1)
        config['general']['max_connection_retries'] = config['general'].get('max_connection_retries',3)
        
        international_format = config['general'].get('internationalFormat', "")
        if not str(international_format).strip():
            logging.warning("The 'internationalFormat' parameter in the 'general' section is missing or empty.")

        return config
    except FileNotFoundError:
        logging.error(f"Configuration file {config_file} not found.")
        raise
    except json.JSONDecodeError as e:
        logging.error(f"Error decoding JSON from config file: {e}")
        raise

# Print configuration parameters with default values
def print_config(config):
    logging.info("Configuration parameters:")
    general_config = config['general']
    logging.info(f"  output_dir: {general_config['output_dir']}")
    logging.info(f"  batch_size: {general_config.get('batch_size', 100)} (default: 100 if not provided)")
    logging.info(f"  delay_seconds: {general_config.get('delay_seconds', 1)} (default: 1 if not provided)")
    logging.info(f"  max_connection_retries: {general_config.get('max_connection_retries', 3)} (default: 3 if not provided)")
    logging.info(f"  sms_template: {general_config['sms_templates']} (required)")

def generate_tid(msisdn):
    timestamp = int(time.time() * 1000)  # Milliseconds timestamp
    last_digits = str(msisdn)[-4:]  # Use the last 4 digits of the MSISDN
    return int(f"{timestamp}{last_digits}")  # Concatenate
def get_international_format(intl_format):
    # Convert to string if it's not already
    if not isinstance(intl_format, str):
        intl_format = str(intl_format)

    # Trim spaces
    intl_format = intl_format.strip()

    # Validate it's numeric if not empty
    if intl_format and not intl_format.isdigit():
        raise ValueError("Invalid international format. Must be a non-negative integer or empty.")

    return intl_format

def connect_db(db_config, db_name, retries=3):
    for attempt in range(retries):
        try:
            connection = pymysql.connect(
                host=db_config['host'],
                port=db_config['port'],
                user=db_config['user'],
                password=db_config['password'],
                database=db_config['database']
            )
            logging.info(f"Connected to database '{db_name}' on attempt {attempt + 1}.")
            return connection
        except OperationalError as e:
            logging.warning(f"Connection attempt {attempt + 1} to '{db_name}' failed: {e}")
            time.sleep(2 ** attempt)
    logging.error(f"Failed to connect to database '{db_name}' after {retries} attempts.")
    raise OperationalError(f"Could not connect to {db_name}")

def execute_query(connection, query, params=None, retries=3):
    start_time = datetime.datetime.now()
    for attempt in range(retries):
        try:
            with connection.cursor() as cursor:
                logging.info(f"Executing query: {query} with params {params}")
                cursor.execute(query, params)
                result = cursor.fetchall()
                row_count = cursor.rowcount
                connection.commit()
                end_time = datetime.datetime.now()  # End timing the query
                query_duration = (end_time - start_time).total_seconds()
            logging.info(f"Query executed successfully, {row_count} rows returned, in {query_duration:.2f} seconds.")
            return result
        except OperationalError as e:
            logging.warning(f"Query execution attempt {attempt + 1} failed: {e}")
            connection.ping(reconnect=True)
            time.sleep(2 ** attempt)
    logging.error("Failed to execute query after retries.")
    raise OperationalError("Query execution failed after retries.")
# Retrieve agent data from OLTP database
def fetch_agents_with_disabled_reports(connection, output_dir, date_str):
    logging.info("Fetching agents with disabled daily bundle commission reports from OLTP database.")
    query = "SELECT msisdn, send_daily_bundle_commission_report FROM ea_agent WHERE send_daily_bundle_commission_report = 0 and state = 'A';"
    
    # Execute query to fetch agents
    result = execute_query(connection, query)
    num_records = len(result)
    logging.info(f"Fetched active agents with reporting disabled {num_records} records from ea_agent.")
    
    agent_data = [ [row[0]] for row in result ]  

    # Use write_to_csv to save the data to CSV
    write_to_csv(
        data=agent_data, 
        output_dir=output_dir, 
        date_str=date_str,
        filename_prefix="active_agents_with_bundle_reports_disabled", 
        data_type="agent_data"  # This will select the appropriate header
    )

    # Return the set of `msisdn`s
    return set(row[0].strip() for row in result)
def fetch_agents_with_enabled_reports(connection, output_dir, date_str):
    logging.info("Fetching agents with enabled daily bundle commission reports from OLTP database.")
    query = "SELECT msisdn,language FROM ea_agent WHERE send_daily_bundle_commission_report = 1 and state = 'A';"
    
    # Execute query to fetch agents
    result = execute_query(connection, query)
    num_records = len(result)
    logging.info(f"Fetched active agents with reporting enabled {num_records} records from ea_agent.")
    
    # Prepare the data in the format write_to_csv expects, now including `send_notification`
    agent_data = [ [row[0]] for row in result ] 

    # Use write_to_csv to save the data to CSV
    write_to_csv(
        data=agent_data, 
        output_dir=output_dir, 
        date_str=date_str,
        filename_prefix="active_agents_with_bundle_reports_enabled", 
        data_type="agent_data"  # This will select the appropriate header
    )

    # Return the set of `msisdn`s
    return set(result)

def fetch_transaction_data(connection, prev_date):
# Retrieve transaction data from OLAP databasedef fetch_transaction_data(connection, prev_date):
    logging.info(f"Fetching transaction data for date {prev_date} from OLAP database.")
    query = f"""
        SELECT 
            ND.a_msisdn, 
            SUM(ND.gross_sales_amount - ND.amount) AS total_commission_amount
        FROM 
            ap_transact AS ND
        WHERE 
            ND.comp_id = 2
            AND ND.type = 'ND'
            AND ND.success = 1
            AND ND.ended_date = %s
            AND ND.id NOT IN (
                SELECT 
                    NR.related_id
                FROM 
                    ap_transact AS NR
                WHERE 
                    NR.comp_id = 2
                    AND NR.type = 'NR'
                    AND NR.success = 1
                    AND NR.related_id IS NOT NULL
                    AND NR.ended_date = %s
            )
        GROUP BY 
            ND.a_msisdn;

    """
    result = execute_query(connection, query, (prev_date, prev_date))
    num_records = len(result)
    logging.info(f"Fetched {num_records} records from ap_transact.")
    return [(row[0], row[1]) for row in result]


def write_to_csv(data, output_dir,date_str, filename_prefix, data_type):
    filename = f"{filename_prefix}_{date_str}.csv"
    filepath = os.path.join(output_dir, filename)

    # Define headers based on data type
    headers = {
        "sms": ["MSISDN", "Message"],
        "agent_data": ["MSISDN"],
        "transaction_data": ["Agentmsisdn", "Total_commission (gross_sales_amount - amount)"]
    }
    header = headers.get(data_type, ["Column1", "Column2"])  # Default header if data_type not found

    logging.info(f"Writing {len(data)} data to CSV file at {filepath}")
    try:
        with open(filepath, 'w', newline='') as csvfile:
            writer = csv.writer(csvfile)
            writer.writerow(header)  # Write the header
            writer.writerows(data)   # Write the data rows
        logging.info(f"Data written successfully to {filepath}")
    except IOError as e:
        logging.error(f"Failed to write CSV file: {e}")
        raise

def combine_agent_commissions(agents_with_reports, transaction_data):
    """
    Combines agent data with transaction commissions. Assigns a commission of 0 for agents 
    in `agents_with_reports` not present in `transaction_data`. Keeps the language column intact.
    """
    # Normalize MSISDNs by stripping spaces
    transacted_dict = {str(msisdn).strip(): commission for msisdn, commission in transaction_data}
    
    # Normalize agent MSISDNs and combine with commission data
    combined_data = [
        (str(msisdn).strip(), language, transacted_dict.get(str(msisdn).strip(), 0))  # Get commission or 0
        for msisdn, language in agents_with_reports
    ]
    
    return combined_data


# Prepare SMS messages using a single configurable template
def prepare_sms_message(agent_data, sms_templates, prev_date):
    logging.info("Preparing SMS messages for agents.")
    messages = []
    
    for msisdn, language, commission in agent_data:
        # Get the appropriate template based on language, defaulting to 'en' if not found
        sms_template = sms_templates.get(language, sms_templates.get("en"))
        
        if not sms_template:
            logging.error(f"No SMS template found for language '{language}' or default 'en'. Skipping MSISDN {msisdn}.")
            continue

        try:
            # Prepare the message, conditionally including msisdn and prev_date if the placeholders exist in the template
            if '{msisdn}' in sms_template and '{prev_date}' in sms_template:
                message = sms_template.format(
                    msisdn=msisdn,
                    commission=f"{commission:.2f}",
                    prev_date=prev_date
                )
            elif '{msisdn}' in sms_template:
                message = sms_template.format(
                    msisdn=msisdn,
                    commission=f"{commission:.2f}"
                )
            elif '{prev_date}' in sms_template:
                message = sms_template.format(
                    commission=f"{commission:.2f}",
                    prev_date=prev_date
                )
            else:
                message = sms_template.format(commission=f"{commission:.2f}")

            messages.append((msisdn, message))
        except KeyError as e:
            logging.error(
                f"Template formatting failed for MSISDN {msisdn}. "
                f"Commission: {commission}, Prev Date: {prev_date}, Language: {language}. Error: {e}"
            )
        except Exception as e:
            logging.error(
                f"Unexpected error while formatting message for MSISDN {msisdn}. "
                f"Commission: {commission}, Prev Date: {prev_date}, Language: {language}. Error: {e}"
            )
    
    logging.info(f"Prepared {len(messages)} SMS messages.")
    return messages


# Insert SMS messages in batches
def insert_sms(connection, messages, batch_size, delay_seconds, agents_without_reports, international_format):
    query = """
INSERT INTO smsq_queue (
    transaction_id, 
    application, 
    source_msisdn, 
    destination_msisdn, 
    dcs_encoding, 
    message, 
    kvpinfo, 
    insertion_date, 
    start_hour, 
    end_hour, 
    ttl
)
VALUES (
    %s,                  -- transaction_id (bigint)
    'CommissionReport',  -- application (static value)
    '123',               -- source_msisdn (static value)
    %s,                  -- destination_msisdn (msisdn parameter)
    3,                   -- dcs_encoding (static value)
    %s,                  -- message (message parameter)
    '',                  -- kvpinfo (static empty string)
    NOW(),               -- insertion_date (current timestamp)
    0,                   -- start_hour (static value)
    24,                  -- end_hour (static value)
    240                  -- ttl (static value)
);
    """
    logging.info(f"Starting SMS insertion process.")
    
    total_batches = len(messages) // batch_size + (1 if len(messages) % batch_size != 0 else 0)
    logging.info(f" Total Records to be inserted: {len(messages)}, Total batches to be processed: {total_batches}")
    
    total_records = len(messages)
    total_inserted = 0
    total_skipped = 0
    
    try:
        with connection.cursor() as cursor:
            for batch_num, i in enumerate(range(0, len(messages), batch_size), start=1):
                batch = messages[i:i + batch_size]
                current_batch_inserted = 0
                current_batch_skipped = 0
                #batch_id = f"batch_{batch_num}_{int(time.time())}"  # Unique batch identifier

                for msisdn, message in batch:
                    if str(msisdn).strip() not in agents_without_reports:
                        transaction_id = generate_tid(msisdn) 
                    if international_format:
                        msisdn = f"{international_format}{msisdn}"    
                        try:
                            cursor.execute(query, (transaction_id, msisdn, message))
                            current_batch_inserted += 1
                        except pymysql.MySQLError as e:
                            logging.error(f"Failed to insert message for MSISDN {msisdn}. Error: {e}")
                            continue  # Skip this record but continue with the rest
                    else:
                        current_batch_skipped += 1
                        logging.info(f"Skipping Insertion for MSISDN {msisdn} as it does not have reporting enabled.")

                # Commit after processing each batch
                connection.commit()

                # Update running totals
                total_inserted += current_batch_inserted
                total_skipped += current_batch_skipped

                percentage_completed = (total_inserted / total_records) * 100

                logging.info(f"Batch ({batch_num}/{total_batches}) processed. "
                             f"Inserted in this batch: {current_batch_inserted}, "
                             f"Skipped in this batch: {current_batch_skipped}. "
                             f"Running Total - Inserted: {total_inserted}, Skipped: {total_skipped}. "
                             f"Progress: {total_inserted}/{total_records} ({percentage_completed:.2f}%).")
                
                time.sleep(delay_seconds)

            logging.info(f"SMS Insertion completed: Total Records: {total_records}, "
                         f"Total Inserted: {total_inserted}, Total Skipped: {total_skipped}.")
            
    except pymysql.MySQLError as e:
        logging.error(f"Failed to insert SMS messages due to a connection error: {e}")
        connection.rollback()
        raise
# Main Script Execution
def main():
    args = parse_arguments()
    logging.info("Starting commission processing script.")
    config = load_config(config_file=args.config)

    # Print the config values
    print_config(config)
    
    output_dir = config['general']['output_dir']  # output_dir is required in config
    batch_size = config['general']['batch_size']  # Default batch_size = 100 if not provided
    delay_seconds = config['general']['delay_seconds']  # Default delay_seconds = 1 if not provided
    sms_template = config['general']['sms_templates']  # sms_template must be provided in config
    internationalFormat = config['general']['internationalFormat']
    prev_date = (datetime.datetime.now() - datetime.timedelta(days=1)).strftime('%Y-%m-%d')
    #prev_date = '2024-12-11'
    timestamp = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')

    
    try:
        # Connect to each database and print the database name
        with connect_db(config['oltp_db'], config['oltp_db']['database']) as oltp_conn, \
             connect_db(config['olap_db'], config['olap_db']['database']) as olap_conn, \
             connect_db(config['smsq_db'], config['smsq_db']['database']) as smsq_conn:
            
            # Fetch agent data with disabled reports from OLTP
            agents_without_reports = fetch_agents_with_disabled_reports(oltp_conn, output_dir,timestamp)
            agents_with_reports_enabled = fetch_agents_with_enabled_reports(oltp_conn, output_dir,timestamp)
            
            # Fetch transaction data from OLAP
            transaction_data = fetch_transaction_data(olap_conn, prev_date)

            write_to_csv(transaction_data, output_dir,timestamp,filename_prefix="ap_transact_raw",data_type="transaction_data")

            all_agent_commissions = combine_agent_commissions(agents_with_reports_enabled, transaction_data)

            sms_messages = prepare_sms_message(all_agent_commissions, sms_template, prev_date)
            
            write_to_csv(sms_messages, output_dir,timestamp ,filename_prefix="smsq_sms" ,data_type="sms")
            international_format = get_international_format(internationalFormat)
            insert_sms(smsq_conn, sms_messages, batch_size, delay_seconds, agents_without_reports, international_format)
        
    except Exception as e:
        logging.error(f"An error occurred during script execution: {e}")
        raise
    finally:
        script_end_time = datetime.datetime.now()
        total_duration = (script_end_time - script_start_time).total_seconds()
        logging.info(f"Script finished at {script_end_time}")
        logging.info(f"Total script execution time: {total_duration:.2f} seconds")
if __name__ == "__main__":
    main()
