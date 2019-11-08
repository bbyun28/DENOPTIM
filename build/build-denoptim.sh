#!/bin/bash

# build script for DENOPTIM
if [ -d ../lib ]; then
    if [ -d lib ]; then
        cp -r ../lib/*.jar lib/
    else
        cp -r ../lib .        
    fi 
fi



find ../src/DENOPTIM/src/ -name *.java > javafiles.txt
javac -cp lib/cdk-1.4.19.jar:lib/commons-io-2.4.jar:lib/commons-math3-3.6.1.jar @javafiles.txt -encoding utf-8 -d .

if [ "$?" != "0" ]; then
    rm javafiles.txt
	echo "Failed to create DENOPTIM.jar."
    exit -1
fi

rm javafiles.txt

echo "Manifest-Version: 1.0" > manifest.mf
echo "Class-Path: lib/cdk-1.4.19.jar lib/commons-io-2.4.jar:lib commons-math3-3.6.1.jar" >> manifest.mf
echo >> manifest.mf

jar cvfm DENOPTIM.jar manifest.mf denoptim

if [ "$?" = "0" ]; then
    rm manifest.mf
    rm -rf denoptim 
    mv DENOPTIM.jar lib/
    cp lib/DENOPTIM.jar ../lib
else
    echo "Failed to create DENOPTIM.jar."
    exit -1
fi

echo "--------------------- Done building DENOPTIM.jar ---------------------"
