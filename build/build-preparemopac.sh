#!/bin/bash

# Building PrepareMOPAC
if [ ! -f lib/DENOPTIM.jar ]; then
	echo "Failed to create PrepareMOPAC.jar. Cannot locate DENOPTIM.jar in lib"
    exit -1
fi

find ../src/misc/PrepareMOPAC/src/ -name *.java > javafiles.txt
javac -cp lib/cdk-1.4.19.jar:lib/DENOPTIM.jar @javafiles.txt -encoding utf-8 -d .

if [ "$?" != "0" ]; then
    rm javafiles.txt
	echo "Failed to create PrepareMOPAC.jar."
    exit -1
fi

rm javafiles.txt



echo "Manifest-Version: 1.0" > manifest.mf
echo "Main-Class: preparemopac.PrepareMOPAC" >> manifest.mf
echo "Class-Path: lib/cdk-1.4.19.jar lib/DENOPTIM.jar" >> manifest.mf
echo >> manifest.mf

jar cvfm PrepareMOPAC.jar manifest.mf preparemopac 

if [ "$?" = "0" ]; then
    rm -rf manifest.mf preparemopac
else
	echo "Failed to create PrepareMOPAC.jar."
    exit -1
fi

echo "--------------------- Done building PrepareMOPAC.jar ---------------------"