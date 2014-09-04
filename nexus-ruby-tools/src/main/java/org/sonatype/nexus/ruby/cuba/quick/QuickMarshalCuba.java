package org.sonatype.nexus.ruby.cuba.quick;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

/**
 * cuba for /quick/Marshal.4.8
 * 
 * @author christian
 *
 */
public class QuickMarshalCuba implements Cuba
{
    private static Pattern FILE = Pattern.compile( "^([^/]/)?([^/]+).gemspec.rz$" );

    /**
     * no sub-directories
     * 
     * create <code>GemspecFile</code>s for {name}-{version}.gemspec.rz or {first-char-of-name}/{name}-{version}.gemspec.rz
     * 
     * the directory itself does not produce the directory listing - only the empty <code>Directory</code>
     * object.
     */
    @Override
    public RubygemsFile on( State state )
    {   
        Matcher m;
        if ( state.name.length() == 1 )
        {
            if ( state.path.length() < 2 )
            {
                return state.context.factory.directory( state.context.original );
            }
            m = FILE.matcher( state.path.substring( 1 ) );
        }
        else
        {
            m = FILE.matcher( state.name );
        }
        if ( m.matches() )
        {
            return state.context.factory.gemspecFile( m.group( 2 ) );
        }
        if ( state.name.isEmpty() )
        {
            return state.context.factory.directory( state.context.original );
        }
        return state.context.factory.notFound( state.context.original );
    }
}