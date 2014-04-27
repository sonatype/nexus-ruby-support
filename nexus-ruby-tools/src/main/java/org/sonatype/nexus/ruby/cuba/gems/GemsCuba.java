package org.sonatype.nexus.ruby.cuba.gems;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class GemsCuba implements Cuba
{
    private static Pattern FILE = Pattern.compile( "^([^/]/)?([^/]+).gem$" );

    @Override
    public RubygemsFile on( State state )
    {      
        Matcher m;
        if ( state.part.length() == 1 )
        {
            if ( state.path.length() < 2 )
            {
                return state.context.layout.directory( state.context.original );
            }
            m = FILE.matcher( state.path.substring( 1 ) );
        }
        else
        {
            m = FILE.matcher( state.part );
        }
        if ( m.matches() )
        {
            return state.context.layout.gemFile( m.group( 2 ) );
        }
        if ( state.part.isEmpty() )
        {
            return state.context.layout.directory( state.context.original );
        }
        return state.context.layout.notFound( state.context.original );
    }
}