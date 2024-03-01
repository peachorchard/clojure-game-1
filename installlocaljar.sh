#!/usr/bin/bash

if [[ $# -eq 1 ]]; then
    echo "locally installing $1"
    JARFILE=$1
    GROUPID="com.daleroyer"
    ARTIFACTID="clojure.deps.$JARFILE"
    VERSION="1.0.0"

    mvn install:install-file -Dfile=$JARFILE -DgroupId=$GROUPID -DartifactId=$ARTIFACTID -Dversion=$VERSION -Dpackaging=jar
else
    echo "else"
    exit -1
fi
