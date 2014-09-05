package org.sonatype.nexus.ruby.cuba.quick;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

/**
 * cuba for /quick/
 * 
 * @author christian
 *
 */
public class QuickCuba implements Cuba
{
    public static final String MARSHAL_4_8 = "Marshal.4.8";

    private final Cuba quickMarshal;
    
    public QuickCuba( Cuba cuba )
    {
        this.quickMarshal = cuba;
    }

    /**
     * directory [Marshal.4.8]
     */
    @Override
    public RubygemsFile on( State state )
    {       
        switch( state.name )
        {
        case MARSHAL_4_8:
            return state.nested( quickMarshal );
        case "":
            return state.context.factory.directory( state.context.original, 
                                                   MARSHAL_4_8 );
        default:
            return state.context.factory.notFound( state.context.original );
        }
    }
}