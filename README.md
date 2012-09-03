Nexus Ruby Support
==================

This repository contains components and Nexus plugins to enhance Nexus with Ruby support. Stay tuned!

This plugin is powered by JRuby.


Build
-----

    mvn install -Dmaven.test.skip
	 
after that you will find the nexus plugin in **nexus-ruby-plugin/target/nexus-ruby-plugin-*-bundle.zip** and the gem with the nexus rubygems command in **nexus-gem/target/nexus-*.gem**.

install the nexus gem for you local ruby environment with

    gem install -l nexus-gem/target/nexus-*.gem

install the nexus plugin into your nexus server with

    unzip -d $NEXUS_HOME/nexus/WEB-INF/plugin-repository/ -o nexus-ruby-plugin/target/nexus-ruby-plugin-*-SNAPSHOT-bundle.zip

nexus command
-------------

when pushing a gem to the nexus rubygems repo the first time the url of the repo and the credentials are prompted. these data will be stored in $HOME/.gemrc/nexus.

    gem nexus my-1.0.gem

using nexus ruby repo
---------------------

with this you can list the latest versions of the gems from the nexus rubygems repo (mind the trailing slash !!)

    gem list --remote --clear-sources --source http://localhost:8081/nexus/content/rubygems/my-repo/

using nexus rubygem repo with rubygems add it as source with (mind the trailing slash !!)

    gem sources --add http://localhost:8081/nexus/content/rubygems/my-repo/
	
now you can install the gems from that repo

    gem install my
	
enjoy !
