## Build from source

```bash
npm install # install modules
npm serve # start the app in development mode
npm build # production build
```

## Build Docker

```
export DOCKER_VERSION=0.0.9
docker build -t ghcr.io/concurrent-systems/crediverse/credistats-portal:$DOCKER_VERSION -f ./Dockerfile .  
docker push ghcr.io/concurrent-systems/crediverse/credistats-portal:$DOCKER_VERSION  
```

## Run with Docker

```
LOG_OUTPUT=/var/logs/stats-portal docker-compose up -d
```

Available environment parameters:  
* **LOG_OUTPUT**  
  Host directory to output log files to, e.g. `/var/log/stats-portal`.  
  Defaults to `./logs`.  

* **WEB_PORTAL_PORT**  
  The stats API service port.  
  Defaults to `8802`.  
  
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


