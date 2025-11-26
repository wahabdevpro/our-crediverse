#!/usr/bin/env python3

import argparse
import time
import re
from datetime import datetime
from timeit import default_timer as timer
from pathlib import Path
import pymysql.cursors

from pymysql import MySQLError

minimal_mysql_version = 5.1
PROBLEM = 'PROBLEM'
OK = 'OK'
yes_pattern = re.compile(r'^y(es)?$', re.IGNORECASE)
no_pattern = re.compile(r'^no?$', re.IGNORECASE)
ordering_direction_pattern = re.compile(r'^(ASC|DESC)$', re.IGNORECASE)
ascending_ordering_direction_pattern = re.compile(r'^ASC$', re.IGNORECASE)
duplication_error_pattern = re.compile(r'.*"Duplicate entry .+ for key \'PRIMARY\'.*')
start_from_pattern = re.compile(r'^(\d+|\d\d\d\d-\d\d-\d\d_\d\d:\d\d:\d\d)$')
table_creation_name_pattern = re.compile(r'.*CREATE TABLE `([^`]+)`.*')
mysql_version_pattern = re.compile(r'^(\d+\.\d+)[.-].*')


def my_time():
    now = datetime.now()
    milliseconds = round(float(now.strftime("%f"))/1000)
    return '[' + str(now.strftime("%Y-%m-%d %H:%M:%S")) + f'.{milliseconds}]'


def execute_select_sql(connection, sql):
    with connection.cursor() as cursor:
        cursor.execute(sql)
        result = cursor.fetchall()
        connection.commit()
        return result


def execute_dml_sql(connection, sql):
    with connection.cursor() as cursor:
        affected_rows = cursor.execute(sql)
        connection.commit()
        return affected_rows


def do_continue(prompt_message):
    answer = input(f'{prompt_message} Do you want to continue? (y/n): ')
    if not yes_pattern.match(answer):
        print(my_time(), 'Exiting...')
        exit()


def problem(message):
    print(my_time(), message)
    return PROBLEM


def is_int(s):
    try:
        int(s)
        return True
    except ValueError:
        return False


