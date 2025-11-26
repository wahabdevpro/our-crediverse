#!/usr/bin/env python2
# vim: set ft=python sts=4 ts=4 sw=4 noexpandtab fo-=t:

import logging
import sys
import argparse
import datetime
import json
import glob
import os
import re
import copy
import subprocess
import threading
import hashlib


class LogPipe(threading.Thread):

	def __init__(self, level):
		threading.Thread.__init__(self)
		self.daemon = False
		self.level = level
		(self.fd_read, self.fd_write) = os.pipe()
		self.pipe_reader = os.fdopen(self.fd_read)
		self.start()

	def fileno(self):
		return self.fd_write

	def run(self):
		for line in iter(self.pipe_reader.readline, ''):
			logging.log(self.level, line.strip('\n'))
		self.pipe_reader.close()

	def close(self):
		os.close(self.fd_write)

class SqlFile( object ):
	basename_regex = re.compile("(upgrade|downgrade)_ecds_(oltp|olap)_([0-9]+)_to_([0-9]+).sql");

	def __init__( self, **kwargs ):
		self.path = kwargs["path"];
		self.path_dirname = os.path.dirname(self.path);
		self.path_basename = os.path.basename(self.path);
		matcher = SqlFile.basename_regex.match( self.path_basename )
		if matcher:
			self.direction = matcher.group(1)
			self.db_type = matcher.group(2)
			if self.direction == "upgrade":
				self.old_version = int(matcher.group(3))
				self.new_version = int(matcher.group(4))
				self.initial_version = self.old_version
				self.final_version = self.new_version
			else:
				self.old_version = int(matcher.group(4))
				self.new_version = int(matcher.group(3))
				self.initial_version = -self.new_version
				self.final_version = -self.old_version
			self.distance = self.new_version - self.old_version
		else:
			raise ValueError("invalid filename {}".format(self.path))

	def __repr__( self ):
		return "SqlFile( direction = {}, db_type = {}, old_version = {}, new_version = {}, distance = {}, initial_version = {}, final_version = {})".format( self.direction, self.db_type, self.old_version, self.new_version, self.distance, self.initial_version, self.final_version )

