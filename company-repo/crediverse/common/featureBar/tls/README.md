# TLS Files and how to create them


##  Certificate Authority Private Key

*Filename:* `concurrent_ca_key.pem` 

*command:* ```openssl genrsa -out concurrent_ca_key.pem```

To be kept secret.

## Certificate Authority Certificate

*Filename:*`concurrent_ca.crt`     

*command:* ```openssl req -new -x509 -key concurrent_ca_key.pem -sha256 -days 35600 -subj '/CN=featurebar/O=Concurrent/C=ZA' -out concurrent_ca.crt```

Root authority to be shiped with client and server

## Server Private Key

*Filename:*`server.key`            

*command:* ```openssl genrsa -out server.key```

shipped with server 

## Server Certificate Request

*Filename:*`server_reqout.txt`

*command:* ```openssl req -new -key server.key  -subj '/CN=featurebar/O=Concurrent/C=ZA' -out server_reqout.txt```

Used in the next step

## Signed Server Certificate

*Filename:*`server.crt`

*command:* ```openssl x509 -req -in server_reqout.txt -days 3650 -sha256 -CAcreateserial -CA concurrent_ca.crt -CAkey concurrent_ca_key.pem -extfile localhost.ext -out server.crt```


 

Shipped with server

## Client Private Key

*Filename:*`client.key`

*command:* ```openssl genrsa -out client.key```

Shipped with clients.

## Client Private Key for Java because Java is special.
*Filename:* `client.key.pem`

*command:* ```openssl pkcs8 -topk8 -nocrypt -in client.key -out client.key.pem```

Shipped with Java clients.

##  Client Certificate Request           

*Filename:*`client_reqout.txt`

*command:* ```openssl req -new -key client.key -subj '/CN=featurebar/O=Concurrent/C=ZA' -out client_reqout.txt```

Used in the next step to create the client certificate.

## Client Certificate                  

*Filename:*`client.crt`

*command:* ```openssl x509 -req -in client_reqout.txt -days 3650 -sha256 -CAcreateserial -CA concurrent_ca.crt -CAkey concurrent_ca_key.pem -out client.crt```

Shipped with Client 

## Certificate Authority Serial Number

*Filename:*`concurrent_ca.srl`

Created and used by above commands
Kept with CA Private Key


