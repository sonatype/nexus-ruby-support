package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;

public class MavenPrereleasesRubygemsArtifactIdCuba implements Cuba
{
    
    private final String name;

    public MavenPrereleasesRubygemsArtifactIdCuba( String name )
    {
        this.name = name;
    }
    
    @Override
    public RubygemsFile on( State state )
    {
        switch( state.part )
        {
        case "maven-metadata.xml":
            return state.context.layout.mavenMetadata( name, true );
        case "maven-metadata.xml.sha1":
            MavenMetadataFile file = state.context.layout.mavenMetadata( name, true );
            return state.context.layout.sha1( file );
        case "":
            return state.context.layout.gemArtifactIdDirectory( state.context.original, name, true );
        default:
            return state.nested( new MavenPrereleasesRubygemsArtifactIdVersionCuba( name,
                                                                                    state.part.replace( "-SNAPSHOT", "" ) ) );
        }
    }
}