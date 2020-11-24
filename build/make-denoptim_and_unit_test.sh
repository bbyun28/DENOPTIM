#!/bin/bash

make lib

## Run all JUnit tests (including CDK's)
#java -jar ../test/junit/junit-platform-console-standalone-1.5.2.jar -cp lib/DENOPTIM.jar:lib/cdk-1.4.19.jar:lib/gson-2.8.6.jar  --scan-classpath --details=tree

# To run a specific test
java -jar ../test/junit/junit-platform-console-standalone-1.5.2.jar -cp lib/DENOPTIM.jar:lib/cdk-1.4.19.jar:lib/gson-2.8.6.jar -p denoptim


# To run a specific test
#java -jar ../test/junit/junit-platform-console-standalone-1.5.2.jar -cp lib/DENOPTIM.jar:lib/cdk-1.4.19.jar:lib/gson-2.8.6.jar  -c denoptim.molecule.DENOPTIMVertexTest
