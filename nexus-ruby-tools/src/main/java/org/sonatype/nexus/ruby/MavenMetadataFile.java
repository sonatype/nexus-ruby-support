package org.sonatype.nexus.ruby;


public class MavenMetadataFile extends RubygemsFile {
    
    private final boolean prereleased;

    MavenMetadataFile( RubygemsFileFactory factory, String path, String name,
                       boolean prereleased )
    {
        super( factory, FileType.MAVEN_METADATA, path, path, name );
        this.prereleased = prereleased;
    }

    public boolean isPrerelease()
    {
        return prereleased;
    }
    
    public DependencyFile dependency(){
        return factory.dependencyFile( name() );
    }
}