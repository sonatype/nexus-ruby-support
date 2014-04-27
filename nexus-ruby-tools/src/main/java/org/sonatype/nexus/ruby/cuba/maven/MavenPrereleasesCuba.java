package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class MavenPrereleasesCuba implements Cuba
{
    static final String RUBYGEMS = "rubygems";

    private final Cuba mavenPrereleasesRubygems;
    
    public MavenPrereleasesCuba( Cuba cuba )
    {
        mavenPrereleasesRubygems = cuba;
    }

    @Override
    public RubygemsFile on( State state )
    {
        switch( state.part )
        {
        case RUBYGEMS:
            return state.nested( mavenPrereleasesRubygems );
        case "":
            return state.context.layout.directory( state.context.original, RUBYGEMS );
        default:
            return state.context.layout.notFound( state.context.original );
        }
    }
}