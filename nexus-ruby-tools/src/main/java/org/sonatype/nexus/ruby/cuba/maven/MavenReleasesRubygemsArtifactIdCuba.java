package org.sonatype.nexus.ruby.cuba.maven;

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
    public RubygemsFile on( State ctx )
    {
        switch( ctx.part )
        {
        case "maven-metadata.xml":
            return ctx.context.layout.mavenMetadata( name, false );
        case "":
            return ctx.context.layout.directory( ctx.context.original );
        default:
            return ctx.nested( new MavenReleasesRubygemsArtifactIdVersionCuba( name, ctx.part ) );
        }
    }
}