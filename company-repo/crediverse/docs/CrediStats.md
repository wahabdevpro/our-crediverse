# CrediStats
## Introduction
This document explains the implementation details of a simple web service that allows a customer to view the total daily sales for the current and previous days. 
The service will not use any kind of authentication and anyone will be able to access it.

## Overview
The web service will comprise an appealing front-end that displays a dashboard showcasing the daily sales figures for both the current and previous days. The back-end will function as a REST API service, providing a single API endpoint for GET requests. When this API is called, the back-end service will initiate a query to the Crediverse Query Slave in order to retrieve the total sales amount for the current and previous days. These numbers will then be sent back to the front-end.

## Technology Stacks
Vue will be utilized for designing the front-end, while NodeJS will be employed for developing the back-end.

## Implementation Details
**Ports**: The default port for the back-end API service will be `8801` but this will be configurable.

**API endpoint**: A GET request on the `credistats/daily_sales` endpoint will be used to fetch the total gross retail sales for the current and previous days. The total gross retail sales value includes successful airtime sales (including self top-ups) and successful bundles sales. A bundle sale transaction where the agent is charged, and then refunded as the bundle provision failed, is not a successful bundle sale - thus not included in the total. 

The response returned by the endpoint will be of the following format:
```
{
	today_sales: <Sales_Amount>
	yesterday_sales: <Sales_Amount>
}
```

**Refresh Rate**: The front-end will have a refresh rate of 30 seconds to get fresh data.

**Deployment**: The front-end and back-end services will be deployed as part of the Crediverse deployment in their dedicated docker containers.

**Configuration**: The back-end service will require the following parameters in a `.env` file:
```
PORT
DB_HOST
DB_PORT
DB_USER
DB_PASSWORD
DB_NAME
```
These parameters will be passed to the docker container in the form of environment variables which will then be replaced into the `.env` file for the back-end service application.
