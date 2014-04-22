package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class MavenReleasesCuba implements Cuba
{
    static final String RUBYGEMS = "rubygems";
    
    private final Cuba mavenReleasesRubygems;

    public MavenReleasesCuba( Cuba mavenReleasesRubygems )
    {
        this.mavenReleasesRubygems = mavenReleasesRubygems;
    }

    @Override
    public RubygemsFile on( State state )
    {
        switch( state.part )
        {
        case RUBYGEMS:
            return state.nested( mavenReleasesRubygems );
        case "":
            return state.context.layout.directory( state.context.original, RUBYGEMS );
        default:
            return state.context.layout.notFound();
        }
    }
}