#!/bin/bash

mvn clean install

cd nexus-ruby-plugin-its

mvn -P run-its clean install -Dit.nexus.version=2.7.2-03
mvn -P run-its clean install -Dit.nexus.version=2.6.4-02
mvn -P run-its clean install -Dit.nexus.version=2.8.1-01
mvn -P run-its clean install -Dit.nexus.version=2.9.2-01
mvn -P run-its clean install -Dit.nexus.version=2.10.0-02
#mvn -P run-its clean install -Dit.nexus.version=2.9.0-SNAPSHOT
