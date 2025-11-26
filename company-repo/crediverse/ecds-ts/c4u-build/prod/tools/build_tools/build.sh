#!/bin/bash

# Command line arguments
# -i input
# -o output
# -d with debug symbols
# -h Help


# Error file for build and unit tests
# Drop database
mysql -u root -pussdgw -e "DROP DATABASE IF EXISTS hxc;"
mysql -u root -pussdgw -e "DROP DATABASE IF EXISTS ecdsap;"

#Check Java version
if [ -z "$JAVA_HOME" ]
then
	JAVA_VER=$(java -version 2>&1 | sed 's/java version "\(.*\)\.\(.*\)\..*"/\1\2/; 1q')
	JAVA_DETECTED=$(java -version 2>&1)
else
	JAVA_VER=$($JAVA_HOME/bin/java -version 2>&1 | sed 's/java version "\(.*\)\.\(.*\)\..*"/\1\2/; 1q')
	JAVA_DETECTED=$($JAVA_HOME/bin/java -version 2>&1)
fi

if [ -z "$ANT_HOME" ]
then
	ANT='ant -Dfile.encoding=UTF-8'
else
	ANT="$ANT_HOME/bin/ant -Dfile.encoding=UTF-8"
fi

if [[ "$JAVA_VER" -lt 17 ]]
then
	echo "Detected Java version: $JAVA_DETECTED"
	echo "Java version 1.7 or greater required for build"
	exit 1
fi

################################################################################
# Pass Command line arguments
################################################################################
BASH_SCRIPT=$0
CUR=`pwd`
CONFIG_FILE=""
OUTPUT_FILE=""
DEBUGGING_SYMBOLS=""

function printHelpExit {
  echo "Usage: $BASH_SCRIPT [OPTIONS]"
  echo "The following [OPTIONS] are available:"

  echo "-i --input   Input configuration file (Required)"
  echo "-o --output  Output file (Required)"
  echo "-d --debug   Build with debuggin symbols"
  echo "-h --help    Show this help"
  exit 1
}

# No command line arguments -> Print help and exit
if [[ $# -eq 0 ]] ; then
  printHelpExit
fi

# Pass command line arguments
while [[ $# -gt 0 ]]
do
  key="$1"
  value="$2"
  case $key in
    -h|--help)
    printHelpExit
    ;;
    -i|--input)
    CONFIG_FILE=$value
    shift
    ;;
    -o|--output)
    OUTPUT_FILE=$value
    shift
    ;;
    -d|--debug)
    DEBUGGING_SYMBOLS="true"
    ;;
    *)
    echo "Unknown argument $key"
    printHelpExit
    ;;
  esac
  shift
done

# Input and Ouput are minimum set of required parameters
if [[ -z "$CONFIG_FILE" || -z "$OUTPUT_FILE" ]]; then
  echo "Arguments missing"
  printHelpExit
fi

:> $OUTPUT_FILE

if [ ! -f $CONFIG_FILE ]; then
	echo "Cannot find the config file."  | tee -a $CUR/$OUTPUT_FILE
	exit 1
fi

# Build AntFileCreator
cd ../AntFileCreator
if ! $ANT 2>>$CUR/$OUTPUT_FILE ; then
	echo "AntFileCreator build file error. Please check the output file."
	exit 1
fi

