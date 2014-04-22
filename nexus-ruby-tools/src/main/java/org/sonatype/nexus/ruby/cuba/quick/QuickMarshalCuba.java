package org.sonatype.nexus.ruby.cuba.quick;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class QuickMarshalCuba implements Cuba
{
    private static Pattern FILE = Pattern.compile( "^([^/]/)?([^/]+).gemspec.rz$" );

    @Override
    public RubygemsFile on( State state )
    {
        Matcher m = FILE.matcher( state.part.length() == 1 ? state.path.substring( 1 ) : state.part );
        if ( m.matches() )
        {
            return state.context.layout.gemspecFile( m.group( 2 ) );
        }
        if ( state.part.isEmpty() )
        {
            state.context.layout.directory( state.context.original );
        }
        return state.context.layout.notFound();
    }
}