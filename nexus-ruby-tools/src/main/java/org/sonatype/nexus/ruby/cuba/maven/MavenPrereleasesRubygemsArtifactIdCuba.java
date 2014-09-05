package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;

/**
 * cuba for /maven/prereleases/rubygems/{artifactId}
 * 
 * @author christian
 *
 */
public class MavenPrereleasesRubygemsArtifactIdCuba implements Cuba
{
    
    public static final String SNAPSHOT = "-SNAPSHOT";

    private final String name;

    public MavenPrereleasesRubygemsArtifactIdCuba( String name )
    {
        this.name = name;
    }

    /**
     * directories one for each version of the gem with given name/artifactId
     * 
     * files [maven-metadata.xml,maven-metadata.xml.sha1]
     */
    @Override
    public RubygemsFile on( State state )
    {
        switch( state.name )
        {
        case MavenReleasesRubygemsArtifactIdCuba.MAVEN_METADATA_XML:
            return state.context.factory.mavenMetadata( name, true );
        case MavenReleasesRubygemsArtifactIdCuba.MAVEN_METADATA_XML + ".sha1":
            MavenMetadataFile file = state.context.factory.mavenMetadata( name, true );
            return state.context.factory.sha1( file );
        case "":
            return state.context.factory.gemArtifactIdDirectory( state.context.original, name, true );
        default:
            return state.nested( new MavenPrereleasesRubygemsArtifactIdVersionCuba( name,
                                                                                    state.name.replace( SNAPSHOT, "" ) ) );
        }
    }
}