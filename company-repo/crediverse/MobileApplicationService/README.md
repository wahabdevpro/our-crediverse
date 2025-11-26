# Mobile Application Service

The mobile application service exposes a gRPC interface on the `MAS_API_PORT` specified in the enviriontment. 


## Environment config
The following environment variables are used to control the service
```
MAS_SESSION_TIMEOUT=90
MAS_CREDIVERSE_URL="http://0.0.0.0:14400"
MAS_API_PORT="5000"

MAS_FEEDBACK_PATH="./output/feedback"
MAS_ANALYTICS_PATH="./output/analytics"

CREDIVERSE_STATS_DB_HOST=localhost
CREDIVERSE_STATS_DB_PORT=3306
CREDIVERSE_STATS_DB_NAME=hxc
CREDIVERSE_STATS_DB_USER=root
CREDIVERSE_STATS_DB_PASSWORD=ussdgw
MAS_ALLOWABLE_CLIENT_TIME_DELTA=30

MAS_JWT_SECRET="Shhh_no_one_knows"

LOG_FILE_SIZE_BYTES=1024000
LOG_FILE_COUNT_LIMIT=100
LOG_FILE_COUNT_MIN=10

MOVIVY_MOBILE_MONEY_URL="https:0.0.0.0"
MOVIVY_MOBILE_MONEY_USE_SSL_CERTIFICATE=FALSE
MOVIVY_MOBILE_MONEY_SSL_CERT_PATH=<Path to PKCS12 SSL certificate>
MOVIVY_MOBILE_MONEY_SSL_PASSWORD=<Passowrd for the SSL certificate>

CREDIVAULT_URL="https://credivault:50051"

    
MOBILE_MONEY_AGENT_USERNAME="ABEL_API_USER"
MOBILE_MONEY_AGENT_PASSWORD="fl@gWSp4"
MOBILE_MONEY_OWNER_PRIVATE_KEY_FILENAME=private_key.pem

AUTOMATIC_WHOLESALE_RETAIL_DISAMBIGUATION="FALSE"

RUST_LOG=TRACE 
```

**NOTES:** 
- The  file at `MOBILE_MONEY_OWNER_PRIVATE_KEY_FILENAME` is a secret, it allows whomever holds it to transact on the associated account!  Its production value should not be visible to unauthorized parties. It should be injected into the docker container after it's creation along with the private server keys. 
- The `CREDIVAULT_URL can not be changed unless the certificates are signed for the address it changes too.
- AUTOMATIC_WHOLESALE_RETAIL_DISAMBIGUATION must be set to `TRUE ` or `FALSE`.  Anything else will make it always false. Not setting it will default to false. 
- The `MOVIVY_MOBILE_MONEY_URL` corresponds to the MovIvy's Mobile Money system. The URL is of the format: `https:<IP>:<PORT>`.
- The `MOVIVY_MOBILE_MONEY_USE_SSL_CERTIFICATE` must be set to `TRUE` or `FALSE`. Anything else will make it always false. Not setting it will default to false. 
- The `MOVIVY_MOBILE_MONEY_SSL_CERT_PATH` and `MOVIVY_MOBILE_MONEY_SSL_PASSWORD` environment variables are the path to the PKCS12 SSL certificate provided by MovIvy and its password respectively. These are mandatory if the `MOVIVY_MOBILE_MONEY_USE_SSL_CERTIFICATE` variable is set to true.

### RUST_LOG 
*Options: *
* `ERROR`
* `WARN`
* `INFO`
* `DEBUG`
* `TRACE`


## Building and Running 
### Build: 
```cargo build```

### clean:
```./cleanup.sh```
this clean up script also delete src/build_info.rs file which is generated during build time it's not necessary for local enviornment but for production build related info are passed.
### Run: 
Make sure you have the `MAS_API_PORT` and `MAS_CREDIVERSE_URL` environment variable set up.

For example:
```
export   MAS_CREDIVERSE_URL="http://0.0.0.0:14400"
export   MAS_API_PORT="500"
```
and then: 
```cargo run --bin mas-service```


### Development

```
cargo run --bin dev-bench
```

### Testing and Integration Testing
The integration testing is currently in a state to be run on the local developer machine as a manual process.

#### Test Design Guidline

*TODO*


The integration testing is currently in a state to be run on the local developer machine as a manual process.

*Prerequisites:*
You need to have docker, docker-compose, cargo and protobuf-compiler installed in order to execute this process.


#### Current ~~automated~~ integration testing mechanism
1. Open a shell.
1. Go to the `MobileApplicationService/testing_jig` directory.
1. Run: `docker-compose up mariadb-crediverse`
1. Leave this shell open
1. Open another shell.
1. Go to the `ecds-ts` directory
1. edit `./services/CreditDistributionService/src/main/java/hxc/services/ecds/rest/Authentication.java`
1. change lines +-997 and line +-1034  which reads `int newPin = random.nextInt(90000) + 10000;` to be `int newPin = 99999;// random.nextInt(90000) + 10000;` **NOTICE: Under NO circumstances should this change be checked in** 
1. Run: `./gradlew runTestHost`
1. Leave this shell open
1. Open another shell.
1. Go to the `MobileApplicationService/helper_scripts` directory.
1. Run: `./startAirSim.sh`
1. Run: `./createMsisdn.sh 0820000014 100`
1. Run: `./createMsisdn.sh 0820000015 100`
1. Run: `./createMsisdn.sh 0820000020 100`
1. Go to the `MobileApplicationService` directory 
1. Run `cargo run --bin mas-service`
1. Leave this shell open
1. In another shell.
1. Go to the `MobileApplicationService` directory.
1. Run  `cargo test --test integration_test_client -- --nocapture` 
1. Make sure the tests passed.
1. Run `git checkout ../ecds-ts/services/CreditDistributionService/src/main/java/hxc/services/ecds/rest/Authentication.java` to make sure the changes for the static pin is reverted. NB!



