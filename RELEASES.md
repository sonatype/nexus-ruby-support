<!--

    Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.

    This program is licensed to you under the Apache License Version 2.0,
    and you may not use this file except in compliance with the Apache License Version 2.0.
    You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

    Unless required by applicable law or agreed to in writing,
    software distributed under the Apache License Version 2.0 is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.

-->
# release notes #

## 1.4.2 ##

* add directory browsing to ./maven/* part of the repo

* internal files api/dependencies have no nested directories anymore - purge dependency task obeys this change

* allow scheduled tasks to access filesystem directly

* cache single dependencies via bundler api

* download all bundler dependencies in one go and cache them per gem


## 1.4.1 ##

* fix update of volatile remote files. since the local file location is different from the remote one, an update needs to be triggered by deleted the outdated file.

* fix too strict about path pattern to allow clean up tasks (and others) to delete files

* run integration tests against nexus 2.8.1

## 1.4.0 ##

<http://central.maven.org/maven2/org/sonatype/nexus/plugins/nexus-ruby-plugin/1.4.0/nexus-ruby-plugin-1.4.0-bundle.zip>

<http://central.maven.org/maven2/org/sonatype/nexus/plugins/nexus-ruby-plugin/1.4.0/nexus-ruby-plugin-1.4.0-bundle.zip.asc>

<http://central.maven.org/maven2/org/sonatype/nexus/plugins/nexus-ruby-plugin/1.4.0/nexus-ruby-plugin-1.4.0-bundle.zip.sha1>

* removed shadow repository completely and added gem-artifact support to all three (hosted, proxy, group) rubygems repositories via the urls /maven/releases and /maven/prereleases which acts to the outside as maven repositories.

* more unit tests for rubygems logic

* the nexus-ruby-plugin is thin layer wireing up the rubygems logic from nexus-ruby-tools to the storage/repository layer of nexus

* removed all obsolete code

## 1.3.1 ##

<http://central.maven.org/maven2/org/sonatype/nexus/plugins/nexus-ruby-plugin/1.3.1/nexus-ruby-plugin-1.3.1-bundle.zip>

<http://central.maven.org/maven2/org/sonatype/nexus/plugins/nexus-ruby-plugin/1.3.1/nexus-ruby-plugin-1.3.1-bundle.zip.asc>

<http://central.maven.org/maven2/org/sonatype/nexus/plugins/nexus-ruby-plugin/1.3.1/nexus-ruby-plugin-1.3.1-bundle.zip.sha1>

* works with nexus-2.6.x and nexus-2.7.x and nexus-2.8.x

## 1.3.0 ##

<http://central.maven.org/maven2/org/sonatype/nexus/plugins/nexus-ruby-plugin/1.3.0/nexus-ruby-plugin-1.3.0-bundle.zip>

<http://central.maven.org/maven2/org/sonatype/nexus/plugins/nexus-ruby-plugin/1.3.0/nexus-ruby-plugin-1.3.0-bundle.zip.asc>

<http://central.maven.org/maven2/org/sonatype/nexus/plugins/nexus-ruby-plugin/1.3.0/nexus-ruby-plugin-1.3.0-bundle.zip.sha1>

* reworked the bundler API to use the remote bundler API (proxy) directly

* pattern matching moved to nexus-ruby-tools which does not depend on the nexus API

