package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

/**
 * cuba for /maven
 * 
 * @author christian
 */
public class MavenCuba implements Cuba
{
    public static final String RELEASES = "releases";
    public static final String PRERELEASES = "prereleases";

    private final Cuba releases;
    private final Cuba prereleases;
    
    public MavenCuba( Cuba releases, Cuba prereleases )
    {
        this.releases = releases;
        this.prereleases = prereleases;
    }
    
    /**
     * directories [releases,prereleases]
     */
    @Override
    public RubygemsFile on( State state )
    {
        switch( state.name )
        {
        case RELEASES:
            return state.nested( releases );
        case PRERELEASES:
            return state.nested( prereleases );
        case "":
            return state.context.factory.directory( state.context.original, 
                                                   PRERELEASES, RELEASES );
        default:
            return state.context.factory.notFound( state.context.original );
        }
    }
}