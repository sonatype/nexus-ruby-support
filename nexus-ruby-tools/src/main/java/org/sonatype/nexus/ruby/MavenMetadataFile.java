package org.sonatype.nexus.ruby;

/**
 * represents /maven/releases/rubygems/{name}/maven-metadata.xml or /maven/prereleases/rubygems/{name}/maven-metadata.xml
 * 
 * @author christian
 *
 */
public class MavenMetadataFile extends RubygemsFile {
    
    private final boolean prereleased;

    MavenMetadataFile( RubygemsFileFactory factory, String path, String name,
                       boolean prereleased )
    {
        super( factory, FileType.MAVEN_METADATA, path, path, name );
        this.prereleased = prereleased;
    }

    /**
     * whether it is a prerelease or not
     * @return
     */
    public boolean isPrerelease()
    {
        return prereleased;
    }
    
    /**
     * retrieve the associated DependencyFile
     * @return
     */
    public DependencyFile dependency(){
        return factory.dependencyFile( name() );
    }
}