def main():
	logging.basicConfig(level=logging.INFO, datefmt='%Y-%m-%dT%H:%M:%S', stream=sys.stderr, format="%(asctime)s %(levelno)03d:%(levelname)-8s %(name)-12s %(module)s:%(lineno)s:%(funcName)s %(message)s")

	argument_parser = argparse.ArgumentParser(add_help = False)
	argument_parser.add_argument("-v", "--verbose", action="count", dest="verbosity", help="increase verbosity level")
	argument_parser.add_argument("-h", "--help", action="help", help="shows this help message and exit")
	argument_parser.add_argument("-d", "--sql-dir", action="store", dest="sql_dir", type=str, default="/var/opt/cs/c4u/share/sql/", help="where sql files are located")
	#argument_parser.add_argument("-p", "--pretend", action="store_true", dest="pretend", default=False, help="pretend mode")
	#argument_parser.add_argument("-t", "--db-type", action="store", dest="db_type", type=str, required="true", help="db type (oltp|olap)")
	argument_parser.add_argument("-l", "--log-file", action="store", dest="log_file", type=str, help="log file to write to", default="/var/opt/cs/c4u/log/dbtool.log")

	subparsers = argument_parser.add_subparsers(dest="subparser_name")
	if True:
		subparser = subparsers.add_parser("list")
		subparser.add_argument("-d", "--direction", action="store", dest="direction", type=str, required="true", help="direction (upgrade|downgrade)")
		subparser.add_argument("-o", "--old-version", action="store", dest="old_version", type=int, help="old version")
		subparser.add_argument("-n", "--new-version", action="store", dest="new_version", type=int, help="new version")
		subparser.add_argument("-t", "--db-type", action="store", dest="db_type", type=str, required="true", help="db type (oltp|olap)")
	if True:
		subparser = subparsers.add_parser("apply")
		subparser.add_argument("-d", "--direction", action="store", dest="direction", type=str, required="true", help="direction (upgrade|downgrade)")
		subparser.add_argument("-o", "--old-version", action="store", dest="old_version", type=int, help="old version")
		subparser.add_argument("-n", "--new-version", action="store", dest="new_version", type=int, help="new version")
		subparser.add_argument("-mh", "--mysql-host", action="store", dest="mysql_host", type=str, help="mysql hostname", required=True)
		subparser.add_argument("-mp", "--mysql-port", action="store", dest="mysql_port", type=int, help="mysql port", required=True)
		subparser.add_argument("-md", "--mysql-database", action="store", dest="mysql_database", type=str, help="mysql database", required=True)
		subparser.add_argument("-mU", "--mysql-username", action="store", dest="mysql_username", type=str, help="mysql username", required=True)
		subparser.add_argument("-mP", "--mysql-password", action="store", dest="mysql_password", type=str, help="mysql password", required=True)
		subparser.add_argument("-p", "--pretend", action="store_true", dest="pretend", default=False, help="pretend mode")
		subparser.add_argument("-t", "--db-type", action="store", dest="db_type", type=str, required="true", help="db type (oltp|olap)")

	arguments = argument_parser.parse_args( args = sys.argv[1:] )

	global verbosity
	verbosity = arguments.verbosity
	
	#logging.error("( 'log_file' in arguments ) = %s", ( 'log_file' in arguments ));
	#if ( 'log_file' in arguments ):
	#	logging.error("( arguments.log_file is not None ) = %s", ( arguments.log_file is not None ));

	#if ( 'log_file' in arguments ) and ( arguments.log_file is not None ):
		#logging.error("( 'log_file' in arguments ) = %s", ( 'log_file' in arguments ));
		#logging.error("( arguments.log_file is not None ) = %s", ( arguments.log_file is not None ));
		#logging.basicConfig(level=logging.ERROR, datefmt='%Y-%m-%dT%H:%M:%S', filename=arguments.log_file, format="%(asctime)s %(levelno)03d:%(levelname)-8s %(name)-12s %(module)s:%(lineno)s:%(funcName)s %(message)s")
	if True:
		file_handler = logging.FileHandler( arguments.log_file )
		file_handler.setFormatter( logging.Formatter( fmt="%(asctime)s %(levelno)03d:%(levelname)-8s %(name)-12s %(module)s:%(lineno)s:%(funcName)s %(message)s", datefmt="%Y-%m-%dT%H:%M:%S" ) )
		file_handler.setLevel(logging.INFO);

		root_logger = logging.getLogger("")
		old_handlers = copy.copy( root_logger.handlers )
		root_logger.addHandler( file_handler )
		#for handler in old_handlers:
		#	root_logger.removeHandler(handler)

	logging.info("STARTING %s", sys.argv)
	
	if arguments.verbosity is not None:
		root_logger = logging.getLogger("")
		root_logger.setLevel( root_logger.getEffectiveLevel() - (arguments.verbosity)*10 )

	if arguments.db_type not in ( "oltp", "olap" ):
		logging.error("invalid db type %s", arguments.db_type);
		argument_parser.print_help()
		sys.exit(1)

	if ( 'direction' in arguments ) and ( arguments.direction not in ( "upgrade", "downgrade" ) ):
		logging.error("invalid direction %s must be 'upgrade' or 'downgrade'", arguments.direction);
		argument_parser.print_help()
		sys.exit(1)

	logging.debug("arguments = %s", arguments)
	logging.debug("logging.level = %s", logging.getLogger("").getEffectiveLevel())

	if not os.path.exists( arguments.sql_dir ):
		logging.error("sql directory %s does not exist", arguments.sql_dir)
		sys.exit(1)

	files = glob.glob( "{}/{}_ecds_{}_*_to_*.sql".format( arguments.sql_dir, arguments.direction, arguments.db_type ) ) 
	logging.debug("files = %s", files)
	file_objects = []
	for fil in files:
		sql_file = SqlFile(path=fil)
		if sql_file.direction != arguments.direction:
			raise ValueError("invalid filename {}".format(path))
		if sql_file.old_version < arguments.old_version:
			logging.debug("ignoring %s as sql_file.old_version(%s) < arguments.old_version(%s)", sql_file, sql_file.old_version, arguments.old_version);
			continue
		file_objects.append(sql_file)

	reverse=( False if arguments.direction == "upgrade" else True )
	logging.debug("reverse = %s", reverse);

	file_objects.sort(key = lambda obj: (obj.initial_version, -obj.distance))
	logging.debug("file_objects = %s", file_objects)

	previous_sql_file = None
	optimized_file_objects = []
	for sql_file in file_objects:
		logging.debug("checking %s", sql_file)
		if previous_sql_file is None:
			optimized_file_objects.append(sql_file)
			previous_sql_file = sql_file
		else:
			if sql_file.final_version <= previous_sql_file.final_version:
				logging.debug("ingoring %s as sql_file.final_version(%s) < previous_sql_file.final_version(%s)", sql_file, sql_file.final_version, previous_sql_file.final_version)
				continue
			elif sql_file.initial_version == previous_sql_file.initial_version:
				logging.debug("ingoring %s as sql_file.initial_version(%s) >= previous_sql_file.initial_version(%s)", sql_file, sql_file.initial_version, previous_sql_file.initial_version)
				continue
			else:
				optimized_file_objects.append(sql_file)
				previous_sql_file = sql_file

	logging.debug("optimized_file_objects = %s", optimized_file_objects)

	if arguments.subparser_name == "list":
		for sql_file in optimized_file_objects:
			sys.stderr.write("{}\n".format(sql_file.path));
	elif arguments.subparser_name == "apply":
		
		for sql_file in optimized_file_objects: 
			sql_file_data = None
			with open(sql_file.path, 'rb') as sql_file_handle:
				sql_file_data=sql_file_handle.read()
			#logging.debug("sql_file_data=%s", sql_file_data)
			logging.info("applying %s [ %s ]", sql_file.path, hashlib.md5(sql_file_data).hexdigest())
			mysql_client_command = [ "/usr/bin/env", "mysql", "--host", arguments.mysql_host, "--port", "{}".format(arguments.mysql_port), "-u{}".format( arguments.mysql_username ), "-p{}".format( arguments.mysql_password ), arguments.mysql_database ]
			logging.debug("mysql_client_command = %s", mysql_client_command)
			if not arguments.pretend:
				log_pipe = LogPipe(logging.INFO)
				try:
						mysql_process = subprocess.Popen(mysql_client_command, stdin=subprocess.PIPE, stdout=log_pipe, stderr=log_pipe)
						with mysql_process.stdin as mysql_process_stdin:
							mysql_process_stdin.write(sql_file_data)
						mysql_process_returncode = mysql_process.wait()
				finally:
					log_pipe.close()
					log_pipe.join()
				logging.info("mysql_process_returncode=%s", mysql_process_returncode)
			else:
				logging.info("not running mysql client as pretend mode is active")

if __name__ == "__main__":
	main()
