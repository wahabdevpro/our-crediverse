#!/usr/bin/env python3

import argparse
import time
from datetime import datetime

import pymysql.cursors
import csv

from pymysql import MySQLError

company_id = 2
n = 1000
PROBLEM = 'PROBLEM'
OK = 'OK'


def execute_sql(connection, sql):
    with connection.cursor() as cursor:
        cursor.execute(sql)
        result = cursor.fetchone()
        connection.commit()
        return result


def get_agent_info(connection, agent, msisdn, early_stop=False, check_balance=True):
    # AGENT EXISTS
    state = execute_sql(connection, f'SELECT state FROM ea_agent where id = {agent}')
    if not state:
        return PROBLEM, [agent, msisdn, 'No such agent', '', '', '', '', '', '', '']

    # AGENT STATE
    state = '' if state['state'] in ['A', 'D', 'S'] else state['state']
    if early_stop and state != '':
        return PROBLEM, [agent, msisdn, state, '', '', '', '', '', '', '']

    # ANOTHER AGENT REFERENCE
    another_agent_reference = execute_sql(connection, f'SELECT id FROM ea_agent where supplier_id = {agent} OR owner_id = {agent}')
    another_agent_reference = '' if not another_agent_reference else another_agent_reference['id']
    if early_stop and another_agent_reference != '':
        return PROBLEM, [agent, msisdn, state, another_agent_reference, '', '', '', '', '', '']

    # TRANSACTIONS
    transaction_count = execute_sql(connection, f"SELECT SUM(agent_transactions) as s FROM (\
            SELECT COUNT(*) AS agent_transactions FROM ec_transact WHERE\
                a_agent = {agent}\
                OR b_agent = {agent}\
                OR a_owner = {agent}\
                OR b_owner = {agent}\
                OR (a_msisdn = '{msisdn}' AND comp_id = {company_id})\
            UNION SELECT COUNT(*) AS agent_transactions FROM ec_transact\
                WHERE (b_msisdn = '{msisdn}' AND comp_id = {company_id})\
            ) ec_transac")
    transaction_count = '' if transaction_count['s'] == 0 else transaction_count['s']
    if early_stop and transaction_count != '':
        return PROBLEM, [agent, msisdn, state, another_agent_reference, transaction_count, '', '', '', '', '']

    # AUDIT ENTRIES
    # audit_reference = execute_sql(connection, f'SELECT id FROM es_audit WHERE -webuser_id = {agent}')
    # audit_reference = '' if not audit_reference else audit_reference['id']
    audit_reference = ''
    if early_stop and audit_reference != '':
        return PROBLEM, [agent, msisdn, state, another_agent_reference, transaction_count, audit_reference, '', '', '', '']

    #  WORK ITEMS
    work_item_references = execute_sql(connection, f'SELECT id FROM ew_item WHERE by_agent_id = {agent}')
    work_item_references = '' if not work_item_references else work_item_references['id']
    if early_stop and work_item_references != '':
        return PROBLEM, [agent, msisdn, state, another_agent_reference, transaction_count, audit_reference, work_item_references, '', '', '']

    # BALANCE/BONUS
    account = execute_sql(connection, f'SELECT * FROM ea_account WHERE agent_id = {agent}')
    if account:
        account = ''
        balances = execute_sql(connection, f'SELECT balance, bonus FROM ea_account WHERE agent_id = {agent}')
        balance = '' if balances['balance'] == 0 else balances['balance']
        bonus_balance = '' if balances['bonus'] == 0 else balances['bonus']

        if check_balance:
            if early_stop and (balance != '' or bonus_balance != ''):
                return PROBLEM, [agent, msisdn, state, another_agent_reference, transaction_count, audit_reference, work_item_references, account, balance,
                                 bonus_balance]

    else:
        if early_stop:
            return PROBLEM, [agent, msisdn, state, another_agent_reference, transaction_count, audit_reference, work_item_references, account, '', '']
        account = 'No acc'
        balance = ''
        bonus_balance = ''

    if early_stop:
        return OK, [agent, msisdn, state, another_agent_reference, transaction_count, audit_reference, work_item_references, account, balance,
                    bonus_balance]

    result = OK
    if check_balance:
        properties_to_check = [state, another_agent_reference, transaction_count, audit_reference, work_item_references, account, balance, bonus_balance]
    else:
        properties_to_check = [state, another_agent_reference, transaction_count, audit_reference, work_item_references, account]

    for e in properties_to_check:
        if e != '':
            result = PROBLEM
            break

    return result, [agent, msisdn, state, another_agent_reference, transaction_count, audit_reference, work_item_references, account, balance,
                    bonus_balance]


