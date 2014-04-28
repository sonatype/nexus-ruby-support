#!/bin/bash

mvn clean install

mvn -P run-its install
mvn -P run-its install -Dit.nexus.version=2.6.4-02
mvn -P run-its install -Dit.nexus.version=2.8.0-05
