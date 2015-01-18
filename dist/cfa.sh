#!/bin/bash

$JAVA_HOME/bin/java -classpath $JAVA_HOME/lib/tools.jar:cfa.jar \
                                               jp.dip.ysfactory.cfa.Main $*