def check_preconditions(args):
    print(my_time(), 'Checking preconditions...')

    try:
        result = execute_select_sql(connection, f'SELECT version() as version;')[0]
        if not result:
            return problem(f'Cannot get MySQL version.')
        matcher = mysql_version_pattern.match(result['version'])
        if matcher:
            mysql_version = matcher.group(1)
            if float(mysql_version) <= minimal_mysql_version:
                return problem(f'MySQL version ({mysql_version}) must be greater than {minimal_mysql_version}.')
    except MySQLError as e:
        return problem(f'Cannot get MySQL version. ' + str(e))

    if not is_int(args.number_of_records) or int(args.number_of_records) < 0:
        return problem(f'Number of records "{args.number_of_records}" is not an integer number or is negative.')

    if not is_int(args.batch_size) or int(args.batch_size) <= 0:
        return problem(f'Batch size "{args.batch_size}" is not an integer number or not a positive number.')

    if not is_int(args.insert_batch_size) or int(args.insert_batch_size) <= 0:
        return problem(f'Insert batch size "{args.insert_batch_size}" is not an integer number or not a positive number.')

    if int(args.insert_batch_size) > int(args.batch_size):
        return problem(f'Insert batch size ({args.insert_batch_size}) cannot be greater than batch size ({args.batch_size}).')

    if int(args.number_of_records) != 0 and int(args.batch_size) >= int(args.number_of_records):
        do_continue(f'Batch size ({args.batch_size}) >= Number of records ({args.number_of_records}). Migration will be done at once.')

    if not is_int(args.offset) or int(args.offset) < 0:
        return problem(f'Offset size "{args.offset}" is not an integer number or is negative.')

    if args.start_from and not start_from_pattern.match(args.start_from):
        return problem(f'Start from "{args.start_from}" is not correct. It should be number or date in format: "yyyy-mm-dd_hh:mm:ss"')

    if args.end_at and not start_from_pattern.match(args.end_at):
        return problem(f'End at "{args.end_at}" is not correct. It should be number or date in format: "yyyy-mm-dd_hh:mm:ss"')

    if not ordering_direction_pattern.match(args.order_direction):
        return problem(f'Ordering is not ASC nor DESC: "{args.order_direction}"')

    if not is_int(args.sleep) or int(args.sleep) < 0:
        return problem(f'Sleep "{args.sleep}" is not an integer number or is negative.')

    if args.table_creation_sql_file and not Path(args.table_creation_sql_file).is_file():
        return problem(f'Table creation SQL file "{args.table_creation_sql_file}" does not exist or is not a file.')

    if db + args.src_table == db + args.dst_table:
        return problem(f'Destination table "{db}.{args.dst_table}" cannot be the same as source table "{db}.{args.src_table}".')

    try:
        result = execute_select_sql(connection, f'SELECT * FROM {db}.{args.src_table} LIMIT 1;')[0]
        if not result:
            return problem(f'Source table {db}.{args.src_table} is empty.')
    except MySQLError as e:
        return problem(f'Cannot select records from source table {db}.{args.src_table}. ' + str(e))

    ordering_column = find_ordering_column(args.ordering_column)
    if not execute_select_sql(connection, f"SHOW COLUMNS FROM {db}.{args.src_table} WHERE `Field` = '{ordering_column}';"):
        return problem(f'Ordering column {db}.{args.src_table}.{ordering_column} does not exist.')

    try:
        result = execute_select_sql(connection, f'SELECT * FROM {db}.{args.dst_table} LIMIT 1;')
        if result and not args.resume:
            do_continue(f'Destination table is not empty. If you continue and "--ignore-duplicates" flag is passed, duplicate rows will be skipped. '
                        f'If the flag is not passed, the script will stop and exit if hits "Duplicate entry" error.')
    except MySQLError as e:
        return problem(f'Problem with destination table {db}.{args.dst_table}. ' + str(e))

    print(my_time(), 'Checking preconditions done.')
    return OK


def find_ordering_column(ordering_column):
    if not ordering_column:
        result = execute_select_sql(connection, f"SHOW COLUMNS FROM {db}.{args.src_table} WHERE `Key` = 'PRI';")
        if not result or len(result) > 1 or not result[0]['Field']:
            print(my_time(), 'Ordering column is not provided and cannot find the primary key or it is composite.')
            print(my_time(), 'Exiting...')
            exit()
        return result[0]['Field']
    else:
        return ordering_column


def print_progress(migrated, count, last_few_batches_execution_time, batch_size, sleep):
    remaining_count = count - migrated
    remaining_batches = remaining_count / batch_size
    records_per_second = round(batch_size * len(last_few_batches_execution_time) / sum(last_few_batches_execution_time))
    average_per_batch = sum(last_few_batches_execution_time) / len(last_few_batches_execution_time)

    remaining_seconds = remaining_batches * (average_per_batch + sleep / 1000)
    remaining_hours = remaining_seconds // 3600 if remaining_seconds >= 3600 else 0
    remaining_seconds = remaining_seconds - remaining_hours * 3600
    remaining_minutes = remaining_seconds // 60 if remaining_seconds >= 60 else 0
    remaining_seconds = remaining_seconds - remaining_minutes * 60
    migrated_percent = migrated / count * 100

    remaining_hours = str(round(remaining_hours)).zfill(2)
    remaining_minutes = str(round(remaining_minutes)).zfill(2)
    remaining_seconds = str(round(remaining_seconds)).zfill(2)

    print(f'\rMigrated: {round(migrated_percent)}%, Records per second: {records_per_second}, '
          f'Remaining time: {remaining_hours}:{remaining_minutes}:{remaining_seconds} ', end="", flush=True)


