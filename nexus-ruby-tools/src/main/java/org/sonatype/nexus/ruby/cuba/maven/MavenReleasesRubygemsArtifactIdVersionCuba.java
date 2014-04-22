package org.sonatype.nexus.ruby.cuba.maven;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class MavenReleasesRubygemsArtifactIdVersionCuba implements Cuba
{

    private static Pattern FILE = Pattern.compile( "^.*\\.(gem|pom)$" );
    
    private final String name;
    private final String version;

    public MavenReleasesRubygemsArtifactIdVersionCuba( String name, String version )
    {
        this.name = name;
        this.version = version;
    }
    
    @Override
    public RubygemsFile on( State state )
    {
        Matcher m = FILE.matcher( state.part );
        if ( m.matches() )
        {
            switch( m.group( 1 ) )
            {
            case "gem":
                return state.context.layout.gemArtifact( name, version );
            case "pom":
                return state.context.layout.pom( name, version );
            default:
            }
        }
        switch( state.part )
        {
        case "":
            return state.context.layout.directory( state.context.original );
        default:
            return state.context.layout.notFound();
        }
    }
}