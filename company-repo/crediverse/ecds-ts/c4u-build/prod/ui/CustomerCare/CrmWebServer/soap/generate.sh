#!/bin/bash
echo "http://c4u:c4u@localhost:14100/HxC?wsdl" > auth
rm -rf com
rm -rf hxc
wsimport -keep -verbose -clientjar soapservices.jar -Xauthfile auth http://localhost:14100/HxC?wsdl
rm auth
cp soapservices.jar ../lib/soapservices.jar
