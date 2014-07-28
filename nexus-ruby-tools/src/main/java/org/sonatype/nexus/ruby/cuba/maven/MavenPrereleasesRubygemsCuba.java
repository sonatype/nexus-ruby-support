package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class MavenPrereleasesRubygemsCuba implements Cuba
{

    @Override
    public RubygemsFile on( State ctx )
    {
        if ( ctx.part.isEmpty() )
        {
            return ctx.context.layout.rubygemsDirectory( ctx.context.original );
        }
        return ctx.nested( new MavenPrereleasesRubygemsArtifactIdCuba( ctx.part ) );
    }
}