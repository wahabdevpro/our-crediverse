openssl genrsa -out client.key 4096

openssl req -new -key client.key.pem -out client.csr

echo "basicConstraints = CA:FALSE\n" \
	"nsCertType = client, email\n" \
	"nsComment = \"OpenSSL Generated Client Certificate\"\n" \
	"subjectKeyIdentifier = hash\n" \
	"authorityKeyIdentifier = keyid,issuer\n" \
	"keyUsage = critical, nonRepudiation, digitalSignature, keyEncipherment\n" \
	"extendedKeyUsage = clientAuth, emailProtection\n"\ 
>client_cert_ext.cnf

## 4: Sign the certificate

openssl x509 -req -in client.csr -passin file:mypass.enc -CA /root/tls/intermediate/certs/ca-chain-bundle.cert.pem -CAkey /root/tls/intermediate/private/intermediate.cakey.pem -out client.cert.pem -CAcreateserial -days 365 -sha256 -extfile client_cert_ext.cnf
