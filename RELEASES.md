# release notes #

## 1.4.0 ##

* removed shadow repository completely and added gem-artifact support to all three (hosted, proxy, group) rubygems repositories via the urls /maven/releases and /maven/prereleases which acts to the outside as maven repositories.

* more unit tests for rubygems logic

* the nexus-ruby-plugin is thin layer wireing up the rubygems logic from nexus-ruby-tools to the storage/repository layer of nexus

* removed all obsolete code

## 1.3.1 ##

* works with nexus-2.6.x and nexus-2.7.x and nexus-2.8.x

## 1.3.0 ##

* reworked the bundler API to use the remote bundler API (proxy) directly

* pattern matching moved to nexus-ruby-tools which does not depend on the nexus API

