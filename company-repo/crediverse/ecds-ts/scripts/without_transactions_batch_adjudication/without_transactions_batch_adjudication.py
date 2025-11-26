#!/usr/bin/env python3

import argparse
import math
import time
from datetime import datetime

import pymysql.cursors
import csv

from pymysql import MySQLError

on_how_many_records_to_print_info = 10
PROBLEM = 'PROBLEM'
OK = 'OK'


def execute_sql(connection, sql):
    with connection.cursor() as cursor:
        cursor.execute(sql)
        result = cursor.fetchone()
        connection.commit()
        return result

def execute_update_sql(connection, sql):
    with connection.cursor() as cursor:
        cursor.execute(sql)
        row_count = cursor.rowcount
        connection.commit()
        return row_count


def get_account_info(connection, agent):
    account = execute_sql(connection, f'SELECT balance, on_hold FROM ea_account WHERE agent_id = {agent}')
    if account:
        return OK, account
    else:
        return PROBLEM, 'No acc'


def get_agent_info(connection, msisdn):
    agent = execute_sql(connection, f'SELECT id FROM ea_agent WHERE msisdn = {msisdn} AND state = "A"')
    if agent:
        return agent["id"]
    else:
        return PROBLEM, 'No agent'


def write_error(connection, csv_writer, transaction_no, agent, on_hold_before, balance_before, error):
    result, data = get_account_info(connection, agent)
    on_hold_after = float(data['on_hold'])
    balance_after = float(data['balance'])
    csv_writer.writerow([transaction_no, agent, balance_before, balance_after, on_hold_before, on_hold_after, error])


def is_number(s):
    """ Returns True if s is a number. """
    try:
        float(s)
        return True
    except ValueError:
        return False


def adjudicate_account(connection, csv_writer, transaction_no, amount, msisdn):
    if not is_number(amount):
        csv_writer.writerow([transaction_no, '', '', '', '', '', f'Amount "{amount}" is not a number.'])
        return
    amount = float(amount)

    try:
        result = get_agent_info(connection, msisdn)
        if result == PROBLEM:
            csv_writer.writerow([transaction_no, msisdn, '', '', '', '', f'Cannot find active agent against msisdn: {msisdn}'])
            return
        agent = result
    except MySQLError as e:
        csv_writer.writerow([transaction_no, msisdn, '', '', '', '', f'Cannot find active agent against msisdn: {msisdn}'])
        return

    try:
        result, data = get_account_info(connection, agent)
        if result == PROBLEM:
            csv_writer.writerow([transaction_no, agent, '', '', '', '', f'Cannot find account for the given agent: {agent}'])
            return
    except MySQLError as e:
        csv_writer.writerow([transaction_no, agent, '', '', '', '', f'Cannot find account for the given agent: {agent}'])
        return

    on_hold_before = float(data['on_hold'])
    balance_before = float(data['balance'])
    if on_hold_before < amount:
        write_error(connection, csv_writer, transaction_no, agent, on_hold_before, balance_before,
                    f'Not enough money! on_hold balance ({on_hold_before}) < amount ({amount})')
        return

    try:
        row_count = execute_update_sql(connection, f'UPDATE ea_account SET on_hold = on_hold - ({amount}), balance = balance + ({amount}) WHERE agent_id = ({agent}) AND on_hold >= ({amount});')
        if row_count == 1:
            on_hold_after = on_hold_before - amount # this should be written in output report
            balance_after = balance_before + amount # this should be written in output report
            
            csv_writer.writerow([transaction_no, agent, balance_before, balance_after, on_hold_before, on_hold_after, 'Adjudicated'])
        else:
            write_error(connection, csv_writer, transaction_no, agent, on_hold_before, balance_before,
                    'Cannot update ea_account record (balance), (on_hold) ' + str(e))
    except MySQLError as e:
        write_error(connection, csv_writer, transaction_no, agent, on_hold_before, balance_before,
                    'Cannot update ea_account record (balance), (on_hold) ' + str(e))
        return

def adjudicate(transactions_filename, report_filename, host, user, password, db, port):
    if not report_filename:
        report_filename = transactions_filename[:-4] + '_adjudication_report' + transactions_filename[-4:]

    print("Reading transactions from:", transactions_filename)
    print("Saving output to:", report_filename)

    with open(transactions_filename) as transactions_file, open(report_filename, 'w') as report_file:
        csv_reader = csv.DictReader(transactions_file, delimiter=",")
        csv_writer = csv.writer(report_file, delimiter=',')

        # output file headers
        csv_writer.writerow(['transaction_no', 'agent_id', 'balance_before', 'balance_after', 'on_hold_before', 'on_hold_after', 'result'])

        connection = pymysql.connect(host=host, user=user, password=password, db=db, port=int(port), charset='utf8mb4',
                                     cursorclass=pymysql.cursors.DictCursor)

        try:
            counter = 0
            start_time = time.time()
            for row in csv_reader:
                adjudicate_account(connection, csv_writer, row['Transaction_ID'], row['Amount'], row['A_MSISDN'])

                counter += 1
                if counter % on_how_many_records_to_print_info == 0:
                    elapsed_time = time.time() - start_time
                    print(counter, f'records already processed. Time for last {on_how_many_records_to_print_info}:', round(elapsed_time, 2),
                          'seconds')
                    start_time = time.time()
        finally:
            connection.close()


if __name__ == '__main__':
    # Usage:
    # python3 without_transactions_batch_adjudication.py --db-host localhost --db-port 3306 --db-user root --db-password ussdgw --db-database hxc --company-id 2 --transactions-file records.csv --output-file output.csv

    try:
        parser = argparse.ArgumentParser()
        parser.add_argument('--db-host', default="localhost", required=False, help='DB host name')
        parser.add_argument('--db-port', default=3306, required=False, help='DB port')
        parser.add_argument('--db-user', required=True, help='DB user name')
        parser.add_argument('--db-password', required=True, help='DB user password')
        parser.add_argument('--db-database', required=True, help='DB database name')
        parser.add_argument('--transactions-file', required=True, help='CSV batch file with transactions')
        parser.add_argument('--output-file', help='Output report ')
        args = parser.parse_args()

        print('ADJUDICATING', datetime.now())
        adjudicate(args.transactions_file, args.output_file, args.db_host, args.db_user, args.db_password, args.db_database, args.db_port)
        print('Done', datetime.now())

    except KeyboardInterrupt:
        print("Terminated")
        pass