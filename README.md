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
Nexus Ruby Support
==================

This repository contains components and Nexus plugins to enhance Nexus with Ruby support. Stay tuned!

This plugin is powered by JRuby.

Note: This plugin is a (much appreciated) open source contribution.  However it is not officially supported by Sonatype.

Prerequisites
-----

Use Maven 3.0.x for this build.

Add the repositories list in this section to your repository group. This might be a bit confusing, this build assumes that you are using Nexus as a repository manager and that you've configure your Maven Settings to hit a repository group.  This group needs to contain the following proxy repositories: 

  * The Sonatype Forge Repository: http://repository.sonatype.org/content/groups/forge
  * The Codehaus Snapshot Repository: http://snapshots.repository.codehaus.org

Having these repositories, you should be able to run the build as it is described below.

Build
-----

the build plugin should work for nexus-2.6.x, nexus-2.7.x and nexus-2.8.x

    mvn clean install -Dmaven.test.skip

after that you will find the nexus plugin in **nexus-ruby-plugin/target/nexus-ruby-plugin-*-bundle.zip**.

to upload gems to hosted gem repository on nexus you need to install the nexus gem for your local ruby environment with

    gem install nexus

install the nexus plugin into your nexus server with

    unzip -d $NEXUS_HOME/../sonatype-work/nexus/plugin-repository/ -o nexus-ruby-plugin/target/nexus-ruby-plugin-*-bundle.zip

**NOTE** if you see `java.lang.OutOfMemoryError: PermGen space` errors when re-starting nexus with the plugin installed, you need to bump the size of the garbage collector's permanent generation. Edit `bin/jsw/conf/wrapper.conf` and add an additional java argument:

```
wrapper.java.additional.3=-XX:MaxPermSize=128m
```

use bundler 1.5.x or newer which comes with mirror support, i.e. you can configure bundler to use nexus proxy to rubygems.org without touching your **Gemfile**s.

nexus command
-------------

when pushing a gem to the nexus rubygems repo the first time the url of the repo and the credentials are prompted. these data will be stored in $HOME/.gem/nexus.

    gem nexus my-1.0.gem

using nexus ruby repo
---------------------

using nexus rubygem repo with rubygems add it as source with (mind the trailing slash !!)

    gem sources --add http://localhost:8081/nexus/content/repositories/my-repo/

now you can install gems from that repo

    gem install some_gem_name

you can use nexus repo without configuring it with rubygems, i.e. list the latest versions of the gems from the nexus rubygems repo (mind the trailing slash !!)

    gem list --remote --clear-sources --source http://localhost:8081/nexus/content/repositories/my-repo/

for bundler (>= 1.5.0) you can configure your proxy to rubygems.org as mirror with:

    bundle config mirror.http://rubygems.org http://localhost:8081/nexus/content/repositories/rubygems
    bundle config mirror.https://rubygems.org http://localhost:8081/nexus/content/repositories/rubygems

enjoy !
