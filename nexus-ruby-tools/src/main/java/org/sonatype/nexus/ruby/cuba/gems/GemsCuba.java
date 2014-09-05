package org.sonatype.nexus.ruby.cuba.gems;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.State;
import org.sonatype.nexus.ruby.cuba.Cuba;

/**
 * cuba for /gems
 * 
 * @author christian
 *
 */
public class GemsCuba implements Cuba
{
    public static final String GEM = ".gem";
    
    private static Pattern FILE = Pattern.compile( "^([^/]/)?([^/]+)" + GEM + "$" );

    /**
     * no sub-directories
     * 
     * create <code>GemFile</code>s for {name}-{version}.gem or {first-char-of-name}/{name}-{version}.gem
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
            return state.context.factory.gemFile( m.group( 2 ) );
        }
        if ( state.name.isEmpty() )
        {
            return state.context.factory.directory( state.context.original );
        }
        return state.context.factory.notFound( state.context.original );
    }
}