def report(agent_filename, report_filename, host, user, password, db, port):
    if not report_filename:
        report_filename = agent_filename[:-4] + '_report' + agent_filename[-4:]
    good_agents_filename = agent_filename[:-4] + '_good' + agent_filename[-4:]
    good_with_balance_agents_filename = agent_filename[:-4] + '_good_with_balance' + agent_filename[-4:]

    print("Reading agents from:", agent_filename)
    print("Saving report to:", report_filename)
    print("Saving good agents to:", good_agents_filename)
    print("Saving good agents with balance to:", good_with_balance_agents_filename)

    with open(agent_filename) as agents_file, \
            open(report_filename, 'w') as report_file, \
            open(good_agents_filename, 'w') as good_agents_file, \
            open(good_with_balance_agents_filename, 'w') as good_with_balance_agents_file:

        csv_reader = csv.reader(agents_file, delimiter=",")
        csv_writer = csv.writer(report_file, delimiter=',')
        good_agents_file_writer = csv.writer(good_agents_file, delimiter=',')
        good_with_balance_agents_file_writer = csv.writer(good_with_balance_agents_file, delimiter=',')

        headers = next(csv_reader, None)
        good_agents_file_writer.writerow(headers)  # headers
        good_with_balance_agents_file_writer.writerow(headers)  # headers

        connection = pymysql.connect(host=host, user=user, password=password, db=db, port=int(port), charset='utf8mb4', cursorclass=pymysql.cursors.DictCursor)

        csv_writer.writerow(['agent_id', 'msisdn', 'state', 'agent_ref', 'number_of_transactions', 'audit_ref', 'work_item_ref', 'account', 'balance', 'bonus'])
        try:
            counter = 0
            start_time = time.time()
            for row in csv_reader:
                agent = row[11]
                msisdn = row[34]

                result, data = get_agent_info(connection, agent, msisdn)
                if result == PROBLEM:
                    csv_writer.writerow(data)

                    result, data = get_agent_info(connection, agent, msisdn, check_balance=False)
                    if result == OK:
                        good_with_balance_agents_file_writer.writerow(row)

                else:
                    good_agents_file_writer.writerow(row)

                counter += 1
                if counter % n == 0:
                    elapsed_time = time.time() - start_time
                    print(counter, f'records already processed. Time for last {n}', round(elapsed_time, 2), 'seconds <<<<<<<<<<<<')
                    start_time = time.time()

        finally:
            connection.close()


def delete_agent(connection, agent, msisdn, check_balance=True):
    result, data = get_agent_info(connection, agent, msisdn, early_stop=True, check_balance=check_balance)
    if result == PROBLEM:
        return 'Precondition failed', data
    else:
        try:
            execute_sql(connection, f'DELETE FROM ea_account WHERE agent_id = {agent}')
        except MySQLError as e:
            return 'Cannot delete ea_account record ' + str(e), data

        try:
            execute_sql(connection, f'DELETE FROM ea_agent WHERE id = {agent}')
        except MySQLError as e:
            return 'Cannot delete agent ' + str(e), data

        return 'Deleted', data


def delete(agent_filename, report_filename, host, user, password, db, port, check_balance=True):
    if not report_filename:
        report_filename = agent_filename[:-4] + '_deletion_report' + agent_filename[-4:]

    print("Reading agents from:", agent_filename)
    print("Saving output to:", report_filename)

    with open(agent_filename) as agents_file, open(report_filename, 'w') as report_file:
        csv_reader = csv.reader(agents_file, delimiter=",")
        csv_writer = csv.writer(report_file, delimiter=',')

        # headers
        next(csv_reader, None)
        csv_writer.writerow(['agent_id', 'result', 'balance', 'bonus'])

        connection = pymysql.connect(host=host, user=user, password=password, db=db, port=int(port), charset='utf8mb4', cursorclass=pymysql.cursors.DictCursor)

        try:
            counter = 0
            start_time = time.time()
            for row in csv_reader:
                agent = row[11]
                msisdn = row[34]
                result, data = delete_agent(connection, agent, msisdn, check_balance)
                csv_writer.writerow([agent, result, data[8], data[9]])

                counter += 1
                if counter % n == 0:
                    elapsed_time = time.time() - start_time
                    print(counter, f'records already processed. Time for last {n}', round(elapsed_time, 2), 'seconds <<<<<<<<<<<<')
                    start_time = time.time()
        finally:
            connection.close()


