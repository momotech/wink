#!/bin/bash

libDir=$1
rootDir=$2

if [ ! $libDir ]; then
	libDir=.idea/wink/lib/
fi

if [ ! $rootDir ]; then
	rootDir=.
fi

java -Xbootclasspath/a:${libDir}slf4j-nop-1.7.30.jar:${libDir}kotlin-csv-jvm.jar:${libDir}kotlin-logging-1.7.9.jar:${libDir}kotlin-stdlib-common.jar:${libDir}kotlin-stdlib.jar:${libDir}org.eclipse.jgit.jar:${libDir}slf4j-api.jar -jar ${libDir}wink-gradle-plugin.jar $rootDir