def create_table(connection, table_creation_sql_file, dst_table):
    try:
        with open(table_creation_sql_file, 'r') as file:
            sql = file.read().replace('\n', '')
        if sql:
            matcher = table_creation_name_pattern.match(sql)
            if matcher:
                new_table_name = matcher.group(1)
                if new_table_name != dst_table:
                    do_continue(f'It looks like dst_table ({dst_table}) differs from table in creation SQL file ({new_table_name})')
            execute_dml_sql(connection, sql)
            print(my_time(), 'New table created.')
    except MySQLError as e:
        return do_continue(f'Problem while creating table. ' + str(e))


def get_table_columns_as_list(connection, db, table):
    columns_list = []
    try:
        dst_columns = execute_select_sql(connection, f'SHOW COLUMNS FROM {db}.{table};')
        for c in dst_columns:
            columns_list.append(c['Field'])
    except MySQLError as e:
        do_continue(f'Cannot get table ({table}) columns: ' + str(e))
    return columns_list


def get_dst_table_columns(connection, db, src_table, dst_table):
    columns = '*'
    src_columns_list = get_table_columns_as_list(connection, db, src_table)
    dst_columns_list = get_table_columns_as_list(connection, db, dst_table)

    if src_columns_list != dst_columns_list:
        columns = ', '.join(dst_columns_list)
    return columns
    
def get_ordering_column_value_last_inserted_record(dst_table, ordering_column, order_direction):
    reversed_direction = 'DESC' if ascending_ordering_direction_pattern.match(order_direction) else 'ASC'
    result = execute_select_sql(connection, f'SELECT `{ordering_column}` as `ordering_column` FROM {db}.{dst_table}'
                                            f' ORDER BY `{ordering_column}` {reversed_direction} LIMIT 1')
    return result[0]['ordering_column'] if result else None


def migrate_batch(connection, db, src_table, dst_table, batch_size, offset, ordering_column, order_direction, where_clause, src_columns, dst_columns, debug,
                  ignore_duplicates):
    
    ignore = 'IGNORE' if ignore_duplicates else ''
    command = f'INSERT {ignore} INTO {db}.{dst_table} ({dst_columns}) SELECT {src_columns} FROM {db}.{src_table} {where_clause}' \
              f' ORDER BY `{ordering_column}` {order_direction} LIMIT {batch_size} OFFSET {offset};'
    if debug:
        print(my_time(), command)
    affected_rows = execute_dml_sql(connection, command)
    connection.commit()
    return affected_rows


