FROM python:3.10.2-slim-bullseye
RUN pip3 install schedule
RUN pip3 install pyyaml
RUN pip3 install --no-cache-dir matplotlib
WORKDIR /usr/src/app
COPY ./crediverse_errors_report.py ./
COPY ./errors_report_properties.yml ./
CMD [ "python", "-u", "./crediverse_errors_report.py"]