#!/bin/bash

mvn clean install

cd nexus-ruby-plugin-its

mvn -P run-its clean install -Dit.nexus.version=2.7.2-03
mvn -P run-its clean install -Dit.nexus.version=2.6.4-02
mvn -P run-its clean install -Dit.nexus.version=2.8.0-05
mvn -P run-its clean install -Dit.nexus.version=2.9.0-SNAPSHOT
