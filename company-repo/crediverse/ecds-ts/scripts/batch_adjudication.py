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


def get_account_info(connection, agent):
    account = execute_sql(connection, f'SELECT balance, on_hold FROM ea_account WHERE agent_id = {agent}')
    if account:
        return OK, account
    else:
        return PROBLEM, 'No acc'


def write_error(connection, csv_writer, transaction_no, agent, on_hold_before, balance_before, error):
    result, data = get_account_info(connection, agent)
    on_hold_after = float(data['on_hold'])
    balance_after = float(data['balance'])
    csv_writer.writerow([transaction_no, agent, balance_before, balance_after, on_hold_before, on_hold_after, error])


def get_a_agent_id_from_transaction_no(connection, company_id, transaction_no):
    try:
        transaction = execute_sql(connection, f'SELECT a_agent FROM ec_transact WHERE follow_up = 1 AND comp_id = {company_id} AND no = \'{transaction_no}\'')
        if transaction:
            return transaction['a_agent']
        else:
            return PROBLEM
    except MySQLError as e:
        return PROBLEM


def is_number(s):
    """ Returns True if s is a number. """
    try:
        float(s)
        return True
    except ValueError:
        return False


def adjudicate_account(connection, csv_writer, company_id, transaction_no, amount, additional_information):
    if not is_number(amount):
        csv_writer.writerow([transaction_no, '', '', '', '', '', f'Amount "{amount}" is not a number.'])
        return
    amount = float(amount)

    result = get_a_agent_id_from_transaction_no(connection, company_id, transaction_no)
    if result == PROBLEM:
        csv_writer.writerow([transaction_no, '', '', '', '', '', 'Cannot find transaction with the given no with follow up = 1'])
        return
    agent = result

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
        new_on_hold = on_hold_before - amount
        execute_sql(connection, f'UPDATE ea_account SET on_hold = {new_on_hold} WHERE agent_id = {agent}')
    except MySQLError as e:
        write_error(connection, csv_writer, transaction_no, agent, on_hold_before, balance_before,
                    'Cannot update ea_account record (on_hold) ' + str(e))
        return

    if 'May have failed - Follow Up!' in additional_information:
        new_balance = balance_before + amount
        try:
            execute_sql(connection, f'UPDATE ea_account SET balance = {new_balance} WHERE agent_id = {agent}')
        except MySQLError as e:
            write_error(connection, csv_writer, transaction_no, agent, on_hold_before, balance_before,
                        'Cannot update ea_account record (balance) ' + str(e))
            return

    result, data = get_account_info(connection, agent)
    on_hold_after = float(data['on_hold'])
    balance_after = float(data['balance'])

    if not math.isclose(on_hold_after, on_hold_before - amount):
        csv_writer.writerow([transaction_no, agent, balance_before, balance_after, on_hold_before, on_hold_after, 'Wrong on_hold after'])
        return

    if 'May have failed - Follow Up!' in additional_information and not math.isclose(balance_after, balance_before + amount):
        csv_writer.writerow([transaction_no, agent, balance_before, balance_after, on_hold_before, on_hold_after, 'Wrong balance after'])
        return

    try:
        execute_sql(connection, f'UPDATE ec_transact SET follow_up = 0 WHERE comp_id = {company_id} AND no = \'{transaction_no}\'')
    except MySQLError as e:
        write_error(connection, csv_writer, transaction_no, agent, on_hold_before, balance_before,
                    'Cannot update ec_transact record ' + str(e))
        return

    csv_writer.writerow([transaction_no, agent, balance_before, balance_after, on_hold_before, on_hold_after, 'Adjudicated'])


def adjudicate(transactions_filename, report_filename, host, user, password, db, port, company_id):
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
                adjudicate_account(connection, csv_writer, company_id, row['transaction_no'], row['amount'], row['additional_information'])

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
    # python3 batch_adjudication.py --db-user root --db-password 123 --db-database hxc --transactions-file ci_ecds_account_20210305_122022.csv

    try:
        parser = argparse.ArgumentParser()
        parser.add_argument('--db-host', default="localhost", required=False, help='DB host name')
        parser.add_argument('--db-port', default=3306, required=False, help='DB port')
        parser.add_argument('--db-user', required=True, help='DB user name')
        parser.add_argument('--db-password', required=True, help='DB user password')
        parser.add_argument('--db-database', required=True, help='DB database name')
        parser.add_argument('--company-id', required=True, help='Company ID (as per es_company table)')
        parser.add_argument('--transactions-file', required=True, help='CSV batch file with transactions')
        parser.add_argument('--output-file', help='Output report ')
        args = parser.parse_args()

        print('ADJUDICATING', datetime.now())
        adjudicate(args.transactions_file, args.output_file, args.db_host, args.db_user, args.db_password, args.db_database, args.db_port, args.company_id)
        print('Done', datetime.now())

    except KeyboardInterrupt:
        print("Terminated")
        pass
