package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class MavenReleasesRubygemsCuba implements Cuba
{
    
    @Override
    public RubygemsFile on( State ctx )
    {
        if ( ctx.part.isEmpty() )
        {
            return ctx.context.layout.directory( ctx.context.original );
        }
        return ctx.nested( new MavenReleasesRubygemsArtifactIdCuba( ctx.part ) );
    }
}