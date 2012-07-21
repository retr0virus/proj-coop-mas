#!/bin/bash
COMPCP=.:src:lib/mason.16.jar
OUTPUT=bin
BASEDIR="src/"
#FILES="sim/util/Properties"
FILES=""
DIRECTORIES="sim/util/ sim/app/snr/ sim/app/snr/agent/ sim/app/snr/message/ sim/app/snr/util/"

OK=0

for i in $FILES
do
	if [[ ${OK} -eq 0 ]]
	then
		echo "Compiling ${BASEDIR}${i}.java"
    	javac -d ${OUTPUT} -cp ${COMPCP} ${BASEDIR}${i}.java
		OK=$?
	else
		echo "Skipping ${BASEDIR}${i}.java"
	fi
done

for i in $DIRECTORIES
do
	if [[ ${OK} -eq 0 ]]
	then
		echo "Compiling ${BASEDIR}${i}*.java"
    	javac -d ${OUTPUT} -cp ${COMPCP} ${BASEDIR}${i}*.java
		OK=$?
	else
		echo "Skipping ${BASEDIR}${i}*.java"
	fi
done

exit ${OK}

if [[ ${OK} -eq 0 ]]
then
	echo "Running..."
	./run.sh
fi
