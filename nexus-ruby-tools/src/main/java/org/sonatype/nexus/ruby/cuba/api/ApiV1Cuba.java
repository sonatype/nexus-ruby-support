package org.sonatype.nexus.ruby.cuba.api;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class ApiV1Cuba implements Cuba
{
    private static final String GEMS = "gems";
    private static final String API_KEY = "api_key";
    static final String DEPENDENCIES = "dependencies";
    
    private final Cuba apiV1Dependencies;
    
    public ApiV1Cuba( Cuba cuba ){
        this.apiV1Dependencies = cuba;
    }
    
    @Override
    public RubygemsFile on( State state )
    {
        switch( state.part )
        {
        case DEPENDENCIES:
            return state.nested( apiV1Dependencies );
        case GEMS:
        case API_KEY:
            return state.context.layout.apiV1File( state.part );
        case "":
            return state.context.layout.directory( state.context.original,
                                                   new String[] { API_KEY, DEPENDENCIES } );
        default:
            return state.context.layout.notFound( state.context.original );
        }
    }
}