#!/usr/bin/bash

if [[ $# -eq 1 ]]; then
    echo "locally installing $1"
    JARFILE=$1
    JARFILE_NO_PATH=$(basename ${JARFILE})
    GROUPID="com.daleroyer"
    ARTIFACTID="clojure.deps.$JARFILE_NO_PATH"
    VERSION="1.0.0"

    mvn install:install-file -Dfile=$JARFILE -DgroupId=$GROUPID -DartifactId=$ARTIFACTID -Dversion=$VERSION -Dpackaging=jar
else
    echo "else"
    exit -1
fi
