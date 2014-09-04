package org.sonatype.nexus.ruby.cuba.maven;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.MavenMetadataSnapshotFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;


/**
 * cuba for /maven/prereleases/rubygems/{artifactId}/{version}-SNAPSHOT/
 * 
 * @author christian
 *
 */
public class MavenPrereleasesRubygemsArtifactIdVersionCuba implements Cuba
{

    private static Pattern FILE = Pattern.compile( "^.*?([^-][^-]*)\\.(gem|pom|gem.sha1|pom.sha1)$" );
    
    private final String artifactId;
    private final String version;

    public MavenPrereleasesRubygemsArtifactIdVersionCuba( String artifactId, String version )
    {
        this.artifactId = artifactId;
        this.version = version;
    }

    /**
     * directories one for each version of the gem with given name/artifactId
     * 
     * files [{artifactId}-{version}-SNAPSHOT.gem,{artifactId}-{version}-SNAPSHOT.gem.sha1,
     *        {artifactId}-{version}-SNAPSHOT.pom,{artifactId}-{version}-SNAPSHOT.pom.sha1]
     */
    @Override
    public RubygemsFile on( State state )
    {
        Matcher m = FILE.matcher( state.name );
        if ( m.matches() )
        {
            switch( m.group( 2 ) )
            {
            case "gem":
                return state.context.factory.gemArtifactSnapshot( artifactId, version, m.group( 1 ) );
            case "pom":
                return state.context.factory.pomSnapshot( artifactId, version, m.group( 1 ) );
            case "gem.sha1":
                RubygemsFile file = state.context.factory.gemArtifactSnapshot( artifactId, version, m.group( 1 ) );
                return state.context.factory.sha1( file );
            case "pom.sha1":
                file = state.context.factory.pomSnapshot( artifactId, version, m.group( 1 ) );
                return state.context.factory.sha1( file );
            default:
            }
        }
        switch( state.name )
        {
        case "maven-metadata.xml":
            return state.context.factory.mavenMetadataSnapshot( artifactId, version );
        case "maven-metadata.xml.sha1":
            MavenMetadataSnapshotFile file = state.context.factory.mavenMetadataSnapshot( artifactId, version );
            return state.context.factory.sha1( file );
        case "":
            return state.context.factory.directory( state.context.original );
        default:
            return state.context.factory.notFound( state.context.original );
        }
    }
}