# Build all outher ANT files (build.xml)
if [[ -n $DEBUGGING_SYMBOLS ]]; then
  # Build with debug symbols
	if [ -z "$JAVA_HOME" ]
	then
		command=( java -jar dist/*.jar --debug )
	else
		command=( $JAVA_HOME/bin/java -jar dist/*.jar --debug )
	fi
else
	# Build Normally
	if [ -z "$JAVA_HOME" ]
	then
		command=( java -jar dist/*.jar )
	else
		command=( $JAVA_HOME/bin/java -jar dist/*.jar )
	fi
fi

echo "Running (inside ${PWD}) ${command[@]}"
"${command[@]}"

cd -

NO=1

while read line
do
	if [ "${line:0:8}" == "basedir=" ]; then
		BASEDIR=${line#*=}
		if  ! cd $BASEDIR 2>/dev/null ; then
			echo "Base directory doesn't exist. Line number:$NO" | tee -a $CUR/$OUTPUT_FILE
			exit 1
		fi
		continue
	fi

	if [ "$TARGETDIR" == "" ] && [ "${line:0:10}" == "targetdir=" ]; then
		TARGETDIR=${line#*=}
		if ! cd ./$TARGETDIR 2>/dev/null ; then
			echo "Target directory doesn't exist. Line number:$NO" | tee -a $CUR/$OUTPUT_FILE
			echo "Target directory [relative]: $TARGETDIR [from path]: " `pwd`
			exit 1
		fi
	fi

	if [ "$TARGET" == "" ] && [ "${line:0:7}" == "target=" ]; then
		TARGET=${line#*=}
		if [ ! "$BASEDIR" == "" ]; then
			if [ ! "$TARGETDIR" == "" ]; then
				if ! ./$TARGET 2>>$CUR/$OUTPUT_FILE ; then
					echo "Target file doesn't exist. Line number:$NO" | tee -a $CUR/$OUTPUT_FILE
					echo "Running ant instead." | tee $CUR/$OUTPUT_FILE
#					ant | tee -a $OUTPUT_FILE

					echo "(cd \"${PWD}\" && ${ANT})"
					if ! $ANT 2>>$CUR/$OUTPUT_FILE ; then
						echo "Something is wrong with the build file." | tee -a $CUR/$OUTPUT_FILE
						exit 1
					fi
				fi
				cd - >/dev/null
				TARGETDIR=""
				TARGET=""
				continue
			fi
		fi
	fi

	if [ ! "$BASEDIR" == "" ]; then
		if [ ! "$TARGETDIR" == "" ]; then
			echo "$BASEDIR/$TARGETDIR"
#			ant | tee -a $OUTPUT_FILE
			echo "(cd \"${PWD}\" && ${ANT})"
			if ! $ANT 2>>$CUR/$OUTPUT_FILE ; then
				echo "Something is wrong with the build file." | tee -a $CUR/$OUTPUT_FILE
				exit 1
			fi
			cd - >/dev/null
			TARGETDIR=""
			TARGET=""
		fi
	fi

	if [ "${line:0:8}" == "testdir=" ]; then
		echo "Changing dir ${line#*=}"
		cd ${line#*=}

#		ant | tee -a $OUTPUT_FILE
		echo "(cd \"${PWD}\" && ${ANT})"
		if ! $ANT 2>>$CUR/$OUTPUT_FILE ; then
			echo "Something is wrong with the build file." | tee -a $CUR/$OUTPUT_FILE
			exit 1
		fi

		mysql -uroot -pussdgw -e "DROP DATABASE IF EXISTS hxc;"
		mysql -uroot -pussdgw -e "DROP DATABASE IF EXISTS ecdsap;"

		if [ -z "$JAVA_HOME" ]
		then
			command=( java -cp dist/* hxc.testsuite.RunAllTests $CUR/$OUTPUT_FILE  )
		else
			command=( $JAVA_HOME/bin/java -cp dist/* hxc.testsuite.RunAllTests $CUR/$OUTPUT_FILE )
		fi
		echo "Running: ( cd \"${PWD}\" && ${command[@]} )"
		"${command[@]}" || { echo "FAILED WITH EXIT CODE \"${?}\" inside \"${PWD}\" WHEN RUNNING ${command[@]}" | tee -a "$CUR/$OUTPUT_FILE"; }
		cd - >/dev/null
	fi

	let NO=$NO+1

done < $CONFIG_FILE

#Check for posible error
if [[ -s $CUR/$OUTPUT_FILE ]] ; then
          echo "Posible build error, check $OUPUT_FILE"
          exit 1
fi

echo "Exiting Successfully"
exit 0
