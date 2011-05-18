## target platform

nexus **>=1.9.2** or **1.9.2-SNAPSHOT** from [https://github.com/sonatype/nexus]()

## build plugin ##

needs access to [http://rubygems-proxy.torquebox.org/releases/]() which cannot be mirrored by nexus since it does not have browsable directories.

`mvn install`

## install ##

just unzip the plugin bundle _target/nexus-ruby-plugin-1.0.0-SNAPSHOT-bundle.zip_ in _$NEXUS\_HOME/runtime/apps/nexus/plugin-repository/_

now you can add a new ruby repository inside the admin-GUI.

## lazy gem materialization ##

add the lazyGemMaterialization tag in _$NEXUS\_HOME/../sonatype-work/nexus/conf/nexus.xml_

    <repositories>
      . . . . .
      <repository>
        <id>ruby</id>
        <name>ruby</name>
        . . . . .
        <externalConfiguration>
          <lazyGemMaterialization>true</lazyGemMaterialization>
          . . . . .
        </externalConfiguration>
      </repository>
    </repositories>
