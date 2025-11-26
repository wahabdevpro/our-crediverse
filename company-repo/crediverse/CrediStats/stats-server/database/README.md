
# Database creation

To create the campaigns database with test data, execute the following commands:
```
cd CrediStats/stats-server/database 

docker build -t campaigns_db .

docker run -d -p5984:5984 --name 'campaigns_db' campaigns_db

node createDb.mjs 

```


