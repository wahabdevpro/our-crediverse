ECDS API Getting Started Guide
==============================

What is the ECDS API
--------------------

The ECDS API is the interface with which a 3rd party system (yours) can
integrate with ECDS to transact on the credit distribution network.
Through the ECDS API you will be able to authenticate to your Agent
account and execute transactions such as transfers, air time sales,
balance enquiries, etc.

Technical Overview
------------------

The goal of the ECDS REST API is to make it as easy as possible for API
consumers to integrate with the ECDS system. The API was designed and
built to use standardized API frameworks and to avoid using non-standard
practices. This makes it straight-forward for developers to integrate
ECDS with their own systems. The API has been designed upon the REST API
methodology with authentication achieved through means of the OAUTH2
framework; both of which are well documented and a myriad of frameworks
exist for just about all popular technologies/languages. The API is
documented using the OpenAPI/Swagger specification which describes the
API in an intuitive way while providing an interactive interface for the
API user/developer to experiment and generate code stubs. The Swagger
Editor and information regarding Swagger, may be obtained here:
<https://swagger.io/>

Authentication
--------------

The OAUTH2 authentication implementation used is standard and it is
therefore straight-forward to implement a client. The OAUTH2 password
grant authentication pattern has been used to authenticate API users.
The password grant pattern is fairly simple in that the user’s
credentials (username and password) are traded for an access token which
can be used to gain access to the ECDS system’s resources. A client\_id
and client\_secret is additionally required to authenticate your client.
Contact Concurrent Systems directly to obtain a client\_id and
client\_secret.

The following bash command will send: `username=myagent`;
`password=P@ssw0rd`; `client_id=myclient`;
`client_secret=myclientsecret` to the ECDS OAuth2 authentication server
located at `ecds-server-address` to obtain an access\_token.

    access_token=$(curl myclient:myclientsecret@ecds-server-address/oauth/token -d grant_type=password -d username=myagent -d password='P@ssw0rd' 2>/dev/null | python -c "import sys, json; print json.load(sys.stdin)['access_token']")

REST Interface
--------------

All operations are executed via the REST interface over HTTPS. Please
refer to the Swagger API document for details of the available
operations. The access\_token obtained from authentication is sent with
every request in the HTTP header `Authorization: Bearer` Example: to
obtain your account details:

    curl -X GET -H "Authorization: Bearer ${access_token}" http://localhost:9084/api/account | xargs -I{} echo {}
