#!/bin/bash
for soaptest in `cat soaptestcases`
do
ssh c4u "/etc/init.d/supervisor stop" > /dev/null
ssh c4u "var/opt/cs/c4u/installations/attach_vip.sh" > /dev/null
ssh c4u "/etc/init.d/supervisor start" > /dev/null
sleep 5
cd ~/SmartBear/SoapUI-Pro-4.6.4-m-SNAPSHOT/bin ; ./testrunner.sh -s"HxCPortBinding TestSuite" -c"$soaptest" -r -a -j -g -f/home/louiseg/c4u/soap-tests/$soaptest/ -FPDF -R"TestCase Report" -M -S -EDefault /home/louiseg/C4U-soapui-project.xml > /dev/null
done
