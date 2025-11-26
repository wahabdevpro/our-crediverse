#!/bin/bash

# Generate SOAP Air Client
echo "http://c4u:c4u@localhost:14100/HxC?wsdl" > auth
wsimport -keep -verbose -clientjar soapservices.jar -Xauthfile auth http://localhost:14100/HxC?wsdl
#jar uf soapservices.jar com
#jar uf soapservices.jar hxc
rm -rf com
rm -rf hxc
rm auth
cp soapservices.jar ../lib/soapservices.jar

# Generate SOAP HxC Client
wsimport -keep -verbose -clientjar airsimsoapclient.jar http://localhost:10012/Air?wsdl
#jar uf airsimsoapclient.jar hxc
rm -rf hxc
cp airsimsoapclient.jar ../lib/airsimsoapclient.jar