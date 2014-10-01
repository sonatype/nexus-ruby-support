package org.sonatype.nexus.ruby.cuba.api;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.RootCuba;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

/**
 * cuba for /api/
 * 
 * @author christian
 *
 */
public class ApiCuba implements Cuba
{
    public static final String V1 = "v1";

    private final Cuba v1;
    private final Cuba quick;
    
    public ApiCuba( Cuba v1, Cuba quick )
    {
        this.v1 = v1;
        this.quick = quick;
    }
    
    /**
     * directory [v1,quick]
     */
    @Override
    public RubygemsFile on( State state )
    {       
        switch( state.name )
        {
        case V1:
            return state.nested( v1 );
        case RootCuba.QUICK:
            return state.nested( quick );
        case "":
            String[] items = { V1, RootCuba.QUICK };
            return state.context.factory.directory( state.context.original, items );
        default:
            return state.context.factory.notFound( state.context.original );
        }
    }
}