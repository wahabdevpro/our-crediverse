# Creation steps


## Certificate authority 
### Create CA Private Key
`openssl genrsa -aes256 -out ca.private.key 4096`

### Create CA Root Certificate
`openssl req -new -x509 -sha256 -days 36500 -key ca.private.key -out ca.root.certificate`

## Server 
### Server Private Key
`openssl genrsa -out server.private.key 4096`

### Server signing request 
`openssl req -new -sha256 -subj "/CN=Crediverse-Mobile" -key server.private.key -out server.csr`

### Adding the alternative server names
`subjectAltName=DNS:SmartApp.test.app.moov-africa.ci,DNS:SmartApp.prod.app.moov-africa.ci`
`echo "subjectAltName=DNS:SmartApp.prod.app.moov-africa.ci" >> extfile.cnf`
`echo "subjectAltName=DNS:SmartApp.test.app.moov-africa.ci,DNS:SmartApp.prod.app.moov-africa.ci" > extfile.cnf`


### CA signed server certificate
`openssl x509 -req -sha256 -days 365 -in server.csr -CA ca.root.certificate -CAkey ca.private.key -out server.certificate -extfile extfile.cnf -CAcreateserial`

