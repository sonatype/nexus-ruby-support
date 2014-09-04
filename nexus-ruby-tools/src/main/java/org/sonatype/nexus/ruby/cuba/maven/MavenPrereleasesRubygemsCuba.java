package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

/**
 * cuba for /maven/prereleases/rubygems/
 * 
 * @author christian
 *
 */
public class MavenPrereleasesRubygemsCuba implements Cuba
{

    /**
     * directories one for each gem (name without version)
     */
    @Override
    public RubygemsFile on( State ctx )
    {
        if ( ctx.name.isEmpty() )
        {
            return ctx.context.factory.rubygemsDirectory( ctx.context.original );
        }
        return ctx.nested( new MavenPrereleasesRubygemsArtifactIdCuba( ctx.name ) );
    }
}