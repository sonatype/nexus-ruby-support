Nexus Ruby Support
==================

This repository contains components and Nexus plugins to enhance Nexus with Ruby support. Stay tuned!

This plugin is powered by JRuby.

Note: This plugin is a (much appreciated) open source contribution.  However it is not officially supported by Sonatype.

Prerequisites
-----

Use Maven 3 for this build.

Add the repositories list in this section to your repository group. This might be a bit confusing, this build assumes that you are using Nexus as a repository manager and that you've configure your Maven Settings to hit a repository group.  This group needs to contain the following proxy repositories: 

  * The Sonatype Forge Repository: http://repository.sonatype.org/content/groups/forge
  * The Codehaus Snapshot Repository: http://snapshots.repository.codehaus.org

Having these repositories, you should be able to run the build as it is described below.

Build
-----

**IMPORTANT** the plugin is build for a particular nexus version. so make sure the nexus version you build the plugin matches the version of the nexus server !!!

    mvn clean install -Dmaven.test.skip -Dnexus.version=2.4.0-1

after that you will find the nexus plugin in **nexus-ruby-plugin/target/nexus-ruby-plugin-*-bundle.zip** and the gem with the nexus rubygems command in **nexus-gem/target/nexus-*.gem**.

install the nexus gem for your local ruby environment with

    gem install -l nexus-gem/target/nexus-*.gem

install the nexus plugin into your nexus server with

    unzip -d $NEXUS_HOME/nexus/WEB-INF/plugin-repository/ -o nexus-ruby-plugin/target/nexus-ruby-plugin-*-SNAPSHOT-bundle.zip

**NOTE** if you see `java.lang.OutOfMemoryError: PermGen space` errors when re-starting nexus with the plugin installed, you need to bump the size of the garbage collector's permanent generation. Edit `bin/jsw/conf/wrapper.conf` and add an additional java argument:

```
wrapper.java.additional.3=-XX:MaxPermSize=128m
```

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


enjoy !
