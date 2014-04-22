package org.sonatype.nexus.ruby;


public class MavenMetadataSnapshotFile extends RubygemsFile {
    
    private final String version;

    MavenMetadataSnapshotFile( Layout layout, String storage, String remote, String name,
                               String version )
    {
        super( layout, FileType.MAVEN_METADATA_SNAPSHOT, storage, remote, name );
        this.version = version;
    }

    public String version()
    {
        return version;
    }

    public DependencyFile dependency()
    {
        return layout.dependencyFile( name() );
    }
}