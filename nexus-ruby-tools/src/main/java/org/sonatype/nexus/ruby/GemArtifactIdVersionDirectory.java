package org.sonatype.nexus.ruby;

public class GemArtifactIdVersionDirectory extends Directory {

    GemArtifactIdVersionDirectory( Layout layout, String storage, String remote, String name,  String version )
    {
        super( layout, storage, remote, name );
        String base = name + "-" + version + ".";
        this.items.add( base + "pom" );
        this.items.add( base + "pom.sha1" );
        this.items.add( base + "gem" );
        this.items.add( base + "gem.sha1" );
    }
}