def migrate(connection, db, src_table, dst_table, batch_size, number_of_records, offset, order_direction, ordering_column, sleep, ignore_duplicates,
            start_from, end_at, debug, resume, column_maps):
    if resume:
        try:
            print(my_time(), 'Getting last inserted record...')
            start_from = get_ordering_column_value_last_inserted_record(dst_table, ordering_column, order_direction)
        except MySQLError as e:
            return problem('Cannot get last inserted record from destination table: ' + str(e))

    where_clause = ''
    if start_from:
        if not isinstance(start_from, int) and not isinstance(start_from, float):
            start_from = str(start_from).replace('_', ' ')
        if ordering_column != 'id':
            start_from = f"'{start_from}'"  # Quote the value
        comparison_sign = '>' if ascending_ordering_direction_pattern.match(order_direction) else '<'
        where_clause = f' WHERE `{ordering_column}` {comparison_sign} {start_from} '

    if end_at:
        if not isinstance(end_at, int) and not isinstance(end_at, float):
            end_at = str(end_at).replace('_', ' ')
        if ordering_column != 'id':
            end_at = f"'{end_at}'"  # Quote the value
        comparison_sign_end = '<' if ascending_ordering_direction_pattern.match(order_direction) else '>'
        if where_clause == '':
            where_clause = f' WHERE `{ordering_column}` {comparison_sign_end} {end_at} '
        else:
            where_clause = where_clause + f' AND `{ordering_column}` {comparison_sign_end} {end_at} '

    try:
        print(my_time(), 'Counting the records in the table...')
        count = execute_select_sql(connection, f'SELECT COUNT(1) as `count` FROM {db}.{src_table} {where_clause}')[0]['count']
    except MySQLError as e:
        return problem('Cannot count records in the source table: ' + str(e))

    if offset >= count:
        return problem(f'Offset ({offset}) is greater or equal to number of records in the table: {count}')

    if number_of_records != 0 and number_of_records < count - offset:
        count = number_of_records
    else:
        count -= offset

    if number_of_records != 0 and batch_size > number_of_records:
        batch_size = number_of_records

    offset_desc = f', skipping first {offset} records' if offset and offset > 0 else ''
    start_from_desc = f', starting from {ordering_column}="{start_from}"' if start_from else ''
    end_at_desc = f', ending at {ordering_column}="{end_at}"' if end_at else ''
    do_continue(f'Migrating {count} records from {db}.{src_table} to {db}.{dst_table} with {batch_size} per batch{offset_desc},'
                f' sleep of {sleep} milliseconds, ordering by {ordering_column} {order_direction}{start_from_desc}{end_at_desc}.')

    src_columns_list = get_table_columns_as_list(connection, db, src_table)
    dst_columns_list = get_table_columns_as_list(connection, db, dst_table)

    select_columns_list = []
    insert_columns_list = []
    adjust_columns(src_columns_list, dst_columns_list, column_maps, select_columns_list, insert_columns_list)

    src_columns = ', '.join(select_columns_list)
    dst_columns = ', '.join(insert_columns_list)

    
    print(my_time(), 'Data migration started1.')
    migrated = 0
    executed_batches_count = 0
    last_few_batches_execution_time = []
    try:
        while migrated < count:
            start = timer()
            try:
                affected_rows = migrate_batch(connection, db, src_table, dst_table, batch_size, offset, ordering_column, order_direction,
                                              where_clause, src_columns, dst_columns, debug, ignore_duplicates)
            except MySQLError as e:
                return problem('Cannot migrate batch: ' + str(e))

            execution_time = timer() - start  # timer() doesn't include sleep

            if ignore_duplicates:
                migrated += batch_size
                offset += batch_size
            else:
                migrated += affected_rows
                offset += affected_rows

            if affected_rows != batch_size and migrated < count and not ignore_duplicates:
                print(my_time(), f'WARNING! Affected rows ({affected_rows}) not equal to batch_size ({batch_size})')

            executed_batches_count += 1
            if sleep != 0:
                time.sleep(sleep / 1000.0)
                execution_time += (sleep / 1000.0)

            if len(last_few_batches_execution_time) > 3:
                last_few_batches_execution_time.pop(0)
            last_few_batches_execution_time.append(execution_time)

            print_progress(count=count, migrated=migrated, batch_size=batch_size, last_few_batches_execution_time=last_few_batches_execution_time,
                           sleep=sleep)
    except KeyboardInterrupt:
        print("\n" + my_time(), "Terminated.")
        print(my_time(), 'To continue migration from here use: "--resume" flag')
        return

    print('\n' + my_time(), 'Done.')

def apply_column_map(dst_columns_list, column_maps):
	if column_maps != None:		
	    for col_map in column_maps:
	    	if col_map[0] in dst_columns_list:
	    		print(col_map[0]," exists")
	    		dst_columns_list[dst_columns_list.index(col_map[0])] = col_map[1]
	    		
def adjust_columns(src_columns, dst_columns, column_maps, select_columns_list, insert_columns_list):
	src_column_map = []
	dst_column_map = []
	if column_maps != None:
	    for col in column_maps:
		    src_column_map.append(col[0])
		    dst_column_map.append(col[1])
	for col in src_columns:
		if col in dst_columns or col in src_column_map:
			select_columns_list.append(col)
	for col in dst_columns:
		if col in src_columns or col in dst_column_map:
			insert_columns_list.append(col)	

