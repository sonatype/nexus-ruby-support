#!/bin/bash

mvn -DskipTests -Prelease clean install

cd target
NAME=`ls -1 *pom | sed s/.pom/-bundle.jar/`
jar -cvf $NAME *pom *asc

cd ../nexus-ruby-tools/target
NAME=`ls -1 *pom | sed s/.pom/-bundle.jar/`
jar -cvf $NAME *pom *jar *asc

cd ../../nexus-ruby-plugin/target
NAME=`ls -1 *pom | sed s/.pom/-bundle.jar/`
jar -cvf $NAME *pom *jar *asc *zip

cd ../../

echo
echo
find -name "*-bundle.jar"
echo
