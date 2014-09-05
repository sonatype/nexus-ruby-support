package org.sonatype.nexus.ruby;


public class MavenMetadataSnapshotFile extends RubygemsFile {
    
    private final String version;

    MavenMetadataSnapshotFile( RubygemsFileFactory factory, String path, String name,
                               String version )
    {
        super( factory, FileType.MAVEN_METADATA_SNAPSHOT, path, path, name );
        this.version = version;
    }

    public String version()
    {
        return version;
    }

    public DependencyFile dependency()
    {
        return factory.dependencyFile( name() );
    }
}