#!/usr/bin/bash

wget -N https://jogamp.org/deployment/jogamp-current/fat/jogamp-fat.jar -P 3rdparty/
wget -N https://repo1.maven.org/maven2/net/java/jinput/jinput/2.0.10/jinput-2.0.10.jar -P 3rdparty/
wget -N https://repo1.maven.org/maven2/net/java/jinput/jinput/2.0.10/jinput-2.0.10-natives-all.jar -P 3rdparty/

pushd 3rdparty
jar -xvf jinput-2.0.10-natives-all.jar
popd