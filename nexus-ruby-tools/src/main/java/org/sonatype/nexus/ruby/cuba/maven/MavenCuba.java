package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class MavenCuba implements Cuba
{
    static final String RELEASES = "releases";
    static final String PRERELEASES = "prereleases";

    private final Cuba releases;
    private final Cuba prereleases;
    
    public MavenCuba( Cuba releases, Cuba prereleases )
    {
        this.releases = releases;
        this.prereleases = prereleases;
    }
    
 
    @Override
    public RubygemsFile on( State state )
    {
        switch( state.part )
        {
        case RELEASES:
            return state.nested( releases );
        case PRERELEASES:
            return state.nested( prereleases );
        case "":
            return state.context.layout.directory( state.context.original, 
                                                   PRERELEASES, RELEASES );
        default:
            return state.context.layout.notFound();
        }
    }
}