def get_olap_info(connection, agent):
    try:
        msisdn_result = execute_sql(connection, f'SELECT msisdn FROM ap_agent_account WHERE id = {agent}')
        if msisdn_result:
            msisdn = msisdn_result['msisdn']
            ap_transact_count = execute_sql(connection, f"SELECT COUNT(1) as count FROM ap_transact WHERE a_msisdn = '{msisdn}' OR b_msisdn = '{msisdn}'")
            return msisdn, ap_transact_count['count']
        else:
            return '', ''
    except MySQLError as e:
        return f'Cannot get OLAP info for agent ' + str(e), ''


def olap_report(deletion_report_file, report_filename, host, user, password, db, port):
    if not deletion_report_file:
        print('You have to provide --deletion-report-file when using --olap-report option')

    if not report_filename:
        report_filename = deletion_report_file[:-20] + '_olap_report' + deletion_report_file[-4:]

    print("Reading agents from:", deletion_report_file)
    print("Saving report to:", report_filename)

    with open(deletion_report_file) as agents_file, \
            open(report_filename, 'w') as report_file:

        csv_reader = csv.reader(agents_file, delimiter=",")
        csv_writer = csv.writer(report_file, delimiter=',')

        connection = pymysql.connect(host=host, user=user, password=password, db=db, port=int(port), charset='utf8mb4', cursorclass=pymysql.cursors.DictCursor)

        # headers
        next(csv_reader, None)
        csv_writer.writerow(['agent_id', 'msisdn', 'ap_transact'])

        try:
            counter = 0
            start_time = time.time()

            for row in csv_reader:
                agent = row[0]
                deleted_in_oltp = True if row[1] == 'Deleted' else False

                if deleted_in_oltp:
                    msisdn, ap_transact_count = get_olap_info(connection, agent)
                    csv_writer.writerow([agent, msisdn, ap_transact_count])
                else:
                    csv_writer.writerow([agent, 'Not deleted in the OLTP', ''])

                counter += 1
                if counter % n == 0:
                    elapsed_time = time.time() - start_time
                    print(counter, f'records already processed. Time for last {n}', round(elapsed_time, 2), 'seconds <<<<<<<<<<<<')
                    start_time = time.time()

        finally:
            connection.close()


def get_olap_msisdn(connection, agent):
    msisdn_result = execute_sql(connection, f'SELECT msisdn FROM ap_agent_account WHERE id = {agent}')
    return msisdn_result['msisdn'] if msisdn_result else ''


def olap_delete_agent(connection, msisdn):
    try:
        execute_sql(connection, f"DELETE FROM ap_transact WHERE a_msisdn = '{msisdn}' OR b_msisdn = '{msisdn}'")
    except MySQLError as e:
        return 'Cannot ap_transact record ' + str(e)

    try:
        execute_sql(connection, f"DELETE FROM ap_agent_account WHERE msisdn = '{msisdn}'")
    except MySQLError as e:
        return 'Cannot delete ap_agent_account record ' + str(e)

    return 'Deleted'


