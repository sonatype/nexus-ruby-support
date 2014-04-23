package org.sonatype.nexus.ruby.cuba.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

public class ApiV1DependenciesCuba implements Cuba
{
    private static Pattern FILE = Pattern.compile( "^([^/]+).json.rz$" );

    @Override
    public RubygemsFile on( State state )
    {
        if ( state.part.isEmpty() )
        {
            if ( state.context.query.startsWith( "gems=" ) )
            {
                if ( state.context.query.contains( "," ) )
                {
                    return state.context.layout.bundlerApiFile( state.context.query.substring( 5 ) );
                }
                else
                {
                    return state.context.layout.dependencyFile( state.context.query.substring( 5 ) );
                }
            }
            return state.context.layout.directory( state.context.original );
        }
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
            return state.context.layout.dependencyFile( m.group( 1 ) );
        }
        return state.context.layout.notFound();
    }
}