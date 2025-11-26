

GET_SESSION_ID_RESPONSE=`curl \
-H "Accept: application/json, application/*+json" \
-H "Connection: keep-alive" \
-H "Content-Type: application/json" \
-H "Host: localhost:14400" \
-H "User-Agent: Apache-HttpClient/4.5.13 (Java/11.0.17)" \
-H "Accept-Encoding: gzip,deflate" \
-d '{"sessionID":null,"inboundTransactionID":null,"inboundSessionID":null,"version":"1","mode":"N","companyID":2,"channel":"W","hostName":null,"macAddress":null,"ipAddress":"0:0:0:0:0:0:0:1","data":null,"username":null,"password":null,"oneTimePin":null,"userType":"AGENT","coSignForSessionID":null,"coSignatoryTransactionID":null,"customPinChangeMessage":null}' \
http://localhost:14400/ecds/authentication/authenticate`

SESSION_ID=`echo -n $GET_SESSION_ID_RESPONSE | jq '.sessionID'  | sed 's/"//g'`

echo $SESSION_ID 

BASE64_MSISDN=`echo -n '0820000015' | base64`
echo 
echo BASE64_MSISDN: $BASE64_MSISDN
echo 

GET_KEY_REQUEST="{\"sessionID\":\"$SESSION_ID\",\"inboundTransactionID\":null,\"inboundSessionID\":null,\"version\":\"1\",\"mode\":\"N\",\"companyID\":2,\"channel\":\"W\",\"hostName\":null,\"macAddress\":null,\"ipAddress\":\"0:0:0:0:0:0:0:1\",\"data\":\"$BASE64_MSISDN\",\"username\":null,\"password\":null,\"oneTimePin\":null,\"userType\":\"AGENT\",\"coSignForSessionID\":null,\"coSignatoryTransactionID\":null,\"customPinChangeMessage\":null}" 

echo
echo GET_KEY_REQUEST: $GET_KEY_REQUEST
echo

GET_KEY_RESPONSE=`curl \
-H "Accept: application/json, application/*+json" \
-H "CS_SID: $SESSION_ID" \
-H "Connection: keep-alive" \
-H "Content-Type: application/json" \
-H "Host: localhost:14400" \
-H "User-Agent: Apache-HttpClient/4.5.13 (Java/11.0.17)" \
-H "Accept-Encoding: gzip,deflate" \
-d $GET_KEY_REQUEST \
http://localhost:14400/ecds/authentication/authenticate`

echo
echo GET_KEY_RESPONSE: $GET_KEY_RESPONSE
echo



KEY=`echo -n $GET_KEY_RESPONSE | jq '.key1' | sed 's/"//g'` 

echo $KEY
echo -----BEGIN RSA PUBLIC KEY----- > key.key
echo $KEY >> key.key
echo -----END RSA PUBLIC KEY----- >> key.key


ENCRYPTED_PIN=`echo -n '00000' | openssl pkeyutl -encrypt -pubin -inkey ./key.key | base64`
rm key.key


echo 
echo ENCRYPTED_PIN: $ENCRYPTED_PIN
echo 

OTP_REQUEST="{\"sessionID\":\"$SESSION_ID\",\"inboundTransactionID\":null,\"inboundSessionID\":null,\"version\":\"1\",\"mode\":\"N\",\"companyID\":2,\"channel\":\"W\",\"hostName\":null,\"macAddress\":null,\"ipAddress\":\"0:0:0:0:0:0:0:1\",\"data\":\"$ENCRYPTED_PIN\",\"username\":null,\"password\":null,\"oneTimePin\":null,\"userType\":\"AGENT\",\"coSignForSessionID\":null,\"coSignatoryTransactionID\":null,\"customPinChangeMessage\":null}" 

echo 
echo OTP_REQUEST: $OTP_REQUEST
echo 


OTP_RESPONSE=`curl \
-H "Accept: application/json, application/*+json" \
-H "CS_SID: " \
-H "Connection: keep-alive" \
-H "Content-Type: application/json" \
-H "Host: localhost:14400" \
-H "User-Agent: Apache-HttpClient/4.5.13 (Java/11.0.17)" \
-H "Accept-Encoding: gzip,deflate" \
-d "$OTP_REQUEST"  \
http://localhost:14400/ecds/authentication/authenticate`

echo 
echo OTP_RESPONSE 
echo $OTP_RESPONSE
echo 

echo please get the otp from the server log and type it here: 
read ONE_TIME_PIN_RAW

echo 
echo ONE_TIME_PIN_RAW
echo $ONE_TIME_PIN_RAW
echo

ONE_TIME_PIN=${ONE_TIME_PIN_RAW//[$'\t\r\n ']}

echo 
echo ONE_TIME_PIN
echo $ONE_TIME_PIN
echo


BASE64_OTP=`echo -n $ONE_TIME_PIN|base64`

echo BASE64_OTP
echo $BASE64_OTP 


AUTHENTICATION_REQUEST="{\"sessionID\":\"$SESSION_ID\",\"inboundTransactionID\":null,\"inboundSessionID\":null,\"version\":\"1\",\"mode\":\"N\",\"companyID\":2,\"channel\":\"W\",\"hostName\":null,\"macAddress\":null,\"ipAddress\":\"0:0:0:0:0:0:0:1\",\"data\":\"$BASE64_OTP\",\"username\":null,\"password\":null,\"oneTimePin\":null,\"userType\":\"AGENT\",\"coSignForSessionID\":null,\"coSignatoryTransactionID\":null,\"customPinChangeMessage\":null}"

echo 
echo AUTHENTICATION_REQUEST:  
echo $AUTHENTICATION_REQUEST
echo 

AUTHENTICATION_RESPONSE=`curl \
-H "Accept: application/json, application/*+json" \
-H "CS_SID: 1c23206926eb4aff8bb14abb2c908cd4" \
-H "Connection: keep-alive" \
-H "Content-Type: application/json" \
-H "Host: localhost:14400" \
-H "User-Agent: Apache-HttpClient/4.5.13 (Java/11.0.17)" \
-H "Accept-Encoding: gzip,deflate" \
-d $AUTHENTICATION_REQUEST \
http://localhost:14400/ecds/authentication/authenticate`

echo 
echo AUTHENTICATION_RESPONSE:
echo $AUTHENTICATION_RESPONSE 
echo 






echo $otp