#### OLD and to be mechanism.  Docker is not working for anything but the database at the moment. 
*NOTE:* The reason this is not working at the moment (2023-03-02) is that it proved to be brittle and error prone. 

1. go to the `ecds-ts` directory.
2. Run `gradle clean ; gradle build ; gradle publish`
3. Go to the `MobileApplicationService` directory
4. Run `cargo clean; cargo build`
5. go to the `MobileApplicationService\testing_jig` directory
6. run `docker-compose up` - (This will bring up a credverse environment with the MAS running.)
6. go to the `MobileApplicationService` in a new terminal.
7. Run `cargo test`


## Building and Running with Docker 
Set the `MAS_API_PORT` and `MAS_CREDIVERSE_URL` environment variables in the Dockerfile

### Building the docker image
```docker build -t mobile_application_service .```

### Running the docker image
```docker run -p5000:5000 mobile_application_service:latest```



## Designs 

### Login Dance
@startuml

App->MAS: Login(msisdn,pin + Metadata())
MAS-->App: (token, REQUIRE_OTP)
App->MAS: Submit_otp(session_id, otp + Metadata())
MAS-->App: (AUTHENTICATED)

@enduml

### Logging 
All log messages must contain their original context(function/class/module name) and the relevant parameters(function parameters/ request / response ) with which the context was entered or created. 

Make sure that all sensitive information in log messages are obfuscated. 


#### Errors
Events that shows that there is something wrong with the service or the services it depends upon.
 - Connection Errors
 - Time outs 
 - A 3rd party service returned an unexpected error or result. Not finding an object is not unexpected. An service or a connection error when trying is. 

#### Warnings
Events that are unexpectedly off the happy path can be logged as warnings.  This includes not finding business objects, security challenge failures, and any business rule failure that might be of interest.   Care should be taken to not log all business rule failures here lest the logs become unmanageable. 

#### Info
As little as possible should be logged at this level. This is also the highest level of logging to be used in production. Only events that are useful for auditing should be logged at this level, and log messages should be kept small. 

#### Debug
This level of logging should only be switched on in development and testing environments.  It is for dumping deep context to be used for debugging the system.  

#### Trace
This level of logging should only be switched on in development and testing environments.  It is for logging tracing events that can be used to trace the exact execution path of the service. 

### Agent Feedback
The feedbacks submitted by agents will be added to a daily CSV file. The format of the filename will `agent_feedback_YYYYMMDD.csv`. The file will be stored in the `<Working Directory>/output/feedback` directory. The working directory at the moment is `/usr/src/mobile_application_service` inside the docker container. The file will have the following structure:
```
Time, Agent ID, Agent MSISDN, Agent Tier, Agent Name, Feedback
10:19:20,120,0820000014,eCabine,Agent,"This is a feedback message"
10:19:34,120,0820000014,eCabine,Agent,"This is another feedback message"
10:20:01,120,0820000014,eCabine,Agent,"This is a feedback message"
...
```

