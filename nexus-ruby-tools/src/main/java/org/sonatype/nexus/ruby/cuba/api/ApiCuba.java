package org.sonatype.nexus.ruby.cuba.api;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class ApiCuba implements Cuba
{
    static final String V1 = "v1";

    private final Cuba apiV1;
    
    
    public ApiCuba( Cuba cuba )
    {
        this.apiV1 = cuba;
    }
    
    @Override
    public RubygemsFile on( State state )
    {       
        switch( state.part )
        {
        case V1:
            return state.nested( apiV1 );
        case "":
            String[] items = { V1 };
            return state.context.layout.directory( state.context.original, items );
        default:
            return state.context.layout.notFound();
        }
    }
}