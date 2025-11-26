#!/usr/bin/python3
import sys
import argparse
import os
import re
import time
from datetime import datetime
import datetime
import csv
import fileinput

def extractInt(length, pdu):
	num = pdu[0:length]
	return int(num, 16), pdu[length:]

def extractVar(pdu):
	string = ''
	while True:
		if len(pdu) < 2:
			break;
		num = int(pdu[0:2], 16)
		pdu = pdu[2:]
		if num == 0:
			break
		string = string + chr(num)
	return string, pdu

def extractBody(esm, length, pdu):
	string = ''
	if esm == 0x40:
		udhLen, pdu = extractInt(2, pdu)
		if udhLen:
			length = length - 1 - udhLen
		pdu = pdu[udhLen*2:]
	for i in range(0, length):
		if len(pdu) < 2:
			break;
		num = int(pdu[0:2], 16)
		pdu = pdu[2:]
		string = string + chr(num)
	return string, pdu

def processLog(fn):
	csvw = csv.writer(sys.stdout)
	for line in fileinput.input( fn ):
		elements = line.split( ' | ', 5 )
		if len(elements) > 5:
			sTime = elements[0].strip()
			sTid = elements[2].strip()
			sComponent = elements[3].strip()
			sThread = elements[4].strip()
			sLog = elements[5].strip()

			if sComponent != 'h.c.smpp.session.SmppSession':
				continue;

			match = re.search( "^grizzly-worker-thread-\((\d+)\)$", sThread )
			if match:
				threadId = match.group(1)
			else:
				continue;

			# 20210704T000000.356
			dtParts = sTime.split( '.' )
			dtMs = None
			dtObj = None
			if len(dtParts) == 2:
				dtObj = datetime.datetime.strptime( dtParts[0], '%Y%m%dT%H%M%S' )
				dtMs = int(dtParts[1])

			#print( sLog )
			match = re.search( "^write bytes: \[(\w+)\]$", sLog )
			if match:
				pduStr = match.group(1)

				pduLen, pduStr = extractInt(8, pduStr)
				pduCommand, pduStr = extractInt(8, pduStr)
				pduStatus, pduStr = extractInt(8, pduStr)
				pduSeqNo, pduStr = extractInt(8, pduStr)
				
				pduServiceType, pduStr = extractVar(pduStr)
				pduSrcTon, pduStr = extractInt(2, pduStr)
				pduSrcNpi, pduStr = extractInt(2, pduStr)
				pduSrc, pduStr = extractVar(pduStr)
				
				pduDestTon, pduStr = extractInt(2, pduStr)
				pduDestNpi, pduStr = extractInt(2, pduStr)
				pduDest, pduStr = extractVar(pduStr)

				pduEsmClass, pduStr = extractInt(2, pduStr)
				pduProtocolId, pduStr = extractInt(2, pduStr)
				pduPriority, pduStr = extractInt(2, pduStr)
				pduScheduleDeliveryTime, pduStr = extractVar(pduStr)
				pduValidityPeriod, pduStr = extractVar(pduStr)
				pduRegisteredDelivery, pduStr = extractInt(2, pduStr)
				pduReplaceIfPresent, pduStr = extractInt(2, pduStr)
				pduDataCoding, pduStr = extractInt(2, pduStr)
				pduSmDefaultMsgId, pduStr = extractInt(2, pduStr)
				pduSmLength, pduStr = extractInt(2, pduStr)
				
				pduMsg, pduStr = extractBody(pduEsmClass, int(pduSmLength), pduStr)

				csvw.writerow([sTime, sTid, pduSrc, pduDest, pduMsg])

if __name__ == '__main__':
    try:
        parser = argparse.ArgumentParser()
        parser.add_argument('log', nargs='?', help='Crediverse log file')

        args = parser.parse_args()

        processLog( args.log )

    except KeyboardInterrupt:
        print( "\n\nTerminated via interrupt\n" )
        pass;

