# Steps to create a signed client certificate and key

## 1: Create the client key

```openssl genrsa -out client.key 4096```

## 2: Create a client cert to be signed by above key

```openssl req -new -key client.key.pem -out client.csr```

## 3: Create conf file

create client_cert_ext.conf with: 


```
basicConstraints = CA:FALSE
nsCertType = client, email
nsComment = "OpenSSL Generated Client Certificate"
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer
keyUsage = critical, nonRepudiation, digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth, emailProtection
```


## 4: Sign the certificate

```
openssl x509 -req -in client.csr -passin file:mypass.enc -CA /root/tls/intermediate/certs/ca-chain-bundle.cert.pem -CAkey /root/tls/intermediate/private/intermediate.cakey.pem -out client.cert.pem -CAcreateserial -days 365 -sha256 -extfile client_cert_ext.cnf
```


# Steps to create a self signed certificate.

## 1: Create the CA(Certificate Authority) private key.

Since we want to sign our own certificates we are the CA.  This is the CA root key.
``` 
openssl genrsa -aes256 -out ca.private.key 4096 
```

## 2: Create the CA certificate

```
openssl req -new -x509 -sha256 -days 36500 -key ca.private.key -out ca.root.certificate
```

`ca.root.certificate` Should be deployed with clients of the server so that they can validate that they are connecting with the correct server and not an imposter. 

## 3: Create the CrediVault certificate

```
openssl genrsa -out server.private.key 4096
```

`server.private.key` should be deployed with the server in a safe way.  Whomever has access to this file has the ability to set up a spoof server. 

## 4: Create the signing request

```
openssl req -new -sha256 -subj "/CN=Crediverse-Mobile" -key server.private.key -out server.csr
```

## 5: Create the alternative names for the signing request 

This is the name of the Crediverse MAS server.  It will only wokr if this name is registered in the local HOSTS file or available through DNS.
```
echo "subjectAltName=DNS:demo.gcp.concurrent.systems" >> extfile.cnf
```

## 6: Create the certificate

```
openssl x509 -req -sha256 -days 365 -in server.csr -CA ca.root.certificate -CAkey ca.private.key -out server.certificate -extfile extfile.cnf -CAcreateserial
```
`server.certificate` is now the credivault server certificate, it should be deployed with the server.











