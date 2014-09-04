package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

/**
 * cuba for /maven/prereleases/
 * 
 * @author christian
 *
 */
public class MavenPrereleasesCuba implements Cuba
{
    static final String RUBYGEMS = "rubygems";

    private final Cuba mavenPrereleasesRubygems;
    
    public MavenPrereleasesCuba( Cuba cuba )
    {
        mavenPrereleasesRubygems = cuba;
    }

    /**
     * directory [rubygems]
     */
    @Override
    public RubygemsFile on( State state )
    {
        switch( state.name )
        {
        case RUBYGEMS:
            return state.nested( mavenPrereleasesRubygems );
        case "":
            return state.context.factory.directory( state.context.original, RUBYGEMS );
        default:
            return state.context.factory.notFound( state.context.original );
        }
    }
}