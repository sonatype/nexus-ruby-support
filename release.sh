#!/bin/bash
#
# Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.
#
# This program is licensed to you under the Apache License Version 2.0,
# and you may not use this file except in compliance with the Apache License Version 2.0.
# You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the Apache License Version 2.0 is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
#


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
