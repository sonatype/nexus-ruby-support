package org.sonatype.nexus.ruby;


public class MavenMetadataFile extends RubygemsFile {
    
    private final boolean prereleased;

    MavenMetadataFile( Layout layout, String storage, String remote, String name,
                       boolean prereleased )
    {
        super( layout, FileType.MAVEN_METADATA, storage, remote, name );
        this.prereleased = prereleased;
    }

    public boolean isPrerelease()
    {
        return prereleased;
    }
    
    public DependencyFile dependency(){
        return layout.dependencyFile( name() );
    }
}