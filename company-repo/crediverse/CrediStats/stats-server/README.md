## Set Environment Variables

Rename the file `example.env` to `.env` and set all the variables

## Build from source

```bash
npm install # install modules
npm run start # start the app in development mode
```
*Notes*
Don't need to build the app for now, as this is small application and doesn't contains anything complex, but later on, we might need to build e.g minify code etc. 

## Build Docker

```
export DOCKER_VERSION=0.0.1  
docker build -t ghcr.io/concurrent-systems/crediverse/credistats-server:$DOCKER_VERSION -f ./Dockerfile .  
docker push ghcr.io/concurrent-systems/crediverse/credistats-server:$DOCKER_VERSION  
```

## Run with Docker

```
LOG_OUTPUT=/var/logs/stats-server docker-compose up  -d
```

Available environment parameters:  
* **LOG_OUTPUT**  
  Host directory to output log files to, e.g. `/var/log/credistats-server`.  
  Defaults to `./logs`.  

* **PORT**  
  The stats API service port.  
  Defaults to `8801`.  
      
* **DB_HOST**  
  The OLTP database hostname.  
  This should ideally be a DR or Query Slave instance, not the primary OLTP.  
  Defaults to `localhost` (which will never work when ran in a docker container).  

* **DB_PORT**  
  The OLTP database port.  
  Defaults to `3306`.  

* **DB_USER**  
  The OLTP database user name.  
  Defaults to `root`.  
      
* **DB_PASSWORD**  
  The OLTP database password.  

* **DB_NAME**  
  The OLTP database name.  
  Defaults to `hxc`.  
  
* **LOG_FILE_SIZE_BYTES**  
  The maxoimum log file size.  
  When the size is reached, the log file is rotated.  
  Defaults to `1000000`.  
  
* **LOG_FILE_COUNT_LIMIT**  
  The maximum number of rotated log files.  
  When the limit is reached, the oldest log file will be deleted.  
  Defaults to `10`.  
  
* **LOG_FILE_COUNT_MIN**  
  The minimum number of rotated log files.  
  When the file system is full, older log files, beyond this limit, will be deleted.  



