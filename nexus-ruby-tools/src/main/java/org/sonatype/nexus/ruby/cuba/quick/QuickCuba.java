package org.sonatype.nexus.ruby.cuba.quick;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class QuickCuba implements Cuba
{
    static final String MARSHAL_4_8 = "Marshal.4.8";

    private final Cuba quickMarshal;
    
    public QuickCuba( Cuba cuba )
    {
        this.quickMarshal = cuba;
    }
    
    @Override
    public RubygemsFile on( State state )
    {       
        switch( state.part )
        {
        case MARSHAL_4_8:
            return state.nested( quickMarshal );
        case "":
            return state.context.layout.directory( state.context.original, 
                                                   MARSHAL_4_8 );
        default:
            return state.context.layout.notFound();
        }
    }
}