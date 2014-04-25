package org.sonatype.nexus.ruby.cuba.maven;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.GemArtifactFile;
import org.sonatype.nexus.ruby.MavenMetadataSnapshotFile;
import org.sonatype.nexus.ruby.PomFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class MavenPrereleasesRubygemsArtifactIdVersionCuba implements Cuba
{

    private static Pattern FILE = Pattern.compile( "^.*?([^-][^-]*)\\.(gem|pom|gem.sha1|pom.sha1)$" );
    
    private final String name;
    private final String version;

    public MavenPrereleasesRubygemsArtifactIdVersionCuba( String name, String version )
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
            switch( m.group( 2 ) )
            {
            case "gem":
                return state.context.layout.gemArtifactSnapshot( name, version, m.group( 1 ) );
            case "pom":
                return state.context.layout.pomSnapshot( name, version, m.group( 1 ) );
            case "gem.sha1":
                RubygemsFile file = state.context.layout.gemArtifactSnapshot( name, version, m.group( 1 ) );
                return state.context.layout.sha1( file );
            case "pom.sha1":
                file = state.context.layout.pomSnapshot( name, version, m.group( 1 ) );
                return state.context.layout.sha1( file );
            default:
            }
        }
        switch( state.part )
        {
        case "maven-metadata.xml":
            return state.context.layout.mavenMetadataSnapshot( name, version );
        case "maven-metadata.xml.sha1":
            MavenMetadataSnapshotFile file = state.context.layout.mavenMetadataSnapshot( name, version );
            return state.context.layout.sha1( file );
        case "":
            return state.context.layout.directory( state.context.original );
        default:
            return state.context.layout.notFound();
        }
    }
}