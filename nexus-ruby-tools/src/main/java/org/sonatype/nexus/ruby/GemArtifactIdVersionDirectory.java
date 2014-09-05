package org.sonatype.nexus.ruby;

/**
 * represent /maven/releases/rubygems/{artifactId}/{version} or /maven/prereleases/rubygems/{artifactId}/{version}
 * 
 * @author christian
 *
 */
public class GemArtifactIdVersionDirectory extends Directory {
    
    /**
     * setup the directory items
     * 
     * @param factory
     * @param path
     * @param remote
     * @param name
     * @param version
     * @param prerelease
     */
    GemArtifactIdVersionDirectory( RubygemsFileFactory factory, String path, String name, String version, boolean prerelease )
    {
        super( factory, path, name );
        String base = name + "-" + version + ".";
        this.items.add( base + "pom" );
        this.items.add( base + "pom.sha1" );
        this.items.add( base + "gem" );
        this.items.add( base + "gem.sha1" );
        if ( prerelease )
        {
            this.items.add( "maven-metadata.xml" );
            this.items.add( "maven-metadata.xml.sha1" );
        }
    }
}