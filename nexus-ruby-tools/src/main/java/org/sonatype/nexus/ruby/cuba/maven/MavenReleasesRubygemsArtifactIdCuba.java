package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class MavenReleasesRubygemsArtifactIdCuba implements Cuba
{
    
    private final String name;

    public MavenReleasesRubygemsArtifactIdCuba( String name )
    {
        this.name = name;
    }
    
    @Override
    public RubygemsFile on( State state )
    {
        switch( state.part )
        {
        case "maven-metadata.xml":
            return state.context.layout.mavenMetadata( name, false );
        case "maven-metadata.xml.sha1":
            MavenMetadataFile file = state.context.layout.mavenMetadata( name, false );
            return state.context.layout.sha1( file );
        case "":
            return state.context.layout.directory( state.context.original );
        default:
            return state.nested( new MavenReleasesRubygemsArtifactIdVersionCuba( name, state.part ) );
        }
    }
}