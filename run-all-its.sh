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


mvn clean install

cd nexus-ruby-plugin-its

mvn -P run-its clean install -Dit.nexus.version=2.7.2-03
mvn -P run-its clean install -Dit.nexus.version=2.6.4-02
mvn -P run-its clean install -Dit.nexus.version=2.8.1-01
mvn -P run-its clean install -Dit.nexus.version=2.9.2-01
mvn -P run-its clean install -Dit.nexus.version=2.10.0-02
#mvn -P run-its clean install -Dit.nexus.version=2.9.0-SNAPSHOT
