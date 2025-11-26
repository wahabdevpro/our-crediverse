#!/usr/bin/python3
import argparse
import csv
from collections import defaultdict
from datetime import datetime, timedelta
from decimal import Decimal
from collections import OrderedDict

FIELD_HEADINGS = {
    'transaction_type': 2,
    'return_code': 42,
    'date': 5,
    'group': 14,
    'amount': 34,
    'agent_id': 11
}

TDR_FIELD_INDEXES = {
    'TRANSACTION_TYPE': 2,
    'RETURN_CODE': 42,
    'DATE': 5,
    'GROUP': 14,
    'AMOUNT': 34,
    'AGENT_ID': 11
}

AGGREGATION_FIELDS_MAPPING = {
    'region': 'Region',
    'distributor': 'Distributor'
}

def parse_args():
    parser = argparse.ArgumentParser(description='ECDS TDR Parser')
    parser.add_argument('--tdr-files', nargs='+', required=True, help='One or more TDR files')
    parser.add_argument('--out', help='Output file')
    parser.add_argument('--aggregation-period', default='day', choices=['hour', 'day', 'week', 'month'], help='Aggregation period')
    parser.add_argument('--aggregation-fields', nargs='+', default=[], choices=list(AGGREGATION_FIELDS_MAPPING.keys()), help='One or more aggregation fields')
    return parser.parse_args()

def get_field_index(field_name):
    return TDR_FIELD_INDEXES.get(field_name)

def get_region(a_group):
    if a_group is not None:
        parts = a_group.split('_')
        if len(parts) >= 2:
            return parts[1]
    return None

def get_distributor(a_group):
    if a_group is not None:
        parts = a_group.split('_')
        if len(parts) >= 1:
            return parts[0]
    return None

def convert_amount(amount, filename, line_number):
    try:
        return float(amount)
    except ValueError:
        print(f"Warning: Invalid amount value '{amount}' in file '{filename}', line {line_number}. Skipping row.")
        return None

def parse_date(date_str):
    year = int(date_str[:4])
    month = int(date_str[4:6])
    day = int(date_str[6:8])
    return datetime(year, month, day)

def aggregate_data(tdr_files, aggregation_period, aggregation_fields):
    data = defaultdict(lambda: defaultdict(lambda: defaultdict(lambda: {'airtime_sales_value': Decimal('0'), 'bundle_sales_value': Decimal('0'), 'airtime_agents': set(), 'bundle_agents': set()})))
    
    for tdr_file in tdr_files:
        with open(tdr_file, 'r') as file:
            reader = csv.reader(file)

            #for i, row in enumerate(reader, start=1):
            i = 1
            for row in reader:
                if len(row) < len(FIELD_HEADINGS):
                    print(f"Warning: Invalid number of fields in file '{tdr_file}', line {i}. Expected {len(FIELD_HEADINGS)} fields, found {len(row)}.")
                    continue

                date_str = row[TDR_FIELD_INDEXES['DATE']][:8]
                if len(date_str) != 8:
                    print(f"Warning: Invalid date format in file '{tdr_file}', line {i}. Skipping row.")
                    continue
                
                try:
                    date = parse_date(date_str)
                except ValueError:
                    print(f"Warning: Invalid date in file '{tdr_file}', line {i}. Skipping row.")
                    continue

                out_date_str = date.strftime("%Y-%m-%d")
                if aggregation_period == 'week':
                    out_date_str = date.strftime("%Y-%U")
                elif aggregation_period == 'month':
                    out_date_str = date.strftime("%Y-%m")
                elif aggregation_period == 'hour':
                    out_date_str = date.strftime("%Y-%m-%d %H:00:00")
                
                return_code = row[TDR_FIELD_INDEXES['RETURN_CODE']]

                if return_code != 'SUCCESS':
                    continue
                
                transaction_type = row[TDR_FIELD_INDEXES['TRANSACTION_TYPE']]
                
                if transaction_type not in ['SL', 'ST', 'ND']:
                    continue

                key = (out_date_str,)

                if "region" in aggregation_fields:
                    a_group = row[TDR_FIELD_INDEXES['GROUP']]
                    region = get_region(a_group)
                    key += (region,)

                if "distributor" in aggregation_fields:
                    a_group = row[TDR_FIELD_INDEXES['GROUP']]
                    distributor = get_distributor(a_group)
                    key += (distributor,)

                amount = convert_amount(row[TDR_FIELD_INDEXES['AMOUNT']], tdr_file, i)
                if amount is None:
                    print(f"Warning: Invalid amount '{tdr_file}', line {i}. Skipping row.")
                    continue

                agent_id = row[TDR_FIELD_INDEXES['AGENT_ID']]
                    
                if not key in data:
                    data[key]['airtime_sales_value'] = 0
                    data[key]['airtime_agents'] = []
                    data[key]['bundle_sales_value'] = 0
                    data[key]['bundle_agents'] = []
                if transaction_type in ['SL', 'ST']:
                    data[key]['airtime_sales_value'] += amount
                    if not agent_id in data[key]['airtime_agents']:
                        data[key]['airtime_agents'].append(agent_id)
                elif transaction_type == 'ND':
                    data[key]['bundle_sales_value'] += amount
                    if not agent_id in data[key]['bundle_agents']:
                        data[key]['bundle_agents'].append(agent_id)
                i = i + 1
    return data

def write_output(data, out_file, aggregation_fields):
    with open(out_file, 'w', newline='') as file:
        writer = csv.writer(file)
        headers = ['Date'] + [AGGREGATION_FIELDS_MAPPING[field] for field in aggregation_fields] + ['Airtime Sales Value', 'Bundle Sales Value', 'Airtime Agents', 'Bundle Agents']
        writer.writerow(headers)
        for key, values in OrderedDict(sorted(data.items(), key=lambda x: x[0])).items():
            row = list(key) + [round(values['airtime_sales_value'], 2), round(values['bundle_sales_value'], 2), len(values['airtime_agents']), len(values['bundle_agents'])]
            writer.writerow(row)

def main():
    args = parse_args()
    data = aggregate_data(args.tdr_files, args.aggregation_period, args.aggregation_fields)
    write_output(data, args.out, args.aggregation_fields)

if __name__ == '__main__':
    main()

