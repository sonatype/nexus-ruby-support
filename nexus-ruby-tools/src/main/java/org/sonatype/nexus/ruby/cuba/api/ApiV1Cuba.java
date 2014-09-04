package org.sonatype.nexus.ruby.cuba.api;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

/**
 * cuba for /api/v1
 * 
 * @author christian
 *
 */
public class ApiV1Cuba implements Cuba
{
    private static final String GEMS = "gems";
    private static final String API_KEY = "api_key";
    static final String DEPENDENCIES = "dependencies";
    
    private final Cuba apiV1Dependencies;
    
    public ApiV1Cuba( Cuba cuba ){
        this.apiV1Dependencies = cuba;
    }
    
    /**
     * directory [dependencies], files [api_key,gems]
     */
    @Override
    public RubygemsFile on( State state )
    {
        switch( state.name )
        {
        case DEPENDENCIES:
            return state.nested( apiV1Dependencies );
        case GEMS:
        case API_KEY:
            return state.context.factory.apiV1File( state.name );
        case "":
            return state.context.factory.directory( state.context.original,
                                                   new String[] { API_KEY, DEPENDENCIES } );
        default:
            return state.context.factory.notFound( state.context.original );
        }
    }
}