def olap_delete(deletion_report_file, report_filename, host, user, password, db, port):
    if not deletion_report_file:
        print('You have to provide --deletion-report-file when using --olap-report option')

    if not report_filename:
        report_filename = deletion_report_file[:-20] + '_olap_deletion_report' + deletion_report_file[-4:]

    print("Reading agents from:", deletion_report_file)
    print("Saving report to:", report_filename)

    with open(deletion_report_file) as agents_file, \
            open(report_filename, 'w') as report_file:

        csv_reader = csv.reader(agents_file, delimiter=",")
        csv_writer = csv.writer(report_file, delimiter=',')

        connection = pymysql.connect(host=host, user=user, password=password, db=db, port=int(port), charset='utf8mb4', cursorclass=pymysql.cursors.DictCursor)

        # headers
        next(csv_reader, None)
        csv_writer.writerow(['agent_id', 'msisdn', 'result'])

        try:
            counter = 0
            start_time = time.time()

            for row in csv_reader:
                agent = row[0]
                deleted_in_oltp = True if row[1] == 'Deleted' else False
                msisdn = get_olap_msisdn(connection, agent)

                if deleted_in_oltp:
                    result = olap_delete_agent(connection, msisdn)
                    csv_writer.writerow([agent, msisdn, result])
                else:
                    csv_writer.writerow([agent, msisdn, 'Not deleted in the OLTP'])

                counter += 1
                if counter % n == 0:
                    elapsed_time = time.time() - start_time
                    print(counter, f'records already processed. Time for last {n}', round(elapsed_time, 2), 'seconds <<<<<<<<<<<<')
                    start_time = time.time()

        finally:
            connection.close()


if __name__ == '__main__':
    # Usage:
    # python3 agent_deletion.py --db-user root --db-password 123 --db-database hxc --report --agent-file ci_ecds_account_20210305_122022.csv --output-file report.csv
    try:
        # parse command line arguments
        parser = argparse.ArgumentParser()
        parser.add_argument('--db-host', default="localhost", required=False, help='DB host name')
        parser.add_argument('--db-port', default=3306, required=False, help='DB port')
        parser.add_argument('--db-user', required=True, help='DB user name')
        parser.add_argument('--db-password', required=True, help='DB user password')
        parser.add_argument('--db-database', required=True, help='DB database name')
        parser.add_argument('--report', required=False, action='store_true',
                            help='Read the agent file, for each agent checks for relations, non-zero balance etc. and report')
        parser.add_argument('--olap-report', required=False, action='store_true',
                            help='Read the deletion report file, for each deleted agent checks for existing records in OLAP database and report')
        parser.add_argument('--delete', required=False, action='store_true',
                            help='Read the agent file, for each agent, if no relations exist and balance is zero, delete')
        parser.add_argument('--olap-delete', required=False, action='store_true',
                            help='Read the deletion report file, for each deleted agent, deletes records in OLAP database')
        parser.add_argument('--delete-with-balance', required=False, action='store_true',
                            help='Read the agent file, for each agent, if no relations exist, delete (even he/she has balance/bonus)')
        parser.add_argument('--agent-file', required=False, help='One or more files to process')
        parser.add_argument('--output-file', help='Output report ')
        parser.add_argument('--deletion-report-file', help='Deletion report file from previous run. Needed for --olap-report and --olap-delete')
        args = parser.parse_args()

        if args.report:
            if not args.agent_file:
                print('You have to provide --agent-file')
                exit()
            print('REPORTING', datetime.now())
            report(args.agent_file, args.output_file, args.db_host, args.db_user, args.db_password, args.db_database, args.db_port)
            print('Done', datetime.now())
        elif args.olap_report:
            print('OLAP REPORTING', datetime.now())
            olap_report(args.deletion_report_file, args.output_file, args.db_host, args.db_user, args.db_password, args.db_database, args.db_port)
            print('Done', datetime.now())
        elif args.delete:
            if not args.agent_file:
                print('You have to provide --agent-file')
                exit()
            print('DELETING will start in 5 seconds', datetime.now())
            time.sleep(5)
            delete(args.agent_file, args.output_file, args.db_host, args.db_user, args.db_password, args.db_database, args.db_port)
            print('Done', datetime.now())
        elif args.olap_delete:
            print('OLAP DELETING will start in 5 seconds', datetime.now())
            time.sleep(5)
            olap_delete(args.deletion_report_file, args.output_file, args.db_host, args.db_user, args.db_password, args.db_database, args.db_port)
            print('Done', datetime.now())
        elif args.delete_with_balance:
            if not args.agent_file:
                print('You have to provide --agent-file')
                exit()
            print('DELETING WITH BALANCE will start in 5 seconds', datetime.now())
            time.sleep(5)
            delete(args.agent_file, args.output_file, args.db_host, args.db_user, args.db_password, args.db_database, args.db_port, check_balance=False)
            print('Done', datetime.now())

    except KeyboardInterrupt:
        print("Terminated")
        pass
