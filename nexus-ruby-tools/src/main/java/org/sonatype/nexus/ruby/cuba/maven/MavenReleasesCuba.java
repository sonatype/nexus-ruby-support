package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

/**
 * cuba for /maven/releases/
 * 
 * @author christian
 *
 */
public class MavenReleasesCuba implements Cuba
{
    public static final String RUBYGEMS = "rubygems";
    
    private final Cuba mavenReleasesRubygems;

    public MavenReleasesCuba( Cuba mavenReleasesRubygems )
    {
        this.mavenReleasesRubygems = mavenReleasesRubygems;
    }

    /**
     * directory [rubygems]
     */
    @Override
    public RubygemsFile on( State state )
    {
        switch( state.name )
        {
        case MavenReleasesCuba.RUBYGEMS:
            return state.nested( mavenReleasesRubygems );
        case "":
            return state.context.factory.directory( state.context.original, MavenReleasesCuba.RUBYGEMS );
        default:
            return state.context.factory.notFound( state.context.original );
        }
    }
}