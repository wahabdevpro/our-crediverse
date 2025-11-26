import re
import os
import gzip
from datetime import datetime, timedelta
import matplotlib.pyplot as plt
import time
import schedule
import yaml
import smtplib

from os.path import basename
from email.mime.application import MIMEApplication
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

MAX_ERROR_LENGTH = 70
NUMBER_OF_ERRORS = 15
errors = {}


def read_properties():
    prop = {}
    with open(f'errors_report_properties.yml') as f:
        prop = yaml.load(f, Loader=yaml.Loader)

    if 'log_dir' not in prop:
        prop['log_dir'] = '/var/opt/cs/c4u/log'

    if 'run_at' not in prop:
        prop['run_at'] = '03:00'

    if 'sender_email' not in prop:
        prop['sender_email'] = 'trayan.momkov@concurrent.systems'

    if 'smtp_server' not in prop:
        prop['smtp_server'] = 'localhost'

    if 'smtp_port' not in prop:
        prop['smtp_port'] = '25'

    if 'recipients' not in prop:
        prop['recipients'] = ['trayan.momkov@concurrent.systems']

    return prop


def get_hostname():
    with open('/etc/hostname') as f:
        return f.readline().rstrip()


def send_email(smtp_server, smtp_port, sender, recipients, subject, files):
    msg = MIMEMultipart()
    msg['From'] = sender
    msg['To'] = ', '.join(recipients)
    msg['Subject'] = subject

    if not files:
        msg.attach(MIMEText(f'{get_hostname()} - No errors.'))

    for f in files or []:
        with open(f, "rb") as fil:
            part = MIMEApplication(
                fil.read(),
                Name=basename(f)
            )
        # After the file is closed
        part['Content-Disposition'] = 'attachment; filename="%s"' % basename(f)
        msg.attach(part)

    smtp = smtplib.SMTP(smtp_server, smtp_port)
    smtp.sendmail(sender, recipients, msg.as_string())
    smtp.close()


def draw_chart(errors, title, image_filename):
    reversed_errors = dict(sorted(errors.items(), key=lambda x: x[1]['count'], reverse=True)[:NUMBER_OF_ERRORS])
    zipped_errors = list(zip(*(sorted(reversed_errors.items(), key=lambda x: x[1]['count']))))

    errors_list = [x['java_class_name'].split('.')[-1:][0] + ': '
                   + (x['error_message'][:MAX_ERROR_LENGTH - 1] + '…' if len(x['error_message']) > MAX_ERROR_LENGTH else x['error_message'])
                   for x in zipped_errors[1]]

    counts_list = [x['count'] for x in zipped_errors[1]]

    fig, ax = plt.subplots()
    ax.barh(range(len(errors_list)), counts_list, align='center', color='red', ecolor='black')
    ax.set_yticks(range(len(errors_list)))
    ax.set_yticklabels(errors_list)
    ax.set_xlabel('Errors count')
    ax.set_ylim(bottom=-0.5, top=NUMBER_OF_ERRORS)  # Manage y-axis properly
    plt.margins(x=0.25, tight=True)

    # Bars labels
    for i, v in enumerate(counts_list):
        ax.text(v + 0.05, i - 0.15, str(v))

    plt.title(title)
    fig.set_size_inches(10.5, 6)
    fig.tight_layout()
    plt.savefig(image_filename)
    plt.subplots_adjust(left=0.65, right=0.98, bottom=0.1, top=0.93)
    # plt.show()


def parse_errors_log_file(input_file, error_line_pattern):
    for line in input_file:
        if line:
            m = re.match(error_line_pattern, line)
            if m:
                java_class_name = m.group(1).strip()
                error_message = m.group(2).strip()
                error_key = f'{java_class_name}: {error_message}'
                if error_key in errors:
                    errors[error_key] = {'java_class_name': java_class_name, 'error_message': error_message, 'count': errors[error_key]['count'] + 1}
                else:
                    errors[error_key] = {'java_class_name': java_class_name, 'error_message': error_message, 'count': 1}


def job():
    print('Starting the report creation...')
    prop = read_properties()
    hostname = get_hostname()
    yesterday = datetime.strftime(datetime.now() - timedelta(1), '%Y-%m-%d')
    output_file = prop['log_dir'] + f'/crediverse_{hostname}_{yesterday}.log_report'
    compressed_report_file = output_file + '.gz'
    chart_file = output_file + '.png'

    # "${TIME_FORMAT} | %-5level | %X{transid} | %logger{36} | %thread | %msg%n"
    error_line_pattern = re.compile(rf'{yesterday}T[\d:.]+\s*\|\s*ERROR\s*\|[^|]*\|\s*([^|]+)\s*\|\s*[^|]*\s*\|\s*(.*)')

    log_files_count = 0
    for filename in os.listdir(prop['log_dir']):
        f = os.path.join(prop['log_dir'], filename)
        if os.path.isfile(f) and filename.startswith('errors'):
            log_files_count += 1
            if filename.endswith('.log'):
                with open(f) as inp:
                    parse_errors_log_file(inp, error_line_pattern)
            elif filename.endswith('.log.gz'):
                with gzip.open(f, 'rt') as inp:
                    parse_errors_log_file(inp, error_line_pattern)

    print(f'{log_files_count} files parsed. Generating report...')

    with gzip.open(compressed_report_file, 'wt') as f:
        f.write(f'count: java_class_name: error_message\n\n')
        for error_key, error_data in sorted(errors.items(), key=lambda x: x[1]['count'], reverse=True):
            f.write(f"{error_data['count']}: {error_data['java_class_name']}: {error_data['error_message']}\n")

    print(f'Report file generated and compressed: {compressed_report_file}\nStarting chart generation...')

    if errors:
        draw_chart(errors, f'Crediverse - {hostname} - {yesterday}', chart_file)

    print(f'Chart generated: {chart_file}')
    print('Sending mail using:')
    print(f"\tServer: {prop['smtp_server']}")
    print(f"\tPort: {prop['smtp_port']}")
    print(f"\tSender: {prop['sender_email']}")

    # Send email
    subject = f'{hostname} - Crediverse errors report'
    files = [compressed_report_file, chart_file]
    if not errors:
        subject = subject + ' - No errors'
        files = []
        print('No errors found.')
    send_email(prop['smtp_server'], prop['smtp_port'], prop['sender_email'], prop['recipients'], subject, files=files)
    print(f'Done.')


if __name__ == '__main__':
    run_at = read_properties()['run_at']
    print(f'Report scheduled for: {run_at}')
    schedule.every().day.at(run_at).do(job)

    while True:
        try:
            schedule.run_pending()
        except Exception as e:
            print('ERROR:', e)
        time.sleep(60)
