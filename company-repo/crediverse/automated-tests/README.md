# Credreverse Automated Regression Tests

## How to Run the Tests

Load the Database dump available in `db_dump` directory to the Credivese OLTP database

```mysql -u root -p PASSWORD -P PORT -h 127.0.0.1 hxc < db_dump/crediverse_regression_dump_oltp.sql```

Ensure that oltp (hxc) is accessible on the `localhost` port `3306` where the suite is running

Ensure that port `10012` `12080` `12082` `9084` from the Crediverse machine is forwarded to the localhost (This is not required if the Crediverse and the suite are running on the same machine)

```ssh -L port:127.0.0.1:port username@crediverse_machine_IP```

Run the following commands to execute the test cases

```docker-compose -f docker-compose.yml up```

Run the following command to install the dependencies and run the test locally for debugging

```
pip3 install \
  --no-cache-dir \
  robotframework==5.0 \
  robotframework-databaselibrary==1.2.4 \
  robotframework-requests==0.9.2 \
  selenium \
  robotframework-seleniumlibrary==6.0.0 \
  robotframework-soaplibrary==0.8 \
  robotframework-jsonlibrary==0.3.1 \
  pymysql 
  ```
