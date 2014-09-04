package org.sonatype.nexus.ruby.cuba.maven;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

/**
 * cuba for /maven/prereleases/rubygems/{artifactId}/{version}/
 * 
 * @author christian
 *
 */
public class MavenReleasesRubygemsArtifactIdVersionCuba implements Cuba
{

    private static Pattern FILE = Pattern.compile( "^.*\\.(gem|pom|gem.sha1|pom.sha1)$" );
    
    private final String name;
    private final String version;

    public MavenReleasesRubygemsArtifactIdVersionCuba( String name, String version )
    {
        this.name = name;
        this.version = version;
    }

    /**
     * directories one for each version of the gem with given name/artifactId
     * 
     * files [{artifactId}-{version}.gem,{artifactId}-{version}.gem.sha1,
     *        {artifactId}-{version}.pom,{artifactId}-{version}.pom.sha1]
     */
    @Override
    public RubygemsFile on( State state )
    {
        Matcher m = FILE.matcher( state.name );
        if ( m.matches() )
        {
            switch( m.group( 1 ) )
            {
            case "gem":
                return state.context.factory.gemArtifact( name, version );
            case "pom":
                return state.context.factory.pom( name, version );
            case "gem.sha1":
                RubygemsFile file = state.context.factory.gemArtifact( name, version );
                return state.context.factory.sha1( file );
            case "pom.sha1":
                file = state.context.factory.pom( name, version );
                return state.context.factory.sha1( file );
            default:
            }
        }
        switch( state.name )
        {
        case "":
            return state.context.factory.gemArtifactIdVersionDirectory( state.context.original, name, version );
        default:
            return state.context.factory.notFound( state.context.original );
        }
    }
}