def migrate_single_select(connection, db, src_table, dst_table, batch_size, insert_batch_size, number_of_records, offset, order_direction,
                          ordering_column, sleep, ignore_duplicates, start_from, end_at, debug, resume, column_maps):
    if resume:
        try:
            print(my_time(), 'Getting last inserted record...')
            start_from = get_ordering_column_value_last_inserted_record(dst_table, ordering_column, order_direction)
        except MySQLError as e:
            return problem('Cannot get last inserted record from destination table: ' + str(e))

    where_clause = ''
    if start_from:
        if not isinstance(start_from, int) and not isinstance(start_from, float):
            start_from = str(start_from).replace('_', ' ')
        if ordering_column != 'id':
            start_from = f"'{start_from}'"  # Quote the value
        comparison_sign = '>' if ascending_ordering_direction_pattern.match(order_direction) else '<'
        where_clause = f' WHERE `{ordering_column}` {comparison_sign} {start_from} '

    if end_at:
        if not isinstance(end_at, int) and not isinstance(end_at, float):
            end_at = str(end_at).replace('_', ' ')
        if ordering_column != 'id':
            end_at = f"'{end_at}'"  # Quote the value
        comparison_sign_end = '<' if ascending_ordering_direction_pattern.match(order_direction) else '>'
        if where_clause == '':
            where_clause = f' WHERE `{ordering_column}` {comparison_sign_end} {end_at} '
        else:
            where_clause = where_clause + f' AND `{ordering_column}` {comparison_sign_end} {end_at} '

    try:
        print(my_time(), 'Counting the records in the table...')
        count = execute_select_sql(connection, f'SELECT COUNT(1) as `count` FROM {db}.{src_table} {where_clause}')[0]['count']
    except MySQLError as e:
        return problem('Cannot count records in the source table: ' + str(e))

    if offset >= count:
        return problem(f'Offset ({offset}) is greater or equal to number of records in the table: {count}')

    if number_of_records != 0 and number_of_records < count - offset:
        count = number_of_records
    else:
        count -= offset

    if number_of_records != 0 and batch_size > number_of_records:
        batch_size = number_of_records

    offset_desc = f', skipping first {offset} records' if offset and offset > 0 else ''
    start_from_desc = f', starting from {ordering_column}="{start_from}"' if start_from else ''
    end_at_desc = f', ending at {ordering_column}="{end_at}"' if end_at else ''
    do_continue(f'Migrating {count} records from {db}.{src_table} to {db}.{dst_table} with {batch_size} per batch, {insert_batch_size} records per'
                f' insert{offset_desc}, sleep of {sleep} milliseconds, ordering by {ordering_column} {order_direction}{start_from_desc}{end_at_desc}.')

    src_columns_list = get_table_columns_as_list(connection, db, src_table)
    

    print(my_time(), 'Data migration started2.')
    migrated = 0
    executed_batches_count = 0
    last_few_batches_execution_time = []

    unbuffered_connection = None
    try:
        unbuffered_connection = pymysql.connect(host=args.db_host, user=args.db_user, password=args.db_password, db=db, port=int(args.db_port),
                                                charset='utf8mb4', cursorclass=pymysql.cursors.SSCursor)

        dst_columns_list = get_table_columns_as_list(connection, db, dst_table)

        select_columns_list = []
        insert_columns_list = []
        adjust_columns(src_columns_list, dst_columns_list, column_maps, select_columns_list, insert_columns_list)

        src_columns = ', '.join(select_columns_list)
        dst_columns = ', '.join(insert_columns_list)
        
        placeholders = '), ('.join([', '.join(['%s' for _ in range(len(select_columns_list))]) for _ in range(insert_batch_size)])

        ignore = 'IGNORE' if ignore_duplicates else ''
        insert_command = f'INSERT {ignore} INTO {db}.{dst_table} ({dst_columns}) VALUES({placeholders});'
        select_query = f'SELECT {src_columns} FROM {db}.{src_table} {where_clause}' \
                       f' ORDER BY `{ordering_column}` {order_direction} LIMIT {count} OFFSET {offset};' # LIMIT is needed for OFFSET. Syntax requirement.

        if debug:
            print(my_time(), 'SELECT: ', select_query)
            print(my_time(), 'INSERT: ', insert_command)

        with unbuffered_connection.cursor() as unbuffered_cursor:
            print(my_time(), 'Executing the single select...')
            unbuffered_cursor.execute(select_query)

            print(my_time(), 'Start inserting...')
            i = 0
            values = []
            with connection.cursor() as cursor:
                try:
                    start = timer()
                    for row in unbuffered_cursor:
                        values.extend(list(row))
                        i += 1

                        if i % insert_batch_size == 0:
                            cursor.execute(insert_command, values)
                            values.clear()

                        if i % batch_size == 0:
                            connection.commit()
                            execution_time = timer() - start  # timer() doesn't include sleep
                            migrated += batch_size
                            executed_batches_count += 1

                            if sleep != 0:
                                time.sleep(sleep / 1000.0)
                                execution_time += (sleep / 1000.0)

                            if len(last_few_batches_execution_time) > 3:
                                last_few_batches_execution_time.pop(0)
                            last_few_batches_execution_time.append(execution_time)

                            print_progress(count=count, migrated=migrated, batch_size=batch_size,
                                           last_few_batches_execution_time=last_few_batches_execution_time, sleep=sleep)
                            start = timer()

                    if values:
                        placeholders = '), ('.join([', '.join(['%s' for _ in range(len(select_columns_list))]) for _ in range(len(values) // len(select_columns_list))])
                        insert_command = f'INSERT {ignore} INTO {db}.{dst_table} ({dst_columns}) VALUES({placeholders});'
                        cursor.execute(insert_command, values)

                    connection.commit()
                except MySQLError as e:
                    return problem('\nCannot insert record: ' + str(e))

            unbuffered_connection.commit()

    except MySQLError as e:
        return problem('\nCannot select records: ' + str(e))
    except KeyboardInterrupt:
        print("\n" + my_time(), "Terminated.")
        print(my_time(), 'To continue migration from here use: "--resume" flag')
        return
    finally:
        if unbuffered_connection:
            print('\n' + my_time(), 'Closing unbuffered DB connection...')
            unbuffered_connection.close()
            print(my_time(), 'Done.')

    print('\n' + my_time(), 'Done.')


if __name__ == '__main__':
    # Usage:
    # python3 scripts/data_migration.py --db-user root --db-password ussdgw --db-database hxc --src-table ec_transact --dst-table ec_transact_new
    # --number-of-records 101 --offset 2 --order-direction asc --sleep 1 --table-creation-sql-file create.sql --ordering-column id
    try:
        parser = argparse.ArgumentParser()
        parser.add_argument('--db-host', required=False, default="localhost", help='DB host name')
        parser.add_argument('--db-port', required=False, default=3306, help='DB port')
        parser.add_argument('--db-user', required=True, help='DB user name')
        parser.add_argument('--db-password', required=True, help='DB user password')
        parser.add_argument('--db-database', required=True, help='DB database name')
        parser.add_argument('--src-table', required=True, help='Source table')
        parser.add_argument('--dst-table', required=True, help='Destination table')
        parser.add_argument('--number-of-records', required=False, default=0, help='How many records to migrate. 0 means all of them.')
        parser.add_argument('--batch-size', required=False, default=100, help='Batch size (how many records to proceed at once).')
        parser.add_argument('--insert-batch-size', required=False, default=10, help='How many records to contain every INSERT statement. Not applicable'
                                                                                    ' when --multiple-selects option is used.')
        parser.add_argument('--offset', required=False, default=0, help='Skip first N records.')
        parser.add_argument('--ordering-column', required=False, help='On which column to order the records. Default is PRIMARY KEY.')
        parser.add_argument('--order-direction', required=False, default='ASC', help='Order ASC or DESC.')
        parser.add_argument('--resume', action='store_true', help='Detect last inserted record based on ordering column and direction and continue '
                                                                  'from there. Can be used only with ordering by column with unique values, '
                                                                  'e.g. primary key. When this flag is passed, --start-from argument is discarded.')
        parser.add_argument('--start-from', required=False, help='ID or date (yyyy-mm-dd_hh:mm:ss) from which to start (excluding). '
                                                                 'This adds: WHERE [ordering-column] > [start-from].')
        parser.add_argument('--end-at', required=False, help='ID or date (yyyy-mm-dd_hh:mm:ss) at which to end (excluding). '
                                                                 'This adds: WHERE [ordering-column] < [end-at].')
        parser.add_argument('--ignore-duplicates', action='store_true', help='Silently ignore (skip) duplicate entries.')
        parser.add_argument('--sleep', required=False, default=0, help='How much time to sleep between batches in milliseconds.')
        parser.add_argument('--table-creation-sql-file', required=False, help='File containing table creation statement.')
        parser.add_argument('--multiple-selects', action='store_true', help='Use select per batch instead of single unbuffered select.')
        parser.add_argument('--debug', action='store_true', help='Print each insert statement.')
        parser.add_argument('--column-mapping', required=False, action='append', nargs=2, help='Mapping between a column name in the source table and destination table'
					                                                         'in case a column name has been changed. Format: src_column dst_column')
        args = parser.parse_args()
        connection = None
        db = args.db_database
        try:
            connection = pymysql.connect(host=args.db_host, user=args.db_user, password=args.db_password, db=db, port=int(args.db_port), charset='utf8mb4',
                                         cursorclass=pymysql.cursors.DictCursor)

            if args.table_creation_sql_file:
                create_table(connection, args.table_creation_sql_file, args.dst_table)

            if check_preconditions(args) == PROBLEM:
                print(my_time(), 'Exiting...')
                exit()

            if args.multiple_selects:
                migrate(connection=connection,
                        db=db,
                        src_table=args.src_table,
                        dst_table=args.dst_table,
                        number_of_records=int(args.number_of_records),
                        batch_size=int(args.batch_size),
                        offset=int(args.offset),
                        start_from=args.start_from,
                        end_at=args.end_at,
                        ordering_column=find_ordering_column(args.ordering_column),
                        order_direction=args.order_direction,
                        sleep=int(args.sleep),
                        ignore_duplicates=args.ignore_duplicates,
                        debug=args.debug,
                        resume=args.resume,
                        column_maps=args.column_mapping)
            else:
                migrate_single_select(connection=connection,
                                      db=db,
                                      src_table=args.src_table,
                                      dst_table=args.dst_table,
                                      number_of_records=int(args.number_of_records),
                                      batch_size=int(args.batch_size),
                                      insert_batch_size=int(args.insert_batch_size),
                                      offset=int(args.offset),
                                      start_from=args.start_from,
                                      end_at=args.end_at,
                                      ordering_column=find_ordering_column(args.ordering_column),
                                      order_direction=args.order_direction,
                                      sleep=int(args.sleep),
                                      ignore_duplicates=args.ignore_duplicates,
                                      debug=args.debug,
                                      resume=args.resume,
                                      column_maps=args.column_mapping)

        finally:
            if connection:
                print(my_time(), 'Closing DB connection...')
                connection.close()
                print(my_time(), 'Done.')

    except KeyboardInterrupt:
        print(my_time(), "Terminated.")
