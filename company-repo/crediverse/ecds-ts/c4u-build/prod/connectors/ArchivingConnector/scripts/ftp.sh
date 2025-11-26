#!/bin/bash
HOST=$1
USER='bgw' 
PASSWD='thule' 
FILE=`basename $2`
PATH=`dirname $2`
FTP_PATH=$3

/bin/ftp -n $HOST << END_SCRIPT
quote USER $USER
quote PASS $PASSWD
cd $FTP_PATH
lcd $PATH
put $FILE
quit
END_SCRIPT
exit 0
