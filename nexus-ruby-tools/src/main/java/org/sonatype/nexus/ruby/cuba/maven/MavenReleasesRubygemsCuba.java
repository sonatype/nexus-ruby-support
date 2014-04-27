package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class MavenReleasesRubygemsCuba implements Cuba
{
    
    @Override
    public RubygemsFile on( State state )
    {
        if ( state.part.isEmpty() )
        {
            return state.context.layout.directory( state.context.original );
        }
        return state.nested( new MavenReleasesRubygemsArtifactIdCuba( state.part ) );
    }
}